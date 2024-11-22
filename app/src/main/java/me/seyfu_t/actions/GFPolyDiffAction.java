package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFPolyDiffAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("F").getAsJsonArray());

        GF128Poly q = new GF128Poly(poly);

        GF128Poly s = diff(q);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("F'", s.toBase64Array());

        return resultMap;
    }

    public static GF128Poly diff(GF128Poly poly) {
        GF128Poly result = new GF128Poly();

        if (poly.isZero()) {
            result.setCoefficient(0, UBigInt16.Zero(true));
            return result;
        }

        // Skip first one, effectively reduce all exponents by 1
        for (int i = 1; i < poly.size(); i++) {
            if (i % 2 == 1)
                result.setCoefficient(i - 1, poly.getCoefficient(i));
        }

        result = result.popLeadingZeros();

        if (result.isZero())
            result.setCoefficient(0, UBigInt16.Zero(true));

        return result.popLeadingZeros();
    }

}
