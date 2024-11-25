package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.util.Util;

public class GFPolyFactorSFFAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] base64ArrayPoly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());

        GF128Poly poly = new GF128Poly(base64ArrayPoly);

        List<Tuple<GF128Poly, Integer>> tupleList = sff(poly);

        JsonArray array = new JsonArray();

        for (Tuple<GF128Poly, Integer> tuple : tupleList) {
            array.add(tuple.toJSON("factor", "exponent"));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("factors", array);

        return resultMap;
    }

    public static List<Tuple<GF128Poly, Integer>> sff(GF128Poly f) {
        GF128Poly c = GFPolyGCDAction.gcd(f, GFPolyDiffAction.diff(f));

        f = GFPolyDivModAction.divModQuotient(f, c);

        List<Tuple<GF128Poly, Integer>> tupleList = new ArrayList<>();

        int exponent = 1;

        while (!f.equals(GF128Poly.DEGREE_ZERO_POLY_ONE)) {
            GF128Poly y = GFPolyGCDAction.gcd(f, c);

            if (!f.equals(y))
                tupleList.add(new Tuple<>(GFPolyDivModAction.divModQuotient(f, y), exponent));

            f = y.copy();
            c = GFPolyDivModAction.divModQuotient(c, y);

            exponent++;
        }

        if (!c.equals(GF128Poly.DEGREE_ZERO_POLY_ONE)) {
            for (Tuple<GF128Poly, Integer> tuple : sff(GFPolySqrtAction.sqrt(c))) {
                tupleList.add(new Tuple<>(tuple.getFirst(), tuple.getSecond() * 2));
            }
        }

        // Sort by exponent
        tupleList.sort(Comparator.comparing(Tuple::getFirst));

        return tupleList;
    }

}
