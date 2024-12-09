package me.seyfu_t;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import me.seyfu_t.model.BigLong;

class BigLongTest {

    @Test
    void testConstructors() {
        BigLong zero = new BigLong();
        assertEquals("0", zero.toHex());

        BigLong one = new BigLong(1L);
        assertEquals("1", one.toHex());

        BigLong fromList = new BigLong(Arrays.asList(1L, 2L, 3L));
        assertEquals("3 0000000000000002 0000000000000001", fromList.toHex());
    }

    @Test
    void testStaticFactoryMethods() {
        assertEquals("0", BigLong.Zero().toHex());
        assertEquals("1", BigLong.One().toHex());
    }

    @Test
    void testBitManipulation() {
        BigLong num = new BigLong(0L);

        num.setBit(0);
        assertEquals("1", num.toHex());

        num.setBit(1);
        assertEquals("3", num.toHex());

        num.unsetBit(0);
        assertEquals("2", num.toHex());

        assertTrue(num.testBit(1));
        assertFalse(num.testBit(0));
    }

    @Test
    void testBitwiseOperations() {
        BigLong a = new BigLong(0b1010);
        BigLong b = new BigLong(0b1100);

        a.xor(b);
        assertEquals("6", a.toHex());

        a = new BigLong(0b1010);
        a.and(b);
        assertEquals("8", a.toHex());

        a = new BigLong(0b1010);
        a.or(b);
        assertEquals("E", a.toHex());
    }

    @Test
    void testShiftOperations() {
        BigLong num = new BigLong(1L);

        num.shiftLeft(4);
        assertEquals("10", num.toHex());

        num.shiftRight(2);
        assertEquals("4", num.toHex());
    }

    @Test
    void testComparison() {
        BigLong a = new BigLong(10L);
        BigLong b = new BigLong(20L);
        BigLong c = new BigLong(10L);

        assertTrue(b.greaterThan(a));
        assertFalse(a.greaterThan(b));
        assertFalse(a.greaterThan(c));

        assertTrue(a.lessThan(b));
        assertFalse(b.lessThan(a));
        assertFalse(a.lessThan(c));

        assertTrue(a.equals(c));
    }

    @Test
    void testLargeNumberOperations() {
        BigLong large1 = new BigLong(Arrays.asList(1L, 2L, 3L));
        BigLong large2 = large1.copy();
        BigLong large3 = large1.copy();
        BigLong large4 = large1.copy();
        BigLong large5 = large1.copy();

        large1.shiftLeft(32);
        assertEquals("300000000 0000000200000000 0000000100000000", large1.toHex());

        large2.shiftRight(32);
        assertEquals("300000000 0000000200000000", large2.toHex());

        large3.shiftRight(64);
        assertEquals("3 0000000000000002", large3.toHex());

        large4.shiftRight(128);
        assertEquals("3", large4.toHex());

        large5.shiftLeft(128);
        assertEquals("3 0000000000000002 0000000000000001 0000000000000000 0000000000000000", large5.toHex());
    }

    @Test
    void testEdgeCases() {
        BigLong zero = new BigLong(0L);
        assertTrue(zero.isZero());

        BigLong largeZero = new BigLong(Arrays.asList(0L, 0L, 1L, 0L));
        assertEquals("1 0000000000000000 0000000000000000", largeZero.toHex());
    }

    @Test
    void visualize() {
        BigLong num = new BigLong(7L);
        for (int i = 0; i < 64; i++) {
            num.shiftLeft(1);
            System.out.println(num.toBinary());
        }
        assertEquals("111 0000000000000000000000000000000000000000000000000000000000000000", num.toBinary());
    }

    @Test
    public void testMultiplication() {
        // Test basic multiplication
        BigLong a = new BigLong(123L);
        BigLong b = new BigLong(456L);
        a.mul(b);
        assertEquals("56088", a.toDecimal());

        // Test multiplication with zero
        a = new BigLong(123L);
        b = new BigLong(0L);
        a.mul(b);
        assertTrue(a.isZero());

        // Test large number multiplication
        a = new BigLong(Long.MAX_VALUE);
        b = new BigLong(2L);
        a.mul(b);
        assertEquals(
                new BigInteger(Long.toString(Long.MAX_VALUE)).multiply(BigInteger.valueOf(2)).toString(),
                a.toDecimal());
    }

    @Test
    public void testSquaring() {
        // Test basic squaring
        BigLong a = new BigLong(12L);
        a.mul(a.copy());
        assertEquals("144", a.toDecimal());

        // Test squaring zero
        a = new BigLong(0L);
        a.mul(a.copy());
        assertTrue(a.isZero());

        // Test large number squaring
        a = new BigLong(Long.MAX_VALUE);
        a.mul(a.copy());
        assertEquals(
                new BigInteger(Long.toString(Long.MAX_VALUE)).pow(2).toString(),
                a.toDecimal());
    }

    @Test
    public void testPow() {
        // Test basic power
        BigLong a = new BigLong(2L);
        BigLong result = a.pow(10);
        assertEquals("1024", result.toDecimal());

        // Test power of zero
        a = new BigLong(5L);
        result = a.pow(0);
        assertEquals("1", result.toDecimal());

        // Test power of one
        result = a.pow(1);
        assertEquals("5", result.toDecimal());

        // Test larger power
        a = new BigLong(3L);
        result = a.pow(20);
        assertEquals(new BigInteger("3").pow(20).toString(), result.toDecimal());
    }

    @Test
    public void testSubtractOne() {
        // Test basic subtraction
        BigLong a = new BigLong(123L);
        a.subByOne();
        assertEquals("122", a.toDecimal());

        // Test subtraction at long boundary
        a = new BigLong(Arrays.asList(0L, 1L)); // Represents 2^64
        a.subByOne();
        assertEquals(new BigLong(Long.MAX_VALUE).toDecimal(), a.toDecimal());

        // Test subtraction to zero
        a = new BigLong(1L);
        a.subByOne();
        assertTrue(a.isZero());
    }

    @Test
    public void testDivBy3() {
        // Test basic division
        BigLong a = new BigLong(123L);
        a.divBy3();
        assertEquals("41", a.toDecimal());

        // Test division of zero
        a = new BigLong(0L);
        a.divBy3();
        assertTrue(a.isZero());

        // Test division of large number
        a = new BigLong(Long.MAX_VALUE);
        a.divBy3();
        assertEquals(
                new BigInteger(Long.toString(Long.MAX_VALUE)).divide(BigInteger.valueOf(3)).toString(),
                a.toDecimal());
    }

    @Test
    public void testMutabilityAndLargeNumbers() {
        BigLong a = new BigLong(Long.MAX_VALUE);
        BigLong original = a.copy();

        a.mul(new BigLong(2L))
                .shiftLeft(10)
                .divBy3()
                .subByOne();

        assertNotEquals(original.toDecimal(), a.toDecimal());
    }

    @Test
    public void testExtremeBoundaryConditions() {
        // Test with maximum possible list of longs
        List<Long> maxLongList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            maxLongList.add(Long.MAX_VALUE);
        }

        BigLong massive = new BigLong(maxLongList);

        // Perform series of operations
        massive.mul(new BigLong(2L))
                .divBy3()
                .shiftRight(32);

        assertFalse(massive.isZero());
    }

    @Test
    public void testMixedSizeOperations() {
        // Combination of small and large numbers
        BigLong small = new BigLong(123L);
        BigLong large = new BigLong(Arrays.asList(Long.MAX_VALUE, Long.MAX_VALUE, 1L));

        BigLong result = small.copy().mul(large);

        assertNotNull(result);
        assertFalse(result.isZero());
    }

    @Test
    public void testBitManipulationEdgeCases() {
        // Test bit manipulation on very large numbers
        BigLong bitTest = new BigLong(Arrays.asList(Long.MAX_VALUE, 0L, 1L));

        bitTest.setBit(200)
                .shiftLeft(10)
                .unsetBit(64);

        assertTrue(bitTest.testBit(210));
        assertFalse(bitTest.testBit(64));
    }

    @Test
    public void testDivisionAndRemainderScenarios() {
        // Test division scenarios that might cause precision issues
        BigLong prime = new BigLong(Long.MAX_VALUE);

        BigLong originalPrime = prime.copy();
        prime.divBy3();

        // Verify division doesn't completely destroy original number's characteristics
        assertNotEquals("0", prime.toDecimal());
        assertTrue(new BigInteger(originalPrime.toDecimal()).divide(BigInteger.valueOf(3))
                .toString()
                .equals(prime.toDecimal()));
    }

    @Test
    public void testInPlaceOperationChaining() {
        // Verify in-place operations can be chained
        BigLong complex = new BigLong(1234L);

        complex.mul(new BigLong(5L))
                .shiftLeft(10)
                .divBy3()
                .subByOne();

        assertNotNull(complex);
        assertFalse(complex.isZero());
        assertEquals("2106025", complex.toDecimal());
    }

}
