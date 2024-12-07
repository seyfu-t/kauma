package me.seyfu_t.actions;

import com.google.gson.JsonObject;

import me.seyfu_t.model.Action;
import me.seyfu_t.model.FieldElement;
import me.seyfu_t.model.GF128Poly;
import me.seyfu_t.model.GFPoly;
import me.seyfu_t.model.UBigInt16;
import me.seyfu_t.util.ResponseBuilder;
import me.seyfu_t.util.Util;

public class GFPolySqrtAction implements Action {

    @Override
    public JsonObject execute(JsonObject arguments) {
        String[] poly = Util.convertJsonArrayToStringArray(arguments.get("Q").getAsJsonArray());

        GFPoly q = new GFPoly(poly);

        return ResponseBuilder.singleResponse("S", sqrt(q).toBase64Array());
    }

    public static GFPoly sqrt(GFPoly poly) {
        // sqrt(d) = d^(2^(m-1)) in F_(2^m)
        FieldElement pow = FieldElement.Zero().setBit(127);
        
        GFPoly result = new GFPoly();

        for (int i = 0; i < poly.size(); i+=2) {
            FieldElement currentCoefficient = poly.getCoefficient(i);
            // Generate a degree 0 polynomial from the coefficient
            GFPoly base = new GFPoly(new FieldElement[]{currentCoefficient});

            // square and multiply, the polynomial wont get bigger
            base = GFPolyPowAction.pow(base, pow);

            // take back the now processed coefficient and save
            result.setCoefficient(i/2, base.getCoefficient(0));
        }

        return result;
    }

    public static GF128Poly sqrt(GF128Poly poly) {
        // sqrt(d) = d^(2^(m-1)) in F_(2^m)
        UBigInt16 pow = UBigInt16.Zero(true).setBit(127);
        
        GF128Poly result = new GF128Poly();

        for (int i = 0; i < poly.size(); i+=2) {
            UBigInt16 currentCoefficient = poly.getCoefficient(i);
            // Generate a degree 0 polynomial from the coefficient
            GF128Poly base = new GF128Poly(new UBigInt16[]{currentCoefficient});

            // square and multiply, the polynomial wont get bigger
            base = GFPolyPowAction.pow(base, pow);

            // take back the now processed coefficient and save
            result.setCoefficient(i/2, base.getCoefficient(0));
        }

        return result;
    }

}
