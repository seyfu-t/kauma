package me.seyfu_t.actions;

import java.util.AbstractMap;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;

public class SubtractNumbersAction implements Action {

    @Override
    public Entry<String, Object> execute(JsonObject arguments) {
        int a = arguments.get("number1").getAsInt();
        int b = arguments.get("number2").getAsInt();

        return new AbstractMap.SimpleEntry<>("sum", a - b); // Very SIMPLE way of creating a SIMPLE key-value pair, that's java for ya
    }

}
