package me.seyfu_t.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import com.google.gson.JsonArray;

import me.seyfu_t.model.FieldElement;

public class Util {

    public static byte[] swapByteOrder(byte[] byteArray) {
        if (byteArray == null)
            return null; // Handle null input

        int length = byteArray.length;
        byte[] swappedArray = new byte[length];

        for (int i = 0; i < length; i++)
            swappedArray[i] = byteArray[length - i - 1];

        return swappedArray;
    }

    public static byte[] swapByteAndBitOrder(byte[] byteArray) {
        if (byteArray == null)
            return null; // Handle null input

        int length = byteArray.length;
        byte[] swappedArray = new byte[length];

        for (int i = 0; i < length; i++)
            swappedArray[i] = Util.swapBitOrder(byteArray[length - i - 1]);

        return swappedArray;
    }

    public static byte[] swapBitOrderInAllBytes(byte[] byteArray) {
        byte[] copy = Arrays.copyOf(byteArray, byteArray.length);

        for (int i = 0; i < copy.length; i++)
            copy[i] = Util.swapBitOrder(byteArray[i]);

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

    public static byte[] concatFieldElementsXEX(List<FieldElement> list) {
        // Calculate size for resulting byte array
        byte[] result = new byte[list.size() * 16];

        // piece-wise copy byte arrays into result array
        for (int listItem = 0; listItem < list.size(); listItem++) {
            byte[] currentBytes = list.get(listItem).toByteArrayXEX();
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

        for (int i = 0; i < array.size(); i++)
            stringArray[i] = array.get(i).getAsString();

        return stringArray;
    }

    public static long[] convertJsonArrayToLongArray(JsonArray array) {
        long[] longArray = new long[array.size()];

        for (int i = 0; i < array.size(); i++)
            longArray[i] = array.get(i).getAsLong();

        return longArray;
    }

    public static int[] convertJsonArrayToIntegerArray(JsonArray array) {
        int[] intArray = new int[array.size()];

        for (int i = 0; i < array.size(); i++)
            intArray[i] = array.get(i).getAsInt();

        return intArray;
    }

    public static long bytesToLong(byte[] byteArray) {
        long result = 0;

        for (int i = 0; i < Math.min(byteArray.length, 8); i++)
            result |= ((long) byteArray[i] & 0xFF) << (i * 8);

        return result;
    }

    public static int bytesToInteger(byte[] byteArray) {
        int result = 0;

        for (int i = 0; i < Math.min(byteArray.length, 4); i++)
            result |= (byteArray[i] & 0xFF) << (i * 8);

        return result;
    }

    public static byte[] longToBytesBigEndian(long value) {
        byte[] byteArray = new byte[Long.BYTES];

        byteArray[0] = (byte) (value >>> 56);
        byteArray[1] = (byte) (value >>> 48);
        byteArray[2] = (byte) (value >>> 40);
        byteArray[3] = (byte) (value >>> 32);
        byteArray[4] = (byte) (value >>> 24);
        byteArray[5] = (byte) (value >>> 16);
        byteArray[6] = (byte) (value >>> 8);
        byteArray[7] = (byte) (value);

        return byteArray;
    }

    public static byte[] longToBytesLittleEndian(long value) {
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

    public static byte[] intToBytesLittleEndian(int value) {
        byte[] byteArray = new byte[Long.BYTES];

        byteArray[3] = (byte) (value >>> 24);
        byteArray[2] = (byte) (value >>> 16);
        byteArray[1] = (byte) (value >>> 8);
        byteArray[0] = (byte) (value);

        return byteArray;
    }

    // Miller-Rabin test
    public boolean isPrime(long candidate, long rounds) {
        long d, s;

        if (candidate == 2)
            return true;
        if (candidate < 2)
            return false;

        // until d is odd
        for (d = 0, s = 1; (d & 1) == 0; s++)
            d = (candidate - 1) / pow(2, s);

        verification: for (long i = 0; i < rounds; i++) {
            // random base in the range [2, n-1]
            long base = (long) ((Math.random() * (candidate - 3)) + 2);

            long x = powMod(base, d, candidate);

            if (x == 1 || x == (candidate - 1))
                continue verification;

            for (long j = 0; j < (s - 1); j++) {
                x = powMod(x, 2, candidate);
                if (x == 1)
                    return false;
                if (x == (candidate - 1))
                    continue verification;
            }

            return false;
        }

        return true;
    }

    public static long pow(long base, long exponent) {
        int shift = 63; // bit position
        long result = base;

        // Skip all leading 0 bits and the most significant 1 bit.
        while (((exponent >> shift--) & 1) == 0)
            ;

        while (shift >= 0) {
            result = result * result;
            if (((exponent >> shift--) & 1) == 1)
                result = result * base;
        }

        return result;
    }

    public static long powMod(long base, long exponent, long modulo) {
        int shift = 63; // bit position
        long result = base;

        // Skip all leading 0 bits and the most significant 1 bit.
        while (((exponent >> shift--) & 1) == 0)
            ;

        while (shift >= 0) {
            result = (result * result) % modulo;
            if (((exponent >> shift--) & 1) == 1)
                result = (result * base) % modulo;
        }

        return result;
    }

    public static String toHex(byte[] input) {
        if (input.length == 0)
            return "0";

        StringBuilder hex = new StringBuilder();
        for (int i = input.length - 1; i >= 0; i--)
            hex.append(String.format("%02x", input[i]));

        while (hex.charAt(0) == '0' && hex.length() > 1)
            hex.deleteCharAt(0);

        hex.deleteCharAt(hex.length() - 1); // remove final space

        return hex.toString().toUpperCase();
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }

        return byteArray;
    }

}
