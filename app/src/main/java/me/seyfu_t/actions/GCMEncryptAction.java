package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.util.AES;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GCMEncryptAction implements Action {

    public static final int AUTH_TAG_INDEX = -1;

    @Override
    public JsonObject execute(JsonObject arguments) {
        String algorithm = arguments.get("algorithm").getAsString();
        String nonce = arguments.get("nonce").getAsString();
        String key = arguments.get("key").getAsString();
        String plaintext = arguments.get("plaintext").getAsString();
        String ad = arguments.get("ad").getAsString();

        return gcmEncrypt(algorithm, nonce, key, plaintext, ad);
    }

    public static JsonObject gcmEncrypt(String algorithm, String base64Nonce, String base64Key,
            String base64Plaintext, String base64AD) {

        FieldElement key = FieldElement.fromBase64XEX(base64Key);
        byte[] plaintextBytes = Base64.getDecoder().decode(base64Plaintext);
        byte[] ciphertextBytes = generateFullText(algorithm, base64Nonce, key, plaintextBytes);
        byte[] adBytes = Base64.getDecoder().decode(base64AD);

        FieldElement H = calculateAuthKey(algorithm, key);
        FieldElement L = calculateLengthOfADAndCiphertexts(adBytes, ciphertextBytes);
        FieldElement ghashResult = ghash(H, adBytes, ciphertextBytes, L);
        // index -1 because 0 is the first ciphertext and this is one before that
        FieldElement authTag = generateSingleTextBlock(AUTH_TAG_INDEX, algorithm, base64Nonce, key, ghashResult);

        String ciphertext = Base64.getEncoder().encodeToString(ciphertextBytes);
        String base64L = L.toBase64XEX();
        String base64H = H.toBase64XEX();
        String base64AuthTag = authTag.toBase64XEX();

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<>("ciphertext", ciphertext),
                new Tuple<>("tag", base64AuthTag),
                new Tuple<>("L", base64L),
                new Tuple<>("H", base64H)));
    }

    public static FieldElement calculateAuthKey(String algorithm, FieldElement key) {
        return switch (algorithm) {
            case "aes128" -> new FieldElement(AES.encrypt(new byte[16], key.toByteArrayXEX()));
            case "sea128" -> new FieldElement(SEA128Action.encryptSEA128(new byte[16], key.toByteArrayXEX()));
            default -> throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
        };
    }

    public static byte[] generateFullText(String algorithm, String nonce, FieldElement key, byte[] text) {

        List<FieldElement> ciphertextBlocksList = new ArrayList<>();
        int blockCount = (text.length / 16) + (text.length % 16 == 0 ? 0 : 1);

        for (int i = 0; i < blockCount; i++) {
            // Copy each 16 byte block into a FieldElement (this will auto-pad the last one
            // if
            // needed)
            FieldElement textBlock = new FieldElement(Arrays.copyOfRange(text, i * 16, (i + 1) * 16));
            FieldElement ciphertextBlock = generateSingleTextBlock(i, algorithm, nonce, key, textBlock);
            ciphertextBlocksList.add(ciphertextBlock);
        }

        byte[] concatedCiphertext = Util.concatFieldElementsXEX(ciphertextBlocksList);
        // undo the part that resulted from padding the last ciphertext block
        byte[] result = Arrays.copyOfRange(concatedCiphertext, 0, text.length);

        return result;
    }

    // also used for creating the Auth Tag
    public static FieldElement generateSingleTextBlock(long index, String algorithm, String nonce, FieldElement key,
            FieldElement plaintextPart) {

        FieldElement nonceConcatCounter = concatNonceAndCounter(nonce, index + 2); // the 0th ciphertext block has Ctr 2

        return switch (algorithm) {
            case "aes128" ->
                new FieldElement(AES.encrypt(nonceConcatCounter.toByteArrayXEX(), key.toByteArrayXEX()))
                        .xor(plaintextPart);
            case "sea128" ->
                new FieldElement(SEA128Action.encryptSEA128(nonceConcatCounter.toByteArrayXEX(), key.toByteArrayXEX()))
                        .xor(plaintextPart);
            default -> throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
        };
    }

    public static FieldElement concatNonceAndCounter(String base64Nonce, long counter) {
        byte[] ctr = new byte[4]; // counter is assumed to have non-gcm bit order
        ctr[0] = (byte) ((counter >> 24) & 0xFF);
        ctr[1] = (byte) ((counter >> 16) & 0xFF); // TODO error could lie here
        ctr[2] = (byte) ((counter >> 8) & 0xFF);
        ctr[3] = (byte) (counter & 0xFF);

        // 12 byte nonce, rest will be filed with 0s
        FieldElement nonce = FieldElement.fromBase64XEX(base64Nonce);
        byte[] result = nonce.toByteArrayXEX();

        // copy counter into remaining 4 bytes
        System.arraycopy(ctr, 0, result, 12, 4);

        return new FieldElement(result);
    }

    public static FieldElement ghash(FieldElement authKey, byte[] ad, byte[] ciphertext, FieldElement concatedLength) {
        FieldElement lastBlock = FieldElement.Zero(); // begin with all 0s
        int adBlockCount = (ad.length / 16) + (ad.length % 16 == 0 ? 0 : 1);

        for (int i = 0; i < adBlockCount; i++) {
            int currentMax = (i + 1) * 16 > ad.length ? ad.length : (i + 1) * 16;
            FieldElement currentADBlock = new FieldElement(Arrays.copyOfRange(ad, i * 16, currentMax));
            lastBlock = singleGashBlock(lastBlock, currentADBlock, authKey);
        }

        int ciphertextBlockCount = (ciphertext.length / 16) + (ciphertext.length % 16 == 0 ? 0 : 1);

        for (int i = 0; i < ciphertextBlockCount; i++) {
            int currentMax = (i + 1) * 16 > ciphertext.length ? ciphertext.length : (i + 1) * 16;
            FieldElement currentCiphertextBlock = new FieldElement(Arrays.copyOfRange(ciphertext, i * 16, currentMax));
            lastBlock = singleGashBlock(lastBlock, currentCiphertextBlock, authKey);
        }
        FieldElement result = singleGashBlock(lastBlock, concatedLength, authKey);

        return result;
    }

    public static FieldElement singleGashBlock(FieldElement inputA, FieldElement inputB, FieldElement authKey) {
        FieldElement xor = inputA.xor(inputB);
        return GFMulAction.mulAndReduce(xor.swapInnerGCMState(), authKey.swapInnerGCMState()).swapInnerGCMState();
    }

    public static FieldElement calculateLengthOfADAndCiphertexts(byte[] ad, byte[] ciphertext) {
        // Amount of bits
        long adLengthInt = ad.length * 8;
        long ciphertextLengthInt = ciphertext.length * 8;

        byte[] adLength = new byte[8];
        byte[] ciphertextLength = new byte[8];

        // Convert the length from long to byte array representation
        for (int i = 0; i < 8; i++) {
            adLength[i] = (byte) ((adLengthInt >> i * 8) & 0xFF);
            ciphertextLength[i] = (byte) ((ciphertextLengthInt >> i * 8) & 0xFF);
        }

        // Go big-endian
        adLength = Util.swapByteOrder(adLength);
        ciphertextLength = Util.swapByteOrder(ciphertextLength);

        byte[] result = new byte[16];

        // Concat into single 16 byte array
        for (int i = 0; i < 8; i++) {
            result[i] = adLength[i];
            result[i + 8] = ciphertextLength[i];
        }

        return new FieldElement(result);
    }

}
