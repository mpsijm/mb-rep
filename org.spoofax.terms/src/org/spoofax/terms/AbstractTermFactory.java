package org.spoofax.terms;

import org.spoofax.interpreter.terms.*;
import org.spoofax.terms.attachments.ITermAttachment;

import java.util.Collection;
import java.util.HashMap;

public abstract class AbstractTermFactory implements ITermFactory {

    @Deprecated
    public static final IStrategoList EMPTY_LIST = new StrategoList(null);

    /** @deprecated Use {@link #EMPTY_TERM_ARRAY} */
    @Deprecated
    public static final IStrategoTerm[] EMPTY = new IStrategoTerm[0];
    /** An empty array of terms. Use this instead of `new IStrategoTerm[0]` to avoid allocating a new array. */
    public static final IStrategoTerm[] EMPTY_TERM_ARRAY = new IStrategoTerm[0];


    private static final HashMap<StrategoConstructor, StrategoConstructor> asyncCtorCache =
        new HashMap<StrategoConstructor, StrategoConstructor>();
    private final StringTermReader reader = new StringTermReader(this);

    static StrategoConstructor createCachedConstructor(String name, int arity) {
        StrategoConstructor result = new StrategoConstructor(name, arity);
        synchronized(TermFactory.class) {
            StrategoConstructor cached = asyncCtorCache.get(result);
            if(cached == null) {
                asyncCtorCache.put(result, result);
            } else {
                result = cached;
            }
        }
        return result;
    }

    public StrategoConstructor makeConstructor(String name, int arity) {
        return createCachedConstructor(name, arity);
    }

    public abstract IStrategoAppl makeAppl(IStrategoConstructor constructor, IStrategoTerm[] kids,
        IStrategoList annotations);

    public abstract IStrategoTuple makeTuple(IStrategoTerm[] kids, IStrategoList annotations);

    public abstract IStrategoList makeList(IStrategoTerm[] kids, IStrategoList annotations);

    public IStrategoAppl replaceAppl(IStrategoConstructor constructor, IStrategoTerm[] kids, IStrategoAppl old) {
        return makeAppl(constructor, kids, old.getAnnotations());
    }

    public IStrategoTuple replaceTuple(IStrategoTerm[] kids, IStrategoTuple old) {
        return makeTuple(kids, old.getAnnotations());
    }

    public IStrategoList replaceList(IStrategoTerm[] kids, IStrategoList old) {
        return makeList(kids, old.getAnnotations());
    }

    public IStrategoList replaceListCons(IStrategoTerm head, IStrategoList tail, IStrategoTerm oldHead,
        IStrategoList oldTail) {
        return makeListCons(head, tail);
    }

    public IStrategoTerm replaceTerm(IStrategoTerm term, IStrategoTerm old) {
        return term;
    }

    public final IStrategoAppl makeAppl(IStrategoConstructor ctr, IStrategoList kids, IStrategoList annotations) {
        return makeAppl(ctr, kids.getAllSubterms(), annotations);
    }

    public final IStrategoAppl makeAppl(IStrategoConstructor ctr, IStrategoList kids) {
        return makeAppl(ctr, kids, null);
    }

    public final IStrategoAppl makeAppl(IStrategoConstructor ctr, IStrategoTerm... terms) {
        return makeAppl(ctr, terms, null);
    }

    public final IStrategoList makeList(IStrategoTerm... terms) {
        return makeList(terms, null);
    }

    public IStrategoList makeList() {
        return makeList(EMPTY, null);
    }

    public IStrategoList makeList(Collection<? extends IStrategoTerm> terms) {
        return makeList(terms.toArray(new IStrategoTerm[terms.size()]));
    }

    public final IStrategoList makeListCons(IStrategoTerm head, IStrategoList tail) {
        return makeListCons(head, tail, null);
    }

    public abstract IStrategoList makeListCons(IStrategoTerm head, IStrategoList tail, IStrategoList annos);

    public final IStrategoTuple makeTuple(IStrategoTerm... terms) {
        return makeTuple(terms, null);
    }

    public IStrategoTerm parseFromString(String text) throws ParseError {
        return reader.parseFromString(text);
    }

    public IStrategoTerm copyAttachments(IStrategoTerm from, IStrategoTerm to) {
        ITermAttachment attach = from.getAttachment(null);
        while(attach != null) {
            try {
                to.putAttachment(attach.clone());
            } catch(CloneNotSupportedException e) {
                throw new IllegalArgumentException(
                    "Copying attachments of this type is not supported: " + attach.getAttachmentType(), e);
            }
            attach = attach.getNext();
        }
        return to;
    }
}
