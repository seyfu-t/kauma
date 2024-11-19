package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class XEXAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String mode = arguments.get("mode").getAsString();
        String key = arguments.get("key").getAsString();
        String tweak = arguments.get("tweak").getAsString();
        String input = arguments.get("input").getAsString();

        String output = xex(mode, key, tweak, input);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("output", output);
        
        return resultMap;
    }

    private static String xex(String mode, String base64Key, String base64Tweak, String base64Input) {
        byte[] fullKey = Base64.getDecoder().decode(base64Key);
        UBigInt16 keyOne = new UBigInt16(Arrays.copyOfRange(fullKey, 0, fullKey.length / 2));
        UBigInt16 keyTwo = new UBigInt16(Arrays.copyOfRange(fullKey, fullKey.length / 2, fullKey.length));

        UBigInt16 tweak = UBigInt16.fromBase64(base64Tweak);

        byte[] input = Base64.getDecoder().decode(base64Input);

        byte[] output = switch (mode) {
            case "encrypt" -> cryptAllBlocks("encrypt", input, tweak, keyOne, keyTwo);
            case "decrypt" -> cryptAllBlocks("decrypt", input, tweak, keyOne, keyTwo);
            default -> throw new IllegalArgumentException("Null is not a valid mode");
        };

        String out = Base64.getEncoder().encodeToString(output);
        return out;
    }

    private static byte[] cryptAllBlocks(String mode, byte[] input, UBigInt16 tweak, UBigInt16 keyOne,
            UBigInt16 keyTwo) {
        List<UBigInt16> blocksList = new ArrayList<>();

        for (int i = 0; i < input.length / 16; i++) {
            UBigInt16 roundPlainOrCipherText = new UBigInt16(Arrays.copyOfRange(input, i * 16, (i + 1) * 16));

            UBigInt16 roundKey = getMasterKeyForRound(i, tweak, keyTwo);

            UBigInt16 block = cryptSingleBlock(mode, roundPlainOrCipherText, roundKey, keyOne);
            blocksList.add(block);
        }
        return Util.concatUBigInt16s(blocksList);
    }

    private static UBigInt16 getMasterKeyForRound(int round, UBigInt16 tweak, UBigInt16 keyTwo) {
        // Starter key
        String base64MasterKey = SEA128Action.sea128("encrypt", tweak.toBase64(), keyTwo.toBase64());

        // For every other block, multiply with alpha in GF2^128
        for (int i = 0; i < round; i++) {
            UBigInt16 roundKey = GFMulAction.combinedMulAndModReduction(UBigInt16.fromBase64(base64MasterKey), UBigInt16.ALPHA);
            base64MasterKey = roundKey.toBase64();
        }

        return UBigInt16.fromBase64(base64MasterKey);
    }

    private static UBigInt16 cryptSingleBlock(String mode, UBigInt16 text, UBigInt16 roundKey, UBigInt16 keyOne) {
        // XOR
        UBigInt16 block = text.xor(roundKey);

        // Encrypt or Decrypt
        block = UBigInt16.fromBase64(SEA128Action.sea128(mode, block.toBase64(), keyOne.toBase64()));

        // XOR
        block = block.xor(roundKey);

        return block;
    }

}