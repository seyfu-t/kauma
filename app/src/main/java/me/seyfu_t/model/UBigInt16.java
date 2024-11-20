package me.seyfu_t.model;

import java.util.Base64;

public class UBigInt16 extends UBigInt<UBigInt16> {
    // Constants specific to UBigInt16
    public static final UBigInt16 REDUCTION_POLY = UBigInt16.Zero().setBit(7).setBit(2).setBit(1).setBit(0);
    public static final UBigInt16 ALPHA = UBigInt16.Zero().setBit(1);

    public UBigInt16() {
        super(16, false);
    }

    public UBigInt16(boolean gcm) {
        super(16, gcm);
    }

    public UBigInt16(byte[] bytes) {
        super(bytes, 16, false);
    }

    public UBigInt16(byte[] bytes, boolean gcm) {
        super(bytes, 16, gcm);
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
        byte[] bytes = new byte[16];
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
}