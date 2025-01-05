package me.seyfu_t;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

public class GlasskeyTest {

    private static final Logger log = Logger.getLogger(AppTest.class.getName());

    private static final String GLASSKEY_PRNG_INPUT = "input/GlasskeyPRNGInput.json";
    private static final String GLASSKEY_PRNG_OUTPUT = "output/GlasskeyPRNGOutput.json";
    private static final String GLASSKEY_PRNG_INT_BITS_INPUT = "input/GlasskeyPRNGIntBitsInput.json";
    private static final String GLASSKEY_PRNG_INT_BITS_OUTPUT = "output/GlasskeyPRNGIntBitsOutput.json";
    private static final String GLASSKEY_PRNG_INT_MIN_MAN_INPUT = "input/GlasskeyPRNGIntMinMaxInput.json";
    private static final String GLASSKEY_PRNG_INT_MIN_MAN_OUTPUT = "output/GlasskeyPRNGIntMinMaxOutput.json";
    private static final String GLASSKEY_GENKEY_INPUT = "input/GlasskeyGenkeyInput.json";
    private static final String GLASSKEY_GENKEY_OUTPUT = "output/GlasskeyGenkeyOutput.json";
    private static final String GLASSKEY_BREAK_INPUT = "input/GlasskeyBreakInput.json";
    private static final String GLASSKEY_BREAK_OUTPUT = "output/GlasskeyBreakOutput.json";

    @Test
    void testGlasskeyPRNG() {
        createTest(GLASSKEY_PRNG_INPUT, GLASSKEY_PRNG_OUTPUT);
    }

    @Test
    void testGlasskeyPRNGIntBits() {
        createTest(GLASSKEY_PRNG_INT_BITS_INPUT, GLASSKEY_PRNG_INT_BITS_OUTPUT);
    }

    @Test
    void testGlasskeyPRNGIntMinMax() {
        createTest(GLASSKEY_PRNG_INT_MIN_MAN_INPUT, GLASSKEY_PRNG_INT_MIN_MAN_OUTPUT);
    }

    @Test
    void testGlasskeyGenkey() {
        createTest(GLASSKEY_GENKEY_INPUT, GLASSKEY_GENKEY_OUTPUT);
    }

    @Test
    void testGlasskeyBreak() {
        createTest(GLASSKEY_BREAK_INPUT, GLASSKEY_BREAK_OUTPUT);
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
