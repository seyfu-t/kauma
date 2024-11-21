package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;

import me.seyfu_t.util.Util;

public class UBigInt16 extends UBigInt<UBigInt16> {
    // Constants specific to UBigInt16
    public static final UBigInt16 REDUCTION_POLY = UBigInt16.Zero().setBit(7).setBit(2).setBit(1).setBit(0);
    public static final UBigInt16 ALPHA = UBigInt16.Zero().setBit(1);

    private static final int BYTE_COUNT = 16;

    public UBigInt16() {
        super(UBigInt16.BYTE_COUNT, false);
    }

    public UBigInt16(boolean gcm) {
        super(UBigInt16.BYTE_COUNT, gcm);
    }

    public UBigInt16(byte[] bytes) {
        super(bytes, UBigInt16.BYTE_COUNT, false);
    }

    public UBigInt16(byte[] bytes, boolean gcm) {
        super(bytes, UBigInt16.BYTE_COUNT, gcm);
    }

    @Override
    protected UBigInt16 createInstance(byte[] bytes, boolean gcm) {
        return new UBigInt16(bytes, gcm);
    }

    // Factory methods
    public static UBigInt16 Zero() {
        return new UBigInt16();
    }

    public static UBigInt16 Zero(boolean gcm) {
        return new UBigInt16(gcm);
    }

    public static UBigInt16 AllOne() {
        return AllOne(false);
    }

    public static UBigInt16 AllOne(boolean gcm) {
        byte[] bytes = new byte[UBigInt16.BYTE_COUNT];
        java.util.Arrays.fill(bytes, (byte) 0xFF);
        return new UBigInt16(bytes, gcm);
    }

    public static UBigInt16 fromBase64(String base64) {
        return fromBase64(base64, false);
    }

    public static UBigInt16 fromBase64(String base64, boolean gcm) {
        if (base64 == null)
            throw new NullPointerException("Base64 string cannot be null");
        return new UBigInt16(Base64.getDecoder().decode(base64), gcm);
    }

    public static UBigInt16 fromBigInt(BigInteger bigInt) {
        return fromBigInt(bigInt, false);
    }

    public static UBigInt16 fromBigInt(BigInteger bigInt, boolean gcm) {
        // BigInteger may or may not have a sign byte
        boolean hasSignByte = Util.hasSignByte(bigInt);
        // Java stores BigInteger as big-endian byte array
        byte[] bigIntBytes = Util.swapByteOrder(bigInt.toByteArray());

        // Copy only 16 bytes maximum
        int maxIndex = Math.max(hasSignByte ? bigIntBytes.length - 1 : bigIntBytes.length, UBigInt16.BYTE_COUNT);
        byte[] byteArray = Arrays.copyOfRange(bigIntBytes, 0, maxIndex);

        return new UBigInt16(byteArray, gcm);
    }
}