package me.seyfu_t.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Tuple<T, U> {
    private final T first;
    private final U second;

    private static final Gson gson = new Gson();

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
        return "{" + gson.toJson(nameFirst) + ":" + gson.toJson(this.getFirst()) + "," + gson.toJson(nameSecond) + ":"
                + gson.toJson(this.getSecond()) + "}";
    }

    public JsonObject toJSON(String nameFirst, String nameSecond) {
        JsonObject jsonObj = new JsonObject();

        if (this.getFirst() instanceof GF128Poly gf128Poly)
            jsonObj.add(nameFirst, gson.toJsonTree(gf128Poly.toBase64Array()));
        else
            jsonObj.add(nameFirst, gson.toJsonTree(this.getFirst()));
            
        if (this.getSecond() instanceof GF128Poly gf128Poly)
            jsonObj.add(nameSecond, gson.toJsonTree(gf128Poly.toBase64Array()));
        else
            jsonObj.add(nameSecond, gson.toJsonTree(this.getSecond()));

        return jsonObj;
    }

    public Map<T, U> toMap() {
        Map<T, U> map = new LinkedHashMap<>();
        map.put(this.getFirst(), this.getSecond());

        return map;
    }
}