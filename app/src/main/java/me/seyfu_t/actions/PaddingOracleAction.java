package me.seyfu_t.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Log;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class PaddingOracleAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String hostname = arguments.get("hostname").getAsString();
        int port = arguments.get("port").getAsInt();
        String iv = arguments.get("iv").getAsString();
        String ciphertext = arguments.get("ciphertext").getAsString();

        return ResponseBuilder.singleResponse("plaintext", paddingOracle(hostname, port, iv, ciphertext));
    }

    private static String paddingOracle(String hostname, int port, String base64IV, String base64Ciphertext) {
        UBigInt16 iv = UBigInt16.fromBase64(base64IV);
        byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);

        Log.debug("IV:", iv);
        Log.debug("Ciphertext:", HexFormat.of().formatHex(ciphertext));

        List<UBigInt16> decryptedBlocksList = new ArrayList<>();

        List<byte[]> ciphertextByteBlocks = Util.splitIntoChunks(ciphertext, 16);

        for (int i = 0; i < ciphertextByteBlocks.size(); i++) {
            UBigInt16 ciphertextBlock = new UBigInt16(ciphertextByteBlocks.get(i));
            UBigInt16 qBlock = (i == 0) ? iv : new UBigInt16(ciphertextByteBlocks.get(i - 1));

            UBigInt16 decryptedBlock = decryptSingleBlock(qBlock, ciphertextBlock, hostname, port);
            decryptedBlocksList.add(decryptedBlock);
        }

        return Base64.getEncoder().encodeToString(Util.concatUBigInt16s(decryptedBlocksList));
    }

    private static UBigInt16 decryptSingleBlock(UBigInt16 qBlock, UBigInt16 ciphertextBlock, String hostname,
            int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port));
            socket.setTcpNoDelay(true);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Handling the first Byte and its edge-case
            out.write(ciphertextBlock.toByteArray());
            byte[] firstResponse = sendAllPossibilities(out, in, 15, null);
            Log.info("response: " + HexFormat.of().formatHex(firstResponse));

            // (if two) Check which byte is not the result of having 0x01 at the end
            byte validPaddingByte = findRelevantPaddingByte(out, in, firstResponse);

            byte[] baseBytes = new byte[16];
            baseBytes[15] = (byte) (validPaddingByte ^ 0x01);
            Log.debug(String.format("Valid base byte: 0x%02X", baseBytes[15] & 0xFF));

            // Handling the remaining bytes
            for (int i = 14; i >= 0; i--) {
                byte[] response = sendAllPossibilities(out, in, i, baseBytes);
                Log.info("response: " + HexFormat.of().formatHex(response));

                int index = getValidPaddingIndex(response);
                Log.debug("Index:", String.format("0x%02X", index & 0xFF));
                Log.debug("XOR:", String.format("0x%02X", (byte) (16 - i)));

                baseBytes[i] = (byte) (index & 0xFF ^ (16 - i));
                Log.debug(String.format("Valid base byte: 0x%02X", baseBytes[i] & 0xFF));
            }
            UBigInt16 base = new UBigInt16(baseBytes); // D(C_n)
            UBigInt16 result = base.xor(qBlock);

            Log.debug("Ciphertext:          ", ciphertextBlock.toString());
            Log.debug("Base:                ", base.toString());
            Log.debug("Q-Block:             ", qBlock.toString());
            Log.debug("Base XOR Q-Block:    ", result.toString());

            return result; // Plaintext = D(C_n) XOR C_(n-1)
        } catch (IOException e) {
            throw new RuntimeException("Padding Oracle failed");
        }
    }

    private static byte findRelevantPaddingByte(OutputStream out, InputStream in, byte[] response)
            throws IOException {
        // Possibly two valid indexes
        List<Integer> validIndexes = getValidPaddingIndexes(response);
        
        Log.debug("Valid indexes size:",validIndexes.size());

        if (validIndexes.size() == 1) {
            return (byte) (validIndexes.get(0) & 0xFF);
        }

        for (int index : validIndexes) {
            if (!is01Padding(out, in, (byte) (index & 0xFF))) {
                Log.debug("correct index:",(index&0xFF));
                return (byte) (index & 0xFF);
            }
        }

        throw new RuntimeException("Not a single valid padding found.");
    }

    private static byte[] sendAllPossibilities(OutputStream out, InputStream in, int byteIndex, byte[] currentBaseBytes)
            throws IOException {
        byte[] lengthBytes = new byte[] { (byte) 0, (byte) 1 }; // 256
        out.write(lengthBytes);

        for (int i = 0; i < 256; i++) {// double loop required
            UBigInt16 pad = genPaddedBlock(byteIndex, (byte) i);
            if (byteIndex < 15) {
                byte[] array = pad.toByteArray();
                for (int j = 15; j > byteIndex; j--)
                    array[j] = (byte) (currentBaseBytes[j] ^ (16 - byteIndex));
                pad = new UBigInt16(array);
            }
            out.write(pad.toByteArray());
            Log.info("Writing:", pad.toString());
        }

        byte[] response = new byte[256];
        in.read(response);
        return response;
    }

    private static boolean is01Padding(OutputStream out, InputStream in, byte toTest) throws IOException {
        byte[] length = new byte[2];
        length[0] = 0x02;
        out.write(length);

        byte[] array = UBigInt16.Zero().toByteArray();
        array[15] = toTest;
        // Test 2 diff. possibilities for other byte to not get it accidentally right
        array[14] = 0x02;
        out.write(array);

        array[14] = 0x01;
        out.write(array);

        byte[] response = new byte[2];
        in.read(response);

        boolean result = (response[0] == 0x01 && response[1] == 0x01);
        Log.debug("is01Padding:",result);
        return !result;
    }

    private static List<Integer> getValidPaddingIndexes(byte[] response) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < response.length; i++) {
            if (response[i] == 0x01)
                list.add(i);
        }
        return list;
    }

    private static int getValidPaddingIndex(byte[] response) {
        for (int i = 0; i < response.length; i++) {
            if (response[i] == 0x01)
                return i;
        }
        throw new RuntimeException("Got all 0s as response");
    }

    private static UBigInt16 genPaddedBlock(int byteIndex, byte pad) {
        byte[] array = UBigInt16.Zero().toByteArray();
        array[byteIndex] = pad;
        return new UBigInt16(array);
    }

}
