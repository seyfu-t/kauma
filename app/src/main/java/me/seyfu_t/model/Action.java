package me.seyfu_t.model;

import com.google.gson.JsonObject;

public interface Action {
    /**
     * @param arguments takes in the contents of "arguments":{}
     * 
     * @return action result which is a key-value pair with the key being the name
     * of the output (e.g. "sum" or "block") and the value being the result data
     * itself
     */
    public JsonObject execute(JsonObject arguments);
}
