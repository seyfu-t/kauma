package me.seyfu_t.actions;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;

public class Block2PolyAction implements Action {

    @Override
    public Entry<String, Object> execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        String base64Block = arguments.get("block").getAsString();

        int[] coefficients = switch (semantic) {
            case "xex" -> convertBlock2Poly(base64Block, false);
            case "gcm" -> convertBlock2Poly(base64Block, true);
            default -> null;
        };

        return new AbstractMap.SimpleEntry<>("coefficients", coefficients);
    }

    private static int[] convertBlock2Poly(String base64Block, boolean gcm) {
        byte[] blockByteArray = Base64.getDecoder().decode(base64Block);

        int[] coefficients = new int[128];
        int slot = 0;// to know the current index of the coefficients array

        // check each bit and add coefficient if bit is set
        for (int byteIndex = 0; byteIndex < 16; byteIndex++) {
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {

                boolean condition;

                if (gcm) // gcm condition
                    condition = (blockByteArray[byteIndex] & (1 << (7 - bitIndex))) != 0;
                else // non-gcm condition
                    condition = (blockByteArray[byteIndex] & (1 << bitIndex)) != 0;

                if (condition) {
                    coefficients[slot] = (byteIndex * 8) + bitIndex;
                    slot++;
                }
            }
        }

        coefficients = Arrays.copyOfRange(coefficients, 0, slot);
        Arrays.sort(coefficients);
        return coefficients;
    }

}
