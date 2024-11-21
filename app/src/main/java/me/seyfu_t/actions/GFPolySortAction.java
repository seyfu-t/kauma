package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
        List<GF128Poly> polysList = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            String[] poly = Util.convertJsonArrayToStringArray(array.get(i).getAsJsonArray());
            polysList.add(new GF128Poly(poly));
        }

        List<GF128Poly> list = gfPolySort(polysList);
        JsonArray resultArray = new JsonArray(list.size());
        for (int i = 0; i < list.size(); i++) {
            String[] currentResult = list.get(i).toBase64Array();
            resultArray.add(new Gson().toJsonTree(currentResult));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sorted_polys", resultArray);
        return resultMap;
    }

    public static List<GF128Poly> gfPolySort(List<GF128Poly> listOfPolysToSort) {
        List<GF128Poly> sortedList = new LinkedList<>(listOfPolysToSort);

        // Java syntax is beautiful... not
        // the sort() method uses merge sort for LinkedLists
        sortedList.sort((p1, p2) -> {
            // First compare by degree
            int degreeComparison = Integer.compare(p1.degree(), p2.degree());
            if (degreeComparison != 0) {
                return degreeComparison; // Sort by degree
            }

            // Degree must be same, if -1, both are []
            if (p1.degree() == -1)
                return 0;

            // If degrees are equal, compare coefficients
            for (int i = p1.size() - 1; i >= 0; i++) {
                if (p1.getCoefficient(i).greaterThan(p2.getCoefficient(i))) {
                    return 1; // p1 > p2
                } else if (p2.getCoefficient(i).greaterThan(p1.getCoefficient(i))) {
                    return -1; // p1 < p2
                }
            }

            // Polynomials are equal
            return 0;
        });

        return sortedList;
    }

}
