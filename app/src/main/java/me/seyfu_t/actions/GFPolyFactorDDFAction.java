package me.seyfu_t.actions;

import java.math.BigInteger;
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
import me.seyfu_t.model.UBigInt512;
import me.seyfu_t.util.Util;

public class GFPolyFactorDDFAction implements Action {

    private static final BigInteger Q = BigInteger.ZERO.setBit(128);

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] base64ArrayPoly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());

        GF128Poly poly = new GF128Poly(base64ArrayPoly);

        List<Tuple<GF128Poly, Integer>> tupleList = ddf(poly);

        JsonArray array = new JsonArray();

        for (Tuple<GF128Poly, Integer> tuple : tupleList) {
            array.add(tuple.toJSON("factor", "degree"));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("factors", array);

        return resultMap;
    }

    public static List<Tuple<GF128Poly, Integer>> ddf(GF128Poly f) {
        List<Tuple<GF128Poly, Integer>> tupleList = new ArrayList<>();
        int d = 1;
        GF128Poly fStar = f.copy();

        while (fStar.degree() >= 2 * d) {
            UBigInt512 bigExponent = UBigInt512.fromBigInt(Q.pow(d));
            // X^(q^d) mod f*
            GF128Poly h = GFPolyPowModAction.powMod(GF128Poly.DEGREE_ONE_POLY_ONE, bigExponent, fStar);
            // - X
            h = GFPolyAddAction.add(h, GF128Poly.DEGREE_ONE_POLY_ONE);

            GF128Poly g = GFPolyGCDAction.gcd(h, fStar);

            if (!g.equals(GF128Poly.DEGREE_ZERO_POLY_ONE)) {
                tupleList.add(new Tuple<>(g, d));
                fStar = GFPolyDivModAction.divModQuotient(fStar, g);
            }
            d++;
        }

        if (!fStar.equals(GF128Poly.DEGREE_ZERO_POLY_ONE))
            tupleList.add(new Tuple<>(fStar, fStar.degree()));
        else if (tupleList.isEmpty())
            tupleList.add(new Tuple<>(f, 1));

        // Sort by polynomials
        tupleList.sort(Comparator.comparing(Tuple::getFirst));

        return tupleList;
    }

}
