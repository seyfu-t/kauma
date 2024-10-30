package me.seyfu_t;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

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
