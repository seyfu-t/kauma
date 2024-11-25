package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.util.Util;

public class GFPolySortAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        JsonArray array = arguments.get("polys").getAsJsonArray();
        List<GF128Poly> polysList = new ArrayList<>(array.size()); // Pre-sizing

        for (int i = 0; i < array.size(); i++) {
            String[] poly = Util.convertJsonArrayToStringArray(array.get(i).getAsJsonArray());
            polysList.add(new GF128Poly(poly));
        }

        List<GF128Poly> sortedList = sort(polysList);

        JsonArray resultArray = new JsonArray();
        Gson gson = new Gson();

        for (GF128Poly poly : sortedList) {
            resultArray.add(gson.toJsonTree(poly.toBase64Array()));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sorted_polys", resultArray);
        return resultMap;
    }

    public static List<GF128Poly> sort(List<GF128Poly> listOfPolysToSort) {
        // Use ArrayList instead of LinkedList for better random access performance
        List<GF128Poly> sortedList = new ArrayList<>(listOfPolysToSort);

        // Java syntax...
        // lambda that in theory must be able to compare each possible pair p1 and p2
        // sortedList.sort((p1, p2) -> {
        //     // Returning 1 means p1 > p2
        //     // Returning 0 means p1 = p2
        //     // Returning -1 means p1 < p2

        //     // First compare by degree
        //     int degreeComparison = Integer.compare(p1.degree(), p2.degree());
        //     if (degreeComparison != 0)
        //         return degreeComparison;

        //     // If both polynomials are empty (degree == -1)
        //     if (p1.degree() == -1)
        //         return 0;

        //     // Compare coefficients from highest to lowest degree
        //     for (int i = p1.degree(); i >= 0; i--) {
        //         if (p1.getCoefficient(i).greaterThan(p2.getCoefficient(i))) {
        //             return 1;
        //         } else if (p2.getCoefficient(i).greaterThan(p1.getCoefficient(i))) {
        //             return -1;
        //         }
        //     }

        //     return 0; // Polynomials are equal
        // });

        sortedList.sort(null);

        return sortedList;
    }
}