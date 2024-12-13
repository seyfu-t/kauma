package me.seyfu_t.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Tuple;

public class ResponseBuilder {

    private static final Gson gson = new Gson();

    private final Map<String, JsonObject> responses;

    public ResponseBuilder() {
        responses = new ConcurrentHashMap<>();
    }

    public void addResponse(String hash, JsonObject response) {
        responses.put(hash, response); // Thread-safe put in ConcurrentHashMap
    }

    public JsonObject build() {
        JsonObject finalObject = new JsonObject();
        JsonObject responsesObject = new JsonObject();

        // Transfer the responses into the final JsonObject
        for (Map.Entry<String, JsonObject> entry : responses.entrySet())
            responsesObject.add(entry.getKey(), entry.getValue());

        finalObject.add("responses", responsesObject);
        return finalObject;
    }

    public static JsonObject single(String key, Object value) {
        JsonObject object = new JsonObject();
        object.add(key, parseToJsonElement(value));

        return object;
    }

    public static JsonObject multi(List<Tuple<String, Object>> response) {
        JsonObject object = new JsonObject();
        for (Tuple<String,Object> tuple : response)
            object.add(tuple.getFirst(), parseToJsonElement(tuple.getSecond()));

        return object;
    }

    public static JsonElement parseToJsonElement(Object result) {
        return gson.toJsonTree(result);
    }

}
