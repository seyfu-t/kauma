package me.seyfu_t.actions.basic;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;

public class Poly2Block implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        int[] coefficients = convertJsonArrayToIntArray(arguments.get("coefficients").getAsJsonArray());
        Arrays.sort(coefficients); // sort ascending, using O(n*log(n)) algorithm BTW

        String block = switch (semantic) {
            case "xex" -> poly2Block(coefficients, false);
            case "gcm" -> poly2Block(coefficients, true);
            default -> throw new IllegalArgumentException(semantic + " is not a valid semantic");
        };

        return ResponseBuilder.singleResponse("block", block);
    }

    private static String poly2Block(int[] coefficients, boolean gcm) {
        byte[] blockByteArray = new byte[16];

        for (int coefficient : coefficients) {
            byte byteIndex = (byte) (coefficient / 8);
            byte bitIndex = (byte) (gcm ? (7 - (coefficient % 8)) : (coefficient % 8));
            blockByteArray[byteIndex] = (byte) (blockByteArray[byteIndex] | (1 << bitIndex));
        }

        return Base64.getEncoder().encodeToString(blockByteArray);
    }

    private static int[] convertJsonArrayToIntArray(JsonArray array) {
        int[] intArray = new int[array.size()];

        for (int i = 0; i < array.size(); i++)
            intArray[i] = array.get(i).getAsInt();

        return intArray;
    }

}
