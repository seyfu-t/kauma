package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class XEXAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String mode = arguments.get("mode").getAsString();
        String key = arguments.get("key").getAsString();
        String tweak = arguments.get("tweak").getAsString();
        String input = arguments.get("input").getAsString();

        return ResponseBuilder.singleResponse("output", xex(mode, key, tweak, input));
    }

    private static String xex(String mode, String base64Key, String base64Tweak, String base64Input) {
        byte[] fullKey = Base64.getDecoder().decode(base64Key);
        FieldElement keyOne = new FieldElement(Arrays.copyOfRange(fullKey, 0, fullKey.length / 2));
        FieldElement keyTwo = new FieldElement(Arrays.copyOfRange(fullKey, fullKey.length / 2, fullKey.length));

        FieldElement tweak = FieldElement.fromBase64XEX(base64Tweak);

        byte[] input = Base64.getDecoder().decode(base64Input);

        byte[] output = switch (mode) {
            case "encrypt" -> cryptAllBlocks("encrypt", input, tweak, keyOne, keyTwo);
            case "decrypt" -> cryptAllBlocks("decrypt", input, tweak, keyOne, keyTwo);
            default -> throw new IllegalArgumentException("Null is not a valid mode");
        };

        String out = Base64.getEncoder().encodeToString(output);
        return out;
    }

    private static byte[] cryptAllBlocks(String mode, byte[] input, FieldElement tweak, FieldElement keyOne,
            FieldElement keyTwo) {
        List<FieldElement> blocksList = new ArrayList<>();

        for (int i = 0; i < input.length / 16; i++) {
            FieldElement roundPlainOrCipherText = new FieldElement(Arrays.copyOfRange(input, i * 16, (i + 1) * 16));

            FieldElement roundKey = getMasterKeyForRound(i, tweak, keyTwo);

            FieldElement block = cryptSingleBlock(mode, roundPlainOrCipherText, roundKey, keyOne);
            blocksList.add(block);
        }
        return Util.concatFieldElements(blocksList);
    }

    private static FieldElement getMasterKeyForRound(int round, FieldElement tweak, FieldElement keyTwo) {
        // Starter key
        String base64MasterKey = SEA128Action.sea128("encrypt", tweak.toBase64XEX(), keyTwo.toBase64XEX());

        // For every other block, multiply with alpha in GF2^128
        for (int i = 0; i < round; i++) {
            FieldElement roundKey = GFMulAction.mulAndReduce(FieldElement.fromBase64XEX(base64MasterKey), FieldElement.ALPHA);
            base64MasterKey = roundKey.toBase64XEX();
        }

        return FieldElement.fromBase64XEX(base64MasterKey);
    }

    private static FieldElement cryptSingleBlock(String mode, FieldElement text, FieldElement roundKey, FieldElement keyOne) {
        // XOR
        FieldElement block = text.xor(roundKey);

        // Encrypt or Decrypt
        block = FieldElement.fromBase64XEX(SEA128Action.sea128(mode, block.toBase64XEX(), keyOne.toBase64XEX()));

        // XOR
        block = block.xor(roundKey);

        return block;
    }

}