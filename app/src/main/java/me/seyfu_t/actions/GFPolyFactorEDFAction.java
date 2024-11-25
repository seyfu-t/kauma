package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.model.UBigInt512;
import me.seyfu_t.util.Util;

public class GFPolyFactorEDFAction implements Action {

    private static final UBigInt512 EXPONENT_Q = UBigInt512.Zero(true).setBit(128);
    private static final UBigInt512 THREE = new UBigInt512(new byte[] { 3 });
    private static final Gson gson = new Gson();

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] base64ArrayPoly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());
        int degree = arguments.get("d").getAsInt();

        GF128Poly poly = new GF128Poly(base64ArrayPoly);

        List<GF128Poly> resultList = edf(poly, degree);
        JsonArray array = new JsonArray();

        for (int i = 0; i < resultList.size(); i++)
            array.add(gson.toJsonTree(resultList.get(i).toBase64Array()));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("factors", array);

        return resultMap;
    }

    public static List<GF128Poly> edf(GF128Poly f, int d) {
        int n = f.degree() / d;

        List<GF128Poly> polyList = new ArrayList<>();
        polyList.add(f);
        UBigInt512 bigExponent = EXPONENT_Q.pow(d).sub(UBigInt512.One()).div(THREE);
        while (polyList.size() < n) {
            GF128Poly h = generateRandomPoly(f.degree());

            GF128Poly g = GFPolyPowModAction.powMod(h, bigExponent, f);
            g = GFPolyAddAction.add(g, GF128Poly.DEGREE_ZERO_POLY_ONE);

            List<GF128Poly> newPolys = new ArrayList<>();
            Iterator<GF128Poly> iterator = polyList.iterator();

            while (iterator.hasNext()) {
                GF128Poly u = iterator.next();
                if (u.degree() > d) {
                    GF128Poly j = GFPolyGCDAction.gcd(u, g);
                    if (!j.equals(GF128Poly.DEGREE_ZERO_POLY_ONE) && !j.equals(u)) {
                        iterator.remove();
                        newPolys.add(j);
                        newPolys.add(GFPolyDivModAction.divModQuotient(u, j));
                    }
                }
            }
            polyList.addAll(newPolys);
        }

        return polyList;
    }

    private static GF128Poly generateRandomPoly(int smallerThan) {
        Random random = new Random();
        GF128Poly randomPoly = new GF128Poly();

        int newDegree = random.nextInt(smallerThan);

        byte[] randomBytes = new byte[16];
        for (int i = 0; i <= newDegree; i++) {
            random.nextBytes(randomBytes);

            UBigInt16 randomCoefficient = new UBigInt16(randomBytes, true);
            randomPoly.setCoefficient(i, randomCoefficient);
        }

        return randomPoly;
    }

}
