/*
 * Created on 15. sep.. 2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
 * 
 * Licensed under the GNU Lesser General Public License, v2.1
 */
package org.spoofax.interpreter.terms;

/**
 * A Stratego Int term.
 */
public interface IStrategoInt extends IStrategoTerm {

    // FIXME: Should be named getValue() for Kotlin compatibility
    /**
     * Gets the value of this term.
     *
     * @return the value
     */
    int intValue();

    @Deprecated
    boolean isUniqueValueTerm();
}
