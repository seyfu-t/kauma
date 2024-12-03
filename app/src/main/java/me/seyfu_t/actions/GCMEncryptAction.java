package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.AES;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GCMEncryptAction implements Action {

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

        UBigInt16 key = UBigInt16.fromBase64(base64Key, true);
        byte[] plaintextBytes = Base64.getDecoder().decode(base64Plaintext);
        byte[] ciphertextBytes = generateFullText(algorithm, base64Nonce, key, plaintextBytes);
        byte[] adBytes = Base64.getDecoder().decode(base64AD);

        UBigInt16 H = calculateAuthKey(algorithm, key);
        UBigInt16 L = calculateLengthOfADAndCiphertexts(adBytes, ciphertextBytes);
        UBigInt16 ghashResult = ghash(H, adBytes, ciphertextBytes, L);
        // index -1 because 0 is the first ciphertext and this is one before that
        UBigInt16 authTag = generateSingleTextBlock(-1, algorithm, base64Nonce, key, ghashResult);

        String ciphertext = Base64.getEncoder().encodeToString(ciphertextBytes);
        String base64L = L.toBase64();
        String base64H = H.toBase64();
        String base64AuthTag = authTag.toBase64();

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<>("ciphertext", ciphertext),
                new Tuple<>("tag", base64AuthTag),
                new Tuple<>("L", base64L),
                new Tuple<>("H", base64H)));
    }

    public static UBigInt16 calculateAuthKey(String algorithm, UBigInt16 key) {
        return switch (algorithm) {
            case "aes128" -> new UBigInt16(AES.encrypt(new byte[16], key.toByteArray()), true);
            case "sea128" -> new UBigInt16(SEA128Action.encryptSEA128(new byte[16], key.toByteArray()), true);
            default -> throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
        };
    }

    public static byte[] generateFullText(String algorithm, String nonce, UBigInt16 key, byte[] text) {

        List<UBigInt16> ciphertextBlocksList = new ArrayList<>();
        int blockCount = (text.length / 16) + (text.length % 16 == 0 ? 0 : 1);

        for (int i = 0; i < blockCount; i++) {
            // Copy each 16 byte block into a UBigInt16 (this will auto-pad the last one if
            // needed)
            UBigInt16 textBlock = new UBigInt16(Arrays.copyOfRange(text, i * 16, (i + 1) * 16), true);
            UBigInt16 ciphertextBlock = generateSingleTextBlock(i, algorithm, nonce, key, textBlock);
            ciphertextBlocksList.add(ciphertextBlock);
        }

        byte[] concatedCiphertext = Util.concatUBigInt16s(ciphertextBlocksList);
        // undo the part that resulted from padding the last ciphertext block
        byte[] result = Arrays.copyOfRange(concatedCiphertext, 0, text.length);

        return result;
    }

    // also used for creating the Auth Tag
    public static UBigInt16 generateSingleTextBlock(long index, String algorithm, String nonce, UBigInt16 key,
            UBigInt16 plaintextPart) {

        UBigInt16 nonceConcatCounter = concatNonceAndCounter(nonce, index + 2); // the 0th ciphertext block has Ctr 2

        return switch (algorithm) {
            case "aes128" ->
                new UBigInt16(AES.encrypt(nonceConcatCounter.toByteArray(), key.toByteArray()), true)
                        .xor(plaintextPart);
            case "sea128" ->
                new UBigInt16(SEA128Action.encryptSEA128(nonceConcatCounter.toByteArray(), key.toByteArray()), true)
                        .xor(plaintextPart);
            default -> throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
        };
    }

    private static UBigInt16 concatNonceAndCounter(String base64Nonce, long counter) {
        byte[] ctr = new byte[4]; // counter is assumed to have non-gcm bit order
        ctr[0] = (byte) ((counter >> 24) & 0xFF);
        ctr[2] = (byte) ((counter >> 8) & 0xFF);
        ctr[1] = (byte) ((counter >> 16) & 0xFF);
        ctr[3] = (byte) (counter & 0xFF);

        // 12 byte nonce, rest will be filed with 0s
        UBigInt16 nonce = UBigInt16.fromBase64(base64Nonce, true);
        byte[] result = nonce.toByteArray();

        // copy counter into remaining 4 bytes
        System.arraycopy(ctr, 0, result, 12, 4);

        return new UBigInt16(result, true);
    }

    public static UBigInt16 ghash(UBigInt16 authKey, byte[] ad, byte[] ciphertext, UBigInt16 concatedLength) {
        UBigInt16 lastBlock = UBigInt16.Zero(true); // begin with all 0s

        int adBlockCount = (ad.length / 16) + (ad.length % 16 == 0 ? 0 : 1);

        for (int i = 0; i < adBlockCount; i++) {
            int currentMax = (i + 1) * 16 > ad.length ? ad.length : (i + 1) * 16;
            UBigInt16 currentADBlock = new UBigInt16(Arrays.copyOfRange(ad, i * 16, currentMax), true);
            lastBlock = singleGashBlock(lastBlock, currentADBlock, authKey);
        }

        int ciphertextBlockCount = (ciphertext.length / 16) + (ciphertext.length % 16 == 0 ? 0 : 1);

        for (int i = 0; i < ciphertextBlockCount; i++) {
            int currentMax = (i + 1) * 16 > ciphertext.length ? ciphertext.length : (i + 1) * 16;
            UBigInt16 currentCiphertextBlock = new UBigInt16(Arrays.copyOfRange(ciphertext, i * 16, currentMax), true);
            lastBlock = singleGashBlock(lastBlock, currentCiphertextBlock, authKey);
        }

        UBigInt16 result = singleGashBlock(lastBlock, concatedLength, authKey);

        return result;
    }

    private static UBigInt16 singleGashBlock(UBigInt16 inputA, UBigInt16 inputB, UBigInt16 authKey) {
        UBigInt16 xor = inputA.xor(inputB);
        return GFMulAction.combinedMulAndModReduction(xor, authKey);
    }

    public static UBigInt16 calculateLengthOfADAndCiphertexts(byte[] ad, byte[] ciphertext) {
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

        return new UBigInt16(result, true);
    }

}
