package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;

import me.seyfu_t.util.Util;

public class UBigInt512 extends UBigInt<UBigInt512> {
    private static final int BYTE_COUNT = 512;

    public UBigInt512() {
        super(UBigInt512.BYTE_COUNT, false);
    }

    public UBigInt512(boolean gcm) {
        super(UBigInt512.BYTE_COUNT, gcm);
    }

    public UBigInt512(byte[] bytes) {
        super(bytes, UBigInt512.BYTE_COUNT, false);
    }

    public UBigInt512(byte[] bytes, boolean gcm) {
        super(bytes, UBigInt512.BYTE_COUNT, gcm);
    }

    @Override
    protected UBigInt512 createInstance(byte[] bytes, boolean gcm) {
        return new UBigInt512(bytes, gcm);
    }

    // Factory methods
    public static UBigInt512 Zero() {
        return new UBigInt512();
    }

    public static UBigInt512 Zero(boolean gcm) {
        return new UBigInt512(gcm);
    }

    public static UBigInt512 One() {
        return new UBigInt512().setBit(0);
    }

    public static UBigInt512 One(boolean gcm) {
        return new UBigInt512(gcm).setBit(0);
    }

    public static UBigInt512 AllOne() {
        return AllOne(false);
    }

    public static UBigInt512 AllOne(boolean gcm) {
        byte[] bytes = new byte[UBigInt512.BYTE_COUNT];
        java.util.Arrays.fill(bytes, (byte) 0xFF);
        return new UBigInt512(bytes, gcm);
    }

    public static UBigInt512 fromBase64(String base64) {
        return fromBase64(base64, false);
    }

    public static UBigInt512 fromBase64(String base64, boolean gcm) {
        if (base64 == null)
            throw new NullPointerException("Base64 string cannot be null");
        return new UBigInt512(Base64.getDecoder().decode(base64), gcm);
    }

    public static UBigInt512 fromBigInt(BigInteger bigInt) {
        return fromBigInt(bigInt, false);
    }

    public static UBigInt512 fromBigInt(BigInteger bigInt, boolean gcm) {
        // BigInteger may or may not have a sign byte
        boolean hasSignByte = Util.hasSignByte(bigInt);
        // Java stores BigInteger as big-endian byte array
        byte[] bigIntBytes = Util.swapByteOrder(bigInt.toByteArray());

        // Copy only 128 bytes maximum
        int maxIndex = Math.max(hasSignByte ? bigIntBytes.length - 1 : bigIntBytes.length, UBigInt512.BYTE_COUNT);
        byte[] byteArray = Arrays.copyOfRange(bigIntBytes, 0, maxIndex);

        return new UBigInt512(byteArray, gcm);
    }
}