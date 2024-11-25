package me.seyfu_t;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ForkJoinPool;

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
        return getResponseJsonFromInputJson(parseFilePathToJson(filePath));
    }

    public static JsonObject getResponseJsonFromInputJson(JsonObject fullJson) {
        JsonObject testcasesJson = fullJson.get("testcases").getAsJsonObject();
        ResponseBuilder responseBuilder = new ResponseBuilder();

        // Use ForkJoinPool for parallel processing
        ForkJoinPool.commonPool().submit(() ->
            testcasesJson.entrySet().parallelStream().forEach(singleCase -> {
                ProcessedTestCase result = processTestCase(singleCase);
                if (result != null && result.result() != null) {
                    synchronized (responseBuilder) {
                        responseBuilder.addResponse(result.hash(), result.result());
                    }
                }
            })
        ).join();

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
            return null;
        }
    }

    private record ProcessedTestCase(String hash, Map<String, Object> result) {}

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