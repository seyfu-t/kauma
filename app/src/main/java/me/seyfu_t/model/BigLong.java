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
        return new BigLong(0L);
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

        this.longList = truncatedList;

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

            base.mul(base.copy());

            exp >>= 1;
        }

        return result.popLeadingZeros();
    }

    public BigLong mul(BigLong other) {
        BigLong result = Zero();
        BigLong currentMultiplier = this.copy();

        for (int i = 0; i < other.longList.size(); i++) {
            long currentFactor = other.longList.get(i);

            // Multiply by each 64-bit chunk of the other number
            for (int bit = 0; bit < 64; bit++) {
                if ((currentFactor & (1L << bit)) != 0)
                    result.add(currentMultiplier);
                currentMultiplier.shiftLeft(1);
            }
        }

        this.longList = result.longList;
        return this;
    }

    public BigLong divBy3() {
        BigLong remainder = Zero();
        BigLong quotient = Zero();

        // Process from most significant long to least significant
        for (int i = this.longList.size() - 1; i >= 0; i--) {
            long currentLong = this.longList.get(i);

            // Process each long in 32-bit chunks for more precise division
            for (int shift = 2; shift >= 0; shift--) {
                // Extract 64-bit dividend combining remainder and current chunk
                long shiftAmount = shift * 21;
                long chunk = (currentLong >>> shiftAmount) & 0x1FFFFFL; // 21-bit chunk

                long combinedDividend = (remainder.longList.get(0) * (1L << 21)) | chunk;
                long localQuotient = combinedDividend / 3;
                long localRemainder = combinedDividend % 3;

                // Update quotient
                quotient.shiftLeft(21);
                quotient = quotient.add(new BigLong(localQuotient));

                // Prepare remainder for next iteration
                remainder = new BigLong(localRemainder);
            }
        }

        this.longList = quotient.longList;
        return this.popLeadingZeros();
    }

    // Helper method to add another BigLong
    public BigLong add(BigLong other) {
        int maxSize = Math.max(this.longList.size(), other.longList.size());
        long carry = 0;

        for (int i = 0; i < maxSize; i++) {
            long a = i < this.longList.size() ? this.longList.get(i) : 0L;
            long b = i < other.longList.size() ? other.longList.get(i) : 0L;

            long sum = a + b + carry;
            carry = (Long.compareUnsigned(sum, a) < 0 || Long.compareUnsigned(sum, b) < 0) ? 1L : 0L;

            if (i < this.longList.size()) {
                this.longList.set(i, sum);
            } else {
                this.longList.add(sum);
            }
        }

        if (carry > 0) {
            this.longList.add(carry);
        }

        return this;
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
