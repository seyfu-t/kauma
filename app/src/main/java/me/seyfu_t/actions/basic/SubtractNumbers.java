package me.seyfu_t.actions.basic;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.util.ResponseBuilder;

public class SubtractNumbers implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        int a = arguments.get("number1").getAsInt();
        int b = arguments.get("number2").getAsInt();

        return ResponseBuilder.singleResponse("difference", a - b);
    }

}
