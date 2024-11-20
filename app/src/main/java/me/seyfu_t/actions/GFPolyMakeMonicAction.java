package me.seyfu_t.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.Util;

public class GFPolyMakeMonicAction implements Action {

    @Override
    public Map<String, Object> execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());

        GF128Poly a = new GF128Poly(polyA);

        GF128Poly monic = gfPolyMakeMonic(a);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("A*", monic.toBase64Array());

        return resultMap;
    }

    public static GF128Poly gfPolyMakeMonic(GF128Poly a) {
        UBigInt16 leadingCoefficient = a.getCoefficient(a.size() - 1);

        GF128Poly poly = new GF128Poly();
        for (int i = 0; i < a.size(); i++) {
            // Divide each coefficient
            UBigInt16 divided = GFDivAction.divide(a.getCoefficient(i), leadingCoefficient);
            poly.setCoefficient(i, divided);
        }

        return poly;
    }
}
