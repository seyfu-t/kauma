package me.seyfu_t.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class BigLong {

    /*
     * This is an unsigned mutable arbitrary precision number represented by a list
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

    public BigLong(long value) {
        this.longList = new ArrayList<>();
        this.longList.add(value);
    }

    public BigLong(List<Long> list) {
        if (!list.isEmpty()) {
            this.longList = new ArrayList<>(list);
            this.popLeadingZeros();
        } else {
            this.longList = new ArrayList<>();
            this.longList.add(0L);
        }
    }

    public BigLong copy() {
        return new BigLong(this.longList);
    }

    /*
     * Static factory methods
     */

    public static BigLong Zero() {
        return new BigLong();
    }

    public static BigLong One() {
        return new BigLong(1L);
    }

    /*
     * Mutate
     */

    public BigLong popLeadingZeros() {
        while (this.longList.getLast() == 0 && this.longList.size() != 1)
            this.longList.removeLast();
        return this;
    }

    /*
     * Bit manipulation
     */

    public BigLong setBit(int index) {
        int longIndex = index / 64;
        int bitIndex = index % 64;

        while (longIndex + 1 > this.longList.size())
            this.longList.add(0L);

        long item = this.longList.get(longIndex);
        item |= 1L << bitIndex;
        this.longList.set(longIndex, item);

        return this.popLeadingZeros();
    }

    public BigLong unsetBit(int index) {
        int longIndex = index / 64;
        int bitIndex = index % 64;

        long item = this.longList.get(longIndex);
        item &= ~(1L << bitIndex);
        this.longList.set(longIndex, item);

        return this.popLeadingZeros();
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
        for (int i = 0; i < itemCount; i++) {
            this.longList.set(i, operation.apply(this.longList.get(i), other.longList.get(i)));
        }
        return this.popLeadingZeros();
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

        this.longList = newList;
        return this.popLeadingZeros();
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
        this.longList = this.longList.subList(longShifts, this.longList.size());

        // Perform bit-level shift
        for (int i = 0; i < this.longList.size(); i++) {
            long current = this.longList.get(i) >>> bitShift;

            // If not the last long, incorporate bits from the next long
            if (i < this.longList.size() - 1) {
                long nextLong = this.longList.get(i + 1);
                current |= (nextLong & ((1L << bitShift) - 1)) << (64 - bitShift);
            }

            this.longList.set(i, current);
        }

        return this.popLeadingZeros();
    }

    /*
     * Calculations
     */

    public BigLong mulBy2() {
        long carry = 0;
        for (int i = 0; i < this.longList.size(); i++) {
            long value = this.longList.get(i);
            this.longList.set(i, (value << 1) | carry);
            carry = (value >>> 63);
        }

        if (carry != 0)
            this.longList.add(carry);

        return this.popLeadingZeros();
    }

    public BigLong divBy2() {
        long carry = 0;
        for (int i = this.longList.size() - 1; i >= 0; i--) {
            long value = this.longList.get(i);
            this.longList.set(i, (value >>> 1) | (carry << 63));
            carry = value & 1;
        }

        return this.popLeadingZeros();
    }

    // Square and multiply
    public BigLong pow(long exp) {
        if (exp == 0L)
            return One();

        if (exp == 1L)
            return this;

        BigLong result = One();
        BigLong base = this.copy();

        while (exp > 0) {
            if ((exp & 1) == 1)
                result.mul(base);

            base.square();

            exp >>= 1;
        }

        return result.popLeadingZeros();
    }

    public BigLong mul(BigLong other) {
        if (this.isZero() || other.isZero())
            return Zero();

        // Create a result list with enough space
        List<Long> result = new ArrayList<>(this.longList.size() + other.longList.size());
        for (int i = 0; i < this.longList.size() + other.longList.size(); i++)
            result.add(0L);

        // Perform multiplication
        for (int i = 0; i < this.longList.size(); i++) {
            long carry = 0;
            for (int j = 0; j < other.longList.size(); j++) {
                // Multiply current digits and add to result
                long product = Long.compareUnsigned(Long.MAX_VALUE - result.get(i + j),
                        this.longList.get(i) * other.longList.get(j)) >= 0
                                ? result.get(i + j) + this.longList.get(i) * other.longList.get(j)
                                : Long.MAX_VALUE;

                long newValue = product + carry;
                result.set(i + j, newValue);

                // Compute carry
                carry = (Long.compareUnsigned(newValue, product) < 0) ? 1 : 0;
            }

            // Add remaining carry if any
            if (carry > 0)
                result.set(i + other.longList.size(), carry);

        }

        // Replace current object with result
        this.longList = result;
        return this.popLeadingZeros();
    }

    public BigLong square() {
        if (this.isZero())
            return Zero();

        // Create a result list with enough space
        List<Long> result = new ArrayList<>(2 * this.longList.size());
        for (int i = 0; i < 2 * this.longList.size(); i++)
            result.add(0L);

        // Squaring
        for (int i = 0; i < this.longList.size(); i++) {
            long carry = 0;
            for (int j = 0; j < this.longList.size(); j++) {
                // Multiply current digits and add to result
                long product = Long.compareUnsigned(Long.MAX_VALUE - result.get(i + j),
                        this.longList.get(i) * this.longList.get(j)) >= 0
                                ? result.get(i + j) + this.longList.get(i) * this.longList.get(j)
                                : Long.MAX_VALUE;

                long newValue = product + carry;
                result.set(i + j, newValue);

                // Compute carry
                carry = (Long.compareUnsigned(newValue, product) < 0) ? 1 : 0;
            }

            // Add remaining carry if any
            if (carry > 0)
                result.set(i + this.longList.size(), carry);
        }

        this.longList = result;
        return this.popLeadingZeros();
    }

    public BigLong subByOne() {
        for (int i = 0; i < this.longList.size(); i++) {
            if (Long.compareUnsigned(this.longList.get(i), 0L) > 0) {
                this.longList.set(i, this.longList.get(i) - 1L);
                break;
            } else
                // set to all 1s when current value is 0
                this.longList.set(i, Long.MAX_VALUE);
        }

        return this.popLeadingZeros();
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
        BigInteger longBase = BigInteger.valueOf(1L).shiftLeft(64); // 2^64

        for (long part : this.longList) {
            // Treat `long` as unsigned by masking out the sign bit
            BigInteger unsignedPart = BigInteger.valueOf(part).and(BigInteger.valueOf(0xFFFFFFFFFFFFFFFFL));
            decimalValue = decimalValue.add(unsignedPart.multiply(base));
            base = base.multiply(longBase);
        }

        return decimalValue.toString();
    }

}
