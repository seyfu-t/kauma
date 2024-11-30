package me.seyfu_t.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import me.seyfu_t.actions.Block2PolyAction;

// implementing Comparable allows Java's sort() to be used in the context of Lists
public class GF128Poly implements Comparable<GF128Poly>{

    public static final GF128Poly DEGREE_ZERO_POLY_ONE = new GF128Poly(new UBigInt16[] {
            UBigInt16.One(true)
    });
    public static final GF128Poly DEGREE_ONE_POLY_ONE = new GF128Poly(new UBigInt16[] {
            UBigInt16.Zero(true), UBigInt16.One(true)
    });

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
            this.coefficients.add(UBigInt16.fromBase64(base64Coefficient, gcm));
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
        if (index >= this.totalSize())
            return UBigInt16.Zero(gcm);
        return this.coefficients.get(index);
    }

    public GF128Poly setCoefficient(int index, UBigInt16 coefficient) {
        growToSize(index);
        this.coefficients.set(index, coefficient);
        return this;
    }

    public GF128Poly insertCoefficient(int index, UBigInt16 coefficient) {
        growToSize(this.coefficients.size() + 1);
        this.coefficients.add(index, coefficient);
        return this;
    }

    public boolean isEmpty() {
        if (this.coefficients.isEmpty())
            return true;

        // This is an edge case in the for loop for some reason
        if (this.totalSize() == 1 && this.getCoefficient(0).sameAs(UBigInt16.Zero(gcm)))
            return true;

        for (int i = 0; i < this.coefficients.size(); i++) {
            if (!this.coefficients.get(i).isZero())
                return false;
        }

        return true;
    }

    public boolean isZero() {
        return this.isEmpty();
    }

    public int size() { // this method has ambiguity, totalSize() has not
        int size = this.coefficients.size();
        // return 0 when []
        if (size == 0)
            return size;

        while (this.coefficients.get(size - 1) == null || this.coefficients.get(size - 1).isZero()) {
            // shave off leading zeros except the last
            size--;
            if (size == 0)// return also 0 when [0] or [0,0,0]... etc
                return size;
        }

        return size;
    }

    public int degree() {
        return this.size() - 1;
    }

    public int totalSize() {
        return this.coefficients.size();
    }

    private void growToSize(int size) {
        while (this.coefficients.size() <= size) {
            this.coefficients.add(UBigInt16.Zero(gcm));
        }
    }

    public GF128Poly copy() {
        GF128Poly copy = new GF128Poly();

        for (int i = 0; i < this.coefficients.size(); i++) {
            copy.coefficients.add(this.coefficients.get(i).copy());
        }

        return copy;
    }

    public GF128Poly popLeadingZeros() {
        while (this.coefficients.size() > 1 && this.coefficients.getLast().isZero())
            this.coefficients.removeLast();

        return this;
    }

    public LinkedList<UBigInt16> getCoefficients() {
        return this.coefficients;
    }

    public boolean equals(GF128Poly otherPoly) {
        if (this.size() != otherPoly.size())
            return false;

        if (this.isZero() && otherPoly.isZero())
            return true;

        for (int i = 0; i < (this.totalSize() == 0 ? otherPoly.size() : this.size()); i++) {
            if (!this.getCoefficient(i).equals(otherPoly.getCoefficient(i)))
                return false;
        }

        return true;
    }

    public boolean greaterThan(GF128Poly otherPoly) {
        // If sizes differ, compare sizes
        if (this.size() != otherPoly.size())
            return this.size() > otherPoly.size();

        // Both are effectively zero polynomials
        if (this.isZero() && otherPoly.isZero())
            return false;

        int highestElement = (this.size() - 1 == -1 ? otherPoly.size() - 1 : this.size() - 1);

        // Compare coefficients from highest degree to lowest
        for (int i = highestElement; i >= 0; i--) {
            if (!this.getCoefficient(i).equals(otherPoly.getCoefficient(i)))
                return this.getCoefficient(i).greaterThan(otherPoly.getCoefficient(i));
        }

        // Polynomials are equal
        return false;
    }

    public boolean lessThan(GF128Poly otherPoly) {
        // If sizes differ, compare sizes
        if (this.size() != otherPoly.size())
            return this.size() < otherPoly.size();

        // Both are effectively zero polynomials
        if (this.isZero() && otherPoly.isZero())
            return false;

        int highestElement = (this.size() - 1 == -1 ? otherPoly.size() - 1 : this.size() - 1);
        // Compare coefficients from highest degree to lowest
        for (int i = highestElement; i >= 0; i--) {
            if (!this.getCoefficient(i).equals(otherPoly.getCoefficient(i)))
                return this.getCoefficient(i).lessThan(otherPoly.getCoefficient(i));
        }

        // Polynomials are equal
        return false;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.toBase64Array());
    }

    public String toHumanReadableString() {
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

    @Override
    public int compareTo(GF128Poly other) {
        // Returning 1 means this > other
        // Returning 0 means this = other
        // Returning -1 means this < other
    
        // First compare by degree
        int degreeComparison = Integer.compare(this.degree(), other.degree());
        if (degreeComparison != 0) {
            return degreeComparison;
        }
    
        // If both polynomials are empty (degree == -1)
        if (this.degree() == -1) {
            return 0;
        }
    
        // Compare coefficients from highest to lowest degree
        for (int i = this.degree(); i >= 0; i--) {
            if (this.getCoefficient(i).greaterThan(other.getCoefficient(i))) {
                return 1;
            } else if (other.getCoefficient(i).greaterThan(this.getCoefficient(i))) {
                return -1;
            }
        }
    
        return 0; // Polynomials are equal
    }
    

}