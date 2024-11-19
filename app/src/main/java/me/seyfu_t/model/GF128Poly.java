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
        growToSize(coefficients.length);
        for (int i = 0; i < coefficients.length; i++) {
            this.coefficients.set(i, coefficients[i]);
        }
    }

    public GF128Poly(List<UBigInt16> coefficients) {
        growToSize(coefficients.size());
        for (int i = 0; i < coefficients.size(); i++) {
            this.coefficients.set(i, coefficients.get(i));
        }
    }

    public GF128Poly(String[] base64Array) {
        for (String base64Coefficient : base64Array) {
            this.coefficients.add(UBigInt16.fromBase64(base64Coefficient));
        }
    }

    public String[] toBase64Array() {
        String[] array = new String[this.coefficients.size()];

        for (int i = 0; i < this.coefficients.size(); i++) {
            array[i] = this.coefficients.get(i).toBase64();
        }

        return array;
    }

    public UBigInt16 getCoefficient(int index) {
        return this.coefficients.get(index);
    }

    public void setCoefficient(int index, UBigInt16 coefficient) {
        growToSize(index);
        this.coefficients.set(index, coefficient);
    }

    public void insertCoefficient(int index, UBigInt16 coefficient) {
        growToSize(this.coefficients.size() + 1);
        this.coefficients.add(index, coefficient);
    }

    public boolean isEmpty() {
        if (this.coefficients.isEmpty())
            return true;

        for (int i = 0; i < this.coefficients.size(); i++) {
            if (this.coefficients.get(i) != null)
                return false;
        }

        return true;
    }

    public int getDegree() {
        int size = this.coefficients.size();
        while (this.coefficients.get(size - 1) == null || this.coefficients.get(size - 1).isZero())
            size--;
        return size;
    }

    private void growToSize(int size) {
        while (this.coefficients.size() <= size) {
            this.coefficients.add(null);
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