package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.function.BinaryOperator;

import me.seyfu_t.util.Util;

public abstract class UBigInt<T extends UBigInt<T>> {
    protected final byte[] byteArray;
    protected final boolean gcm;
    protected final int byteCount;

    /*
     * Constructors and instance creation
     */

    protected UBigInt(int byteCount, boolean gcm) {
        this.byteCount = byteCount;
        this.gcm = gcm;
        this.byteArray = new byte[byteCount];
    }

    protected UBigInt(byte[] bytes, int byteCount, boolean gcm) {
        this.byteCount = byteCount;
        this.gcm = gcm;
        this.byteArray = new byte[byteCount];
        initUBigInt(bytes);
    }

    private void initUBigInt(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException("Input byte array cannot be null");

        if (bytes.length < byteCount) {
            System.arraycopy(bytes, 0, this.byteArray, 0, bytes.length);
            for (int i = bytes.length; i < byteCount; i++)
                this.byteArray[i] = 0;

        } else
            System.arraycopy(bytes, 0, this.byteArray, 0, byteCount);

    }

    protected abstract T createInstance(byte[] bytes, boolean gcm);

    /*
     * Bit operations
     */

    @SuppressWarnings("unchecked")
    public T shiftLeft(int bits) {
        return (T) shift(bits, true);
    }

    @SuppressWarnings("unchecked")
    public T shiftRight(int bits) {
        return (T) shift(bits, false);
    }

    private UBigInt<T> shift(int bits, boolean isLeft) {
        int maxBits = byteCount * 8;
        if (bits < 0 || bits >= maxBits)
            throw new IllegalArgumentException("Shift out of bounds");

        byte[] result = new byte[byteCount];
        int byteShift = bits / 8;
        int bitShift = bits % 8;

        byte[] workArray;

        if (this.gcm) {
            workArray = Util.swapByteOrder(this.byteArray);
            shiftFullBytes(result, workArray, byteShift, !isLeft);
            if (bitShift > 0)
                shiftPartialBits(result, byteShift, bitShift, !isLeft);

            return createInstance(Util.swapByteOrder(result), this.gcm);
        } else {
            workArray = Arrays.copyOf(this.byteArray, this.byteArray.length);
            shiftFullBytes(result, workArray, byteShift, isLeft);
            if (bitShift > 0)
                shiftPartialBits(result, byteShift, bitShift, isLeft);
            return createInstance(result, this.gcm);
        }

    }

    private void shiftFullBytes(byte[] result, byte[] workArray, int byteShift, boolean isLeft) {
        if (isLeft)
            for (int i = byteCount - 1 - byteShift; i >= 0; i--)
                result[i + byteShift] = workArray[i];

        else
            for (int i = byteShift; i < byteCount; i++)
                result[i - byteShift] = workArray[i];

    }

    private void shiftPartialBits(byte[] result, int byteShift, int bitShift, boolean isLeft) {
        if (isLeft) {
            for (int i = byteCount - 1; i > byteShift; i--) {
                result[i] = (byte) ((result[i] << bitShift) & 0xFF);
                result[i] |= (byte) ((result[i - 1] & 0xFF) >>> (8 - bitShift));
            }
            result[byteShift] = (byte) ((result[byteShift] << bitShift) & 0xFF);
        } else {
            for (int i = 0; i < byteCount - byteShift - 1; i++) {
                result[i] = (byte) ((result[i] & 0xFF) >>> bitShift);
                result[i] |= (byte) ((result[i + 1] & 0xFF) << (8 - bitShift));
            }
            result[byteCount - byteShift - 1] = (byte) ((result[byteCount - byteShift - 1] & 0xFF) >>> bitShift);
        }
    }

    @SuppressWarnings("unchecked")
    public T xor(T bigInt) {
        return (T) applyOperation(bigInt, (a, b) -> (byte) (a ^ b));
    }

    @SuppressWarnings("unchecked")
    public T and(T bigInt) {
        return (T) applyOperation(bigInt, (a, b) -> (byte) (a & b));
    }

    @SuppressWarnings("unchecked")
    public T or(T bigInt) {
        return (T) applyOperation(bigInt, (a, b) -> (byte) (a | b));
    }

    private UBigInt<T> applyOperation(UBigInt<T> bigInt, BinaryOperator<Byte> operator) {
        byte[] bytes = bigInt.toByteArray();

        if (this.gcm != bigInt.gcm)
            bytes = Util.swapBitOrderInAllBytes(bytes);

        for (int i = 0; i < byteCount; i++)
            bytes[i] = operator.apply(bytes[i], this.byteArray[i]);

        return createInstance(bytes, this.gcm);
    }

    public T copy() {
        return (T) createInstance(this.byteArray, gcm);
    }

    /*
     * Setter
     */

    public T setBit(int bit) {
        if (bit < 0 || bit >= byteCount * 8)
            throw new IllegalArgumentException("Bit index out of bounds: " + bit);

        byte[] bytes = Arrays.copyOf(this.byteArray, this.byteArray.length);
        int byteIndex = bit / 8;
        int bitIndex = bit % 8;

        if (this.gcm)
            bitIndex = 7 - bitIndex;

        bytes[byteIndex] |= (1 << bitIndex);
        return (T) createInstance(bytes, this.gcm);
    }

    public T unsetBit(int bit) {
        if (bit < 0 || bit >= byteCount * 8)
            throw new IllegalArgumentException("Bit index out of bounds: " + bit);

        byte[] bytes = Arrays.copyOf(this.byteArray, this.byteArray.length);
        int byteIndex = bit / 8;
        int bitIndex = bit % 8;

        if (this.gcm)
            bitIndex = 7 - bitIndex;

        bytes[byteIndex] &= ~(1 << bitIndex);
        return (T) createInstance(bytes, this.gcm);
    }

    public T swapEndianness() {
        return (T) createInstance(Util.swapByteOrder(this.byteArray), this.gcm);
    }

    /*
     * Getter
     */

    public int countOfSetBytes() {
        for (int i = byteCount - 1; i >= 0; i--)
            if ((byteArray[i] & 0xFF) != 0)
                return i + 1;

        return 0;
    }

    public boolean testBit(int bit) {
        if (bit < 0 || bit >= byteCount * 8)
            throw new IllegalArgumentException("Bit index out of bounds: " + bit);

        int byteIndex = bit / 8;
        int bitIndex = bit % 8;

        if (this.gcm)
            bitIndex = 7 - bitIndex;

        return ((this.byteArray[byteIndex] >> bitIndex) & 0x1) == 1;
    }

    public boolean isEmpty() {
        return this.isZero();
    }

    public boolean isZero() {
        for (byte b : this.byteArray) {
            if (b != 0)
                return false;
        }
        return true;
    }

    public boolean isGCM() {
        return this.gcm;
    }

    /*
     * Compare
     */

    public boolean sameAs(T bigInt) {
        if (bigInt == null || bigInt.byteCount != this.byteCount)
            return false;

        return Arrays.equals(this.byteArray, bigInt.byteArray);
    }

    public boolean equals(T otherBigInt) {
        UBigInt<T> a = this;
        UBigInt<T> b = otherBigInt;

        if (a.gcm)
            a = (T) createInstance(Util.swapBitOrderInAllBytes(a.toByteArray()), false);

        if (b.gcm)
            b = (T) createInstance(Util.swapBitOrderInAllBytes(b.toByteArray()), false);

        for (int i = 0; i < a.byteCount; i++)
            if (a.byteArray[i] != b.byteArray[i])
                return false;

        return true;
    }

    public boolean greaterThan(T otherBigInt) {
        UBigInt<T> a = this;
        UBigInt<T> b = otherBigInt;

        if (a.gcm)
            a = (T) createInstance(Util.swapBitOrderInAllBytes(a.toByteArray()), false);

        if (b.gcm)
            b = (T) createInstance(Util.swapBitOrderInAllBytes(b.toByteArray()), false);

        for (int i = a.byteCount - 1; i >= 0; i--)
            if (a.byteArray[i] != b.byteArray[i])
                return (int) (a.byteArray[i] & 0xFF) > (int) (b.byteArray[i] & 0xFF);

        return false; // Equal values, so not greater
    }

    public boolean lessThan(T otherBigInt) {
        UBigInt<T> a = this;
        UBigInt<T> b = otherBigInt;

        if (a.gcm)
            a = (T) createInstance(Util.swapBitOrderInAllBytes(a.toByteArray()), false);

        if (b.gcm)
            b = (T) createInstance(Util.swapBitOrderInAllBytes(b.toByteArray()), false);

        for (int i = a.byteCount - 1; i >= 0; i--)
            if (a.byteArray[i] != b.byteArray[i])
                return (a.byteArray[i] & 0xFF) < (b.byteArray[i] & 0xFF);

        return false; // Equal values, so not greater
    }

    /*
     * Converter
     */

    @Override
    public String toString() {
        return this.toString(16);
    }

    public String toBase64() {
        return Base64.getEncoder().encodeToString(this.byteArray);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(this.byteArray, this.byteArray.length);
    }

    public String toString(int radix) {
        return switch (radix) {
            case 2 -> this.formatBinary();
            case 10 -> this.formatDecimal();
            case 16 -> HexFormat.of().formatHex(byteArray);
            default -> this.toString(16);
        };
    }

    private String formatBinary() {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : byteArray)
            binaryString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).append(" ");

        return binaryString.toString().trim();
    }

    private String formatDecimal() {
        // BigInteger uses big-endian
        BigInteger bigInt = new BigInteger(1, Util.swapByteOrder(this.byteArray));
        return bigInt.toString(10);
    }
}