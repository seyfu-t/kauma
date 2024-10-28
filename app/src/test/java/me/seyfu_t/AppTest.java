package me.seyfu_t;

import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

class AppTest {

    private static final Logger log = Logger.getLogger(AppTest.class.getName());

    private static final String ADD_SUBTRACT_INPUT = "AddSubtractNumbersInput.json";
    private static final String ADD_SUBTRACT_OUTPUT = "AddSubtractNumbersOutput.json";

    @Test
    @SuppressWarnings("LoggerStringConcat")
    void testAddSubtractActions() {
        String addSubtractInputPath = getClass().getClassLoader().getResource(ADD_SUBTRACT_INPUT).getFile();
        String addSubtractOutputPath = getClass().getClassLoader().getResource(ADD_SUBTRACT_OUTPUT).getFile();

        JsonObject inputJson = App.parseFilePathToJson(addSubtractInputPath);
        log.info("Input JSON: " + inputJson.toString());

        // Execute your method(s) using the input JSON
        JsonObject actualOutputJson = App.getResponseJsonFromInputJson(inputJson);

        // Read expected output JSON file
        JsonObject expectedOutputJson = App.parseFilePathToJson(addSubtractOutputPath);
        log.info("Expected Output JSON: " + expectedOutputJson.toString());

        // Compare actual output with expected output
        Assertions.assertEquals(expectedOutputJson, actualOutputJson,
                "The output JSON does not match the expected output.");
    }
}
