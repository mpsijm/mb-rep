package org.spoofax.interpreter.library.language.spxlang;

import java.net.URI;

import org.spoofax.interpreter.terms.IStrategoTerm;

/*
 * This interface defines Scope for semantic index.
 * */
interface Scope 
{
	public IStrategoTerm getScopeId();
	
	public Scope getEnclosingScope();
	
	public IStrategoTerm getType();
	
	public URI getScopeURI();
	
	//TODO : Add Scope specific Define and Resolve 
	//
}