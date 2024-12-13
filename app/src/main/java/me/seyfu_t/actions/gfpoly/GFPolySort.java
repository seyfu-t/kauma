package me.seyfu_t.actions.gfpoly;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolySort implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        JsonArray array = arguments.get("polys").getAsJsonArray();
        List<GFPoly> polysList = new ArrayList<>(array.size()); // Pre-sizing

        for (int i = 0; i < array.size(); i++) {
            String[] poly = Util.convertJsonArrayToStringArray(array.get(i).getAsJsonArray());
            polysList.add(new GFPoly(poly));
        }

        List<GFPoly> sortedList = sort(polysList);

        JsonArray resultArray = new JsonArray();
        Gson gson = new Gson();

        for (GFPoly poly : sortedList)
            resultArray.add(gson.toJsonTree(poly.toBase64Array()));

        return ResponseBuilder.single("sorted_polys", resultArray);
    }

    public static List<GFPoly> sort(List<GFPoly> listOfPolysToSort) {
        List<GFPoly> sortedList = new ArrayList<>(listOfPolysToSort);

        sortedList.sort(null);

        return sortedList;
    }
}