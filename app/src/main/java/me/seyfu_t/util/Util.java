package me.seyfu_t.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import me.seyfu_t.model.UBigInt16;

public class Util {
    // skipping index 128, will fall out when using XOR anyways
    public static final UBigInt16 REDUCTION_POLY = new UBigInt16().setBit(7).setBit(2).setBit(1).setBit(0);
    public static final UBigInt16 ALPHA = new UBigInt16().setBit(1);

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

    public static UBigInt16 combinedMulAndModReduction(UBigInt16 a, UBigInt16 b) {
        UBigInt16 result = new UBigInt16(a.isGCM()); // take the gcm of a (doesn't matter which one is used)
        while (!b.isZero()) {
            boolean overflow;
            if (b.testBit(0)) {
                result = result.xor(a);
            }

            overflow = a.testBit(127);

            a = a.shiftLeft(1);

            if (overflow) {
                a = a.xor(REDUCTION_POLY);
            }

            b = b.shiftRight(1);
        }
        return result;
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

}
