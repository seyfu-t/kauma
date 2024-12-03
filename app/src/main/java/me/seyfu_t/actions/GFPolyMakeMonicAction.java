package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyMakeMonicAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());

        GF128Poly a = new GF128Poly(polyA);

        return ResponseBuilder.singleResponse("A*", makeMonic(a).toBase64Array());
    }

    public static GF128Poly makeMonic(GF128Poly a) {
        UBigInt16 leadingCoefficient = a.getCoefficient(a.size() - 1);

        GF128Poly poly = new GF128Poly();
        for (int i = 0; i < a.size() - 1; i++) {
            // Divide each coefficient
            UBigInt16 divided = GFDivAction.div(a.getCoefficient(i), leadingCoefficient);
            poly.setCoefficient(i, divided);
        }
        poly.setCoefficient(a.size() - 1, UBigInt16.One(true));

        return poly;
    }
}
