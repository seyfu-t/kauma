package me.seyfu_t;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import me.seyfu_t.actions.*;
import me.seyfu_t.actions.basic.*;
import me.seyfu_t.actions.gcm.*;
import me.seyfu_t.actions.gf.*;
import me.seyfu_t.actions.gfpoly.*;
import me.seyfu_t.actions.glasskey.*;
import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;

public class App {

    private static final Logger log = Logger.getLogger(App.class.getName());
    private static final boolean PROFILE_MODE;

    public static final Level LOG_LEVEL;
    static {
        String envLogLevel = System.getenv("KAUMA_LOG_LEVEL");
        LOG_LEVEL = switch (envLogLevel != null ? envLogLevel : "") {
            case "DEBUG" -> Level.FINE;
            case "INFO" -> Level.INFO;
            case "WARNING" -> Level.WARNING;
            case "SEVERE" -> Level.SEVERE;
            default -> Level.SEVERE;
        };

        // Profile mode makes this program single threaded
        String profileMode = System.getenv("KAUMA_PROFILE_MODE");
        PROFILE_MODE = profileMode != null && profileMode.equalsIgnoreCase("true");
    }

    public static void main(String[] args) {
        if (args.length == 0 || !new File(args[0]).exists()) {
            log.severe("Datei existiert nicht!");
            System.exit(1);
        }

        JsonObject response = getResponseJsonFromInputPath(args[0]);
        System.out.println(response.toString());
    }

    public static JsonObject getResponseJsonFromInputPath(String filePath) {
        return getResponseJsonFromInputJson(parseFilePathToJson(filePath));
    }

    public static JsonObject getResponseJsonFromInputJson(JsonObject fullJson) {
        JsonObject testcasesJson = fullJson.get("testcases").getAsJsonObject();
        ResponseBuilder responseBuilder = new ResponseBuilder();

        if (PROFILE_MODE) {
            iterateOverAllCasesSingleThreaded(responseBuilder, testcasesJson);
            return responseBuilder.build();
        }

        // Separate padding_oracle cases from other cases
        List<Entry<String, JsonElement>> paddingOracleCases = new ArrayList<>();
        List<Entry<String, JsonElement>> concurrentCases = new ArrayList<>();

        // Categorize test cases in concurrent and sequential
        for (Entry<String, JsonElement> singleCase : testcasesJson.entrySet()) {
            JsonObject remainderJsonObject = singleCase.getValue().getAsJsonObject();
            String actionName = remainderJsonObject.get("action").getAsString();

            if ("padding_oracle".equals(actionName))
                paddingOracleCases.add(singleCase);
            else
                concurrentCases.add(singleCase);

        }

        // Process padding_oracle cases sequentially
        for (Entry<String, JsonElement> paddingOracleCase : paddingOracleCases) {
            ProcessedTestCase result = processTestCase(paddingOracleCase);
            if (result != null && result.result() != null)
                responseBuilder.addResponse(result.hash(), result.result());

        }

        // Return early if there is only padding_oracle
        if (concurrentCases.isEmpty())
            return responseBuilder.build();

        // Process other cases concurrently
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ProcessedTestCase>> futures = new ArrayList<>();

            // Submit tasks to the virtual thread executor
            for (Entry<String, JsonElement> singleCase : concurrentCases) {
                Future<ProcessedTestCase> future = executor.submit(() -> processTestCase(singleCase));
                futures.add(future);
            }

            // Collect results
            for (Future<ProcessedTestCase> future : futures) {
                ProcessedTestCase result = future.get(); // This will block until the task completes
                if (result != null && result.result() != null)
                    responseBuilder.addResponse(result.hash(), result.result());

            }

        } catch (InterruptedException | ExecutionException e) {
            log.severe("Error processing test cases: " + e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        return responseBuilder.build();
    }

    private static void iterateOverAllCasesSingleThreaded(ResponseBuilder builder, JsonObject testcasesJson) {
        for (Entry<String, JsonElement> singleCase : testcasesJson.entrySet()) {
            JsonObject remainderJsonObject = singleCase.getValue().getAsJsonObject();

            // the 3 relevant parts of each case
            String uniqueHash = singleCase.getKey();
            String actionName = remainderJsonObject.get("action").getAsString();
            JsonObject arguments = remainderJsonObject.get("arguments").getAsJsonObject();

            Action action = getActionClass(actionName); // get the appropriate instance

            if (action == null)
                continue;

            // execute
            JsonObject resulJsonObject = action.execute(arguments);

            builder.addResponse(uniqueHash, resulJsonObject);
        }
    }

    private static ProcessedTestCase processTestCase(Entry<String, JsonElement> singleCase) {
        try {
            JsonObject remainderJsonObject = singleCase.getValue().getAsJsonObject();
            String uniqueHash = singleCase.getKey();
            String actionName = remainderJsonObject.get("action").getAsString();
            JsonObject arguments = remainderJsonObject.get("arguments").getAsJsonObject();

            Action action = getActionClass(actionName);
            if (action == null)
                return null;

            JsonObject resultEntry = action.execute(arguments);
            return new ProcessedTestCase(uniqueHash, resultEntry);
        } catch (Exception e) {
            log.warning("Error processing testcase: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private record ProcessedTestCase(String hash, JsonObject result) {
    }

    public static Action getActionClass(String actionName) {
        return switch (actionName) {
            case "add_numbers" -> new AddNumbers();
            case "subtract_numbers" -> new SubtractNumbers();
            case "poly2block" -> new Poly2Block();
            case "block2poly" -> new Block2Poly();
            case "gfmul" -> new GFMul();
            case "sea128" -> new SEA128();
            case "xex" -> new XEX();
            case "gcm_encrypt" -> new GCMEncrypt();
            case "gcm_decrypt" -> new GCMDecrypt();
            case "padding_oracle" -> new PaddingOracle();
            case "gfpoly_add" -> new GFPolyAdd();
            case "gfpoly_mul" -> new GFPolyMul();
            case "gfpoly_pow" -> new GFPolyPow();
            case "gfdiv" -> new GFDiv();
            case "gfpoly_make_monic" -> new GFPolyMakeMonic();
            case "gfpoly_divmod" -> new GFPolyDivMod();
            case "gfpoly_powmod" -> new GFPolyPowMod();
            case "gfpoly_sqrt" -> new GFPolySqrt();
            case "gfpoly_sort" -> new GFPolySort();
            case "gfpoly_diff" -> new GFPolyDiff();
            case "gfpoly_gcd" -> new GFPolyGCD();
            case "gfpoly_factor_sff" -> new GFPolyFactorSFF();
            case "gfpoly_factor_ddf" -> new GFPolyFactorDDF();
            case "gfpoly_factor_edf" -> new GFPolyFactorEDF();
            case "gcm_crack" -> new GCMCrack();
            case "glasskey_prng" -> new GlasskeyPRNG();
            case "glasskey_prng_int_bits" -> new GlasskeyPRNGIntBits();
            case "glasskey_prng_int_min_max" -> new GlasskeyPRNGIntMinMax();
            case "glasskey_genkey" -> new GlasskeyGenkey();
            case "glasskey_break" -> new GlasskeyBreak();
            default -> null;
        };
    }

    public static JsonObject parseFilePathToJson(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            log.severe("File could not be read. Missing permissions maybe?");
            e.printStackTrace();
            System.exit(1);
        } catch (JsonParseException e) {
            log.severe("File is not valid json!");
            e.printStackTrace();
            System.exit(1);
        }
        throw new RuntimeException("This line of code should've never been reached");
    }
}