package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolySortAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        JsonArray array = arguments.get("polys").getAsJsonArray();
        List<GF128Poly> polysList = new ArrayList<>(array.size()); // Pre-sizing

        for (int i = 0; i < array.size(); i++) {
            String[] poly = Util.convertJsonArrayToStringArray(array.get(i).getAsJsonArray());
            polysList.add(new GF128Poly(poly));
        }

        List<GF128Poly> sortedList = sort(polysList);

        JsonArray resultArray = new JsonArray();
        Gson gson = new Gson();

        for (GF128Poly poly : sortedList)
            resultArray.add(gson.toJsonTree(poly.toBase64Array()));

        return ResponseBuilder.singleResponse("sorted_polys", resultArray);
    }

    public static List<GF128Poly> sort(List<GF128Poly> listOfPolysToSort) {
        List<GF128Poly> sortedList = new ArrayList<>(listOfPolysToSort);

        sortedList.sort(null);

        return sortedList;
    }
}