package me.seyfu_t;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import me.seyfu_t.model.UBigInt16;

class AppTest {

    private static final Logger log = Logger.getLogger(AppTest.class.getName());

    private static final String ADD_SUBTRACT_INPUT = "AddSubtractNumbersInput.json";
    private static final String ADD_SUBTRACT_OUTPUT = "AddSubtractNumbersOutput.json";
    private static final String POLY2BLOCK_INPUT = "Poly2BlockInput.json";
    private static final String POLY2BLOCK_OUTPUT = "Poly2BlockOutput.json";
    private static final String BLOCK2POLY_INPUT = "Block2PolyInput.json";
    private static final String BLOCK2POLY_OUTPUT = "Block2PolyOutput.json";
    private static final String GFMUL_INPUT = "GFMulInput.json";
    private static final String GFMUL_OUTPUT = "GFMulOutput.json";
    private static final String SEA128_INPUT = "SEA128Input.json";
    private static final String SEA128_OUTPUT = "SEA128Output.json";
    private static final String XEX_INPUT = "XEXInput.json";
    private static final String XEX_OUTPUT = "XEXOutput.json";
    private static final String GCMENCRYPT_INPUT = "GCMEncryptInput.json";
    private static final String GCMENCRYPT_OUTPUT = "GCMEncryptOutput.json";
    private static final String GCMDECRYPT_INPUT = "GCMDecryptInput.json";
    private static final String GCMDECRYPT_OUTPUT = "GCMDecryptOutput.json";

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
        createTest(GFMUL_INPUT, GFMUL_OUTPUT);
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
        createTest(GCMENCRYPT_INPUT, GCMENCRYPT_OUTPUT);
    }

    @Test
    void testGCMDecryptAction() {
        createTest(GCMDECRYPT_INPUT, GCMDECRYPT_OUTPUT);
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
