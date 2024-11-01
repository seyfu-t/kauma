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

        int[] coefficients = null;

        if (semantic.equalsIgnoreCase("xex")) {
            coefficients = convertBlock2PolyXEX(base64Block);
        } else if (semantic.equalsIgnoreCase("gcm")) {
            coefficients = convertBlock2PolyGCM(base64Block);
        }

        return new AbstractMap.SimpleEntry<>("coefficients", coefficients);
    }

    private static int[] convertBlock2PolyXEX(String base64Block) {
        byte[] blockByteArray = Base64.getDecoder().decode(base64Block);

        int[] coefficients = new int[128];
        int slot = 0;// to know the current index of the coefficients array
        // check each bit and add coefficient if bit is set
        for (int byteIndex = 0; byteIndex < 16; byteIndex++) {
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                if ((blockByteArray[byteIndex] & (1 << bitIndex)) != 0) {
                    coefficients[slot] = (byteIndex * 8) + bitIndex;
                    slot++;
                }
            }
        }

        coefficients = Arrays.copyOfRange(coefficients, 0, slot);
        Arrays.sort(coefficients);
        return coefficients;
    }

    private static int[] convertBlock2PolyGCM(String base64Block) {
        byte[] blockByteArray = Base64.getDecoder().decode(base64Block);

        int[] coefficients = new int[128];
        int slot = 0;// to know the current index of the coefficients array
        // check each bit and add coefficient if bit is set
        for (int byteIndex = 0; byteIndex < 16; byteIndex++) {
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                if ((blockByteArray[byteIndex] & (1 << bitIndex)) != 0) {
                    coefficients[slot] = (byteIndex * 8) + 7 - bitIndex;
                    slot++;
                }
            }
        }

        coefficients = Arrays.copyOfRange(coefficients, 0, slot);
        Arrays.sort(coefficients);
        return coefficients;
    }

}
