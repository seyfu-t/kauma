package me.seyfu_t;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.Util;

public class FieldElementTest {

    @Test
    void testConcatMultiples() {
        FieldElement a1 = new FieldElement().setBit(0).setBit(5);
        FieldElement b1 = new FieldElement().setBit(3).setBit(8);

        List<FieldElement> list1 = new ArrayList<>();
        list1.add(a1);
        list1.add(b1);

        byte[] concatted1 = Util.concatFieldElementsXEX(list1);

        FieldElement a2 = new FieldElement().setBit(0).setBit(5);
        FieldElement b2 = new FieldElement().setBit(3).setBit(8);

        List<FieldElement> list2 = new ArrayList<>();
        list2.add(a2);
        list2.add(b2);

        byte[] concatted2 = Util.concatFieldElementsXEX(list2);

        System.err.println(Arrays.toString(concatted1));
        System.err.println(Arrays.toString(concatted2));

        Assertions.assertArrayEquals(concatted1, concatted2);
    }

    @Test
    void testByteArrayBitShift() {
        byte[] tb = new byte[] { (byte) 0x81, (byte) 0xFF, (byte) 0x18, (byte) 0x4c,
                (byte) 0x81, (byte) 0x81, (byte) 0xCC, (byte) 0x4c,
                (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c,
                (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c,
        };

        FieldElement b = new FieldElement(tb);

        String shift1Expect = "10011000 00110001 00000011 00000010 10011000 00110001 00000011 00000010 10011001 10011001 00000011 00000010 10011000 00110001 11111111 00000010";
        String shift2Expect = "00110000 01100010 00000110 00000101 00110000 01100010 00000110 00000101 00110011 00110010 00000110 00000101 00110000 01100011 11111110 00000100";
        String shift3Expect = "01100000 11000100 00001100 00001010 01100000 11000100 00001100 00001010 01100110 01100100 00001100 00001010 01100000 11000111 11111100 00001000";

        String shift1Reality = b.shiftLeft(1).toString(2);
        String shift2Reality = b.shiftLeft(2).toString(2);
        String shift3Reality = b.shiftLeft(3).toString(2);

        String shift4Expect = "00100110 00001100 01000000 11000000 10100110 00001100 01000000 11000000 10100110 01100110 01000000 11000000 10100110 00001100 01111111 11000000";
        String shift5Expect = "00010011 00000110 00100000 01100000 01010011 00000110 00100000 01100000 01010011 00110011 00100000 01100000 01010011 00000110 00111111 11100000";
        String shift6Expect = "00001001 10000011 00010000 00110000 00101001 10000011 00010000 00110000 00101001 10011001 10010000 00110000 00101001 10000011 00011111 11110000";

        String shift4Reality = b.shiftRight(1).toString(2);
        String shift5Reality = b.shiftRight(2).toString(2);
        String shift6Reality = b.shiftRight(3).toString(2);

        Assertions.assertEquals(shift1Expect, shift1Reality);
        Assertions.assertEquals(shift2Expect, shift2Reality);
        Assertions.assertEquals(shift3Expect, shift3Reality);

        Assertions.assertEquals(shift4Expect, shift4Reality);
        Assertions.assertEquals(shift5Expect, shift5Reality);
        Assertions.assertEquals(shift6Expect, shift6Reality);

    }

    @Test
    void testComparators() {
        FieldElement[] aa = new FieldElement[] {
                FieldElement.Zero(), FieldElement.Zero(), FieldElement.Zero(),
        };
        FieldElement[] bb = new FieldElement[] {
                FieldElement.Zero()
        };
        FieldElement[] cc = new FieldElement[] {
                FieldElement.AllOne(),
        };
        FieldElement[] dd = new FieldElement[] {
                new FieldElement(new byte[] { (byte) 0xAF })
        };
        FieldElement[] ee = new FieldElement[] {
                new FieldElement(new byte[] { (byte) 0x01 })
        };
        FieldElement[] ff = new FieldElement[] {
                new FieldElement(new byte[] { (byte) 0xFF })
        };

        GFPoly a = new GFPoly(aa);
        GFPoly b = new GFPoly(bb);
        GFPoly c = new GFPoly(cc);
        GFPoly d = new GFPoly(dd);
        GFPoly e = new GFPoly(ee);
        GFPoly f = new GFPoly(ff);

        // Equality checks
        Assertions.assertTrue(a.equals(b));
        Assertions.assertTrue(b.equals(a));
        Assertions.assertFalse(a.equals(c));

        // Greater than checks
        Assertions.assertTrue(c.greaterThan(d)); // AllOne > 0xAF
        Assertions.assertTrue(d.greaterThan(e)); // 0xAF > 0x01
        Assertions.assertFalse(f.greaterThan(c)); // 0xFF > AllOne

        // Less than checks
        Assertions.assertTrue(d.lessThan(c)); // 0xAF < AllOne
        Assertions.assertTrue(e.lessThan(d)); // 0x01 < 0xAF
        Assertions.assertFalse(c.lessThan(f)); // AllOne < 0xFF

        // Mixed relational checks
        Assertions.assertTrue(a.lessThan(e)); // Zero < 0x01
        Assertions.assertFalse(a.greaterThan(b)); // Zero is not greater than Zero
        Assertions.assertTrue(f.greaterThan(b)); // 0xFF > Zero
    }

}
