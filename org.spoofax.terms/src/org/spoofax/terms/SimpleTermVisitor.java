package org.spoofax.terms;

import java.util.Iterator;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;


/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public abstract class SimpleTermVisitor implements ISimpleTermVisitor {
	
	public final boolean visit(ISimpleTerm term) {
		Iterator<ISimpleTerm> iterator = tryGetListIterator(term); 
		for (int i = 0, max = term.getSubtermCount(); i < max; i++) {
			ISimpleTerm child = iterator == null ? term.getSubterm(i) : iterator.next();
			preVisit(child);
			boolean isDone = visit(child);
			postVisit(child);
			if (isDone || isDone(null)) return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static Iterator<ISimpleTerm> tryGetListIterator(ISimpleTerm term) {
		boolean isList = term.isList();
		if (isList) {
			if (term instanceof Iterable) {
				return ((Iterable<ISimpleTerm>) term).iterator();
			} else if (term instanceof IStrategoList) {
				Iterator<?> iterator = StrategoListIterator.iterable((IStrategoList) term).iterator();
				return (Iterator<ISimpleTerm>) iterator;
			}
		}
		return null;
	}
	
	public void postVisit(ISimpleTerm node) {
		// No default implementation
	}
	
	public boolean isDone(ISimpleTerm term) {
		return false;
	}
}

//Local interface avoids abstract method and subsequent @Override annotation requirement

interface ISimpleTermVisitor {
	void preVisit(ISimpleTerm term);

	void postVisit(ISimpleTerm term);
	
	boolean isDone(ISimpleTerm term);
}