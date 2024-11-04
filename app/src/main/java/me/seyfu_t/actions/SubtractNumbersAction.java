package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;

public class SubtractNumbersAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        int a = arguments.get("number1").getAsInt();
        int b = arguments.get("number2").getAsInt();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("difference", a - b);

        return resultMap;
    }

}
