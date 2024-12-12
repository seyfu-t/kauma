package me.seyfu_t.actions.basic;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;

public class Block2Poly implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String base64Block = arguments.get("block").getAsString();

        int[] coefficients = switch (semantic) {
            case "xex" -> block2Poly(base64Block, false);
            case "gcm" -> block2Poly(base64Block, true);
            default -> throw new IllegalArgumentException(semantic + " is not a valid semantic");
        };

        return ResponseBuilder.singleResponse("coefficients", coefficients);
    }

    public static int[] block2Poly(String base64Block, boolean gcm) {
        byte[] blockByteArray = Base64.getDecoder().decode(base64Block);
        int[] coefficients = new int[blockByteArray.length * 8];
        int slot = 0;

        for (int byteIndex = 0; byteIndex < blockByteArray.length; byteIndex++) {
            int currentByte = blockByteArray[byteIndex] & 0xFF;

            while (currentByte != 0) {
                int bitPosition = Integer.numberOfTrailingZeros(currentByte);
                coefficients[slot++] = (byteIndex * 8) + (gcm ? (7 - bitPosition) : bitPosition);
                currentByte &= currentByte - 1;
            }
        }
        int[] result = Arrays.copyOfRange(coefficients, 0, slot);
        Arrays.sort(result);
        return result;
    }

}
