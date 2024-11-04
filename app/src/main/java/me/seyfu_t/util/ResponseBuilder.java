package me.seyfu_t.util;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResponseBuilder {

    private final JsonObject responses;

    public ResponseBuilder() {
        responses = new JsonObject();
    }

    public void addResponse(String hash, Map<String, Object> response) {
        JsonObject innerJson = new JsonObject(); // e.g. {"sum":"300"}

        for (String actionResultName : response.keySet()) {
            innerJson.add(actionResultName, parseResultToJsonElement(response.get(actionResultName)));

        }

        responses.add(hash, innerJson); // e.g. {"HASH":<innerJson>}
    }

    private static JsonElement parseResultToJsonElement(Object result) {
        return new Gson().toJsonTree(result);
    }

    public JsonObject build() {
        JsonObject finalObject = new JsonObject();
        finalObject.add("responses", responses); // final {"responses":{<each response>}} structure
        return finalObject;
    }
}
