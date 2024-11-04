package me.seyfu_t.model;

import java.util.Map;

import com.google.gson.JsonObject;

public interface Action {
    /*
     * @param takes in the contents of "arguments":{}
     * 
     * @return action result which is a key-value pair with the key being the name
     * of the output (e.g. "sum" or "block") and the value being the result data
     * itself
     */
    public Map<String, Object> execute(JsonObject arguments);
}
