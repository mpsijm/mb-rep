package org.spoofax.interpreter.library.language.spxlang.index;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.applAt;
import static org.spoofax.interpreter.core.Tools.stringAt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.language.spxlang.index.data.IdentifiableConstruct;
import org.spoofax.interpreter.library.language.spxlang.index.data.LanguageDescriptor;
import org.spoofax.interpreter.library.language.spxlang.index.data.ModuleDeclaration;
import org.spoofax.interpreter.library.language.spxlang.index.data.ModuleDefinition;
import org.spoofax.interpreter.library.language.spxlang.index.data.PackageDeclaration;
import org.spoofax.interpreter.library.language.spxlang.index.data.SpxCompilationUnitInfo;
import org.spoofax.interpreter.library.language.spxlang.index.data.SpxSymbol;
import org.spoofax.interpreter.library.language.spxlang.index.data.SpxSymbolTableEntry;
import org.spoofax.interpreter.library.language.spxlang.index.data.SpxSymbolTableException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.TermConverter;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.StrategoListIterator;
import org.spoofax.terms.attachments.TermAttachmentSerializer;
import org.spoofax.terms.attachments.TermAttachmentStripper;

//TODO :  refactor this class  to multiple facades one for package, one for modules 
//TODO FIXME : combine symbol table and index
public class SpxSemanticIndexFacade {
	
	private ISpxPersistenceManager _persistenceManager;
	
	private final String _projectPath ;
	private String _indexId;
	private final ITermFactory _termFactory;
	private final IOAgent _agent;
	private final TermAttachmentStripper _stripper;
	private final TermAttachmentSerializer _termAttachmentSerializer;
	private final TermConverter _converter;
	private final SpxConstructors _spxConstructors;
	
	private long _currentCodeGenerationStratedOn;

	
	/**
	 * Initialises the factory for the semantic index
	 * 
	 * @param projectPath name of the project 
	 * @param termFactory {@link ITermFactory}
	 * @param agent {@link IOAgent}
	 * @throws Exception 
	 */
	public SpxSemanticIndexFacade(IStrategoTerm projectPath , ITermFactory termFactory , IOAgent agent){
		_projectPath = asJavaString(projectPath);
		_indexId = getProjectName() + "";
		_termFactory = termFactory;
		_agent = agent;
		
		_stripper = new TermAttachmentStripper(_termFactory);
		_converter = new TermConverter(_termFactory);
		_converter.setOriginEnabled(true);
		
		_termAttachmentSerializer = new TermAttachmentSerializer(_termFactory);
		
		_spxConstructors = new SpxConstructors(_termFactory);
	}
	
	public synchronized void initializePersistenceManager() throws Exception {
		_persistenceManager = new SpxPersistenceManager(this);
		_persistenceManager.initializeSymbolTables(this._projectPath, this);
		_indexId = _persistenceManager.getIndexId();
		
		
	}
	
	protected void finalize() throws Throwable {
		try {
			close(true);
		} catch (Exception e) {
		}
		finally {
			super.finalize();
		}
	}
	
	public SpxConstructors getCons(){ return _spxConstructors;}
	
	public TermAttachmentSerializer getTermAttachmentSerializer() {
		return _termAttachmentSerializer;
	}
	
	public String indexId() {return _indexId; }
	/**
	 * Returns the TermFactory 
	 * @return
	 */
	public ITermFactory getTermFactory() { return _termFactory; }

	public TermConverter getTermConverter() {return _converter ; }
	
	/**
	 * Gets the project name as String
	 * @return
	 */
	public String getProjectPath(){ return Utils.toAbsPathString(_projectPath); }
	
	public String getProjectName(){ return new File(_projectPath).getName(); }

	/**
	 * Returns an instance of the Persistence Manager active for the current Facade
	 * @return
	 */
	public ISpxPersistenceManager persistenceManager(){	return _persistenceManager; }

	public void onInitCodeGeneration(){
		this._currentCodeGenerationStratedOn = System.currentTimeMillis(); 
	}
	
	public void onCompleteCodeGeneration() { 
		this.persistenceManager().spxSymbolTable().setLastCodeGeneratedOn(this._currentCodeGenerationStratedOn);
	}
	
	/**
	 * Returns CompilationUnit located in {@code spxCompilationUnitPath} as {@link IStrategoTerm}
	 * 
	 * @param spxCompilationUnitPath Location to the CompilationUnit
	 * @return {@link IStrategoTerm} 
	 */
	public IStrategoTerm getCompilationUnit(IStrategoString spxCompilationUnitPath){
		IStrategoAppl retTerm = null; 
		URI resUri = toFileURI(spxCompilationUnitPath);
		
		logMessage("SpxSemanticIndexFacade.getCompilationUnit . Arguments : " + spxCompilationUnitPath);
		
		SpxCompilationUnitTable table = _persistenceManager.spxCompilcationUnitTable();
		
		IStrategoAppl term = (IStrategoAppl)table.get(this, resUri);
		
		if ( term != null)
			retTerm = forceImploderAttachment(term, resUri);
		
		logMessage("SpxSemanticIndexFacade.getCompilationUnit :  Returning Following APPL for uri " + resUri +  " : "+ retTerm);
		
		return retTerm;
	}
	
	public SpxCompilationUnitInfo getCompilationUnitInfo(String absUriPath){
		URI resUri = Utils.getAbsolutePathUri(absUriPath ,_agent);
		
		SpxCompilationUnitTable table = _persistenceManager.spxCompilcationUnitTable();
		return table.getInfo(this, resUri);
	}

	/**
	 * Removes CompilationUnit located in {@code spxCompilationUnitPath} file path.  
	 * 
	 * @param spxCompilationUnitPath file path
	 * @throws IOException
	 */
	public void removeCompilationUnit( IStrategoString spxCompilationUnitPath ) throws IOException{
		URI resUri = toFileURI(spxCompilationUnitPath);
		
		SpxCompilationUnitTable table = _persistenceManager.spxCompilcationUnitTable();
		
		table.remove(resUri);
	}
	
	/**
	 * Indexes CompilationUnit in the symbol table.
	 * 
	 * @param spxCompilationUnitPath path of the SpxCompilation Unit. It can be a relative path (  relative to project) or absolute path. 
	 * @param spxCompilationUnitAST SPXCompilationUnit AST 
	 * @throws IOException 
	 */
	public void indexCompilationUnit( IStrategoString spxCompilationUnitPath, IStrategoAppl spxCompilationUnitAST) throws IOException {

		URI resUri = toFileURI(spxCompilationUnitPath); // Converting IStrategoString to File URI 
		
		IStrategoTerm astTerm = toCompactPositionInfo(spxCompilationUnitAST);

		SpxCompilationUnitTable table = _persistenceManager.spxCompilcationUnitTable();
	
		logMessage("Storing following compilation unit. Path : ["+  spxCompilationUnitPath +"]");
		
		table.define(this, resUri, astTerm);
	}
	
	/**
	 * Indexes {@code moduleDefinition}
	 * 
	 * @param moduleDefinition
	 * @throws IllegalArgumentException
	 * @throws IOException 
	 */
	public void indexModuleDefinition(IStrategoAppl moduleDefinition) throws IllegalArgumentException, IOException
	{
		verifyConstructor(moduleDefinition.getConstructor() , getCons().getModuleDefCon() , "Illegal Module Definition" );
		
		indexModuleDefinition( 
				applAt(moduleDefinition ,  ModuleDeclaration.ModuleTypedQNameIndex),
				stringAt(moduleDefinition, ModuleDeclaration.ModulePathIndex),
				applAt(moduleDefinition ,  ModuleDeclaration.PackageTypedQNameIndex),
				applAt(moduleDefinition ,  ModuleDeclaration.AstIndex),
				applAt(moduleDefinition ,  ModuleDeclaration.AnalyzedAstIndex));
	}

	/**
	 * Indexes Module Definition, e.g. ModuleDef :  Module * String * Package * Term * Term -> Def
	 * 
	 * @param moduleQName
	 * @param spxCompilationUnitPath
	 * @param packageQName
	 * @param ast
	 * @param analyzedAst
	 * @throws IOException 
	 */
	public void indexModuleDefinition(IStrategoAppl moduleQName,
			IStrategoString spxCompilationUnitPath, 
			IStrategoAppl packageQName,
			IStrategoAppl ast, 
			IStrategoAppl analyzedAst) throws IOException {

		SpxModuleLookupTable table = _persistenceManager.spxModuleTable();

		IStrategoList moduleId = ModuleDeclaration.getModuleId( this, moduleQName);
		IStrategoList packageId = PackageDeclaration.getPackageId(this, packageQName);
		
		_persistenceManager.spxPackageTable().verifyPackageIDExists(packageId) ;
		
		moduleId = (IStrategoList) strip(moduleId);
		packageId = (IStrategoList) strip(packageId);
		ast = (IStrategoAppl) ast;
		analyzedAst = (IStrategoAppl)analyzedAst;
		spxCompilationUnitPath = (IStrategoString) spxCompilationUnitPath;

		SpxCompilationUnitInfo info =  getCompilationUnitInfo(toAbsulatePath(spxCompilationUnitPath));
		
		if(info ==null){ throw new IllegalStateException("Related compilation unit is no longer exists in symbols table ");}
		
		ModuleDeclaration mDecl = new ModuleDeclaration(info.getAbsPathString(), moduleId, packageId);
		mDecl.setLastModifiedOn(info.getLastModifiedOn()) ;
		
		table.define(this , mDecl, ast, analyzedAst);// updating/adding module to index 
		
		//Defining ModuleNamespace for Symbol-Table
		defineNamespace(mDecl);
	}

	/**
	 * Stores PackageDeclaration in Symbol Table 
	 * 
	 * @param packageDeclaration
	 */
	public void indexPackageDeclaration(IStrategoAppl packageDeclaration){
		verifyConstructor( packageDeclaration.getConstructor(), getCons().getPackageDeclCon(), "Illegal PackageDeclaration");
	
		indexPackageDeclaration(
				(IStrategoAppl)  packageDeclaration.getSubterm(PackageDeclaration.PACKAGE_ID_INDEX), // package id
				(IStrategoString)packageDeclaration.getSubterm(PackageDeclaration.SPX_COMPILATION_UNIT_PATH)  // package location absolute path  
		);
	}
	
	/**
	 * Indexes {@link PackageDeclaration}
	 * 
	 * @param packageIdAppl 
	 * @param spxCompilationUnitPath
	 */
	public void indexPackageDeclaration(IStrategoAppl packageIdAppl, IStrategoString spxCompilationUnitPath){
		
		SpxPackageLookupTable table = _persistenceManager.spxPackageTable();
		SpxCompilationUnitTable spxTable = _persistenceManager.spxCompilcationUnitTable();
		
		IStrategoList packageId = PackageDeclaration.getPackageId(this, packageIdAppl);
		
		//verify valid package URI. Checking whether compilation unit exist with this URI
		// in compilation unit table.
		spxCompilationUnitPath  = (IStrategoString)toCompactPositionInfo((IStrategoTerm)spxCompilationUnitPath);
		String absFilePath = this.toAbsulatePath(spxCompilationUnitPath);
		spxTable.verifyUriExists(absFilePath);

		packageId = (IStrategoList)toCompactPositionInfo((IStrategoTerm)packageId);
		
		
		if(table.containsPackage(packageId)){
			// Package is already there in the index. Hence,just adding the Uri where 
			// this package declaration is found.
			table.addPackageDeclarationLocation(packageId,absFilePath);
 			
		}else{	
			// Defining PackageDeclaration in the Index
			PackageDeclaration pDecl = new PackageDeclaration(absFilePath,packageId);
			table.definePackageDeclaration(pDecl);
			
			defineNamespace(pDecl); 
		}
	}

	public IStrategoTerm insertNewScope(IStrategoAppl namespaceAppl) throws SpxSymbolTableException {
		
		IStrategoList parentId = getNamespaceId(namespaceAppl);
		
		SpxPrimarySymbolTable  symbolTable = persistenceManager().spxSymbolTable();
		INamespace ns = symbolTable.newAnonymousNamespace(parentId);
		
		return this.getTermFactory().makeAppl(getCons().getLocalNamespaceTypeCon(), ns.namespaceUri().id());
	}
	
	public IStrategoTerm destroyScope(IStrategoAppl namespaceAppl) throws SpxSymbolTableException {
		verifyConstructor( namespaceAppl.getConstructor(), this.getCons().getLocalNamespaceTypeCon(), "Expected LocalNamespace. This operation has not been implementated for other type of Namespace.");
		
		IStrategoList id = getNamespaceId(namespaceAppl);
		
		SpxPrimarySymbolTable  symbolTable = persistenceManager().spxSymbolTable();
		INamespace deletedLocalNs = symbolTable.destroyNamespace(id);
		
		return _termFactory.makeAppl(getCons().getLocalNamespaceTypeCon(), deletedLocalNs.namespaceUri().id());
	}
	
	// SymbolDef : namespace * id * type *  value -> Def  
	public void indexSymbol(IStrategoAppl symbolDefinition) throws SpxSymbolTableException, IOException{	
		final int NAMESPACE_ID_INDEX  = 0;
		verifyConstructor(symbolDefinition.getConstructor(), getCons().getSymbolTableEntryDefCon(), "Illegal SymbolDefinition argument");
		IStrategoConstructor typeCtor = null;
		
		try{
			typeCtor = verifyKnownContructorExists((IStrategoAppl)symbolDefinition.getSubterm(SpxSymbolTableEntry.TYPE_INDEX));
		}catch(IllegalArgumentException ex){
			// It seems like the constructor does not exist in local type declarations. 
			// Hence, defining it to be used further.
			IStrategoConstructor ctor = ((IStrategoAppl)symbolDefinition.getSubterm(SpxSymbolTableEntry.TYPE_INDEX)).getConstructor();
			typeCtor = _spxConstructors.indexConstructor(ctor);
		}
		
		// Constructing Spx Symbol-Table Entry from the provided symbolDefinition argument.  
		// Note: TermAttachment or Annotation are stripped from the ID Term since, in symbol-table, term attachments 
		// is not require and will make the equals operation a bit complicated. 
		SpxSymbolTableEntry entry = 
			SpxSymbolTableEntry.newEntry()
						  .with(
								  strip(symbolDefinition.getSubterm(SpxSymbolTableEntry.SYMBOL_ID_INDEX))
						   )
						  .instanceOf(typeCtor)	
					      .uses(this._termAttachmentSerializer)
					      .data(symbolDefinition.getSubterm(SpxSymbolTableEntry.DATA_INDEX))
					      .build();
					   		
		
		SpxPrimarySymbolTable  symbolTable = persistenceManager().spxSymbolTable();
		symbolTable.defineSymbol(getNamespaceId((IStrategoAppl)symbolDefinition.getSubterm(NAMESPACE_ID_INDEX)), entry);
	}
	
	public SpxSymbol verifySymbolExists(IStrategoTuple searchCriteria) throws SpxSymbolTableException {
		if (searchCriteria.getSubtermCount() != 3)
			throw new IllegalArgumentException(" verifySymbolExists | Illegal symbolLookupTerm Argument ; expected 4 subterms. Found : " + searchCriteria.getSubtermCount());
		
		IStrategoAppl typeAppl =  (IStrategoAppl)searchCriteria.getSubterm(2);
		IStrategoConstructor typeCtor = null; 
		try{
			typeCtor = verifyKnownContructorExists(typeAppl);
		}catch(IllegalArgumentException ex){
			// It seems like the constructor does not exist in local type declarations. 
			// Hence, defining it to be used further.
			IStrategoConstructor ctor = (IStrategoConstructor)typeAppl.getConstructor();
			typeCtor = _spxConstructors.indexConstructor(ctor);
		}
		
		IStrategoList namespaceID = this.getNamespaceId((IStrategoAppl)searchCriteria.get(0));
		SpxPrimarySymbolTable  symbolTable = persistenceManager().spxSymbolTable();
		
		return symbolTable.resolveSymbol(namespaceID, strip(searchCriteria.get(1)), typeCtor);
	}
	
	// (namespace * idTolookupFor * type constructor)
	public IStrategoTerm resolveSymbols(IStrategoTuple searchCriteria) throws SpxSymbolTableException{
		if (searchCriteria.getSubtermCount() != 4)
			throw new IllegalArgumentException(" resolveSymbols | Illegal symbolLookupTerm Argument ; expected 4 subterms. Found : " + searchCriteria.getSubtermCount());
		
		String searchMode = asJavaString(searchCriteria.get(3)).trim();
		
		IStrategoAppl typeAppl =  (IStrategoAppl)searchCriteria.getSubterm(2);
		IStrategoConstructor typeCtor = null; 
		try{
			typeCtor = verifyKnownContructorExists(typeAppl);
		}catch(IllegalArgumentException ex){
			// It seems like the constructor does not exist in local type declarations. 
			// Hence, defining it to be used further.
			IStrategoConstructor ctor = (IStrategoConstructor)typeAppl.getConstructor();
			typeCtor = _spxConstructors.indexConstructor(ctor);
		}
		
		Collection<SpxSymbol> spxSymbols = null;
		if (typeCtor != null) {
			if(searchMode.equalsIgnoreCase(Utils.AllWithDuplicates)){
				spxSymbols = resolveSymbols( (IStrategoAppl)searchCriteria.get(0), searchCriteria.get(1),typeCtor, true);
			}
			else if(searchMode.equalsIgnoreCase(Utils.All)){
				spxSymbols = resolveSymbols( (IStrategoAppl)searchCriteria.get(0), searchCriteria.get(1),typeCtor, false);
			}else if(searchMode.equalsIgnoreCase(Utils.ONLY_ONE)){
				spxSymbols = resolveSymbol( (IStrategoAppl)searchCriteria.get(0), searchCriteria.get(1),typeCtor);
			}
			else{
				throw new IllegalArgumentException(" Illegal symbolLookupTerm searchMode Argument ; expected * or . . Found : " + searchMode);
			}
		}else
			this.logMessage("resolve symbols. Unknown Type Contructor "+typeAppl.getConstructor().getName() );
		
		return SpxSymbol.toTerms(this, spxSymbols);
	}
	
	public IStrategoTerm undefineSymbols (IStrategoTuple searchCriteria) throws SpxSymbolTableException{
	    if (searchCriteria.getSubtermCount() != 3)
	        throw new IllegalArgumentException(" undefineSymbols| Illegal symbolLookupTerm Argument ; expected 3 subterms. Found : " + searchCriteria.getSubtermCount());
	    
	    
	    IStrategoList namespaceID = this.getNamespaceId(Tools.applAt(searchCriteria, 0));
	    IStrategoTerm symbolID = Tools.termAt(searchCriteria, 1);
	    IStrategoAppl typeAppl =  Tools.applAt(searchCriteria, 2);
	    IStrategoConstructor typeCtor = null; 
		try{
			typeCtor = verifyKnownContructorExists(typeAppl);
		}catch(IllegalArgumentException ex){
			// It seems like the constructor does not exist in local type declarations. 
			// Hence, defining it to be used further.
			IStrategoConstructor ctor = (IStrategoConstructor)typeAppl.getConstructor();
			typeCtor = _spxConstructors.indexConstructor(ctor);
		}
		
	    Set<SpxSymbol> spxSymbols = this.persistenceManager().spxSymbolTable()
	    					.undefineSymbols(namespaceID, 
	    									 symbolID,
    										 typeCtor);
	                        
	    return SpxSymbol.toTerms(this, spxSymbols);
	}  
	
	public void invalidateGlobalNamespace() {
		SpxPrimarySymbolTable  symbolTable = persistenceManager().spxSymbolTable();
		
		symbolTable.clearGlobalNamespce(this);
		
		
	}
	
	/**
	 * Resolves symbols from {@link SpxPrimarySymbolTable}.
	 * 
	 * @param namespaceToStartSearchWith Starts search from this namespace. 
	 * @param symbolId symbol Id to resolve
	 * @param symbolType Type of Symbols to look for
	 * @param returnDuplicates TODO
	 * @return {@link IStrategoList} representation of resolved {@code symbols} 
	 * 
	 * @throws SpxSymbolTableException
	 */
	public Collection<SpxSymbol> resolveSymbols(IStrategoAppl namespaceToStartSearchWith, IStrategoTerm symbolId, IStrategoConstructor  symbolType, boolean returnDuplicates) throws SpxSymbolTableException {
		IStrategoList namespaceID = this.getNamespaceId(namespaceToStartSearchWith);

		SpxPrimarySymbolTable  symbolTable = persistenceManager().spxSymbolTable();
		
		Collection<SpxSymbol> resolvedSymbols = symbolTable.resolveSymbols(namespaceID, strip(symbolId), symbolType, returnDuplicates);
		return resolvedSymbols;
	}
	
	public Set<SpxSymbol> resolveSymbol(IStrategoAppl namespaceToStartSearchWith, IStrategoTerm symbolId, IStrategoConstructor  symbolType) throws SpxSymbolTableException {
		Set<SpxSymbol> resolvedSymbols= new HashSet<SpxSymbol>();
		
		IStrategoList namespaceID = this.getNamespaceId(namespaceToStartSearchWith);
		SpxPrimarySymbolTable  symbolTable = persistenceManager().spxSymbolTable();
		
		SpxSymbol sym = symbolTable.resolveSymbol(namespaceID, strip(symbolId), symbolType);
		if(sym != null)
			resolvedSymbols.add(sym) ;
		
		return resolvedSymbols;
		
	}

	/**
	 * @param symbolLookupTerm
	 * @return
	 * @throws IllegalArgumentException
	 */
	private IStrategoConstructor verifyKnownContructorExists(IStrategoAppl symbolType) throws IllegalArgumentException {
		IStrategoConstructor typeCtor = getCons().getConstructor( symbolType.getConstructor().getName(), symbolType.getConstructor().getArity()) ;
		if(typeCtor == null) {
			throw new IllegalArgumentException("Illegal Argument . Unknown Symbol Type. Found " + symbolType.getConstructor());
		}
		return typeCtor;
	}

	/**
	 * @param namespaceTypedQname
	 * @return
	 * @throws SpxSymbolTableException
	 */
	private IStrategoList getNamespaceId(IStrategoAppl namespaceTypedQname) throws SpxSymbolTableException {
		IStrategoList namespaceId;
		if (namespaceTypedQname.getConstructor() == getCons().getModuleQNameCon() || namespaceTypedQname.getConstructor() == getCons().getPackageQNameCon()) {
			
			namespaceId = IdentifiableConstruct.getID(this, (IStrategoAppl) namespaceTypedQname.getSubterm(0));
			
		} else if (namespaceTypedQname.getConstructor() == getCons().getGlobalNamespaceTypeCon()) {
			
			namespaceId = GlobalNamespace.getGlobalNamespaceId(this);
			
		} else if ( namespaceTypedQname.getConstructor() == getCons().getLocalNamespaceTypeCon()){
			
			namespaceId = LocalNamespace.getLocalNamespaceId(namespaceTypedQname.getSubterm(0));
		} 
		else
			throw new SpxSymbolTableException("Unknown namespace uri : " + namespaceTypedQname);
		
		return namespaceId;
	}	
	
	/**
	 * Indexes LanguageDescriptor for a particular Package specified in {@code langaugeDescriptor}
	 * 
	 * @param languageDescriptor
	 */
	public void indexLanguageDescriptor (IStrategoAppl languageDescriptor)
	{
		verifyConstructor(languageDescriptor.getConstructor(), getCons().getLanguageDescriptorCon(), "Invalid LanguageDescriptor argument : "+ languageDescriptor.toString());

		IStrategoList moduleId = ModuleDeclaration.getModuleId(this, (IStrategoAppl)languageDescriptor.getSubterm(0)) ;
		SpxModuleLookupTable table = _persistenceManager.spxModuleTable();

		table.verifyModuleIDExists(moduleId);

		//FIXME : move the following logic to extract information and 
		//construct instance in respective classes . e.g. in LanguageDesrciptor class
		moduleId = (IStrategoList)toCompactPositionInfo((IStrategoTerm)moduleId);

		IStrategoList lNames = (IStrategoList) this.strip(languageDescriptor.getSubterm(LanguageDescriptor.LanguageNamesIndex));
		IStrategoList lIds = (IStrategoList) this.strip(languageDescriptor.getSubterm(LanguageDescriptor.LanguageIdsIndex));
		IStrategoList lEsvStartSymbols = (IStrategoList) this.strip(languageDescriptor.getSubterm(LanguageDescriptor.EsvStartSymbolsIndex));
		IStrategoList lSdfStartSymbols = (IStrategoList) this.strip(languageDescriptor.getSubterm(LanguageDescriptor.SdfStartSymbolsIndex));

		LanguageDescriptor current  = LanguageDescriptor.newInstance(this.getTermFactory() , moduleId , lIds, lNames,lSdfStartSymbols , lEsvStartSymbols);
		
		table.defineLanguageDescriptor(moduleId, current);
	}
	
	/**
	 * @param importReferences
	 */
	public void indexImportReferences(IStrategoAppl importReferences) throws SpxSymbolTableException{
		verifyConstructor(importReferences.getConstructor(), getCons().getImportDeclCon(), "Illegal ImportDeclaration Constructor encountered.");
		
		IStrategoAppl namespaceId = (IStrategoAppl) importReferences.getSubterm(0);
		IStrategoList imports = (IStrategoList) importReferences.getSubterm(1);
		IStrategoList packageId; 
		
		if (namespaceId.getConstructor() == getCons().getModuleQNameCon()) {
			packageId = persistenceManager()
					.spxModuleTable()
					.packageId(ModuleDeclaration.getModuleId(this, namespaceId));
			
			
		} else if (namespaceId.getConstructor() == getCons().getPackageQNameCon()) {
			packageId = PackageDeclaration.getPackageId(this, namespaceId);
		} else
			throw new IllegalArgumentException("Unknown Namespace "	+ namespaceId.toString());

		PackageDeclaration packageDeclaration= this.lookupPackageDecl(packageId);
		
		packageDeclaration.addImportRefernces(this, imports);
		persistenceManager().spxPackageTable().definePackageDeclaration(packageDeclaration);
	}
	
	
	/**
	 * @param mDecl
	 */
	private void defineNamespace(INamespaceFactory nsFactory) {
		SpxPrimarySymbolTable symTable =  this.persistenceManager().spxSymbolTable();
		
		for( INamespace ns : nsFactory.newNamespaces(this) ) {  symTable.defineNamespace(ns) ; }
	}
	

	/**
	 * Returning all the import reference of the current package / module construct. Package/ Module  
	 * are the scoped symbol for the current implementation of the spoofaxlang. Whenever 
	 * looking for a import reference of a module, it returns the import refernece of it enclosing 
	 * namespace , i.e. package. 
	 * 
	 * Currently this lookup is hard-coded . Later , plan is to move to more generic and dynamic 
	 * lookup environment. 
	 * 
	 * @param namespaceId
	 * @return {@link IStrategoTerm}
	 * @throws SpxSymbolTableException 
	 */
	public IStrategoTerm getImportReferences(IStrategoAppl namespaceId) throws SpxSymbolTableException {
		IdentifiableConstruct ns; 

		if (namespaceId.getConstructor() == getCons().getModuleQNameCon()) {
			IStrategoList packageId = persistenceManager()
					.spxModuleTable()
					.packageId(ModuleDeclaration.getModuleId(this, namespaceId));
			ns = lookupPackageDecl(packageId);
		} else if (namespaceId.getConstructor() == getCons().getPackageQNameCon()) {
			ns = this.lookupPackageDecl(namespaceId);
		} else
			throw new IllegalArgumentException("Unknown Namespace "	+ namespaceId.toString());
		
		return ns.getImports(this);
	}
	
	

	/**
	 * Returning all the imported to reference of the current package / module construct. Package/ Module  
	 * are the scoped symbol for the current implementation of the spoofaxlang. Whenever 
	 * looking for a importto reference of a module, it returns the import refernece of it enclosing 
	 * namespace , i.e. package. 
	 * 
	 * Currently this lookup is hard-coded . Later , plan is to move to more generic and dynamic 
	 * lookup environment. 
	 * 
	 * @param namespaceId
	 * @return {@link IStrategoTerm} 
	 * @throws SpxSymbolTableException 
	 */
	public IStrategoTerm getImportedToReferences(IStrategoAppl namespaceId) throws SpxSymbolTableException {
		PackageDeclaration ns; 

		if (namespaceId.getConstructor() == getCons().getModuleQNameCon()) {
			IStrategoList packageId = persistenceManager()
					.spxModuleTable()
					.packageId(ModuleDeclaration.getModuleId(this, namespaceId));
			ns = lookupPackageDecl(packageId);
		} else if (namespaceId.getConstructor() == getCons().getPackageQNameCon()) {
			ns = this.lookupPackageDecl(namespaceId);
		} else
			throw new IllegalArgumentException("Unknown Namespace "	+ namespaceId.toString());
		
		return ns.getImportedToReferences(this);
	}
	
	/**
	 * Returns the package declaration indexed with {@code packageIdAppl} typed qualified name.
	 * 
	 * @param packageTypedQName
	 * @return
	 * @throws Exception 
	 */
	public IStrategoTerm getPackageDeclaration(IStrategoAppl packageTypedQName) throws SpxSymbolTableException
	{
		PackageDeclaration decl = lookupPackageDecl(packageTypedQName);
		
		return decl.toTerm(this);
	}

	
	/**
	 * @param packageTypedQName
	 * @return
	 * @throws IllegalArgumentException
	 */
	public PackageDeclaration lookupPackageDecl(IStrategoAppl packageTypedQName) throws SpxSymbolTableException {
		
		IStrategoList packageId = PackageDeclaration.getPackageId(this, packageTypedQName);
		
		return lookupPackageDecl(packageId);
	}
	

	public IStrategoTerm getRelatedFilesOfPackages(IStrategoList packageList) {
		logMessage("getRelatedFilesOfPackages | Arguments : " + packageList);
		
		HashSet<String> resourcePaths = new HashSet<String>();
		
		SpxPackageLookupTable table = persistenceManager().spxPackageTable();
		for (IStrategoTerm packageName: StrategoListIterator.iterable(packageList)) {
			IStrategoAppl packageTypedQName = (IStrategoAppl)packageName;
			IStrategoList packageId = PackageDeclaration.getPackageId(this, packageTypedQName);
			
			PackageDeclaration decl = table.getPackageDeclaration(packageId);
			if(decl!=null){
				resourcePaths.addAll(decl.getAllFilePaths());
			}
		}
		
		logMessage("getRelatedFilesOfPackages | Found : " + resourcePaths);
		IStrategoList result = this.getTermFactory().makeList();
		for (String s : resourcePaths){
			result = getTermFactory().makeListCons(getTermFactory().makeString(s), result);
		}
		
		return result;
	}

	public IStrategoList getPackageDeclarations(IStrategoString filePath){
		logMessage("getPackageDeclarationsByUri | Arguments : " + filePath);
		
		SpxPackageLookupTable table = persistenceManager().spxPackageTable();
		String filepathString = asJavaString(filePath);
		
		Set<PackageDeclaration> decls; 
		if(Utils.All == filepathString) {
			decls = table.getPackageDeclarations();  //returning all the package declarations found in the current project
		}else{
			String absFilePath = toAbsulatePath(filePath);
			table.verifyUriExists(absFilePath); // verifying file path exists 
			decls = table.packageDeclarationsByUri(absFilePath);
		}
		
		IStrategoList result =  Utils.toTerm(this, decls);
		logMessage("getPackageDeclarationsByUri | Returning IStrategoList : " + result );

		return result;
	}
	

	public IStrategoList getPackageDeclarationsByLanguageName(IStrategoString langName) {
		logMessage("getPackageDeclarationsByLanguageName | Arguments : " + langName);
		
		SpxModuleLookupTable table = persistenceManager().spxModuleTable();
		
		Set<IStrategoList> decls  = new HashSet<IStrategoList>(); 
		
		Iterable<IStrategoList> mdecls = table.getModuleIdsByLangaugeName(langName);
		
		for ( IStrategoList mId: mdecls){
			decls.add(table.getModuleDeclaration(mId).enclosingPackageID);
		}
		
		IStrategoList result = getTermFactory().makeList();
		for(IStrategoList  pId : decls){ 
			result  = getTermFactory().makeListCons(PackageDeclaration.toPackageQNameAppl(this, pId), result);
		}
		
		logMessage("getPackageDeclarationsByLanguageName | Returning IStrategoList : " + result );

		return result;
	}
	
	/**
	 * Returns {@link ModuleDeclaration} indexed with Module Id - {@code moduleTypeQName}  
	 * 
	 * @param moduleTypeQName
	 * @return
	 * @throws IllegalArgumentException If {@link ModuleDeclaration} with {@code moduleTypeQName} is not found 
	 * @throws SpxSymbolTableException 
	 */
	public IStrategoTerm getModuleDeclaration(IStrategoAppl moduleTypeQName) throws IllegalArgumentException, SpxSymbolTableException
	{
		ModuleDeclaration decl = lookupModuleDecl(moduleTypeQName);

		return decl.toTerm(this);
	}

	/**
	 * @param moduleTypeQName
	 * @return
	 * @throws SpxSymbolTableException 
	 */
	public ModuleDeclaration lookupModuleDecl(IStrategoAppl moduleTypeQName) throws SpxSymbolTableException {
		
		SpxModuleLookupTable table = persistenceManager().spxModuleTable();
		
		IStrategoList moduleId = ModuleDeclaration.getModuleId(this, moduleTypeQName);
		
		ModuleDeclaration decl = table.getModuleDeclaration(moduleId);
		
		if (decl == null)
			throw new SpxSymbolTableException( "Unknown Module Id "+ moduleTypeQName.toString());
		
		
		return decl;
	}
	
	/**
	 * Returns the {@link IStrategoTerm} representation of the list of {@link ModuleDeclaration}
	 * of the specified File Uri or from the enclosed Package.
	 *  
	 * @param res
	 * @return {@link IStrategoTerm} 
	 * @throws SpxSymbolTableException 
	 */
	public IStrategoTerm getModuleDeclarationsOf(IStrategoTuple searchQueryTuple) throws SpxSymbolTableException {
		
		IStrategoTerm res = searchQueryTuple.getSubterm(0);
		IStrategoString queryType = (IStrategoString)searchQueryTuple.getSubterm(1);
		
		IStrategoTerm retValue ;
		
		if(Tools.isTermAppl(res))
		{	
			if( Utils.DIRTY  == asJavaString(queryType)) {
				retValue  = this.getDirtyModuleDeclarations((IStrategoAppl)res , (IStrategoAppl)searchQueryTuple.getSubterm(2)) ;
			}else if (Utils.All == asJavaString(queryType)){
				retValue  = this.getModuleDeclarations((IStrategoAppl)res);
			}else
				throw new IllegalArgumentException("Unknown queryType argument in getModuleDeclarationOf: " + queryType);
		}	
		else if(Tools.isTermString(res))
			retValue = this.getModuleDeclarations((IStrategoString)res);
		else
			throw new IllegalArgumentException("Unknown argument in getModuleDeclarationOf: " + res);
		
		return retValue;
	}

	public IStrategoList getModuleDeclarations (IStrategoString filePath){
		logMessage("getModuleDeclarations | Arguments : " + filePath);
		
		SpxModuleLookupTable table = persistenceManager().spxModuleTable();
		String filepathString = asJavaString(filePath);
		
		Iterable<ModuleDeclaration> decls; 
		if(Utils.All == filepathString) {
			decls = table.getModuleDeclarations();  //returning all the package declarations found in the current project
		}else{	
			String absFilePath = toAbsulatePath(filePath);
			table.verifyUriExists(absFilePath);
			decls = table.getModuleDeclarationsByUri(absFilePath);
		}
		
		IStrategoList result =  Utils.toTerm(this, decls);
		logMessage("getModuleDeclarations | Returning IStrategoList : " + result );
		return result;
	}

	/**
	 * Returns IStrategoList of {@link ModuleDeclaration} enclosed in a Package.
	 * 
	 * @param packageQName Qualified Name of Package
	 * @return {@link IStrategoList}
	 * @throws SpxSymbolTableException 
	 */
	public IStrategoList getModuleDeclarations(IStrategoAppl packageQName) throws SpxSymbolTableException {
		logMessage("getModuleDeclarations | Arguments : " + packageQName);
		
		IStrategoList packageID = PackageDeclaration.getPackageId(this, packageQName);
		
		Iterable<ModuleDeclaration> decls = getModuleDeclarations(packageID);
		logMessage("getModuleDeclarations | Found following result from SymbolTable : " + decls);
		
		IStrategoList result =  Utils.toTerm(this, decls);
		logMessage("getModuleDeclarations | Returning IStrategoList : " + result );
		
		return result;
	}	
	
	
	public IStrategoList getDirtyModuleDeclarations(IStrategoAppl packageQName , IStrategoAppl qualifiedFor) throws SpxSymbolTableException {
		logMessage("getDirtyModuleDeclarations| Arguments : " + packageQName);
		
		List<ModuleDeclaration> dirtyModuleDeclarations  = new ArrayList<ModuleDeclaration>();
		IStrategoList packageID = PackageDeclaration.getPackageId(this, packageQName);
		
		Iterable<ModuleDeclaration> decls = getModuleDeclarations(packageID);
		logMessage("getDirtyModuleDeclarations | Found following result from SymbolTable : " + decls);
		
		long ts = 0;
	
		if( getCons().hasEqualConstructor( qualifiedFor , getCons().getToCompileCon()))
			ts = _persistenceManager.spxSymbolTable().getIntializedOn();
		else if ( getCons().hasEqualConstructor( qualifiedFor , getCons().getToCodeGenerateCon()))
			ts = this.persistenceManager().spxSymbolTable().getLastCodeGeneratedOn();
		else
			throw new SpxSymbolTableException("Illegal qualifiedFor constructor at getDirtyModuleDeclarations");
		
		
		for(ModuleDeclaration decl : decls ){
			if( decl.getLastModifiedOn() >= ts){ 
				dirtyModuleDeclarations.add(decl);
			}
		}
		IStrategoList result =  Utils.toTerm(this, dirtyModuleDeclarations);
		logMessage("getDirtyModuleDeclarations | Returning IStrategoList : " + result );
		
		return result;
	}	
	
	public Iterable<ModuleDeclaration> getModuleDeclarations(IStrategoList pacakgeID) throws SpxSymbolTableException
	{
		SpxModuleLookupTable table = persistenceManager().spxModuleTable();
		_persistenceManager.spxPackageTable().verifyPackageIDExists(pacakgeID) ;
		
		return table.getModuleDeclarationsByPackageId(pacakgeID);
	}
	/** 
	 * Returns ModuleDefinition for the Module with ID :  {@code moduleTypedQName}
	 * 
	 * @param moduleTypedQName
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SpxSymbolTableException 
	 */
	public IStrategoTerm getModuleDefinition(IStrategoAppl moduleTypedQName) throws IllegalArgumentException, SpxSymbolTableException
	{
		ModuleDeclaration decl = lookupModuleDecl(moduleTypedQName);

		SpxModuleLookupTable table = persistenceManager().spxModuleTable();
		
		IStrategoList qualifiedModuleId = ModuleDeclaration.getModuleId(this, moduleTypedQName);
		IStrategoTerm moduleAterm =table.getModuleDefinition(this, qualifiedModuleId) ;
		IStrategoTerm moduleAnnotatedAterm  = table.getAnalyzedModuleDefinition(this, qualifiedModuleId);
		
		return new ModuleDefinition( decl , (IStrategoAppl)moduleAterm, (IStrategoAppl)moduleAnnotatedAterm).toTerm(this);
	}
	
	/**
	 * Returns {@link LanguageDescriptor} for Spoofaxlang package with {@link packageTypedQName}}
	 *  
	 * @param packageTypedQName
	 * @return {@link IStrategoTerm} representation of {@link IStrategoTerm}
	 * @throws IllegalArgumentException if the package id is not found in the symbol table 
	 * @throws Exception  If package Id is valid but does not have any language descriptor registered
	 */
	public IStrategoTerm getLanguageDescriptor ( IStrategoAppl packageTypedQName) throws IllegalArgumentException, Exception{
		IStrategoList  packageId = PackageDeclaration.getPackageId(this, packageTypedQName);
		
		SpxPackageLookupTable packageTable = persistenceManager().spxPackageTable();
		packageTable.verifyPackageIDExists(packageId) ;
		
		LanguageDescriptor desc = getLangaugeDescriptorByPackageId(packageId);
		
		return desc.toTerm(this);
	}

	/**
	 * Returns language descriptor associated with id
	 * 
	 * @param id
	 *            module id whose language descriptor is to be returned
	 * @return {@link LanguageDescriptor}
	 * @throws SpxSymbolTableException 
	 */
	LanguageDescriptor getLangaugeDescriptorByPackageId(IStrategoList packageId) throws SpxSymbolTableException {
		
		SpxModuleLookupTable moduleLookupTable = persistenceManager().spxModuleTable();
		
		LanguageDescriptor ret = LanguageDescriptor.newInstance(getTermFactory(), packageId);
		Iterable<ModuleDeclaration> moduleDeclarations = this.getModuleDeclarations(packageId);
	
		for( ModuleDeclaration m : moduleDeclarations){
			
			LanguageDescriptor moduleLangaugeDescriptor = moduleLookupTable.getLangaugeDescriptor(m.getId());
			
			ret = LanguageDescriptor.appendLanguageDescriptors(getTermFactory(),ret , moduleLangaugeDescriptor);
		}
		
		return ret;
	}
	
	/**
	 * Removes PackageDeclaration mapped with the {@code spxCompilationUnitPath}
	 * 
	 * @param spxCompilationUnitPath
	 * @param packageId
	 */
	public void removePackageDeclaration(
			IStrategoString spxCompilationUnitPath , 
			IStrategoAppl namespaceID){
		SpxPackageLookupTable table = _persistenceManager.spxPackageTable();
		
		spxCompilationUnitPath  = (IStrategoString)toCompactPositionInfo((IStrategoTerm)spxCompilationUnitPath);
		IStrategoList packageId = (IStrategoList)toCompactPositionInfo(PackageDeclaration.getPackageId(this, namespaceID));
		
		table.verifyPackageIDExists(packageId) ;
		
		table.removePackageDeclarationLocation(
				packageId, 
				toAbsulatePath(spxCompilationUnitPath));
	}

	
	/**
	 * looks up for a package declaration given its ID. 
	 * 
	 * @param packageId
	 * @return
	 * @throws SpxSymbolTableException
	 */
	public PackageDeclaration lookupPackageDecl(IStrategoList packageId) throws SpxSymbolTableException {
		
		SpxPackageLookupTable table = persistenceManager().spxPackageTable();
		PackageDeclaration decl = table.getPackageDeclaration(packageId);
		
		if (decl == null)
			throw new SpxSymbolTableException( "Unknown Package Id : "+ packageId.toString());
		
		return decl;
	}
	
	/**
	 * Saves(Commits) any unsaved data. 
	 *  
	 * @throws IOException
	 */
	public void persistChanges() throws IOException {
		_persistenceManager.commit();
		if (Utils.DEBUG){
			try {
				_persistenceManager.spxSymbolTable().printSymbols(this, "commit", this.getProjectPath(), this.indexId());
			} catch (SpxSymbolTableException e) {
			}
		}
	}	

	/**
	 * Closes any underlying open connection. 
	 * @param shouldCommit TODO
	 *  
	 * @throws IOException
	 */
	public void close(boolean shouldCommit) throws IOException {
		if (!isPersistenceManagerClosed()) {
			logMessage("close | closing underlying persistence manager instance.");
			if(shouldCommit)
				_persistenceManager.commit();
			
			_persistenceManager.spxSymbolTable().setCompileSessionEndedOn();
			_persistenceManager.close();
			_persistenceManager = null;
			
		}else {
			logMessage("close | underlying persistence manager is already closed. ");
		}	
	}	

	/**
	 * Re-initialize Symbol Tables . It clears all the existing entries from  
	 * symbol tables.
	 * @throws Exception 
	 */
	public void reinitSymbolTable() throws Exception {	
		if (!isPersistenceManagerClosed()){
			persistenceManager().clear(); // cleaning persistence manager.
			persistenceManager().commitAndClose();
			//tryCleanupIndexDirectory();
			invalidateSpxCacheDirectory(); //cleaning the SpxCache as well.
		}
		initializePersistenceManager();
	}

	/**
	 * Deletes the Spx Cache directory configured in Utils. By this way, the Spx cache will 
	 * be invalidated and all the symbols will be indexed again. 
	 */
	void invalidateSpxCacheDirectory() {
		Utils.deleteSpxCacheDir( new File(  _projectPath +"/" + Utils.SPX_CACHE_DIRECTORY));
	}
	
	private void tryCleanupIndexDirectory(){
		try{
			Utils.tryDeleteSpxIndexDir( new File( _projectPath + "/" + Utils.SPX_INDEX_DIRECTORY));
		}catch(Exception ex){
			// In case of SecurityException , Do nothing
		}
	}
	
	public void rollbackChanges() throws IOException{	
		
		if (! isPersistenceManagerClosed())
			persistenceManager().rollback();
	}

	
	/**
	 * Checks whether the underlying persistence manager is already open. 
	 * 
	 * @return true if PersistenceManage is open. Otherwise returns false.
	 */
	boolean isPersistenceManagerClosed() { 	return (_persistenceManager == null) || _persistenceManager.isClosed(); }


	/**
	 * @param spxCompilationUnitAST
	 * @return
	 */
	private IStrategoTerm toCompactPositionInfo(IStrategoTerm term) {
		if( term == null) return term;
		
		ImploderAttachment astAttachment = ImploderAttachment.getCompactPositionAttachment(term, true);
		IStrategoTerm astTerm = _stripper.strip(term);
		astTerm.putAttachment(astAttachment);
		return astTerm;
	}

	private IStrategoTerm strip(IStrategoTerm term) { return _stripper.strip(term); 	}
	
	/**
	 * Returns the Absolute Path of the given URI 
	 * 
	 * @param uri URI of the Resource. 
	 * @return Absolute Path represented by the URI  
	 */
	public String toAbsulatePath( IStrategoString uri){ return Utils.uriToAbsPathString(toFileURI(uri)); }

	private URI toFileURI(IStrategoTerm filePath) {	return Utils.getAbsolutePathUri(Tools.asJavaString(filePath) ,_agent); }

	/**
	 * Verify type of declaration . 
	 * 
	 * @param actual
	 * @param expected
	 * @param message
	 */
	public  void verifyConstructor( IStrategoConstructor actual , IStrategoConstructor expected , String message){
		if( actual != expected)
			throw new IllegalArgumentException(message);
	}

	/**
	 * Logs message 
	 * 
	 * @param message
	 */
	private void logMessage(String message) { _persistenceManager.logMessage("SpxSemanticIndexFacade", message); }
	
	
	String fromFileURI(URI uri) {
		File file = new File(uri);
		return file.toString();
	}

	IOAgent getIOAgent() { return _agent; }
	
	/**
	 * Prints error message
	 * @param errMessage
	 */
	void printError(String errMessage){ _agent.printError(errMessage); }
	
	/**
	 * Force an imploder attachment for a term.
	 * This ensures that there is always some form of position info,
	 * and makes sure that origin info is not added to the term.
	 * (The latter would be bad since we cache in {@link #term}.)
	 */
	public static IStrategoAppl forceImploderAttachment(IStrategoAppl term , URI file) {
		return forceImploderAttachment(term, term, file);
	}
	
	public static IStrategoAppl forceImploderAttachment(IStrategoTerm id, IStrategoAppl term , URI file) {
		
		ImploderAttachment attach = ImploderAttachment.get(id);
		if (attach != null) {
			ImploderAttachment.putImploderAttachment(term, false, attach.getSort(), attach.getLeftToken(), attach.getRightToken());
		} else {
			String fn = file == null ? null : file.toString();
			term.putAttachment(ImploderAttachment.createCompactPositionAttachment(
					fn, 0, 0, 0, -1));
		}
		return term;
	}

	public void clearCache() throws IOException{ persistenceManager().clearCache();	}

	
}