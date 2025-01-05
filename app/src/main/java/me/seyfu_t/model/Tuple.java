package me.seyfu_t.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.util.ResponseBuilder;

public class Tuple<T, U> {
    private final T first;
    private final U second;

    public Tuple(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public String toJSONString(String nameFirst, String nameSecond) {
        return "{" + ResponseBuilder.asJSON(nameFirst) + ":" + ResponseBuilder.asJSON(this.getFirst()) + "," + ResponseBuilder.asJSON(nameSecond) + ":"
                + ResponseBuilder.asJSON(this.getSecond()) + "}";
    }

    public JsonObject toJSON(String nameFirst, String nameSecond) {
        JsonObject jsonObj = new JsonObject();

        if (this.getFirst() instanceof GFPoly gfPoly)
            jsonObj.add(nameFirst, ResponseBuilder.asJSON(gfPoly.toBase64Array()));
        else
            jsonObj.add(nameFirst, ResponseBuilder.asJSON(this.getFirst()));

        if (this.getSecond() instanceof GFPoly gfPoly)
            jsonObj.add(nameSecond, ResponseBuilder.asJSON(gfPoly.toBase64Array()));
        else
            jsonObj.add(nameSecond, ResponseBuilder.asJSON(this.getSecond()));

        return jsonObj;
    }

    public Map<T, U> toMap() {
        Map<T, U> map = new LinkedHashMap<>();
        map.put(this.getFirst(), this.getSecond());

        return map;
    }
}
