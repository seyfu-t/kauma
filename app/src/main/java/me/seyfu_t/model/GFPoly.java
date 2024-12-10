package me.seyfu_t.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.seyfu_t.actions.basic.Block2PolyAction;

// implementing Comparable allows Java's sort() to be used in the context of Lists
public class GFPoly implements Comparable<GFPoly> {

    public static final GFPoly DEGREE_ZERO_POLY_ONE = new GFPoly().setCoefficient(0, FieldElement.One());
    public static final GFPoly DEGREE_ONE_POLY_ONE = new GFPoly().setCoefficient(1, FieldElement.One());
    public static final GFPoly ZERO_POLY = new GFPoly().setCoefficient(0, FieldElement.Zero());

    private final List<FieldElement> coefficients = new ArrayList<>();
    private final boolean gcm = true;

    /*
     * Constructors
     */

    public GFPoly() {
    }

    public GFPoly(FieldElement[] coefficients) {
        growToSize(coefficients.length - 1);
        for (int i = 0; i < coefficients.length; i++)
            this.coefficients.set(i, coefficients[i]);
    }

    public GFPoly(String[] base64Array) {
        for (String base64Coefficient : base64Array)
            this.coefficients.add(FieldElement.fromBase64GCM(base64Coefficient));
    }

    public GFPoly copy() {
        GFPoly copy = new GFPoly();

        for (int i = 0; i < this.coefficients.size(); i++)
            copy.coefficients.add(this.coefficients.get(i)); // copy could be needed

        return copy;
    }

    /*
     * Getter
     */

    public FieldElement getCoefficient(int index) {
        if (index >= this.totalSize())
            return FieldElement.Zero();
        return this.coefficients.get(index);
    }

    public boolean isZero() {
        if (this.coefficients.isEmpty())
            return true;

        // This is an edge case in the for loop for some reason
        if (this.totalSize() == 1 && this.getCoefficient(0).equals(FieldElement.Zero()))
            return true;

        for (int i = 0; i < this.coefficients.size(); i++) {
            if (!this.coefficients.get(i).isZero())
                return false;
        }

        return true;
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

    /*
     * Method that explicitly change inner state
     */

    public GFPoly setCoefficient(int index, FieldElement coefficient) {
        growToSize(index);
        this.coefficients.set(index, coefficient);
        return this;
    }

    public GFPoly popLeadingZeros() {
        while (this.coefficients.size() > 1 && this.coefficients.get(this.coefficients.size() - 1).isZero())
            this.coefficients.remove(this.coefficients.size() - 1);

        return this;
    }

    private void growToSize(int size) {
        while (this.coefficients.size() <= size)
            this.coefficients.add(FieldElement.Zero());
    }

    /*
     * Converter
     */

    @Override
    public String toString() {
        return Arrays.toString(this.toBase64Array());
    }

    public String[] toBase64Array() {
        String[] array = new String[this.coefficients.size()];

        for (int i = 0; i < this.coefficients.size(); i++)
            array[i] = this.coefficients.get(i).toBase64GCM();

        return array;
    }

    public String toHumanReadableString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.coefficients.size(); i++) {
            FieldElement coefficient = this.coefficients.get(i);

            if (coefficient == null || coefficient.isZero())
                continue; // Skip zero coefficients

            // Convert the field element to its polynomial representation
            int[] elementPoly = Block2PolyAction.block2Poly(coefficient.toBase64GCM(), gcm);

            // Build the coefficient representation
            StringBuilder coefStr = new StringBuilder();
            if (elementPoly.length == 1) {
                // Single element in the polynomial representation
                coefStr.append("a");
                if (elementPoly[0] != 1)
                    coefStr.append("^").append(elementPoly[0]);

            } else if (elementPoly.length > 1) {
                // Multi-term polynomial representation
                coefStr.append("(");
                for (int j = 0; j < elementPoly.length; j++) {
                    if (j > 0)
                        coefStr.append(" + ");

                    coefStr.append("a");
                    if (elementPoly[j] != 1)
                        coefStr.append("^").append(elementPoly[j]);

                }
                coefStr.append(")");
            }

            // Append the coefficient and variable (X) term
            if (sb.length() > 0)
                sb.append(" + ");

            sb.append(coefStr);

            // Append the power of X
            if (i > 0) {
                sb.append("X");
                if (i > 1)
                    sb.append("^").append(i);

            }
        }

        // Return "0" if no terms were added
        return sb.length() > 0 ? sb.toString() : "0";
    }

    /*
     * Compare methods
     */

    public boolean equals(GFPoly otherPoly) {
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

    public boolean greaterThan(GFPoly otherPoly) {
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

    public boolean lessThan(GFPoly otherPoly) {
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
    public int compareTo(GFPoly other) {
        // Returning 1 means this > other
        // Returning 0 means this = other
        // Returning -1 means this < other

        int thisDegree = this.degree();

        // First compare by degree
        int degreeComparison = Integer.compare(thisDegree, other.degree());
        if (degreeComparison != 0)
            return degreeComparison;

        // If both polynomials are empty (degree == -1)
        if (thisDegree == -1)
            return 0;

        // Compare coefficients from highest to lowest degree
        for (int i = thisDegree; i >= 0; i--) {
            FieldElement thisCoefficient = this.getCoefficient(i);
            FieldElement otherCoefficient = other.getCoefficient(i);

            if (thisCoefficient.greaterThan(otherCoefficient))
                return 1;
            else if (otherCoefficient.greaterThan(thisCoefficient))
                return -1;
        }

        return 0; // Polynomials are equal
    }

}