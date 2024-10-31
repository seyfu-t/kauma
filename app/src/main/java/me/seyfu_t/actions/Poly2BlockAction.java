package me.seyfu_t.actions;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.Util;

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
        // Arbitrary size number, in little endian
        BigInteger block = BigInteger.ZERO;

        for (int i : coefficients) {
            block = block.setBit(i);
        }

        // go little-endian
        block = Util.changeEndianness(block);

        // Fix size to 16 bytes
        byte[] finalBlockByteArray = Util.littleEndianSignedBigIntTo16Bytes(block).toByteArray();

        // Convert to base64 and return
        String base64 = Base64.getEncoder().encodeToString(finalBlockByteArray);
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
