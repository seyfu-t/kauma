package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class BigLong {

    /*
     * This is an unsigned immutable arbitrary precision number represented by a
     * list
     * of longs
     */

    private List<Long> longList;

    /*
     * Constructors
     */

    public BigLong() {
        this.longList = new ArrayList<>();
        this.longList.add(0L);
    }

    public BigLong(FieldElement element) {
        this.longList = new ArrayList<>();
        this.longList.add(element.low());
        this.longList.add(element.high());
    }

    public BigLong(long value) {
        this.longList = new ArrayList<>();
        this.longList.add(value);
    }

    public BigLong(List<Long> list) {
        if (!list.isEmpty()) {
            this.longList = popLeadingZeros(list);
        } else {
            this.longList = new ArrayList<>();
            this.longList.add(0L);
        }
    }

    public BigLong copy() {
        return new BigLong(new ArrayList<>(this.longList));
    }

    /*
     * Static factory methods
     */

    public static BigLong Zero() {
        return new BigLong(0L);
    }

    public static BigLong One() {
        return new BigLong(1L);
    }

    /*
     * Mutate
     */

    public BigLong popLeadingZeros() {
        BigLong copy = new BigLong(this.longList);
        if (copy.longList.isEmpty()) {
            copy.longList.add(0L);
            return copy;
        }

        if (copy.longList.size() == 1)
            return copy;

        while (copy.longList.size() > 1)
            if (copy.longList.get(copy.longList.size() - 1) == 0)
                copy.longList.remove(copy.longList.size() - 1);
            else
                break;

        return copy;
    }

    private static List<Long> popLeadingZeros(List<Long> original) {
        List<Long> list = new ArrayList<>(original);
        if (list.isEmpty()) {
            list.add(0L);
            return list;
        }

        if (list.size() == 1)
            return list;

        while (list.size() > 1)
            if (list.get(list.size() - 1) == 0)
                list.remove(list.size() - 1);
            else
                break;

        return list;
    }

    /*
     * Bit manipulation
     */

    public BigLong setBit(int index) {
        int longIndex = index / 64;
        int bitIndex = index % 64;

        BigLong copy = this.copy();

        while (longIndex + 1 > copy.longList.size())
            copy.longList.add(0L);

        long item = copy.longList.get(longIndex);
        item |= 1L << bitIndex;
        copy.longList.set(longIndex, item);

        return copy.popLeadingZeros();
    }

    public BigLong unsetBit(int index) {
        int longIndex = index / 64;
        int bitIndex = index % 64;

        BigLong copy = this.copy();

        long item = copy.longList.get(longIndex);
        item &= ~(1L << bitIndex);
        copy.longList.set(longIndex, item);

        return copy.popLeadingZeros();
    }

    public boolean testBit(int index) {
        int longIndex = index / 64;
        int bitIndex = index % 64;

        return (this.longList.get(longIndex) & (1L << bitIndex)) != 0;
    }

    /*
     * Bit-wise operations
     */

    private BigLong applyOperation(BigLong other, BiFunction<Long, Long, Long> operation) {
        int itemCount = Math.min(this.longList.size(), other.longList.size());
        BigLong copy = this.copy();
        for (int i = 0; i < itemCount; i++) {
            copy.longList.set(i, operation.apply(copy.longList.get(i), other.longList.get(i)));
        }
        return copy.popLeadingZeros();
    }

    public BigLong xor(BigLong other) {
        return applyOperation(other, (a, b) -> a ^ b);
    }

    public BigLong and(BigLong other) {
        return applyOperation(other, (a, b) -> a & b);
    }

    public BigLong or(BigLong other) {
        return applyOperation(other, (a, b) -> a | b);
    }

    /*
     * Shifting
     */

    public BigLong shiftLeft(int bits) {
        if (bits == 0)
            return this;

        int longShifts = bits / 64;
        int bitShift = bits % 64;

        List<Long> newList = new ArrayList<>(this.longList.size() + longShifts + 1);

        // Add zeros for full long shifts
        for (int i = 0; i < longShifts; i++)
            newList.add(0L);

        long carry = 0;
        for (long value : this.longList) {
            long newValue = (value << bitShift) | carry;
            newList.add(newValue);
            carry = bitShift == 0 ? 0 : (value >>> (64 - bitShift));
        }

        if (carry != 0)
            newList.add(carry);

        return new BigLong(newList).popLeadingZeros();
    }

    public BigLong shiftRight(int bits) {
        if (bits == 0)
            return this;

        int longShifts = bits / 64;
        int bitShift = bits % 64;

        // If shifting more than or equal to total bits, return zero
        if (longShifts >= this.longList.size())
            return Zero();
        // Remove full longs from the beginning
        List<Long> truncatedList = new ArrayList<>(this.longList.subList(longShifts, this.longList.size()));

        // Perform bit-level shift
        for (int i = 0; i < truncatedList.size(); i++) {
            long current = truncatedList.get(i) >>> bitShift;

            // If not the last long, incorporate bits from the next long
            if (i < truncatedList.size() - 1) {
                long nextLong = truncatedList.get(i + 1);
                current |= (nextLong & ((1L << bitShift) - 1)) << (64 - bitShift);
            }

            truncatedList.set(i, current);
        }

        return new BigLong(truncatedList).popLeadingZeros();
    }

    /*
     * Compare operators
     */

    public boolean equals(BigLong other) {
        if (this.longList.size() != other.longList.size())
            return false;

        for (int i = 0; i < this.longList.size(); i++)
            if (this.longList.get(i) != other.longList.get(i))
                return false;

        return true;
    }

    public boolean greaterThan(BigLong other) {
        if (this.longList.size() != other.longList.size())
            return this.longList.size() > other.longList.size();

        for (int i = this.longList.size() - 1; i >= 0; i--)
            if (this.longList.get(i) != other.longList.get(i))
                return Long.compareUnsigned(this.longList.get(i), other.longList.get(i)) > 0;

        return false;
    }

    public boolean lessThan(BigLong other) {
        if (this.longList.size() != other.longList.size())
            return this.longList.size() < other.longList.size();

        for (int i = this.longList.size() - 1; i >= 0; i--)
            if (this.longList.get(i) != other.longList.get(i))
                return Long.compareUnsigned(this.longList.get(i), other.longList.get(i)) < 0;

        return false;
    }

    /*
     * Getters
     */

    public boolean isZero() {
        for (int i = this.longList.size() - 1; i >= 0; i--)
            if (this.longList.get(i) != 0L)
                return false;
        return true;
    }

    public long getLongAt(int index) {
        return this.longList.get(index);
    }

    public long getMostSignificantBitIndex() {
        for (int i = Long.SIZE - 1; i >= 0; i--)
            if (((this.longList.get((int) this.size() - 1) >>> i) & 1) == 1)
                return i;

        throw new RuntimeException("This BigLong wasn't cleaned up");
    }

    public long size() {
        return this.longList.size();
    }

    /*
     * String converters
     */

    @Override
    public String toString() {
        return this.toString(16);
    }

    public String toString(int radix) {
        return switch (radix) {
            case 2 -> this.toBinary();
            case 10 -> this.toDecimal();
            case 16 -> this.toHex();
            default -> this.toString(16);
        };
    }

    public String toHex() {
        if (this.isZero())
            return "0";

        StringBuilder hex = new StringBuilder();
        for (int i = this.longList.size() - 1; i >= 0; i--)
            hex.append(String.format("%016x ", this.longList.get(i)));

        while (hex.charAt(0) == '0' && hex.length() > 1)
            hex.deleteCharAt(0);

        hex.deleteCharAt(hex.length() - 1); // remove final space

        return hex.toString().toUpperCase();
    }

    public String toBinary() {
        if (this.isZero())
            return "0";

        StringBuilder bin = new StringBuilder();

        for (int i = this.longList.size() - 1; i >= 0; i--) {
            String binaryString = Long.toBinaryString(this.longList.get(i));
            bin.append(String.format("%64s", binaryString).replace(' ', '0')).append(" ");
        }

        while (bin.charAt(0) == '0' && bin.length() > 1)
            bin.deleteCharAt(0);

        bin.deleteCharAt(bin.length() - 1); // remove final space

        return bin.toString();
    }

    public String toDecimal() {
        if (this.isZero())
            return "0";

        BigInteger decimalValue = BigInteger.ZERO;
        BigInteger base = BigInteger.ONE;
        BigInteger longBase = BigInteger.ONE.shiftLeft(64);

        for (long part : this.longList) {
            // Correctly handle unsigned conversion for each long
            BigInteger unsignedPart = part >= 0 ? BigInteger.valueOf(part)
                    : BigInteger.valueOf(part & 0x7FFFFFFFFFFFFFFFL).setBit(63);

            decimalValue = decimalValue.add(unsignedPart.multiply(base));
            base = base.multiply(longBase);
        }

        return decimalValue.toString();
    }

}
