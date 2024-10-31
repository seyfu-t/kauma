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
    private static final String FDE_INPUT = "FDEInput.json";
    private static final String FDE_OUTPUT = "FDEOutput.json";

    @Test
    void testByteArrayBitShift() {
        byte[] tb = new byte[] { (byte) 0x81, (byte) 0xFF, (byte) 0x18, (byte) 0x4c,
            (byte) 0x81, (byte) 0x81, (byte) 0xCC, (byte) 0x4c,
            (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c,
            (byte) 0x81, (byte) 0x81, (byte) 0x18, (byte) 0x4c, };
        UBigInt16 b = new UBigInt16(tb);

        Assertions.assertEquals("10011000 00110001 00000011 00000010 10011000 00110001 00000011 00000010 10011001 10011001 00000011 00000010 10011000 00110001 11111111 00000010", b.shiftLeft(1).swapEndiannes().toString(2));
        Assertions.assertEquals("00110000 01100010 00000110 00000101 00110000 01100010 00000110 00000101 00110011 00110010 00000110 00000101 00110000 01100011 11111110 00000100", b.shiftLeft(2).swapEndiannes().toString(2));
        Assertions.assertEquals("01100000 11000100 00001100 00001010 01100000 11000100 00001100 00001010 01100110 01100100 00001100 00001010 01100000 11000111 11111100 00001000", b.shiftLeft(3).swapEndiannes().toString(2));

        Assertions.assertEquals("00000010 11111111 00110001 10011000 00000010 00000011 10011001 10011001 00000010 00000011 00110001 10011000 00000010 00000011 00110001 10011000", b.shiftLeft(1).toString(2));
        Assertions.assertEquals("00000100 11111110 01100011 00110000 00000101 00000110 00110010 00110011 00000101 00000110 01100010 00110000 00000101 00000110 01100010 00110000", b.shiftLeft(2).toString(2));
        Assertions.assertEquals("00001000 11111100 11000111 01100000 00001010 00001100 01100100 01100110 00001010 00001100 11000100 01100000 00001010 00001100 11000100 01100000", b.shiftLeft(3).toString(2));

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
    void testFDEAction() {
        createTest(FDE_INPUT, FDE_OUTPUT);
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
