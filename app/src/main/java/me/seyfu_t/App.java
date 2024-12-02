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
        if (args.length == 0 || !new File(args[0]).exists()) {
            log.severe("Datei existiert nicht!");
            System.exit(1);
        }

        JsonObject response = getResponseJsonFromInputPath(args[0]);
        System.out.println(response.toString());
    }

    public static JsonObject getResponseJsonFromInputPath(String filePath) {
        return getResponseJsonFromInputJsonVirtual(parseFilePathToJson(filePath));
    }

    public static JsonObject getResponseJsonFromInputJsonVirtual(JsonObject fullJson) {
        JsonObject testcasesJson = fullJson.get("testcases").getAsJsonObject();
        ResponseBuilder responseBuilder = new ResponseBuilder();

        // Track virtual threads to ensure they all complete
        List<Thread> threads = new ArrayList<>();

        // Use virtual threads to process the test cases concurrently
        testcasesJson.entrySet().forEach(singleCase -> {
            Thread thread = Thread.ofVirtual().start(() -> {
                ProcessedTestCase result = processTestCase(singleCase);
                if (result != null && result.result() != null) {
                    synchronized (responseBuilder) {
                        responseBuilder.addResponse(result.hash(), result.result());
                    }
                }
            });

            threads.add(thread);
        });

        // Ensure all threads complete before returning the response
        threads.forEach(thread -> {
            try {
                thread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return responseBuilder.build();
    }

    private static ProcessedTestCase processTestCase(Entry<String, JsonElement> singleCase) {
        try {
            JsonObject remainderJsonObject = singleCase.getValue().getAsJsonObject();
            String uniqueHash = singleCase.getKey();
            String actionName = remainderJsonObject.get("action").getAsString();
            JsonObject arguments = remainderJsonObject.get("arguments").getAsJsonObject();

            Action action = getActionClass(actionName);
            if (action == null) {
                return null;
            }

            Map<String, Object> resultEntry = action.execute(arguments);
            return new ProcessedTestCase(uniqueHash, resultEntry);
        } catch (Exception e) {
            log.warning("Error processing testcase: " + e.getMessage());
            return null;
        }
    }

    private record ProcessedTestCase(String hash, Map<String, Object> result) {
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
        // Separate padding_oracle cases from other cases
        List<Entry<String, JsonElement>> paddingOracleCases = new ArrayList<>();
        List<Entry<String, JsonElement>> otherCases = new ArrayList<>();

        for (Entry<String, JsonElement> singleCase : testcasesJson.entrySet()) {
            JsonObject remainderJsonObject = singleCase.getValue().getAsJsonObject();
            String actionName = remainderJsonObject.get("action").getAsString();
            
            if ("padding_oracle".equals(actionName)) {
                paddingOracleCases.add(singleCase);
            } else {
                otherCases.add(singleCase);
            }
        }

        // Get the number of available processors for other cases
        int availableThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableThreads);

        try {
            // Handle padding_oracle cases sequentially first
            for (Entry<String, JsonElement> singleCase : paddingOracleCases) {
                processCase(builder, singleCase);
            }

            // Handle other cases concurrently
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Entry<String, JsonElement> singleCase : otherCases) {
                tasks.add(() -> {
                    processCase(builder, singleCase);
                    return null;
                });
            }

            // Execute all concurrent tasks
            List<Future<Void>> futures = executorService.invokeAll(tasks);

            // Ensure all tasks complete
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.severe("An error occurred during execution: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown(); // Shutdown the executor service
        }
    }

    private static void processCase(ResponseBuilder builder, Entry<String, JsonElement> singleCase) {
        JsonObject remainderJsonObject = singleCase.getValue().getAsJsonObject();

        // the 3 relevant parts of each case
        String uniqueHash = singleCase.getKey();
        String actionName = remainderJsonObject.get("action").getAsString();
        JsonObject arguments = remainderJsonObject.get("arguments").getAsJsonObject();

        Action action = getActionClass(actionName); // get the appropriate instance

        if (action == null)
            return;

        // execute
        Map<String, Object> resultEntry = action.execute(arguments);

        // Adding response to builder
        builder.addResponse(uniqueHash, resultEntry);
    }

    public static JsonObject parseFilePathToJson(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            return new Gson().fromJson(reader, JsonObject.class);
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