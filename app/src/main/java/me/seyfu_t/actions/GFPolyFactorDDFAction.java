package me.seyfu_t.actions;

import java.math.BigInteger;
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

public class GFPolyFactorDDFAction implements Action {

    private static final BigInteger Q = BigInteger.ZERO.setBit(128);

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] base64ArrayPoly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());

        GFPoly poly = new GFPoly(base64ArrayPoly);

        List<Tuple<GFPoly, Integer>> tupleList = ddf(poly);

        JsonArray array = new JsonArray();

        for (Tuple<GFPoly, Integer> tuple : tupleList)
            array.add(tuple.toJSON("factor", "degree"));

        return ResponseBuilder.singleResponse("factors", array);
    }

    public static List<Tuple<GFPoly, Integer>> ddf(GFPoly f) {
        List<Tuple<GFPoly, Integer>> tupleList = new ArrayList<>();
        int d = 1;
        GFPoly fStar = f.copy();

        while (fStar.degree() >= 2 * d) {
            BigInteger bigExponent = Q.pow(d);
            // X^(q^d) mod f*
            GFPoly h = GFPolyPowModAction.powMod(GFPoly.DEGREE_ONE_POLY_ONE, bigExponent, fStar);
            // - X
            h = GFPolyAddAction.add(h, GFPoly.DEGREE_ONE_POLY_ONE);

            GFPoly g = GFPolyGCDAction.gcd(h, fStar);

            if (!g.equals(GFPoly.DEGREE_ZERO_POLY_ONE)) {
                tupleList.add(new Tuple<>(g, d));
                fStar = GFPolyDivModAction.divModQuotient(fStar, g);
            }
            d++;
        }

        if (!fStar.equals(GFPoly.DEGREE_ZERO_POLY_ONE))
            tupleList.add(new Tuple<>(fStar, fStar.degree()));
        else if (tupleList.isEmpty())
            tupleList.add(new Tuple<>(f, 1));

        // Sort by polynomials
        tupleList.sort(Comparator.comparing(Tuple::getFirst));

        return tupleList;
    }

}
