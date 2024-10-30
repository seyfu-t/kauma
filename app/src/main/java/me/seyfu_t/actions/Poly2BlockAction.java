package me.seyfu_t.actions;

import java.math.BigInteger;
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

    private String convertPoly2BlockXEX(int[] coefficients) {
        // Arbitrary size number, in little endian
        BigInteger block = BigInteger.ZERO;

        for (int i : coefficients) {
            block = block.setBit(i);
        }

        block = changeEndianness(block);

        // Fix size to 16 bytes
        byte[] finalBlockByteArray = cutBigEndianBigIntTo16ByteSize(block);

        // Convert to base64 and return
        String base64 = Base64.getEncoder().encodeToString(finalBlockByteArray);
        return base64;
    }

    private byte[] cutBigEndianBigIntTo16ByteSize(BigInteger num) {
        byte[] limitedBytes = new byte[16];
        byte[] currentBytes = num.toByteArray();

        for (int i = 0; i < 16; i++) {
            // Padding with 0, if BigInt was less than 16 bytes
            if (i < currentBytes.length) {
                limitedBytes[i] = currentBytes[i];
            } else {
                limitedBytes[i] = 0;
            }
        }

        return limitedBytes;
    }

    private BigInteger changeEndianness(BigInteger num) {
        // Note: More efficient algorithm exists
        byte[] oldBytes = num.toByteArray();
        byte[] newBytes = new byte[oldBytes.length];

        for (int i = 0; i < oldBytes.length; i++) {
            newBytes[i] = oldBytes[oldBytes.length - i - 1];
        }

        return new BigInteger(newBytes);
    }

    private int[] convertJsonArrayToIntArray(JsonArray array) {
        int[] intArray = new int[array.size()];

        for (int i = 0; i < array.size(); i++) {
            intArray[i] = array.get(i).getAsInt();
        }

        return intArray;
    }

}
