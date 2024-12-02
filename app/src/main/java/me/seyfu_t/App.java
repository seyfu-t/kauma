package me.seyfu_t;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
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
import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;

public class App {

    private static final Logger log = Logger.getLogger(App.class.getName());

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
    }

    public static void main(String[] args) {
        // Checking if file exists
        String filePath = args[0];
        if (!new File(filePath).exists()) {
            log.severe("Datei existiert nicht!");
            System.exit(1);
        }

        // pipeline
        JsonObject response = getResponseJsonFromInputPath(filePath);

        // output result
        System.out.print(response.toString());
    }

    public static JsonObject getResponseJsonFromInputPath(String filePath) {
        return getResponseJsonFromInputJson(parseFilePathToJson(filePath));
    }

    public static JsonObject getResponseJsonFromInputJson(JsonObject fullJson) {
        // extracting the relevant part
        JsonObject testcasesJson = fullJson.get("testcases").getAsJsonObject(); // get only the value of "responses"

        // building
        ResponseBuilder responseBuilder = new ResponseBuilder();
        iterateOverAllCases(responseBuilder, testcasesJson);

        // finalize and return
        JsonObject finalResponse = responseBuilder.build();
        return finalResponse;
    }

    public static Action getActionClass(String actionName) {
        return switch (actionName) {
            case "add_numbers" -> new AddNumbersAction();
            case "subtract_numbers" -> new SubtractNumbersAction();
            case "poly2block" -> new Poly2BlockAction();
            case "block2poly" -> new Block2PolyAction();
            case "gfmul" -> new GFMulAction();
            case "sea128" -> new SEA128Action();
            case "xex" -> new XEXAction();
            case "gcm_encrypt" -> new GCMEncryptAction();
            case "gcm_decrypt" -> new GCMDecryptAction();
            case "padding_oracle" -> new PaddingOracleAction();
            case "gfpoly_add" -> new GFPolyAddAction();
            case "gfpoly_mul" -> new GFPolyMulAction();
            case "gfpoly_pow" -> new GFPolyPowAction();
            case "gfdiv" -> new GFDivAction();
            case "gfpoly_make_monic" -> new GFPolyMakeMonicAction();
            case "gfpoly_divmod" -> new GFPolyDivModAction();
            case "gfpoly_powmod" -> new GFPolyPowModAction();
            case "gfpoly_sqrt" -> new GFPolySqrtAction();
            case "gfpoly_sort" -> new GFPolySortAction();
            case "gfpoly_diff" -> new GFPolyDiffAction();
            case "gfpoly_gcd" -> new GFPolyGCDAction();
            case "gfpoly_factor_sff" -> new GFPolyFactorSFFAction();
            case "gfpoly_factor_ddf" -> new GFPolyFactorDDFAction();
            case "gfpoly_factor_edf" -> new GFPolyFactorEDFAction();
            default -> null;
        };
    }

    public static void iterateOverAllCases(ResponseBuilder builder, JsonObject testcasesJson) {
        // Get the number of available processors, which will determine the number of
        // threads
        int availableThreads = Runtime.getRuntime().availableProcessors();

        // Create an ExecutorService to manage a pool of threads
        ExecutorService executorService = Executors.newFixedThreadPool(availableThreads);

        // Create a list of tasks for each test case
        List<Callable<Void>> tasks = new ArrayList<>();

        for (Entry<String, JsonElement> singleCase : testcasesJson.entrySet()) {
            tasks.add(() -> {
                JsonObject remainderJsonObject = singleCase.getValue().getAsJsonObject();

                // the 3 relevant parts of each case
                String uniqueHash = singleCase.getKey();
                String actionName = remainderJsonObject.get("action").getAsString();
                JsonObject arguments = remainderJsonObject.get("arguments").getAsJsonObject();

                Action action = getActionClass(actionName); // get the appropriate instance

                if (action == null)
                    return null;

                // execute
                Map<String, Object> resultEntry = action.execute(arguments);

                // Adding response to builder
                builder.addResponse(uniqueHash, resultEntry);

                return null;
            });
        }

        try {
            // Execute all tasks asynchronously and wait for completion
            List<Future<Void>> futures = executorService.invokeAll(tasks);

            // Ensure all tasks complete
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.severe("An error occurred during parallel execution: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown(); // Shutdown the executor service
        }
    }

    public static JsonObject parseFilePathToJson(String filePath) {
        // Reading could fail, needs try-catch
        try (FileReader reader = new FileReader(filePath)) {
            // Try parsing the JSON file to a JsonObject
            JsonObject jsonObj = new Gson().fromJson(reader, JsonObject.class);
            return jsonObj;

            // If there is any fail at this stage here, continuation isn't possible
        } catch (IOException e) {
            log.severe("File could not be read. Missing permissions maybe?");
            System.exit(1);
        } catch (JsonParseException e) {
            log.severe("File is not valid json!");
            System.exit(1);
        }

        throw new RuntimeException("This line of code should've never been reached");
    }
}
