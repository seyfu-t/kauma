package me.seyfu_t.model;

import java.util.LinkedList;
import java.util.List;

import me.seyfu_t.actions.Block2PolyAction;

public class GF128Poly {
    private final LinkedList<UBigInt16> coefficients = new LinkedList<>();
    private final boolean gcm = true;

    public GF128Poly() {
    }

    public GF128Poly(UBigInt16[] coefficients) {
        for (int i = 0; i < coefficients.length; i++) {
            this.coefficients.add(i, coefficients[i]);
        }
    }

    public GF128Poly(List<UBigInt16> coefficients) {
        for (int i = 0; i < coefficients.size(); i++) {
            this.coefficients.add(i, coefficients.get(i));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
    
        for (int i = 0; i < this.coefficients.size(); i++) {
            UBigInt16 coefficient = this.coefficients.get(i);
    
            if (coefficient == null || coefficient.isZero()) {
                continue; // Skip zero coefficients
            }
    
            // Convert the field element to its polynomial representation
            int[] elementPoly = Block2PolyAction.convertBlock2Poly(coefficient.toBase64(), gcm);
    
            // Build the coefficient representation
            StringBuilder coefStr = new StringBuilder();
            if (elementPoly.length == 1) {
                // Single element in the polynomial representation
                coefStr.append("a");
                if (elementPoly[0] != 1) {
                    coefStr.append("^").append(elementPoly[0]);
                }
            } else if (elementPoly.length > 1) {
                // Multi-term polynomial representation
                coefStr.append("(");
                for (int j = 0; j < elementPoly.length; j++) {
                    if (j > 0) {
                        coefStr.append(" + ");
                    }
                    coefStr.append("a");
                    if (elementPoly[j] != 1) {
                        coefStr.append("^").append(elementPoly[j]);
                    }
                }
                coefStr.append(")");
            }
    
            // Append the coefficient and variable (X) term
            if (sb.length() > 0) {
                sb.append(" + ");
            }
            sb.append(coefStr);
    
            // Append the power of X
            if (i > 0) {
                sb.append("X");
                if (i > 1) {
                    sb.append("^").append(i);
                }
            }
        }
    
        // Return "0" if no terms were added
        return sb.length() > 0 ? sb.toString() : "0";
    }
    

}