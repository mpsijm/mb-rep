package org.spoofax.interpreter.library.index.primitives;

import static org.spoofax.interpreter.core.Tools.isTermAppl;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.IndexEntry;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class LANG_index_get_children extends AbstractPrimitive {
	private static String NAME = "LANG_index_get_children";

	public LANG_index_get_children() {
		super(NAME, 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if(isTermAppl(tvars[0])) {
			final IIndex ind = IndexManager.getInstance().getCurrent();
			final IStrategoAppl template = (IStrategoAppl) tvars[0];
			final Iterable<IndexEntry> entries = ind.getChildren(template);
			env.setCurrent(IndexEntry.toTerms(env.getFactory(), entries));
			return true;
		} else {
			return false;
		}
	}
}