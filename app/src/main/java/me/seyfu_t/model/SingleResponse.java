package me.seyfu_t.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class SingleResponse {
    private final String hash;
    private final String actionResultName;
    private final Object result;

    private final Gson gson = new Gson();

    public SingleResponse(String hash, String actionResultName, Object result) {
        this.hash = hash;
        this.actionResultName = actionResultName;
        this.result = result;
    }

    public String getActionResultName() {
        return actionResultName;
    }

    public String getHash() {
        return hash;
    }

    public Object getResult() {
        return result;
    }

    public JsonElement getParsedResultAsJsonElement() {
        return gson.toJsonTree(result);
    }
}
