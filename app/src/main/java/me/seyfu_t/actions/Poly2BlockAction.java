package me.seyfu_t.actions;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;

public class Poly2BlockAction implements Action {

    @Override
    public Entry<String, Object> execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        int[] coefficients = convertJsonArrayToIntArray(arguments.get("coefficients").getAsJsonArray());
        Arrays.sort(coefficients); // sort ascending, using O(n*log(n)) algorithm BTW

        String block = "";

        if (semantic.equalsIgnoreCase("xex")) {
            block = convertPoly2BlockXEX(coefficients);
        }

        return new AbstractMap.SimpleEntry<>("block", block); // Very SIMPLE way of creating a SIMPLE key-value pair, that's java for ya
    }

    private static String convertPoly2BlockXEX(int[] coefficients) {
        byte[] blockByteArray = new byte[16];

        for (int co : coefficients) {
            byte byteIndex = (byte) Math.floor(co / 8);
            byte bitIndex = (byte) (co % 8);
            blockByteArray[byteIndex] = (byte) (blockByteArray[byteIndex] | (1 << bitIndex));
        }

        // Convert to base64 and return
        String base64 = Base64.getEncoder().encodeToString(blockByteArray);
        return base64;
    }

    private static int[] convertJsonArrayToIntArray(JsonArray array) {
        int[] intArray = new int[array.size()];

        for (int i = 0; i < array.size(); i++) {
            intArray[i] = array.get(i).getAsInt();
        }

        return intArray;
    }

}
