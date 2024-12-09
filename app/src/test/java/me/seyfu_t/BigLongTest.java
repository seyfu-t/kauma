package me.seyfu_t;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

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
    
}
