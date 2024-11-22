package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;

import me.seyfu_t.util.Util;

public class UBigInt32 extends UBigInt<UBigInt32> {
    private static final int BYTE_COUNT = 32;

    public UBigInt32() {
        super(UBigInt32.BYTE_COUNT, false);
    }

    public UBigInt32(boolean gcm) {
        super(UBigInt32.BYTE_COUNT, gcm);
    }

    public UBigInt32(byte[] bytes) {
        super(bytes, UBigInt32.BYTE_COUNT, false);
    }

    public UBigInt32(byte[] bytes, boolean gcm) {
        super(bytes, UBigInt32.BYTE_COUNT, gcm);
    }

    @Override
    protected UBigInt32 createInstance(byte[] bytes, boolean gcm) {
        return new UBigInt32(bytes, gcm);
    }

    // Factory methods
    public static UBigInt32 Zero() {
        return new UBigInt32();
    }

    public static UBigInt32 Zero(boolean gcm) {
        return new UBigInt32(gcm);
    }

    public static UBigInt32 One() {
        return new UBigInt32().setBit(0);
    }

    public static UBigInt32 One(boolean gcm) {
        return new UBigInt32(gcm).setBit(0);
    }

    public static UBigInt32 AllOne() {
        return AllOne(false);
    }

    public static UBigInt32 AllOne(boolean gcm) {
        byte[] bytes = new byte[UBigInt32.BYTE_COUNT];
        java.util.Arrays.fill(bytes, (byte) 0xFF);
        return new UBigInt32(bytes, gcm);
    }

    public static UBigInt32 fromBase64(String base64) {
        return fromBase64(base64, false);
    }

    public static UBigInt32 fromBase64(String base64, boolean gcm) {
        if (base64 == null)
            throw new NullPointerException("Base64 string cannot be null");
        return new UBigInt32(Base64.getDecoder().decode(base64), gcm);
    }

    public static UBigInt32 fromBigInt(BigInteger bigInt) {
        return fromBigInt(bigInt, false);
    }

    public static UBigInt32 fromBigInt(BigInteger bigInt, boolean gcm) {
        // BigInteger may or may not have a sign byte
        boolean hasSignByte = Util.hasSignByte(bigInt);
        // Java stores BigInteger as big-endian byte array
        byte[] bigIntBytes = Util.swapByteOrder(bigInt.toByteArray());

        // Copy only 32 bytes maximum
        int maxIndex = Math.max(hasSignByte ? bigIntBytes.length - 1 : bigIntBytes.length, UBigInt32.BYTE_COUNT);
        byte[] byteArray = Arrays.copyOfRange(bigIntBytes, 0, maxIndex);

        return new UBigInt32(byteArray, gcm);
    }
}