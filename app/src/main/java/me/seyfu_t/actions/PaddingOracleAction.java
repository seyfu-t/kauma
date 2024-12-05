package me.seyfu_t.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class PaddingOracleAction implements Action {

    private static final int DEBUG_VALUE = 500000;
    private static final int RESPONSE_SIZE = 256;

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

        List<UBigInt16> decryptedBlocksList = new ArrayList<>();

        List<byte[]> ciphertextByteBlocks = Util.splitIntoChunks(ciphertext, 16);

        for (int i = 0; i < ciphertextByteBlocks.size(); i++) {
            UBigInt16 ciphertextBlock = new UBigInt16(ciphertextByteBlocks.get(i));
            UBigInt16 qBlock = (i == 0) ? iv : new UBigInt16(ciphertextByteBlocks.get(i - 1));

            UBigInt16 decryptedBlock = decryptSingleBlock(qBlock, ciphertextBlock, hostname, port);
            if (decryptedBlock == null)
                return null;
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

            // (if two) Check which byte is not the result of having 0x01 at the end
            byte validPaddingByte = findRelevantPaddingByte(out, in, firstResponse);

            byte[] baseBytes = new byte[16];
            baseBytes[15] = (byte) (validPaddingByte ^ 0x01);

            // Handling the remaining bytes
            for (int i = 14; i >= 0; i--) {
                byte[] response = sendAllPossibilities(out, in, i, baseBytes);

                int index = getValidPaddingIndex(response);
                if (index == DEBUG_VALUE)
                    return null;

                baseBytes[i] = (byte) (index & 0xFF ^ (16 - i));
            }
            UBigInt16 base = new UBigInt16(baseBytes); // D(C_n)
            UBigInt16 result = base.xor(qBlock);

            return result; // Plaintext = D(C_n) XOR C_(n-1)
        } catch (IOException e) {
            throw new RuntimeException("Padding Oracle failed");
        }
    }

    private static byte findRelevantPaddingByte(OutputStream out, InputStream in, byte[] response)
            throws IOException {
        // Possibly two valid indexes
        List<Integer> validIndexes = getValidPaddingIndexes(response);

        if (validIndexes.size() == 1)
            return (byte) (validIndexes.get(0) & 0xFF);

        for (int index : validIndexes)
            if (!is01Padding(out, in, (byte) (index & 0xFF)))
                return (byte) (index & 0xFF);

        throw new RuntimeException("Not a single valid padding found.");
    }

    private static byte[] sendAllPossibilities(OutputStream out, InputStream in, int byteIndex, byte[] currentBaseBytes)
            throws IOException {
        byte[] lengthBytes = new byte[] { (byte) 0, (byte) 1 }; // = 256
        out.write(lengthBytes);

        UBigInt16[] DEBUG_SENT = new UBigInt16[RESPONSE_SIZE];

        for (int i = 0; i < RESPONSE_SIZE; i++) {
            UBigInt16 pad = genPaddedBlock(byteIndex, (byte) i);
            if (byteIndex < 15) {
                byte[] array = pad.toByteArray();
                for (int j = 15; j > byteIndex; j--)
                    array[j] = (byte) (currentBaseBytes[j] ^ (16 - byteIndex));
                pad = new UBigInt16(array);
            }
            DEBUG_SENT[i] = pad;
            out.write(pad.toByteArray());
        }

        byte[] response = ensureFullyReadInAllBytes(in, RESPONSE_SIZE);
        // DEBUG
        if (getValidPaddingIndex(response) == DEBUG_VALUE) {
            System.err.println("ALL 0s DETECTED");
            System.err.println("CURRENT BYTE INDEX IS: " + byteIndex);
            System.err.println("THESE BYTES HAVE ALREADY BEEN DETERMINED: "
                    + (currentBaseBytes == null ? "NONE" : new UBigInt16(currentBaseBytes)));
            System.err.println("THESE WERE SENT TO THE PADDING ORACLE: ");
            for (UBigInt16 sentBytes : DEBUG_SENT)
                System.err.println(sentBytes + " : " + sentBytes.toBase64());
        }

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
        return !result;
    }

    private static List<Integer> getValidPaddingIndexes(byte[] response) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < response.length; i++)
            if (response[i] == 0x01)
                list.add(i);

        return list;
    }

    private static int getValidPaddingIndex(byte[] response) {
        for (int i = 0; i < response.length; i++) {
            if (response[i] == 0x01)
                return i;
        }

        return DEBUG_VALUE; // DEBUG
        // throw new RuntimeException("Got all 0s as response");
    }

    private static UBigInt16 genPaddedBlock(int byteIndex, byte pad) {
        byte[] array = UBigInt16.Zero().toByteArray();
        array[byteIndex] = pad;
        return new UBigInt16(array);
    }

    private static byte[] ensureFullyReadInAllBytes(InputStream in, int expectedLength) throws IOException {
        byte[] buffer = new byte[expectedLength];
        int bytesRead = 0;
        while (bytesRead < expectedLength) {
            int count = in.read(buffer, bytesRead, expectedLength - bytesRead);
            if (count == -1)
                throw new IOException("Padding oracle stream ended before reading required bytes");
            bytesRead += count;
        }
        return buffer;
    }

}
