package me.seyfu_t.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import com.google.gson.JsonArray;

import me.seyfu_t.model.UBigInt16;

public class Util {

    public static byte[] swapByteOrder(byte[] byteArray) {
        if (byteArray == null) {
            return null; // Handle null input
        }

        int length = byteArray.length;
        byte[] swappedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            swappedArray[i] = byteArray[length - i - 1];
        }

        return swappedArray;
    }

    public static byte[] swapBitOrderInAllBytes(byte[] byteArray) {
        byte[] copy = Arrays.copyOf(byteArray, byteArray.length);

        for (int i = 0; i < copy.length; i++) {
            copy[i] = Util.swapBitOrder(byteArray[i]);
        }

        return copy;
    }

    // Clever algorithm I found online
    // (https://graphics.stanford.edu/~seander/bithacks.html#ReverseParallel)
    public static byte swapBitOrder(byte b) {
        int x = b & 0xFF;
        // 0x55 odd bits, 0xAA even bits
        // I just pretend I understand this
        // It's black magic
        x = ((x & 0x55) << 1) | ((x & 0xAA) >>> 1);
        x = ((x & 0x33) << 2) | ((x & 0xCC) >>> 2);
        x = ((x & 0x0F) << 4) | ((x & 0xF0) >>> 4);
        return (byte) x;
    }

    public static byte[] concatUBigInt16s(List<UBigInt16> list) {
        // Calculate size for resulting byte array
        byte[] result = new byte[list.size() * 16];

        // piece-wise copy byte arrays into result array
        for (int listItem = 0; listItem < list.size(); listItem++) {
            byte[] currentBytes = list.get(listItem).toByteArray();
            System.arraycopy(currentBytes, 0, result, listItem * 16, 16);
        }
        return result;
    }

    public static List<byte[]> splitIntoChunks(byte[] array, int chunkSize) {
        List<byte[]> chunks = new ArrayList<>();
        Log.debug("Full byte array: " + HexFormat.of().formatHex(array));
        for (int i = 0; i < array.length; i += chunkSize) {
            // Last block could be less than 16 bytes in size
            int maxIndex = Math.min(i + chunkSize, array.length);
            // Copy the relevant slice
            byte[] chunk = Arrays.copyOfRange(array, i, maxIndex);
            chunks.add(chunk);

            Log.debug("Chunk: " + HexFormat.of().formatHex(chunk));
        }

        return chunks;
    }

    public static String[] convertJsonArrayToStringArray(JsonArray array) {
        String[] stringArray = new String[array.size()];

        for (int i = 0; i < array.size(); i++) {
            stringArray[i] = array.get(i).getAsString();
        }

        return stringArray;
    }

    public static boolean hasSignByte(BigInteger bigInteger) {
        byte[] byteArray = bigInteger.toByteArray();

        // Check if the first byte is the sign byte
        byte signByte = byteArray[0];
        boolean isNegative = bigInteger.signum() < 0;

        // Non-negative numbers have a leading 0x00 as a sign byte
        if (!isNegative && signByte == 0x00) {
            return true;
        }
        // Negative numbers have a leading 0xFF as a sign byte

        return isNegative && signByte == (byte) 0xFF;
    }

    public static long bytesToLong(byte[] byteArray, int offset) {
        return (long) ((byteArray[7 + offset] & 0xFF) << 56 |
                (byteArray[6 + offset] & 0xFF) << 48 |
                (byteArray[5 + offset] & 0xFF) << 40 |
                (byteArray[4 + offset] & 0xFF) << 32 |
                (byteArray[3 + offset] & 0xFF) << 24 |
                (byteArray[2 + offset] & 0xFF) << 16 |
                (byteArray[1 + offset] & 0xFF) << 8 |
                (byteArray[0 + offset] & 0xFF));
    }

    public static byte[] longToBytes(long value) {
        byte[] byteArray = new byte[Long.BYTES];

        byteArray[7] = (byte) (value >>> 56);
        byteArray[6] = (byte) (value >>> 48);
        byteArray[5] = (byte) (value >>> 40);
        byteArray[4] = (byte) (value >>> 32);
        byteArray[3] = (byte) (value >>> 24);
        byteArray[2] = (byte) (value >>> 16);
        byteArray[1] = (byte) (value >>> 8);
        byteArray[0] = (byte) (value);

        return byteArray;
    }
}
