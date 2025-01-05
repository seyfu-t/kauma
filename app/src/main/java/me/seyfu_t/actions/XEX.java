package me.seyfu_t.actions;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.actions.basic.SEA128;
import me.seyfu_t.actions.gf.GFMul;
import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;

public class XEX implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String mode = arguments.get("mode").getAsString();
        byte[] key = Base64.getDecoder().decode(arguments.get("key").getAsString());
        byte[] tweak = Base64.getDecoder().decode(arguments.get("tweak").getAsString());
        byte[] input = Base64.getDecoder().decode(arguments.get("input").getAsString());

        return ResponseBuilder.single("output", xex(mode, key, tweak, input));
    }

    private static String xex(String mode, byte[] key, byte[] tweak, byte[] input) {
        byte[] keyOne = Arrays.copyOfRange(key, 0, key.length / 2);
        byte[] keyTwo = Arrays.copyOfRange(key, key.length / 2, key.length);

        byte[] output = switch (mode) {
            case "encrypt" -> cryptAllBlocks("encrypt", input, tweak, keyOne, keyTwo);
            case "decrypt" -> cryptAllBlocks("decrypt", input, tweak, keyOne, keyTwo);
            default -> throw new IllegalArgumentException("Null is not a valid mode");
        };

        return Base64.getEncoder().encodeToString(output);
    }

    private static byte[] cryptAllBlocks(String mode, byte[] input, byte[] tweak, byte[] keyOne, byte[] keyTwo) {
        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length / 16; i++) {
            byte[] roundPlainOrCipherText = Arrays.copyOfRange(input, i * 16, (i + 1) * 16);

            FieldElement roundKey = getMasterKeyForRound(i, tweak, keyTwo);

            byte[] block = cryptSingleBlock(mode, roundPlainOrCipherText, roundKey.toByteArrayXEX(), keyOne);
            System.arraycopy(block, 0, output, i * 16, 16);
        }
        return output;
    }

    private static FieldElement getMasterKeyForRound(int round, byte[] tweak, byte[] keyTwo) {
        // Starter key
        FieldElement masterKey = new FieldElement(SEA128.encryptSEA128(tweak, keyTwo));

        // For every other block, multiply with alpha in GF2^128
        for (int i = 0; i < round; i++)
            masterKey = GFMul.mulAndReduce(masterKey, FieldElement.ALPHA);

        return masterKey;
    }

    private static byte[] cryptSingleBlock(String mode, byte[] text, byte[] roundKey, byte[] keyOne) {
        // XOR
        for (int i = 0; i < 16; i++)
            text[i] ^= roundKey[i];

        // Encrypt or Decrypt
        text = SEA128.sea128(mode, text, keyOne);

        // XOR
        for (int i = 0; i < 16; i++)
            text[i] ^= roundKey[i];

        return text;
    }

}