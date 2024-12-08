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
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class PaddingOracleAction implements Action {

    private static final int Q_BLOCK_COUNT = 256;
    private static final int LENGTH_BYTES_COUNT = 2;

    @Override
    public JsonObject execute(JsonObject arguments) {
        String hostname = arguments.get("hostname").getAsString();
        int port = arguments.get("port").getAsInt();
        String iv = arguments.get("iv").getAsString();
        String ciphertext = arguments.get("ciphertext").getAsString();

        return ResponseBuilder.singleResponse("plaintext", paddingOracle(hostname, port, iv, ciphertext));
    }

    private static String paddingOracle(String hostname, int port, String base64IV, String base64Ciphertext) {
        FieldElement iv = FieldElement.fromBase64XEX(base64IV);
        byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);

        List<FieldElement> decryptedBlocksList = new ArrayList<>();

        List<byte[]> ciphertextByteBlocks = Util.splitIntoChunks(ciphertext, 16);

        for (int i = 0; i < ciphertextByteBlocks.size(); i++) {
            FieldElement ciphertextBlock = new FieldElement(ciphertextByteBlocks.get(i));
            FieldElement qBlock = (i == 0) ? iv : new FieldElement(ciphertextByteBlocks.get(i - 1));

            FieldElement decryptedBlock = decryptSingleBlock(qBlock, ciphertextBlock, hostname, port);
            decryptedBlocksList.add(decryptedBlock);
        }

        return Base64.getEncoder().encodeToString(Util.concatFieldElementsXEX(decryptedBlocksList));
    }

    private static FieldElement decryptSingleBlock(FieldElement qBlock, FieldElement ciphertextBlock, String hostname,
            int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port));
            socket.setTcpNoDelay(true);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Handling the first Byte and its edge-case
            out.write(ciphertextBlock.toByteArrayXEX());
            byte[] firstResponse = sendAllPossibilities(out, in, 15, null);

            // (if two) Check which byte is not the result of having 0x01 at the end
            byte validPaddingByte = findRelevantPaddingByteInFirstResponse(out, in, firstResponse);

            byte[] baseBytes = new byte[16];
            baseBytes[15] = (byte) (validPaddingByte ^ 0x01);

            // Handling the remaining bytes
            for (int i = 14; i >= 0; i--) {
                byte[] response = sendAllPossibilities(out, in, i, baseBytes);

                int index = getValidPaddingIndex(response);

                baseBytes[i] = (byte) (index & 0xFF ^ (16 - i));
            }
            FieldElement base = new FieldElement(baseBytes); // D(C_n)
            FieldElement result = base.xor(qBlock);

            return result; // Plaintext = D(C_n) XOR C_(n-1)
        } catch (IOException e) {
            throw new RuntimeException("Padding Oracle failed");
        }
    }

    private static byte[] sendAllPossibilities(OutputStream out, InputStream in, int byteIndex, byte[] currentBaseBytes)
            throws IOException {
        byte[] payload = new byte[LENGTH_BYTES_COUNT + Q_BLOCK_COUNT * FieldElement.BYTE_COUNT];

        // length bytes
        payload[0] = 0;
        payload[1] = 1;

        // q blocks
        for (int i = 0; i < Q_BLOCK_COUNT; i++) {
            FieldElement pad = toPaddedBlock(byteIndex, (byte) i);

            // Fill in already known bytes
            if (byteIndex < 15) {
                byte[] array = pad.toByteArrayXEX();
                for (int j = 15; j > byteIndex; j--)
                    array[j] = (byte) (currentBaseBytes[j] ^ (16 - byteIndex));
                System.arraycopy(
                        array, 0,
                        payload, LENGTH_BYTES_COUNT + i * FieldElement.BYTE_COUNT, FieldElement.BYTE_COUNT);
            } else
                System.arraycopy(
                        pad.toByteArrayXEX(), 0,
                        payload, LENGTH_BYTES_COUNT + i * FieldElement.BYTE_COUNT, FieldElement.BYTE_COUNT);
        }

        out.write(payload);

        return readInResponse(in, Q_BLOCK_COUNT);
    }

    private static byte findRelevantPaddingByteInFirstResponse(OutputStream out, InputStream in, byte[] response)
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

    private static boolean is01Padding(OutputStream out, InputStream in, byte toTest) throws IOException {
        byte[] testerPayload = new byte[LENGTH_BYTES_COUNT + FieldElement.BYTE_COUNT * 2];

        // length bytes
        testerPayload[0] = 2;
        testerPayload[1] = 0;

        // Test 2 diff. possibilities for other byte to not get it accidentally right
        testerPayload[LENGTH_BYTES_COUNT + FieldElement.BYTE_COUNT - 1] = toTest;
        testerPayload[LENGTH_BYTES_COUNT + FieldElement.BYTE_COUNT - 2] = 0x02; // some value
        testerPayload[LENGTH_BYTES_COUNT + FieldElement.BYTE_COUNT * 2 - 1] = toTest;
        testerPayload[LENGTH_BYTES_COUNT + FieldElement.BYTE_COUNT * 2 - 2] = 0x01; // some other value

        out.write(testerPayload);

        byte[] response = new byte[2];
        in.read(response);

        boolean result = (response[0] == 0x01 && response[1] == 0x01);
        return !result;
    }

    // This is for the first run, since there can be two correct answers
    private static List<Integer> getValidPaddingIndexes(byte[] response) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < response.length; i++)
            if (response[i] == 0x01)
                list.add(i);

        return list;
    }

    // For every packet after the first one
    private static int getValidPaddingIndex(byte[] response) {
        for (int i = 0; i < response.length; i++) {
            if (response[i] == 0x01)
                return i;
        }

        throw new RuntimeException("Got all 0s as response");
    }

    private static FieldElement toPaddedBlock(int byteIndex, byte pad) {
        byte[] array = FieldElement.Zero().toByteArrayXEX();
        array[byteIndex] = pad;
        return new FieldElement(array);
    }

    private static byte[] readInResponse(InputStream in, int expectedLength) throws IOException {
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
