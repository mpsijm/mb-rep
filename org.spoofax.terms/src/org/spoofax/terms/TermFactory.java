package org.spoofax.terms;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoPlaceholder;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.StringInterner;
import org.spoofax.terms.util.TermUtils;

import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;

public class TermFactory extends AbstractTermFactory implements ITermFactory {
    private static final int MAX_POOLED_STRING_LENGTH = 200;
    private static final StringInterner usedStrings = new StringInterner();

    private IStrategoConstructor placeholderConstructor;


    public TermFactory() {
        super();
    }

    @Override
    public IStrategoAppl makeAppl(IStrategoConstructor ctr, IStrategoTerm[] terms, IStrategoList annotations) {
        assert ctr.getArity() == terms.length;
        return new StrategoAppl(ctr, terms, annotations);
    }

    public IStrategoInt makeInt(int i) {
        return new StrategoInt(i, null);
    }

    @Override
    public IStrategoList makeList() {
        return new StrategoArrayList();
    }

    @Override
    public IStrategoList makeList(IStrategoTerm[] terms, IStrategoList outerAnnos) {
        return new StrategoArrayList(terms, outerAnnos);
    }

    @Override
    public IStrategoList makeListCons(IStrategoTerm head, IStrategoList tail, IStrategoList annotations) {
        if(head == null)
            return makeList();
        return new StrategoList(head, tail, annotations);
    }

    public IStrategoReal makeReal(double d) {
        return new StrategoReal(d, null);
    }

    public IStrategoString makeString(String s) {
        if(s.length() <= MAX_POOLED_STRING_LENGTH) {
            synchronized(usedStrings) {
                s = usedStrings.intern(s);
            }
        }
        return new StrategoString(s, null);
    }

    public IStrategoString tryMakeUniqueString(String s) {
        synchronized(usedStrings) {
            if(usedStrings.contains(s)) {
                return null;
            } else if(s.length() > MAX_POOLED_STRING_LENGTH) {
                throw new UnsupportedOperationException("String too long to be pooled (newname not allowed): " + s);
            } else {
                return makeString(s);
            }
        }
    }

    @Override
    public IStrategoTuple makeTuple(IStrategoTerm[] terms, IStrategoList annos) {
        return new StrategoTuple(terms, annos);
    }

    public IStrategoTerm annotateTerm(IStrategoTerm term, IStrategoList annotations) {
        IStrategoList currentAnnos = term.getAnnotations();
        if(currentAnnos == annotations) { // cheap check
            return term;
        } else if(annotations.isEmpty() && TermUtils.isString(term)) {
            return makeString(((IStrategoString) term).stringValue());
        } else if(term instanceof StrategoTerm) {
            StrategoTerm result = ((StrategoTerm) term).clone(true);
            result.internalSetAnnotations(annotations);
            return result;
        } else {
            throw new UnsupportedOperationException(
                "Unable to annotate term of type " + term.getClass().getName() + " in " + getClass().getName());
        }
    }

    public IStrategoPlaceholder makePlaceholder(IStrategoTerm template) {
        if(placeholderConstructor == null)
            placeholderConstructor = makeConstructor("<>", 1);
        return new StrategoPlaceholder(placeholderConstructor, template, TermFactory.EMPTY_LIST);
    }
}
