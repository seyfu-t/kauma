package me.seyfu_t;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class App {

    private static Logger log = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {

        // TODO: parse and go through each "packet" in a loop

        String filePath = args[0];

        if (!new File(filePath).exists()) {
            log.severe("Datei existiert nicht!");
            System.exit(1);
        }

        JsonObject fullJson = parseFilePathToJson(filePath).get();
        JsonObject testcasesJson = fullJson.get("testcases").getAsJsonObject();

        for (Entry<String, JsonElement> packets : testcasesJson.entrySet()) {
            String uniqueID = packets.getKey();

            JsonObject remainderJsonObj = packets.getValue().getAsJsonObject();

            String action = remainderJsonObj.get("action").getAsString();
            JsonObject arguments = remainderJsonObj.get("arguments").getAsJsonObject();

            log.info(uniqueID);
            log.info(remainderJsonObj.toString());
            log.info(action);
            log.info(arguments.toString());
        }

    }

    private static Optional<JsonObject> parseFilePathToJson(String filePath) {
        // Reading could fail, needs try-catch
        try (FileReader reader = new FileReader(filePath)) {
            // Parse the JSON file to a JsonObject
            JsonObject jsonObj = new Gson().fromJson(reader, JsonObject.class);
            return Optional.of(jsonObj);
            // If there is any fail at this stage here, continuation isn't possible, so the programs need to be stopped
        } catch (IOException e) {
            log.severe("File could not be read. Missing permissions maybe?");
            System.exit(1);
        } catch (JsonParseException e) {
            log.severe("File is not valid json!");
            System.exit(1);
        }
        return Optional.empty(); // This line of code will never be reached, but required for type validation
    }
}
