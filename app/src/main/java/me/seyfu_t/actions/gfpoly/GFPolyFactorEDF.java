package me.seyfu_t.actions.gfpoly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.BigLong;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.MT19937Random;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyFactorEDF implements Action {

    private static final MT19937Random RANDOM = new MT19937Random(System.currentTimeMillis());
    private static final long PATTERN = 0x5555555555555555L;

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] base64ArrayPoly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());
        int degree = arguments.get("d").getAsInt();

        GFPoly poly = new GFPoly(base64ArrayPoly);

        List<GFPoly> resultList = edf(poly, degree);
        JsonArray array = new JsonArray();

        for (int i = 0; i < resultList.size(); i++)
            array.add(ResponseBuilder.asJSON(resultList.get(i).toBase64Array()));

        return ResponseBuilder.single("factors", array);
    }

    public static List<GFPoly> edf(GFPoly f, int d) {
        int n = f.degree() / d;

        List<GFPoly> polyList = new ArrayList<>();
        polyList.add(f);

        BigLong bigExponent = getExponent(d);

        while (polyList.size() < n) {
            GFPoly h = generateRandomPolynomial(f.degree());
            GFPoly g = GFPolyPowMod.powMod(h, bigExponent, f);
            g = GFPolyAdd.add(g, GFPoly.DEGREE_ZERO_POLY_ONE);

            List<GFPoly> newPolys = new ArrayList<>();
            Iterator<GFPoly> iterator = polyList.iterator();

            while (iterator.hasNext()) {
                GFPoly u = iterator.next();
                if (u.degree() > d) {
                    GFPoly j = GFPolyGCD.gcd(u, g);
                    if (!j.equals(GFPoly.DEGREE_ZERO_POLY_ONE) && !j.equals(u)) {
                        iterator.remove();
                        newPolys.add(j);
                        newPolys.add(GFPolyDivMod.divModQuotient(u, j));
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

        int newDegree = RANDOM.nextInt(smallerThan + 1);

        for (int i = 0; i < newDegree; i++)
            randomPoly.setCoefficient(i, new FieldElement(RANDOM.nextLong(), RANDOM.nextLong()));

        return randomPoly;
    }

    private static BigLong getExponent(int d) {
        List<Long> list = new ArrayList<>(Arrays.asList(PATTERN, PATTERN));
        for (int i = 1; i < d; i++){
            list.add(PATTERN);
            list.add(PATTERN);
        }

        return new BigLong(list);
    }

}