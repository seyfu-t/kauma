package me.seyfu_t.actions.gfpoly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.model.Tuple;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyFactorSFFAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] base64ArrayPoly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());

        GFPoly poly = new GFPoly(base64ArrayPoly);

        List<Tuple<GFPoly, Integer>> tupleList = sff(poly);

        JsonArray array = new JsonArray();

        for (Tuple<GFPoly, Integer> tuple : tupleList)
            array.add(tuple.toJSON("factor", "exponent"));

        return ResponseBuilder.singleResponse("factors", array);
    }

    public static List<Tuple<GFPoly, Integer>> sff(GFPoly f) {
        GFPoly c = GFPolyGCDAction.gcd(f, GFPolyDiffAction.diff(f));

        f = GFPolyDivModAction.divModQuotient(f, c);

        List<Tuple<GFPoly, Integer>> tupleList = new ArrayList<>();

        int exponent = 1;

        while (!f.equals(GFPoly.DEGREE_ZERO_POLY_ONE)) {
            GFPoly y = GFPolyGCDAction.gcd(f, c);

            if (!f.equals(y))
                tupleList.add(new Tuple<>(GFPolyDivModAction.divModQuotient(f, y), exponent));

            f = y.copy();
            c = GFPolyDivModAction.divModQuotient(c, y);

            exponent++;
        }

        if (!c.equals(GFPoly.DEGREE_ZERO_POLY_ONE)) {
            for (Tuple<GFPoly, Integer> tuple : sff(GFPolySqrtAction.sqrt(c))) {
                tupleList.add(new Tuple<>(tuple.getFirst(), tuple.getSecond() * 2));
            }
        }

        // Sort by polynomials
        tupleList.sort(Comparator.comparing(Tuple::getFirst));

        return tupleList;
    }

}
