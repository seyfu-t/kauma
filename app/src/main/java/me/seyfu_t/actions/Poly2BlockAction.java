package me.seyfu_t.actions;

import java.util.Arrays;
import java.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;

public class Poly2BlockAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String semantic = arguments.get("semantic").getAsString();
        int[] coefficients = convertJsonArrayToIntArray(arguments.get("coefficients").getAsJsonArray());
        Arrays.sort(coefficients); // sort ascending, using O(n*log(n)) algorithm BTW

        String block = switch (semantic) {
            case "xex" -> convertPoly2Block(coefficients, false);
            case "gcm" -> convertPoly2Block(coefficients, true);
            default -> throw new IllegalArgumentException(semantic + " is not a valid semantic");
        };

        return ResponseBuilder.singleResponse("block", block);
    }

    private static String convertPoly2Block(int[] coefficients, boolean gcm) {
        byte[] blockByteArray = new byte[16];

        for (int co : coefficients) {
            byte byteIndex = (byte) Math.floor(co / 8);
            byte bitIndex = (byte) (gcm ? (7 - (co % 8)) : (co % 8));
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
