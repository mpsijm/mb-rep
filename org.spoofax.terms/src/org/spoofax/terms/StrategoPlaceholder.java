package org.spoofax.terms;

import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoPlaceholder;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoPlaceholder extends StrategoAppl implements IStrategoPlaceholder {

    public StrategoPlaceholder(IStrategoConstructor ctor, IStrategoTerm template, IStrategoList annotations, int storageType) {
        super(ctor, new IStrategoTerm[] { template }, annotations, storageType);
    }
    
    public IStrategoTerm getTemplate() {
        return getSubterm(0);
    }
    
    @Override
    public int getTermType() {
        return PLACEHOLDER;
    }

    @Override
    public String toString() {
        return "<" + getTemplate() + ">";
    }
    
    @Override
    public void prettyPrint(ITermPrinter pp) {
        pp.print("<");
        getTemplate().prettyPrint(pp);
        pp.print(">");
    }
}