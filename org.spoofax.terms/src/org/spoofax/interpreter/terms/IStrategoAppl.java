/*
 * Created on 15. sep.. 2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
 * 
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.interpreter.terms;


public interface IStrategoAppl extends IStrategoTerm, IStrategoNamed {

    public IStrategoConstructor getConstructor();
    
    @Deprecated
    public IStrategoTerm[] getArguments();

}
