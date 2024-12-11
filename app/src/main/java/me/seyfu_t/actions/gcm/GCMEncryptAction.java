package me.seyfu_t.actions.gcm;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.actions.basic.SEA128Action;
import me.seyfu_t.actions.gf.GFMulAction;
import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.util.AES;
import me.seyfu_t.util.ResponseBuilder;

public class GCMEncryptAction implements Action {

    public static final int AUTH_TAG_COUNTER = 1;
    private static final int BLOCK_SIZE = 16;

    @Override
    public JsonObject execute(JsonObject arguments) {
        String algorithm = arguments.get("algorithm").getAsString();
        byte[] nonce = Base64.getDecoder().decode(arguments.get("nonce").getAsString());
        byte[] key = Base64.getDecoder().decode(arguments.get("key").getAsString());
        byte[] plaintext = Base64.getDecoder().decode(arguments.get("plaintext").getAsString());
        byte[] ad = Base64.getDecoder().decode(arguments.get("ad").getAsString());

        return gcmEncrypt(algorithm, nonce, key, plaintext, ad);
    }

    public static JsonObject gcmEncrypt(String algorithm, byte[] nonce, byte[] key, byte[] plaintext, byte[] ad) {

        byte[] ciphertext = crypt(algorithm, nonce, key, plaintext);

        FieldElement authTagMask = new FieldElement(mask(algorithm, key, nonce, AUTH_TAG_COUNTER));
        FieldElement authKey = authKey(algorithm, key);
        FieldElement lengthBlock = lengthBlock(ad, ciphertext);
        FieldElement ghash = ghash(ciphertext, ad, authKey, lengthBlock);
        FieldElement authTag = authTagMask.xor(ghash);

        return ResponseBuilder.multiResponse(Arrays.asList(
                new Tuple<>("ciphertext", Base64.getEncoder().encodeToString(ciphertext)),
                new Tuple<>("tag", authTag.toBase64XEX()),
                new Tuple<>("L", lengthBlock.toBase64XEX()),
                new Tuple<>("H", authKey.toBase64XEX())));
    }

    // En-/Decryption

    public static byte[] crypt(String algorithm, byte[] nonce, byte[] key, byte[] plaintext) {
        byte[] ciphertext = new byte[plaintext.length];

        int plaintextBlockCount = (plaintext.length / BLOCK_SIZE) + (plaintext.length % BLOCK_SIZE == 0 ? 0 : 1);

        for (int i = 0; i < plaintextBlockCount; i++) {
            byte[] plaintextSlice = Arrays.copyOfRange(plaintext, i * BLOCK_SIZE,
                    Math.min((i + 1) * BLOCK_SIZE, plaintext.length));
            byte[] mask = mask(algorithm, key, nonce, i + 2);
            for (int j = 0; j < plaintextSlice.length; j++)
                ciphertext[j + i * BLOCK_SIZE] = (byte) (mask[j] ^ plaintextSlice[j]);
        }

        return ciphertext;
    }

    // GHASH

    public static FieldElement ghash(byte[] ciphertext, byte[] ad, FieldElement authKey, FieldElement lengthBlock) {
        FieldElement lastBlock = FieldElement.Zero();
        int adBlockCount = (ad.length / BLOCK_SIZE) + (ad.length % BLOCK_SIZE == 0 ? 0 : 1);

        for (int i = 0; i < adBlockCount; i++) {
            byte[] adSlice = Arrays.copyOfRange(ad, i * BLOCK_SIZE, Math.min((i + 1) * BLOCK_SIZE, ad.length));
            FieldElement adBlock = new FieldElement(adSlice);
            lastBlock = singleGashBlock(lastBlock, adBlock, authKey);
        }

        int ciphertextBlockCount = (ciphertext.length / BLOCK_SIZE) + (ciphertext.length % BLOCK_SIZE == 0 ? 0 : 1);

        for (int i = 0; i < ciphertextBlockCount; i++) {
            byte[] ciphertextSlice = Arrays.copyOfRange(ciphertext, i * BLOCK_SIZE,
                    Math.min((i + 1) * BLOCK_SIZE, ciphertext.length));
            FieldElement ciphertextBlock = new FieldElement(ciphertextSlice);
            lastBlock = singleGashBlock(lastBlock, ciphertextBlock, authKey);
        }

        FieldElement result = singleGashBlock(lastBlock, lengthBlock, authKey);

        return result;
    }

    public static FieldElement lengthBlock(byte[] ad, byte[] ciphertext) {
        long adLengthBits = ad.length * 8L;
        long ciphertextLengthBits = ciphertext.length * 8L;

        byte[] result = new byte[16];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) ((adLengthBits >>> (56 - i * 8)) & 0xFF);
            result[i + 8] = (byte) ((ciphertextLengthBits >>> (56 - i * 8)) & 0xFF);
        }

        return new FieldElement(result);
    }

    public static FieldElement singleGashBlock(FieldElement lastBlock, FieldElement block, FieldElement authKey) {
        return GFMulAction.mulAndReduceGHASH(lastBlock.xor(block), authKey);
    }

    public static FieldElement authKey(String algorithm, byte[] key) {
        byte[] authKey = switch (algorithm) {
            case "aes128" -> AES.encrypt(new byte[BLOCK_SIZE], key);
            case "sea128" -> SEA128Action.encryptSEA128(new byte[BLOCK_SIZE], key);
            default -> throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
        };

        return new FieldElement(authKey);
    }

    // Auth Tag

    public static byte[] mask(String algorithm, byte[] key, byte[] nonce, long counter) {
        byte[] input = concatNonceCounter(nonce, counter);

        return switch (algorithm) {
            case "aes128" -> AES.encrypt(input, key);
            case "sea128" -> SEA128Action.encryptSEA128(input, key);
            default -> throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
        };
    }

    public static byte[] concatNonceCounter(byte[] nonce, long counter) {
        byte[] ctr = new byte[4]; // counter is assumed to have non-gcm bit order
        ctr[0] = (byte) ((counter >> 24) & 0xFF);
        ctr[1] = (byte) ((counter >> 16) & 0xFF);
        ctr[2] = (byte) ((counter >> 8) & 0xFF);
        ctr[3] = (byte) (counter & 0xFF);

        byte[] result = Arrays.copyOf(nonce, BLOCK_SIZE);
        // concat
        System.arraycopy(ctr, 0, result, 12, 4);

        return result;
    }

}
