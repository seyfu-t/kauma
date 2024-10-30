package me.seyfu_t.util;

import java.math.BigInteger;

public class Util {

    public static BigInteger changeEndianness(BigInteger num) {
        // Note: More efficient algorithm exists
        byte[] oldBytes = num.toByteArray();
        byte[] newBytes = new byte[oldBytes.length];

        for (int i = 0; i < oldBytes.length; i++) {
            newBytes[i] = oldBytes[oldBytes.length - i - 1];
        }

        return new BigInteger(1, newBytes);
    }

}
