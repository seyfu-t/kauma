package me.seyfu_t;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import me.seyfu_t.model.FieldElement;
import me.seyfu_t.util.Util;

class AppTest {

    private static final Logger log = Logger.getLogger(AppTest.class.getName());

    private static final String ADD_SUBTRACT_INPUT = "input/AddSubtractNumbersInput.json";
    private static final String ADD_SUBTRACT_OUTPUT = "output/AddSubtractNumbersOutput.json";
    private static final String POLY2BLOCK_INPUT = "input/Poly2BlockInput.json";
    private static final String POLY2BLOCK_OUTPUT = "output/Poly2BlockOutput.json";
    private static final String BLOCK2POLY_INPUT = "input/Block2PolyInput.json";
    private static final String BLOCK2POLY_OUTPUT = "output/Block2PolyOutput.json";
    private static final String SEA128_INPUT = "input/SEA128Input.json";
    private static final String SEA128_OUTPUT = "output/SEA128Output.json";
    private static final String XEX_INPUT = "input/XEXInput.json";
    private static final String XEX_OUTPUT = "output/XEXOutput.json";

    private static final String GCM_ENCRYPT_INPUT = "input/GCMEncryptInput.json";
    private static final String GCM_ENCRYPT_OUTPUT = "output/GCMEncryptOutput.json";
    private static final String GCM_DECRYPT_INPUT = "input/GCMDecryptInput.json";
    private static final String GCM_DECRYPT_OUTPUT = "output/GCMDecryptOutput.json";

    private static final String GF_POLY_ADD_INPUT = "input/GFPolyAddInput.json";
    private static final String GF_POLY_ADD_OUTPUT = "output/GFPolyAddOutput.json";
    private static final String GF_POLY_MUL_INPUT = "input/GFPolyMulInput.json";
    private static final String GF_POLY_MUL_OUTPUT = "output/GFPolyMulOutput.json";
    private static final String GF_POLY_DIV_MOD_INPUT = "input/GFPolyDivModInput.json";
    private static final String GF_POLY_DIV_MOD_OUTPUT = "output/GFPolyDivModOutput.json";
    private static final String GF_POLY_POW_INPUT = "input/GFPolyPowInput.json";
    private static final String GF_POLY_POW_OUTPUT = "output/GFPolyPowOutput.json";
    private static final String GF_POLY_POW_MOD_INPUT = "input/GFPolyPowModInput.json";
    private static final String GF_POLY_POW_MOD_OUTPUT = "output/GFPolyPowModOutput.json";
    private static final String GF_POLY_DIFF_INPUT = "input/GFPolyDiffInput.json";
    private static final String GF_POLY_DIFF_OUTPUT = "output/GFPolyDiffOutput.json";
    private static final String GF_POLY_MAKE_MONIC_INPUT = "input/GFPolyMakeMonicInput.json";
    private static final String GF_POLY_MAKE_MONIC_OUTPUT = "output/GFPolyMakeMonicOutput.json";
    private static final String GF_POLY_GCD_INPUT = "input/GFPolyGCDInput.json";
    private static final String GF_POLY_GCD_OUTPUT = "output/GFPolyGCDOutput.json";
    private static final String GF_POLY_SQRT_INPUT = "input/GFPolySqrtInput.json";
    private static final String GF_POLY_SQRT_OUTPUT = "output/GFPolySqrtOutput.json";
    private static final String GF_POLY_SORT_INPUT = "input/GFPolySortInput.json";
    private static final String GF_POLY_SORT_OUTPUT = "output/GFPolySortOutput.json";

    private static final String GF_MUL_INPUT = "input/GFMulInput.json";
    private static final String GF_MUL_OUTPUT = "output/GFMulOutput.json";
    private static final String GF_DIV_INPUT = "input/GFDivInput.json";
    private static final String GF_DIV_OUTPUT = "output/GFDivOutput.json";

    private static final String GF_POLY_FACTOR_SFF_INPUT = "input/GFPolyFactorSFFInput.json";
    private static final String GF_POLY_FACTOR_SFF_OUTPUT = "output/GFPolyFactorSFFOutput.json";
    private static final String GF_POLY_FACTOR_DDF_INPUT = "input/GFPolyFactorDDFInput.json";
    private static final String GF_POLY_FACTOR_DDF_OUTPUT = "output/GFPolyFactorDDFOutput.json";
    private static final String GF_POLY_FACTOR_EDF_INPUT = "input/GFPolyFactorEDFInput.json";
    private static final String GF_POLY_FACTOR_EDF_OUTPUT = "output/GFPolyFactorEDFOutput.json";

    private static final String GCM_CRACK_INPUT = "input/GCMCrackInput.json";
    private static final String GCM_CRACK_OUTPUT = "output/GCMCrackOutput.json";

    private static final String PADDING_ORACLE_INPUT = "input/PaddingOracleInput.json";
    private static final String PADDING_ORACLE_OUTPUT = "output/PaddingOracleOutput.json";

    private static final String ALL_INPUT = "input/AllInput.json";
    private static final String ALL_OUTPUT = "output/AllOutput.json";

    // @Test
    // void testConcatMultiples(){
    //     UBigInt16 a1 = new UBigInt16().setBit(0).setBit(5);
    //     UBigInt16 b1 = new UBigInt16().setBit(3).setBit(8);

    //     List<UBigInt16> list1 = new ArrayList<>();
    //     list1.add(a1);
    //     list1.add(b1);

    //     byte[] concatted1 = Util.concatUBigInt16s(list1);


    //     FieldElement a2 = new FieldElement().setBit(0).setBit(5);
    //     FieldElement b2 = new FieldElement().setBit(3).setBit(8);

    //     List<FieldElement> list2 = new ArrayList<>();
    //     list2.add(a2);
    //     list2.add(b2);

    //     byte[] concatted2 = Util.concatFieldElementsXEX(list2);

    //     System.err.println(Arrays.toString(concatted1));
    //     System.err.println(Arrays.toString(concatted2));

    //     Assertions.assertArrayEquals(concatted1, concatted2);
    // }

    // @Test
    // void testByteArrayBitShift() {
    //     byte[] tb = new byte[] { (byte) 0x81, (byte) 0xFF, (byte) 0x18, (byte) 0x4c,
    //             (byte) 0x81, (byte) 0x81, (byte) 0xCC, (byte) 0x4c,
    //             (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c,
    //             (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c, };
    //     UBigInt16 b = new UBigInt16(tb);

    //     String shift1Expectation = "10011000 00110001 00000011 00000010 10011000 00110001 00000011 00000010 10011001 10011001 00000011 00000010 10011000 00110001 11111111 00000010";
    //     String shift2Expectation = "00110000 01100010 00000110 00000101 00110000 01100010 00000110 00000101 00110011 00110010 00000110 00000101 00110000 01100011 11111110 00000100";
    //     String shift3Expectation = "01100000 11000100 00001100 00001010 01100000 11000100 00001100 00001010 01100110 01100100 00001100 00001010 01100000 11000111 11111100 00001000";

    //     String shift1Reality = b.shiftLeft(1).swapEndianness().toString(2);
    //     String shift2Reality = b.shiftLeft(2).swapEndianness().toString(2);
    //     String shift3Reality = b.shiftLeft(3).swapEndianness().toString(2);

    //     String shift4Expectation = "00000010 11111111 00110001 10011000 00000010 00000011 10011001 10011001 00000010 00000011 00110001 10011000 00000010 00000011 00110001 10011000";
    //     String shift5Expectation = "00000100 11111110 01100011 00110000 00000101 00000110 00110010 00110011 00000101 00000110 01100010 00110000 00000101 00000110 01100010 00110000";
    //     String shift6Expectation = "00001000 11111100 11000111 01100000 00001010 00001100 01100100 01100110 00001010 00001100 11000100 01100000 00001010 00001100 11000100 01100000";

    //     String shift4Reality = b.shiftLeft(1).toString(2);
    //     String shift5Reality = b.shiftLeft(2).toString(2);
    //     String shift6Reality = b.shiftLeft(3).toString(2);

    //     Assertions.assertEquals(shift1Expectation, shift1Reality);
    //     Assertions.assertEquals(shift2Expectation, shift2Reality);
    //     Assertions.assertEquals(shift3Expectation, shift3Reality);

    //     Assertions.assertEquals(shift4Expectation, shift4Reality);
    //     Assertions.assertEquals(shift5Expectation, shift5Reality);
    //     Assertions.assertEquals(shift6Expectation, shift6Reality);

    // }

    // @Test
    // void testComparators() {
    //     UBigInt16[] aa = new UBigInt16[] {
    //             UBigInt16.Zero(), UBigInt16.Zero(), UBigInt16.Zero(),
    //     };
    //     UBigInt16[] bb = new UBigInt16[] {
    //             UBigInt16.Zero()
    //     };
    //     UBigInt16[] cc = new UBigInt16[] {
    //             UBigInt16.AllOne(),
    //     };
    //     UBigInt16[] dd = new UBigInt16[] {
    //             new UBigInt16(new byte[] { (byte) 0xAF })
    //     };
    //     UBigInt16[] ee = new UBigInt16[] {
    //             new UBigInt16(new byte[] { (byte) 0x01 })
    //     };
    //     UBigInt16[] ff = new UBigInt16[] {
    //             new UBigInt16(new byte[] { (byte) 0xFF })
    //     };

    //     GF128Poly a = new GF128Poly(aa);
    //     GF128Poly b = new GF128Poly(bb);
    //     GF128Poly c = new GF128Poly(cc);
    //     GF128Poly d = new GF128Poly(dd);
    //     GF128Poly e = new GF128Poly(ee);
    //     GF128Poly f = new GF128Poly(ff);

    //     // Equality checks
    //     Assertions.assertTrue(a.equals(b));
    //     Assertions.assertTrue(b.equals(a));
    //     Assertions.assertFalse(a.equals(c));

    //     // Greater than checks
    //     Assertions.assertTrue(c.greaterThan(d)); // AllOne > 0xAF
    //     Assertions.assertTrue(d.greaterThan(e)); // 0xAF > 0x01
    //     Assertions.assertFalse(f.greaterThan(c)); // 0xFF > AllOne

    //     // Less than checks
    //     Assertions.assertTrue(d.lessThan(c)); // 0xAF < AllOne
    //     Assertions.assertTrue(e.lessThan(d)); // 0x01 < 0xAF
    //     Assertions.assertFalse(c.lessThan(f)); // AllOne < 0xFF

    //     // Mixed relational checks
    //     Assertions.assertTrue(a.lessThan(e)); // Zero < 0x01
    //     Assertions.assertFalse(a.greaterThan(b)); // Zero is not greater than Zero
    //     Assertions.assertTrue(f.greaterThan(b)); // 0xFF > Zero
    // }

    @Test
    void paddingOracle() {
        createTest(PADDING_ORACLE_INPUT, PADDING_ORACLE_OUTPUT);
    }

    @Test
    void testGCMCrack() {
        createTest(GCM_CRACK_INPUT, GCM_CRACK_OUTPUT);
    }

    @Test
    void testFactorEDF() {
        createTest(GF_POLY_FACTOR_EDF_INPUT, GF_POLY_FACTOR_EDF_OUTPUT);
    }

    @Test
    void testFactorDDF() {
        createTest(GF_POLY_FACTOR_DDF_INPUT, GF_POLY_FACTOR_DDF_OUTPUT);
    }

    @Test
    void testFactorSFF() {
        createTest(GF_POLY_FACTOR_SFF_INPUT, GF_POLY_FACTOR_SFF_OUTPUT);
    }

    @Test
    void testGFPolyGCD() {
        createTest(GF_POLY_GCD_INPUT, GF_POLY_GCD_OUTPUT);
    }

    @Test
    void testGFPolyDiff() {
        createTest(GF_POLY_DIFF_INPUT, GF_POLY_DIFF_OUTPUT);
    }

    @Test
    void testGFDiv() {
        createTest(GF_DIV_INPUT, GF_DIV_OUTPUT);
    }

    @Test
    void testGFPolyMakeMonic() {
        createTest(GF_POLY_MAKE_MONIC_INPUT, GF_POLY_MAKE_MONIC_OUTPUT);
    }

    @Test
    void testGFPolySqrt() {
        createTest(GF_POLY_SQRT_INPUT, GF_POLY_SQRT_OUTPUT);
    }

    @Test
    void testGFPolySort() {
        createTest(GF_POLY_SORT_INPUT, GF_POLY_SORT_OUTPUT);
    }

    @Test
    void testGFPolyDivMod() {
        createTest(GF_POLY_DIV_MOD_INPUT, GF_POLY_DIV_MOD_OUTPUT);
    }

    @Test
    void testGFPolyPowMod() {
        createTest(GF_POLY_POW_MOD_INPUT, GF_POLY_POW_MOD_OUTPUT);
    }

    @Test
    void testGFPolyPow() {
        createTest(GF_POLY_POW_INPUT, GF_POLY_POW_OUTPUT);
    }

    @Test
    void testGFPolyMul() {
        createTest(GF_POLY_MUL_INPUT, GF_POLY_MUL_OUTPUT);
    }

    @Test
    void testGFPolyAdd() {
        createTest(GF_POLY_ADD_INPUT, GF_POLY_ADD_OUTPUT);
    }

    @Test
    void testAll() {
        createTest(ALL_INPUT, ALL_OUTPUT);
    }

    @Test
    void testAddSubtractActions() {
        createTest(ADD_SUBTRACT_INPUT, ADD_SUBTRACT_OUTPUT);
    }

    @Test
    void testPoly2BlockXEXAction() {
        createTest(POLY2BLOCK_INPUT, POLY2BLOCK_OUTPUT);
    }

    @Test
    void testBlock2PolyXEXAction() {
        createTest(BLOCK2POLY_INPUT, BLOCK2POLY_OUTPUT);
    }

    @Test
    void testGFMulAction() {
        createTest(GF_MUL_INPUT, GF_MUL_OUTPUT);
    }

    @Test
    void testSEA128Action() {
        createTest(SEA128_INPUT, SEA128_OUTPUT);
    }

    @Test
    void testXEXAction() {
        createTest(XEX_INPUT, XEX_OUTPUT);
    }

    @Test
    void testGCMEncryptAction() {
        createTest(GCM_ENCRYPT_INPUT, GCM_ENCRYPT_OUTPUT);
    }

    @Test
    void testGCMDecryptAction() {
        createTest(GCM_DECRYPT_INPUT, GCM_DECRYPT_OUTPUT);
    }

    private void createTest(String input, String output) {
        String inputPath = getClass().getClassLoader().getResource(input).getFile();
        String outputPath = getClass().getClassLoader().getResource(output).getFile();

        JsonObject inputJson = App.parseFilePathToJson(inputPath);
        info("Input JSON: {0}", inputJson.toString());

        JsonObject actualOutputJson = App.getResponseJsonFromInputJson(inputJson);
        info("Output JSON: {0}", actualOutputJson.toString());

        JsonObject expectedOutputJson = App.parseFilePathToJson(outputPath);
        info("Expected Output JSON: {0}", expectedOutputJson.toString());

        Assertions.assertEquals(expectedOutputJson, actualOutputJson,
                "The output JSON does not match the expected output.");
    }

    private void info(String msg, Object... param) {
        log.log(Level.INFO, msg, param);
    }
}
