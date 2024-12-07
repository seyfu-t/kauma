package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.Base64;

import me.seyfu_t.util.Util;

public class FieldElement {

    private long high;
    private long low;

    // Constants
    public static final FieldElement REDUCTION_POLY = FieldElement.Zero().setBit(7).setBit(2).setBit(1).setBit(0);
    public static final long REDUCTION_POLY_LONG = 0x87; // when reducing, the big at index 128 will fall off anyway
    public static final long ALPHA_LONG = 0x02;

    public static final int BYTE_COUNT = 16;

    // Constructors
    public FieldElement() {
        this.high = 0;
        this.low = 0;
    }

    public FieldElement(long high, long low) {
        this.high = high;
        this.low = low;
    }

    public FieldElement(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            this.high = 0;
            this.low = 0;
            return;
        }

        // Ensure we take exactly 16 bytes, zero-padding if shorter
        byte[] fullBytes = new byte[16];
        int copyLength = Math.min(bytes.length, 16);
        int sourceOffset = Math.max(0, bytes.length - 16);

        System.arraycopy(bytes, sourceOffset, fullBytes, 16 - copyLength, copyLength);

        // Convert to two longs, treating as big-endian
        for (int i = 0; i < 8; i++) {
            this.high = (this.high << 8) | (fullBytes[i] & 0xFF);
            this.low = (this.low << 8) | (fullBytes[i + 8] & 0xFF);
        }
    }

    // Static factory methods
    public static FieldElement Zero() {
        return new FieldElement();
    }

    public static FieldElement One() {
        return new FieldElement(0, 1);
    }

    public static FieldElement AllOne() {
        return new FieldElement(-1L, -1L); // two's complement
    }

    public static FieldElement fromBase64(String base64) {
        if (base64 == null)
            return null;
        byte[] swappedToGCM = Util.swapByteOrder(Util.swapBitOrderInAllBytes(Base64.getDecoder().decode(base64)));
        return new FieldElement(swappedToGCM);
    }

    public static FieldElement fromBase64XEX(String base64) {
        if (base64 == null)
            return null;
        byte[] swappedToGCM = Util.swapByteOrder(Base64.getDecoder().decode(base64));
        return new FieldElement(swappedToGCM);
    }

    // Bit manipulation methods
    public FieldElement setBit(int bit) {
        FieldElement result = new FieldElement(this.high, this.low);
        if (bit < 64)
            result.low |= 1L << bit;
        else
            result.high |= 1L << (bit - 64);

        return result;
    }

    public FieldElement unsetBit(int bit) {
        FieldElement result = new FieldElement(this.high, this.low);
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
    public FieldElement xor(FieldElement other) {
        return new FieldElement(this.high ^ other.high, this.low ^ other.low);
    }

    public FieldElement and(FieldElement other) {
        return new FieldElement(this.high & other.high, this.low & other.low);
    }

    public FieldElement or(FieldElement other) {
        return new FieldElement(this.high | other.high, this.low | other.low);
    }

    // Shift operations
    public FieldElement shiftLeft(int bits) {
        if (bits >= 64)
            return new FieldElement(this.low << (bits - 64), 0);
        else {
            long newHigh = (this.high << bits) | (this.low >>> (64 - bits));
            long newLow = this.low << bits;
            return new FieldElement(newHigh, newLow);
        }
    }

    public FieldElement shiftRight(int bits) {
        if (bits >= 64)
            return new FieldElement(0, high >>> (bits - 64));
        else {
            long newLow = (low >>> bits) | (high << (64 - bits));
            long newHigh = high >>> bits;
            return new FieldElement(newHigh, newLow);
        }
    }

    public FieldElement divBy2() {
        // Optimized path for a single bit shift right
        long newLow = (this.low >>> 1) | ((this.high & 1) << 63);
        long newHigh = this.high >>> 1;
        return new FieldElement(newHigh, newLow);
    }

    public FieldElement mulBy2() {
        // Optimized path for single bit shift left
        long newHigh = (this.high << 1) | ((this.low >>> 63) & 1);
        long newLow = this.low << 1;
        return new FieldElement(newHigh, newLow);
    }

    // Compare operators
    public boolean equals(FieldElement other) {
        return this.high == other.high && this.low == other.low;
    }

    public boolean greaterThan(FieldElement other) {
        return Long.compareUnsigned(this.high, other.high) > 0 ||
                (this.high == other.high &&
                        Long.compareUnsigned(this.low, other.low) > 0);
    }

    public boolean lessThan(FieldElement other) {
        return Long.compareUnsigned(this.high, other.high) < 0 ||
                (this.high == other.high &&
                        Long.compareUnsigned(this.low, other.low) < 0);
    }

    // Getters
    public boolean isZero() {
        return high == 0 && low == 0;
    }

    public long low() {
        return this.low;
    }

    public long high() {
        return this.high;
    }

    // Converter
    private byte[] toByteArray() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[15 - i] = (byte) ((low >>> (i * 8)) & 0xFF);
            bytes[7 - i] = (byte) ((high >>> (i * 8)) & 0xFF);
        }
        return bytes;
    }

    public String toBase64() {
        return Base64.getEncoder().encodeToString(Util.swapByteOrder(Util.swapBitOrderInAllBytes(this.toByteArray())));
    }

    public String toBase64XEX() {
        return Base64.getEncoder().encodeToString(Util.swapByteOrder(this.toByteArray()));
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

    // private long swapBitOrderInLong(long value) {
    // long result = 0;
    // for (int byteIndex = 0; byteIndex < 8; byteIndex++) {
    // // Extract the byte
    // long byte_slice = (value >> (byteIndex * 8)) & 0xFF;

    // // Swap bits within this byte
    // long swapped_byte = 0;
    // for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
    // if (((byte_slice >> bitIndex) & 1) == 1) {
    // swapped_byte |= 1L << (7 - bitIndex);
    // }
    // }

    // // Place the swapped byte back into the result
    // result |= swapped_byte << (byteIndex * 8);
    // }
    // return result;
    // }

}
