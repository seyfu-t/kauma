package me.seyfu_t.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResponseBuilder {

    private static final Gson gson = new Gson();

    private final Map<String, JsonObject> responses;

    public ResponseBuilder() {
        responses = new ConcurrentHashMap<>();
    }

    public void addResponse(String hash, Map<String, Object> response) {
        JsonObject innerJson = new JsonObject(); // e.g. {"sum":"300"}
    
        for (String actionResultName : response.keySet()) {
            innerJson.add(actionResultName, parseResultToJsonElement(response.get(actionResultName)));
        }
    
        responses.put(hash, innerJson); // Thread-safe put in ConcurrentHashMap
    }
    

    private static JsonElement parseResultToJsonElement(Object result) {
        return gson.toJsonTree(result);
    }

    public JsonObject build() {
        JsonObject finalObject = new JsonObject();
        JsonObject responsesObject = new JsonObject();
    
        // Transfer the responses into the final JsonObject
        for (Map.Entry<String, JsonObject> entry : responses.entrySet()) {
            responsesObject.add(entry.getKey(), entry.getValue());
        }
    
        finalObject.add("responses", responsesObject);
        return finalObject;
    }
    
}
