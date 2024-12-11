package me.seyfu_t.actions.gfpoly;

import com.google.gson.JsonObject;

import me.seyfu_t.actions.gf.GFDivAction;
import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolyMakeMonicAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] polyA = Util.convertJsonArrayToStringArray(arguments.get("A").getAsJsonArray());

        GFPoly a = new GFPoly(polyA);

        return ResponseBuilder.singleResponse("A*", makeMonic(a).toBase64Array());
    }

    public static GFPoly makeMonic(GFPoly a) {
        FieldElement leadingCoefficient = a.getCoefficient(a.size() - 1);

        GFPoly poly = new GFPoly();
        for (int i = 0; i < a.size() - 1; i++) {
            // Divide each coefficient
            FieldElement divided = GFDivAction.div(a.getCoefficient(i), leadingCoefficient);
            poly.setCoefficient(i, divided);
        }
        poly.setCoefficient(a.size() - 1, FieldElement.One());

        return poly;
    }
}
