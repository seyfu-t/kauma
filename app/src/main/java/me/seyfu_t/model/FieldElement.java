package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.Base64;

import me.seyfu_t.util.Util;

public class FieldElement {

    private long high;
    private long low;

    // Constants
    public static final FieldElement REDUCTION_POLY = FieldElement.Zero().setBit(7).setBit(2).setBit(1).setBit(0);
    public static final FieldElement ALPHA = FieldElement.Zero().setBit(1);
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
        this.high = 0;
        this.low = 0;
        if (bytes == null || bytes.length == 0)
            return;

        // Ensure we take exactly 16 bytes, zero-padding if shorter
        byte[] fullBytes = new byte[16];
        int copyLength = Math.min(bytes.length, 16);

        System.arraycopy(bytes, 0, fullBytes, 0, copyLength);

        // Convert to two longs, treating as little-endian
        for (int i = 0; i < 8; i++) {
            this.low |= ((long) (fullBytes[i] & 0xFF) << (i * 8));
            this.high |= ((long) (fullBytes[i + 8] & 0xFF) << (i * 8));
        }
    }

    public FieldElement(BigInteger bigInt) {
        this(Util.swapByteOrder(bigInt.toByteArray()));
    }

    public FieldElement(BigLong bigLong) {
        this.low = bigLong.getLongAt(0);
        if (bigLong.size() > 1)
            this.high = bigLong.getLongAt(1);
        else
            this.high = 0L;
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

    public static FieldElement fromBase64GCM(String base64) {
        if (base64 == null)
            return null;
        byte[] swappedToGCM = Util.swapBitOrderInAllBytes(Base64.getDecoder().decode(base64));
        return new FieldElement(swappedToGCM);
    }

    public static FieldElement fromBase64XEX(String base64) {
        if (base64 == null)
            return null;
        byte[] swappedToGCM = Base64.getDecoder().decode(base64);
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

    public int getHighestSetBitIndex() {
        if (this.isZero())
            return -1;

        for (int i = 127; i >= 0; i--)
            if (this.testBit(i))
                return i;

        return -1;
    }

    // Converter

    public BigInteger toBigInteger() {
        return new BigInteger(1, Util.swapByteOrder(this.toByteArrayXEX()));
    }

    public byte[] toByteArrayXEX() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) ((low >>> (i * 8)) & 0xFF);
            bytes[i + 8] = (byte) ((high >>> (i * 8)) & 0xFF);
        }
        return bytes;
    }

    public byte[] toByteArrayGCM() {
        return Util.swapBitOrderInAllBytes(this.toByteArrayXEX());
    }

    public String toBase64GCM() {
        return Base64.getEncoder().encodeToString(this.toByteArrayGCM());
    }

    public String toBase64XEX() {
        return Base64.getEncoder().encodeToString(this.toByteArrayXEX());
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

    public FieldElement swapInnerGCMState() {
        return new FieldElement(this.toByteArrayGCM());
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
        byte[] bytes = this.toByteArrayXEX();
        // Use BigInteger with unsigned interpretation
        BigInteger bigInt = new BigInteger(1, bytes);
        return bigInt.toString(10);
    }

}
