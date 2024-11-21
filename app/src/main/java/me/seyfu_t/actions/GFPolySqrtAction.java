package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFPolySqrtAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("Q").getAsJsonArray());

        GF128Poly q = new GF128Poly(poly);

        GF128Poly s = gfPolySqrt(q);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("S", s.toBase64Array());

        return resultMap;
    }

    public static GF128Poly gfPolySqrt(GF128Poly poly) {
        // sqrt(d) = d^(2^(m-1)) in F_(2^m)
        UBigInt16 pow = UBigInt16.Zero(true).setBit(127);
        
        GF128Poly result = new GF128Poly();

        for (int i = 0; i < poly.size(); i+=2) {
            UBigInt16 currentCoefficient = poly.getCoefficient(i);
            // Generate a degree 0 polynomial from the coefficient
            GF128Poly base = new GF128Poly(new UBigInt16[]{currentCoefficient});

            // square and multiply, the polynomial wont get bigger
            base = GFPolyPowAction.gfPolyPow(base, pow);

            // take back the now processed coefficient and save
            result.setCoefficient(i/2, base.getCoefficient(0));
        }

        return result;
    }

}
