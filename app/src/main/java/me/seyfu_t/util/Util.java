package me.seyfu_t.util;

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

    public static UBigInt16 combinedMulAndModReduction(UBigInt16 a, UBigInt16 b) {
        UBigInt16 result = new UBigInt16();
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

}
