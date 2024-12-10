package me.seyfu_t.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SplittableRandom;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.BigLong;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyFactorEDFAction implements Action {

    private static final Gson gson = new Gson();
    private static final SplittableRandom random = new SplittableRandom();

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] base64ArrayPoly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());
        int degree = arguments.get("d").getAsInt();

        GFPoly poly = new GFPoly(base64ArrayPoly);

        List<GFPoly> resultList = edf(poly, degree);
        JsonArray array = new JsonArray();

        for (int i = 0; i < resultList.size(); i++)
            array.add(gson.toJsonTree(resultList.get(i).toBase64Array()));

        return ResponseBuilder.singleResponse("factors", array);
    }

    public static List<GFPoly> edf(GFPoly f, int d) {
        int n = f.degree() / d;

        List<GFPoly> polyList = new ArrayList<>();
        polyList.add(f);

        BigLong bigExponent = getExponent(d);

        while (polyList.size() < n) {
            GFPoly h = generateRandomPolynomial(f.degree());
            GFPoly g = GFPolyPowModAction.powMod(h, bigExponent, f);
            g = GFPolyAddAction.add(g, GFPoly.DEGREE_ZERO_POLY_ONE);

            List<GFPoly> newPolys = new ArrayList<>();
            Iterator<GFPoly> iterator = polyList.iterator();

            while (iterator.hasNext()) {
                GFPoly u = iterator.next();
                if (u.degree() > d) {
                    GFPoly j = GFPolyGCDAction.gcd(u, g);
                    if (!j.equals(GFPoly.DEGREE_ZERO_POLY_ONE) && !j.equals(u)) {
                        iterator.remove();
                        newPolys.add(j);
                        newPolys.add(GFPolyDivModAction.divModQuotient(u, j));
                    }
                }
            }
            polyList.addAll(newPolys);
        }

        polyList.sort(null);

        return polyList;
    }

    private static GFPoly generateRandomPolynomial(int smallerThan) {
        GFPoly randomPoly = new GFPoly();

        int newDegree = random.nextInt(1, smallerThan + 1);

        for (int i = 0; i < newDegree; i++)
            randomPoly.setCoefficient(i, new FieldElement(random.nextLong(), random.nextLong()));

        return randomPoly;
    }

    private static BigLong getExponent(int d) {
        long pattern = 0x5555555555555555L;
        List<Long> list = new ArrayList<>(Arrays.asList(pattern, pattern, pattern, pattern));
        for (int i = 1; i < d; i++){
            list.add(pattern);
            list.add(pattern);
            list.add(pattern);
            list.add(pattern);
        }

        return new BigLong(list);
    }

}