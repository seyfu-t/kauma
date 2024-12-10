package me.seyfu_t;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

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
