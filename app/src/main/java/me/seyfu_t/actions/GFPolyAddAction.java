package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.util.Util;

public class GFPolyAddAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());
        String[] polyB = Util.convertJsonArrayToStringArray(arguments.get("B").getAsJsonArray());

        GF128Poly a = new GF128Poly(polyA);
        GF128Poly b = new GF128Poly(polyB);

        GF128Poly sum = gfPolyAdd(a, b);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("S", sum.toBase64Array());

        return resultMap;
    }

    public static GF128Poly gfPolyAdd(GF128Poly a, GF128Poly b) {
        int minIndex = Math.min(a.size(), b.size());
        int maxIndex = Math.max(a.size(), b.size());

        GF128Poly result = new GF128Poly();

        for (int i = 0; i < minIndex; i++) {
            result.setCoefficient(i, a.getCoefficient(i).xor(b.getCoefficient(i).copy()));
        }

        for (int i = minIndex; i < maxIndex; i++) {
            result.setCoefficient(i, a.size() > b.size() ? a.getCoefficient(i).copy() : b.getCoefficient(i).copy());
        }

        return result.popLeadingZeros();
    }

    public static GF128Poly gfPolyAdd(GF128Poly[] summands) {
        GF128Poly result = new GF128Poly();

        for (GF128Poly summand : summands) {
            if (result.isEmpty())
                result = summand;

            result = gfPolyAdd(result, summand);
        }

        return result;
    }

}
