package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.Base64;

import me.seyfu_t.util.Util;

public class FieldElementGCM {

    private long high;
    private long low;

    // Constants
    public static final long REDUCTION_POLY_LONG = 0x87; // when reducing, the big at index 128 will fall off anyway
    public static final long ALPHA_LONG = 0x02;

    // Constructors
    public FieldElementGCM() {
        this.high = 0;
        this.low = 0;
    }

    public FieldElementGCM(long high, long low) {
        this.high = high;
        this.low = low;
    }

    public FieldElementGCM(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            this.high = 0;
            this.low = 0;
        } else if (bytes.length < 16) {
            for (int i = 0; i < Math.min(bytes.length, 16); i++) {
                this.low |= ((long) (bytes[bytes.length - 1 - i] & 0xFF)) << (i * 8);
                if (i < bytes.length - 1)
                    this.high |= ((long) (bytes[bytes.length - 1 - i + 8] & 0xFF)) << (i * 8);
            }
        } else { // yes this is seems redundant, but there is one compare less at every iteration
            for (int i = 0; i < Math.min(bytes.length, 16); i++) {
                this.low |= ((long) (bytes[bytes.length - 1 - i] & 0xFF)) << (i * 8);
                this.high |= ((long) (bytes[bytes.length - 1 - i + 8] & 0xFF)) << (i * 8);
            }
        }
    }

    // Static factory methods
    public static FieldElementGCM Zero() {
        return new FieldElementGCM();
    }

    public static FieldElementGCM One() {
        return new FieldElementGCM(0, 1);
    }

    public static FieldElementGCM AllOne() {
        return new FieldElementGCM(-1L, -1L); // two's complement
    }

    // Bit manipulation methods
    public FieldElementGCM setBit(int bit) {
        FieldElementGCM result = new FieldElementGCM(this.high, this.low);
        if (bit < 64)
            result.low |= 1L << bit;
        else
            result.high |= 1L << (bit - 64);

        return result;
    }

    public FieldElementGCM unsetBit(int bit) {
        FieldElementGCM result = new FieldElementGCM(this.high, this.low);
        if (bit < 64)
            result.low &= ~(1L << bit);
        else
            result.high &= ~(1L << (bit - 64));

        return result;
    }

    public boolean testBit(int bit) {
        if (bit < 64)
            return ((low >> bit) & 0x1) == 1;
        else
            return ((high >> (bit - 64)) & 0x1) == 1;
    }

    // Bitwise operations
    public FieldElementGCM xor(FieldElementGCM other) {
        return new FieldElementGCM(this.high ^ other.high, this.low ^ other.low);
    }

    public FieldElementGCM and(FieldElementGCM other) {
        return new FieldElementGCM(this.high & other.high, this.low & other.low);
    }

    public FieldElementGCM or(FieldElementGCM other) {
        return new FieldElementGCM(this.high | other.high, this.low | other.low);
    }

    // Shift operations
    public FieldElementGCM shiftLeft(int bits) {
        if (bits >= 64)
            return new FieldElementGCM(this.low << (bits - 64), 0);
        else {
            long newHigh = (this.high << bits) | (this.low >>> (64 - bits));
            long newLow = this.low << bits;
            return new FieldElementGCM(newHigh, newLow);
        }
    }

    public FieldElementGCM shiftRight(int bits) {
        if (bits >= 64)
            return new FieldElementGCM(0, high >>> (bits - 64));
        else {
            long newLow = (low >>> bits) | (high << (64 - bits));
            long newHigh = high >>> bits;
            return new FieldElementGCM(newHigh, newLow);
        }
    }

    public boolean equals(FieldElementGCM other) {
        return this.high == other.high && this.low == other.low;
    }

    public boolean greaterThan(FieldElementGCM other) {
        return Long.compareUnsigned(this.high, other.high) > 0 ||
                (this.high == other.high &&
                        Long.compareUnsigned(this.low, other.low) > 0);
    }

    public boolean lessThan(FieldElementGCM other) {
        return Long.compareUnsigned(this.high, other.high) < 0 ||
                (this.high == other.high &&
                        Long.compareUnsigned(this.low, other.low) < 0);
    }

    public boolean isZero() {
        return high == 0 && low == 0;
    }

    private byte[] toByteArray() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[15 - i] = (byte) ((low >>> (i * 8)) & 0xFF);
            bytes[7 - i] = (byte) ((high >>> (i * 8)) & 0xFF);
        }
        return bytes;
    }

    public String toBase64() {
        return Base64.getEncoder().encodeToString(Util.swapBitOrderInAllBytes(this.toByteArray()));
    }

    @Override
    public String toString() {
        return this.toString(16);
    }

    public String toString(int radix) {
        return switch (radix) {
            case 2 -> this.formatBinary();
            case 10 -> this.formatDecimal();
            case 16 -> String.format("%016X%016X", high, low);
            default -> this.toString(16);
        };
    }

    private String formatBinary() {
        StringBuilder binaryString = new StringBuilder();

        // Convert high long to binary
        for (int i = 7; i >= 0; i--) {
            long byteMask = 0xFFL << (i * 8);
            long byteValue = (high & byteMask) >>> (i * 8);
            binaryString.append(String.format("%8s", Long.toBinaryString(byteValue)).replace(' ', '0')).append(" ");
        }

        // Convert low long to binary
        for (int i = 7; i >= 0; i--) {
            long byteMask = 0xFFL << (i * 8);
            long byteValue = (low & byteMask) >>> (i * 8);
            binaryString.append(String.format("%8s", Long.toBinaryString(byteValue)).replace(' ', '0')).append(" ");
        }

        return binaryString.toString().trim();
    }

    private String formatDecimal() {
        // Convert to byte array first
        byte[] bytes = this.toByteArray();
        // Use BigInteger with unsigned interpretation
        BigInteger bigInt = new BigInteger(1, bytes);
        return bigInt.toString(10);
    }

    public static FieldElementGCM fromBase64(String base64) {
        if (base64 == null)
            return null;
        return new FieldElementGCM(Util.swapBitOrderInAllBytes(Base64.getDecoder().decode(base64)));
    }

    // private long swapBitOrderInLong(long value) {
    //     long result = 0;
    //     for (int byteIndex = 0; byteIndex < 8; byteIndex++) {
    //         // Extract the byte
    //         long byte_slice = (value >> (byteIndex * 8)) & 0xFF;

    //         // Swap bits within this byte
    //         long swapped_byte = 0;
    //         for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
    //             if (((byte_slice >> bitIndex) & 1) == 1) {
    //                 swapped_byte |= 1L << (7 - bitIndex);
    //             }
    //         }

    //         // Place the swapped byte back into the result
    //         result |= swapped_byte << (byteIndex * 8);
    //     }
    //     return result;
    // }

}
