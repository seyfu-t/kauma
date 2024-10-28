package me.seyfu_t.util;

import com.google.gson.JsonObject;

import me.seyfu_t.model.SingleResponse;

public class ResponseBuilder {

    private final JsonObject responses;

    public ResponseBuilder() {
        responses = new JsonObject();
    }

    public void addSingleResponse(SingleResponse response) {
        JsonObject innerJson = new JsonObject(); // e.g. {"sum":"300"}
        innerJson.add(response.getActionResultName(), response.getParsedResultAsJsonElement());

        responses.add(response.getHash(), innerJson); // e.g. {"HASH":<innerJson>}
    }

    public JsonObject build() {
        JsonObject finalObject = new JsonObject();
        finalObject.add("responses", responses); // final {"responses":{<each response>}} structure
        return finalObject;
    }
}
