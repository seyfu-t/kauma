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

    @Test
    void testAddSubtractActions() {
        String addSubtractInputPath = getClass().getClassLoader().getResource(ADD_SUBTRACT_INPUT).getFile();
        String addSubtractOutputPath = getClass().getClassLoader().getResource(ADD_SUBTRACT_OUTPUT).getFile();

        JsonObject inputJson = App.parseFilePathToJson(addSubtractInputPath);
        info("Input JSON: {0}", inputJson.toString());

        JsonObject actualOutputJson = App.getResponseJsonFromInputJson(inputJson);
        info("Output JSON: {0}", actualOutputJson.toString());

        JsonObject expectedOutputJson = App.parseFilePathToJson(addSubtractOutputPath);
        info("Expected Output JSON: {0}", expectedOutputJson.toString());

        Assertions.assertEquals(expectedOutputJson, actualOutputJson,
                "The output JSON does not match the expected output.");
    }

    @Test
    void testPoly2BlockXEXAction() {
        String poly2BlockInputPath = getClass().getClassLoader().getResource(POLY2BLOCK_INPUT).getFile();
        String poly2BlockOutputPath = getClass().getClassLoader().getResource(POLY2BLOCK_OUTPUT).getFile();

        JsonObject inputJson = App.parseFilePathToJson(poly2BlockInputPath);
        info("Input JSON: {0}", inputJson.toString());

        JsonObject actualOutputJson = App.getResponseJsonFromInputJson(inputJson);
        info("Output JSON: {0}", actualOutputJson.toString());

        JsonObject expectedOutputJson = App.parseFilePathToJson(poly2BlockOutputPath);
        info("Expected Output JSON: {0}", expectedOutputJson.toString());

        Assertions.assertEquals(expectedOutputJson, actualOutputJson,
                "The output JSON does not match the expected output.");
    }

    @Test
    void testBlock2PolyXEXAction() {
        String block2polyInputPath = getClass().getClassLoader().getResource(BLOCK2POLY_INPUT).getFile();
        String block2polyOutputPath = getClass().getClassLoader().getResource(BLOCK2POLY_OUTPUT).getFile();

        JsonObject inputJson = App.parseFilePathToJson(block2polyInputPath);
        info("Input JSON: {0}", inputJson.toString());

        JsonObject actualOutputJson = App.getResponseJsonFromInputJson(inputJson);
        info("Output JSON: {0}", actualOutputJson.toString());

        JsonObject expectedOutputJson = App.parseFilePathToJson(block2polyOutputPath);
        info("Expected Output JSON: {0}", expectedOutputJson.toString());

        Assertions.assertEquals(expectedOutputJson, actualOutputJson,
                "The output JSON does not match the expected output.");
    }

    private void info(String msg, Object...param){
        log.log(Level.INFO, msg, param);
    }
}
