package org.metaborg.unification.terms;

import org.metaborg.unification.IPrimitiveTerm;
import org.metaborg.unification.ITermFunction;
import org.metaborg.unification.ITermPredicate;

public final class IntTerm implements IPrimitiveTerm {

    private static final long serialVersionUID = -5101998574209587501L;

    private final int value;
    private final int hashCode;

    public IntTerm(int value) {
        this.value = value;
        this.hashCode = calcHashCode();
    }

    public int getValue() {
        return value;
    }

    @Override public boolean isGround() {
        return true;
    }

    @Override public <T> T apply(ITermFunction<T> visitor) {
        return visitor.apply(this);
    }

    @Override public boolean test(ITermPredicate predicate) {
        return predicate.test(this);
    }

    private int calcHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override public int hashCode() {
        return hashCode;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntTerm other = (IntTerm) obj;
        if (value != other.value)
            return false;
        return true;
    }

    @Override public java.lang.String toString() {
        return Integer.toString(value);
    }

}