package me.seyfu_t.model;

import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.function.BinaryOperator;

import me.seyfu_t.util.Util;

public class UBigInt16 {

    private final byte[] byteArray = new byte[16];

    private final boolean gcm;

    // All 0s UBigInt16
    public UBigInt16() {
        this.gcm = false;
    }

    public UBigInt16(boolean gcm) {
        this.gcm = gcm;
    }

    public UBigInt16(byte[] bytes, boolean gcm) {
        this.gcm = gcm;
        initUBigInt16(bytes);
    }

    public UBigInt16(byte[] bytes) {
        this.gcm = false;
        initUBigInt16(bytes);
    }

    private void initUBigInt16(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException("Input byte array cannot be null");

        if (bytes.length < 16) {
            System.arraycopy(bytes, 0, this.byteArray, 0, bytes.length);
            for (int i = bytes.length; i < 16; i++) {
                this.byteArray[i] = 0;
            }
        } else {
            System.arraycopy(bytes, 0, this.byteArray, 0, 16);
        }
    }

    public UBigInt16 shiftLeft(int bits) {
        return shift(bits, true);
    }

    public UBigInt16 shiftRight(int bits) {
        return shift(bits, false);
    }

    private UBigInt16 shift(int bits, boolean isLeft) {
        // Validate shift range
        if (bits < 0 || bits >= 128)
            throw new IllegalArgumentException("Shift out of bounds");

        // Create work array for reversing bits when gcm is true
        byte[] workArray;

        if (this.gcm) { // Swap bit order to non-gcm
            workArray = Util.swapBitOrderInAllBytes(this.byteArray);
        } else {
            workArray = Arrays.copyOf(this.byteArray, this.byteArray.length);
        }

        // Prepare result array
        byte[] result = new byte[16];

        // Full byte shift
        int byteShift = bits / 8;
        // Bit shift within bytes
        int bitShift = bits % 8;

        // Do the actual shifting
        shiftFullBytes(result, workArray, byteShift, isLeft);
        if (bitShift > 0)
            shiftPartialBits(result, byteShift, bitShift, isLeft);

        if (this.gcm) { // Swap bit order back to gcm
            result = Util.swapBitOrderInAllBytes(result);
        }

        return new UBigInt16(result, this.gcm);
    }

    private void shiftFullBytes(byte[] result, byte[] workArray, int byteShift, boolean isLeft) {
        if (isLeft)
            for (int i = 15 - byteShift; i >= 0; i--) {
                result[i + byteShift] = workArray[i];
            }
        else
            for (int i = byteShift; i < 16; i++) {
                result[i - byteShift] = workArray[i];
            }
    }

    private void shiftPartialBits(byte[] result, int byteShift, int bitShift, boolean isLeft) {
        if (isLeft) {
            for (int i = 15; i > byteShift; i--) {
                // Shift current byte
                result[i] = (byte) ((result[i] << bitShift) & 0xFF);
                // Carry over bits from previous byte
                result[i] |= (byte) ((result[i - 1] & 0xFF) >>> (8 - bitShift));
            }
            // Shift the first affected byte
            result[byteShift] = (byte) ((result[byteShift] << bitShift) & 0xFF);
        } else {
            for (int i = 0; i < 16 - byteShift - 1; i++) {
                // Shift current byte
                result[i] = (byte) ((result[i] & 0xFF) >>> bitShift);
                // Carry over bits from next byte
                result[i] |= (byte) ((result[i + 1] & 0xFF) << (8 - bitShift));
            }
            // Shift the last affected byte
            result[16 - byteShift - 1] = (byte) ((result[16 - byteShift - 1] & 0xFF) >>> bitShift);
        }
    }

    public UBigInt16 xor(UBigInt16 bigInt) {
        return applyOperation(bigInt, (a, b) -> (byte) (a ^ b));
    }

    public UBigInt16 and(UBigInt16 bigInt) {
        return applyOperation(bigInt, (a, b) -> (byte) (a & b));
    }

    public UBigInt16 or(UBigInt16 bigInt) {
        return applyOperation(bigInt, (a, b) -> (byte) (a | b));
    }

    // When or, and, xor is used with two numbers having opposing gcm,
    // the gcm of the original (not the input) will be preferred
    private UBigInt16 applyOperation(UBigInt16 bigInt, BinaryOperator<Byte> operator) {
        if (bigInt == null)
            throw new NullPointerException("Input UBigInt16 is null");

        // Create a copy of input
        byte[] bytes = Arrays.copyOf(bigInt.toByteArray(), bigInt.toByteArray().length);

        // Since the original gcm will be preferred, the copy must be flipped
        if (this.gcm != bigInt.gcm) {
            bytes = Util.swapBitOrderInAllBytes(bytes);
        }

        for (int i = 0; i < 16; i++) {
            // Apply operation and save to the copy
            bytes[i] = operator.apply(bytes[i], this.byteArray[i]);
        }

        return new UBigInt16(bytes, this.gcm); // return modified copy and original gcm
    }

    public UBigInt16 setBit(int bit) {
        if (bit < 0 || bit >= 128)
            throw new IllegalArgumentException("Bit index out of bounds: " + bit);

        byte[] bytes = Arrays.copyOf(this.byteArray, this.byteArray.length);
        int byteIndex = bit / 8;
        int bitIndex = bit % 8;

        if (this.gcm)
            bitIndex = 7 - bitIndex;

        bytes[byteIndex] |= (1 << bitIndex);
        return new UBigInt16(bytes, this.gcm);
    }

    public boolean testBit(int bit) {
        if (bit < 0 || bit >= 128)
            throw new IllegalArgumentException("Bit index out of bounds: " + bit);

        int byteIndex = bit / 8;
        int bitIndex = bit % 8;

        if (this.gcm)
            bitIndex = 7 - bitIndex;

        byte test = (byte) ((this.byteArray[byteIndex] >> bitIndex) & (0x1));
        return test == 1;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(this.byteArray, this.byteArray.length);
    }

    public UBigInt16 swapEndianness() {
        return new UBigInt16(Util.swapByteOrder(this.byteArray), this.gcm);
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

    public boolean isEmpty() {
        return this.isZero();
    }

    public boolean isZero() {
        return this.sameAs(UBigInt16.Zero());
    }

    public boolean isGCM() {
        return this.gcm;
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

    public String toBase64() {
        return Base64.getEncoder().encodeToString(this.byteArray);
    }

    public int countOfSetBytes() {
        for (int i = 15; i >= 0; i--) {
            if ((byteArray[i] & 0xFF) != 0) {
                return i + 1;
            }
        }
        return 0;
    }

    public UBigInt16 copy() {
        return new UBigInt16(this.byteArray);
    }

    public static UBigInt16 Zero() {
        return new UBigInt16();
    }

    public static UBigInt16 Zero(boolean gcm) {
        return new UBigInt16(true);
    }

    public static UBigInt16 fromBase64(String base64) {
        if (base64 == null)
            throw new NullPointerException("Base64 string cannot be null");
        return new UBigInt16(Base64.getDecoder().decode(base64));
    }

    public static UBigInt16 fromBase64(String base64, boolean gcm) {
        if (base64 == null)
            throw new NullPointerException("Base64 string cannot be null");
        return new UBigInt16(Base64.getDecoder().decode(base64), gcm);
    }

}
