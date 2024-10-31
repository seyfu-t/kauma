package me.seyfu_t.model;

import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

import me.seyfu_t.util.Util;

public class UBigInt16 {

    // public static final UBigInt16 ZERO = new UBigInt16(new byte[16]);

    private final byte[] byteArray = new byte[16];

    // for all 0s
    public UBigInt16() {
        // byteArray is already empty, nothing to do
    }

    public UBigInt16(byte[] bytes) {
        if (bytes.length == 0)
            throw new IllegalArgumentException("hä");

        if (bytes.length < 16) {
            System.arraycopy(bytes, 0, byteArray, 0, bytes.length);
            for (int i = bytes.length; i < 16; i++) {
                byteArray[i] = 0;
            }
        } else {
            System.arraycopy(bytes, 0, byteArray, 0, 16);
        }
    }

    public UBigInt16 shiftLeft(int bits) {
        // Validate shift range
        if (bits < 0 || bits >= 128)
            throw new IllegalArgumentException("Shift out of bounds");

        // Prepare result array
        byte[] result = new byte[16];

        // Full byte shift
        int byteShift = bits / 8;
        // Bit shift within bytes
        int bitShift = bits % 8;

        // If there's a full byte shift, move bytes
        for (int i = 15 - byteShift; i >= 0; i--) {
            result[i + byteShift] = byteArray[i];
        }

        // If there's a bit shift within bytes
        if (bitShift > 0) {
            for (int i = 15; i > byteShift; i--) {
                // Shift current byte
                result[i] = (byte) ((result[i] << bitShift) & 0xFF);
                // Carry over bits from previous byte
                result[i] |= (byte) ((result[i - 1] & 0xFF) >>> (8 - bitShift));
            }
            // Shift the first affected byte
            result[byteShift] = (byte) ((result[byteShift] << bitShift) & 0xFF);
        }

        return new UBigInt16(result);
    }

    public UBigInt16 shiftRight(int bits) {
        // Validate shift range
        if (bits < 0 || bits >= 128)
            throw new IllegalArgumentException("Shift out of bounds");

        // Prepare result array
        byte[] result = new byte[16];

        // Full byte shift
        int byteShift = bits / 8;
        // Bit shift within bytes
        int bitShift = bits % 8;

        // If there's a full byte shift, move bytes
        for (int i = byteShift; i < 16; i++) {
            result[i - byteShift] = byteArray[i];
        }

        // If there's a bit shift within bytes
        if (bitShift > 0) {
            for (int i = 0; i < 16 - byteShift - 1; i++) {
                // Shift current byte
                result[i] = (byte) ((result[i] & 0xFF) >>> bitShift);
                // Carry over bits from next byte
                result[i] |= (byte) ((result[i + 1] & 0xFF) << (8 - bitShift));
            }

            // Shift the last affected byte
            result[16 - byteShift - 1] = (byte) ((result[16 - byteShift - 1] & 0xFF) >>> bitShift);
        }

        return new UBigInt16(result);
    }

    public UBigInt16 xor(UBigInt16 bigInt) {
        byte[] bytes = Arrays.copyOf(bigInt.toByteArray(), bigInt.toByteArray().length);
        for (int i = 0; i < 16; i++) {
            bytes[i] = (byte) (bytes[i] ^ this.byteArray[i]);
        }
        return new UBigInt16(bytes);
    }

    public UBigInt16 and(UBigInt16 bigInt) {
        byte[] bytes = Arrays.copyOf(bigInt.toByteArray(), bigInt.toByteArray().length);
        for (int i = 0; i < 16; i++) {
            bytes[i] = (byte) (bytes[i] & this.byteArray[i]);
        }
        return new UBigInt16(bytes);
    }

    public UBigInt16 or(UBigInt16 bigInt) {
        byte[] bytes = Arrays.copyOf(bigInt.toByteArray(), bigInt.toByteArray().length);
        for (int i = 0; i < 16; i++) {
            bytes[i] = (byte) (bytes[i] | this.byteArray[i]);
        }
        return new UBigInt16(bytes);
    }

    public UBigInt16 setBit(int bit) {
        int byteIndex = bit / 8;
        int bitIndex = bit % 8;
        this.byteArray[byteIndex] |= (1 << bitIndex);
        return this;
    }

    public boolean testBit(int bit) {
        int byteIndex = bit / 8;
        int bitIndex = bit % 8;
        byte test = (byte) ((this.byteArray[byteIndex] >> bitIndex) & (0x1));
        return test == 1;
    }

    public byte[] toByteArray() {
        return byteArray;
    }

    public byte[] copyAsArray() {
        return Arrays.copyOf(byteArray, byteArray.length);
    }

    public UBigInt16 swapEndiannes() {
        return new UBigInt16(Util.swapByteOrder(byteArray));
    }

    @Override
    public String toString() {
        return this.toString(16);
    }

    public boolean sameAs(UBigInt16 bigInt) {
        if (bigInt == null) {
            return false;
        }

        // Compare each byte
        for (int byteIndex = 0; byteIndex < 16; byteIndex++) {
            if (this.byteArray[byteIndex] != bigInt.byteArray[byteIndex]) {
                return false;
            }
        }

        return true;
    }

    public String toString(int radix) {
        return switch (radix) {
            case 2 -> this.formatBinary();
            case 16 -> HexFormat.of().formatHex(byteArray);
            default -> this.toString(16);
        };
    }

    private String formatBinary() {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : byteArray) {
            // Format each byte to an 8-character binary string, padding with leading zeros
            binaryString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).append(" ");
        }
        return binaryString.toString().trim();
    }

    public String toBase64(){
        return Base64.getEncoder().encodeToString(this.byteArray);
    }

    public static UBigInt16 fromBase64(String base64){
        return new UBigInt16(Base64.getDecoder().decode(base64));
    }

}
