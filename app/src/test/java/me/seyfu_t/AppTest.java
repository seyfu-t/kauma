package me.seyfu_t;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;

class AppTest {

    private static final Logger log = Logger.getLogger(AppTest.class.getName());

    private static final String ADD_SUBTRACT_INPUT = "AddSubtractNumbersInput.json";
    private static final String ADD_SUBTRACT_OUTPUT = "AddSubtractNumbersOutput.json";
    private static final String POLY2BLOCK_INPUT = "Poly2BlockInput.json";
    private static final String POLY2BLOCK_OUTPUT = "Poly2BlockOutput.json";
    private static final String BLOCK2POLY_INPUT = "Block2PolyInput.json";
    private static final String BLOCK2POLY_OUTPUT = "Block2PolyOutput.json";
    private static final String SEA128_INPUT = "SEA128Input.json";
    private static final String SEA128_OUTPUT = "SEA128Output.json";
    private static final String XEX_INPUT = "XEXInput.json";
    private static final String XEX_OUTPUT = "XEXOutput.json";
    
    private static final String GCM_ENCRYPT_INPUT = "GCMEncryptInput.json";
    private static final String GCM_ENCRYPT_OUTPUT = "GCMEncryptOutput.json";
    private static final String GCM_DECRYPT_INPUT = "GCMDecryptInput.json";
    private static final String GCM_DECRYPT_OUTPUT = "GCMDecryptOutput.json";

    private static final String GF_POLY_ADD_INPUT = "GFPolyAddInput.json";
    private static final String GF_POLY_ADD_OUTPUT = "GFPolyAddOutput.json";
    private static final String GF_POLY_MUL_INPUT = "GFPolyMulInput.json";
    private static final String GF_POLY_MUL_OUTPUT = "GFPolyMulOutput.json";
    private static final String GF_POLY_DIV_MOD_INPUT = "GFPolyDivModInput.json";
    private static final String GF_POLY_DIV_MOD_OUTPUT = "GFPolyDivModOutput.json";
    private static final String GF_POLY_POW_INPUT = "GFPolyPowInput.json";
    private static final String GF_POLY_POW_OUTPUT = "GFPolyPowOutput.json";
    private static final String GF_POLY_POW_MOD_INPUT = "GFPolyPowModInput.json";
    private static final String GF_POLY_POW_MOD_OUTPUT = "GFPolyPowModOutput.json";
    private static final String GF_POLY_DIFF_INPUT = "GFPolyDiffInput.json";
    private static final String GF_POLY_DIFF_OUTPUT = "GFPolyDiffOutput.json";
    private static final String GF_POLY_MAKE_MONIC_INPUT = "GFPolyMakeMonicInput.json";
    private static final String GF_POLY_MAKE_MONIC_OUTPUT = "GFPolyMakeMonicOutput.json";
    private static final String GF_POLY_GCD_INPUT = "GFPolyGCDInput.json";
    private static final String GF_POLY_GCD_OUTPUT = "GFPolyGCDOutput.json";
    private static final String GF_POLY_SQRT_INPUT = "GFPolySqrtInput.json";
    private static final String GF_POLY_SQRT_OUTPUT = "GFPolySqrtOutput.json";
    private static final String GF_POLY_SORT_INPUT = "GFPolySortInput.json";
    private static final String GF_POLY_SORT_OUTPUT = "GFPolySortOutput.json";
    
    private static final String GF_MUL_INPUT = "GFMulInput.json";
    private static final String GF_MUL_OUTPUT = "GFMulOutput.json";
    private static final String GF_DIV_INPUT = "GFDivInput.json";
    private static final String GF_DIV_OUTPUT = "GFDivOutput.json";

    private static final String GF_POLY_FACTOR_SFF_INPUT = "GFPolyFactorSFFInput.json";
    private static final String GF_POLY_FACTOR_SFF_OUTPUT = "GFPolyFactorSFFOutput.json";
    private static final String GF_POLY_FACTOR_DDF_INPUT = "GFPolyFactorDDFInput.json";
    private static final String GF_POLY_FACTOR_DDF_OUTPUT = "GFPolyFactorDDFOutput.json";
    private static final String GF_POLY_FACTOR_EDF_INPUT = "GFPolyFactorEDFInput.json";
    private static final String GF_POLY_FACTOR_EDF_OUTPUT = "GFPolyFactorEDFOutput.json";

    private static final String ALL_INPUT = "AllInput.json";
    private static final String ALL_OUTPUT = "AllOutput.json";

    @Test
    void testByteArrayBitShift() {
        byte[] tb = new byte[] { (byte) 0x81, (byte) 0xFF, (byte) 0x18, (byte) 0x4c,
                (byte) 0x81, (byte) 0x81, (byte) 0xCC, (byte) 0x4c,
                (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c,
                (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c, };
        UBigInt16 b = new UBigInt16(tb);

        String shift1Expectation = "10011000 00110001 00000011 00000010 10011000 00110001 00000011 00000010 10011001 10011001 00000011 00000010 10011000 00110001 11111111 00000010";
        String shift2Expectation = "00110000 01100010 00000110 00000101 00110000 01100010 00000110 00000101 00110011 00110010 00000110 00000101 00110000 01100011 11111110 00000100";
        String shift3Expectation = "01100000 11000100 00001100 00001010 01100000 11000100 00001100 00001010 01100110 01100100 00001100 00001010 01100000 11000111 11111100 00001000";

        String shift1Reality = b.shiftLeft(1).swapEndianness().toString(2);
        String shift2Reality = b.shiftLeft(2).swapEndianness().toString(2);
        String shift3Reality = b.shiftLeft(3).swapEndianness().toString(2);

        String shift4Expectation = "00000010 11111111 00110001 10011000 00000010 00000011 10011001 10011001 00000010 00000011 00110001 10011000 00000010 00000011 00110001 10011000";
        String shift5Expectation = "00000100 11111110 01100011 00110000 00000101 00000110 00110010 00110011 00000101 00000110 01100010 00110000 00000101 00000110 01100010 00110000";
        String shift6Expectation = "00001000 11111100 11000111 01100000 00001010 00001100 01100100 01100110 00001010 00001100 11000100 01100000 00001010 00001100 11000100 01100000";

        String shift4Reality = b.shiftLeft(1).toString(2);
        String shift5Reality = b.shiftLeft(2).toString(2);
        String shift6Reality = b.shiftLeft(3).toString(2);

        Assertions.assertEquals(shift1Expectation, shift1Reality);
        Assertions.assertEquals(shift2Expectation, shift2Reality);
        Assertions.assertEquals(shift3Expectation, shift3Reality);

        Assertions.assertEquals(shift4Expectation, shift4Reality);
        Assertions.assertEquals(shift5Expectation, shift5Reality);
        Assertions.assertEquals(shift6Expectation, shift6Reality);

    }

    @Test
    void testComparators() {
        UBigInt16[] aa = new UBigInt16[] {
                UBigInt16.Zero(), UBigInt16.Zero(), UBigInt16.Zero(),
        };
        UBigInt16[] bb = new UBigInt16[] {
                UBigInt16.Zero()
        };
        UBigInt16[] cc = new UBigInt16[] {
                UBigInt16.AllOne(),
        };
        UBigInt16[] dd = new UBigInt16[] {
                new UBigInt16(new byte[] { (byte) 0xAF })
        };
        UBigInt16[] ee = new UBigInt16[] {
                new UBigInt16(new byte[] { (byte) 0x01 })
        };
        UBigInt16[] ff = new UBigInt16[] {
                new UBigInt16(new byte[] { (byte) 0xFF })
        };

        GF128Poly a = new GF128Poly(aa);
        GF128Poly b = new GF128Poly(bb);
        GF128Poly c = new GF128Poly(cc);
        GF128Poly d = new GF128Poly(dd);
        GF128Poly e = new GF128Poly(ee);
        GF128Poly f = new GF128Poly(ff);

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

    // @Test
    // void testFactorEDF(){
    //     createTest(GF_POLY_FACTOR_EDF_INPUT, GF_POLY_FACTOR_EDF_OUTPUT);
    // }

    @Test
    void testFactorDDF(){
        createTest(GF_POLY_FACTOR_DDF_INPUT, GF_POLY_FACTOR_DDF_OUTPUT);
    }

    @Test
    void testFactorSFF(){
        createTest(GF_POLY_FACTOR_SFF_INPUT, GF_POLY_FACTOR_SFF_OUTPUT);
    }

    @Test
    void testGFPolyGCD(){
        createTest(GF_POLY_GCD_INPUT, GF_POLY_GCD_OUTPUT);
    }

    @Test
    void testGFPolyDiff(){
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
