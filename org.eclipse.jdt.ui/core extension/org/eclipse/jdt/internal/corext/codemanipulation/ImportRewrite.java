/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.codemanipulation;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import org.eclipse.jface.text.IRegion;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.jdt.internal.corext.Assert;
import org.eclipse.jdt.internal.corext.textmanipulation.TextBuffer;

/**
 * A rewriter for imports that considers the organize import 
 * settings.
 */
public final class ImportRewrite {
	
	private ImportsStructure fImportsStructure;
	
	public ImportRewrite(ICompilationUnit cunit, CodeGenerationSettings settings) throws CoreException {
		Assert.isNotNull(cunit);
		Assert.isNotNull(settings);
		fImportsStructure= new ImportsStructure(cunit, settings.importOrder, settings.importThreshold, true);
	}
	
	public final TextEdit createEdit(TextBuffer buffer) throws CoreException {
		IRegion region= fImportsStructure.getReplaceRange(buffer);
		String text= fImportsStructure.getReplaceString(buffer, region);
		if (text == null) {
			return new MultiTextEdit(region.getOffset(), 0);
		}
		return new ReplaceEdit(region.getOffset(), region.getLength(), text);		
	}
	
	public final void rewrite(TextBuffer buffer, TextEdit rootEdit) throws MalformedTreeException, CoreException {
		IRegion region= fImportsStructure.getReplaceRange(buffer);
		String text= fImportsStructure.getReplaceString(buffer, region);
		if (text != null) {
			rootEdit.addChild(new ReplaceEdit(region.getOffset(), region.getLength(), text));
		}
	}
	
	public ICompilationUnit getCompilationUnit() {
		return fImportsStructure.getCompilationUnit();
	}
	
	/**
	 * @see ImportsStructure#setFilterImplicitImports(boolean)
	 */
	public void setFilterImplicitImports(boolean filterImplicitImports) {
		fImportsStructure.setFilterImplicitImports(filterImplicitImports);
	}
	
	/**
	 * @see ImportsStructure#setFindAmbiguousImports(boolean)
	 */
	public void setFindAmbiguosImports(boolean findAmbiguosImports) {
		fImportsStructure.setFindAmbiguousImports(findAmbiguosImports);
	}	
	
	/**
	 * Adds a new import declaration that is sorted in the structure using
	 * a best match algorithm. If an import already exists, the import is
	 * not added.
	 * @param qualifiedTypeName The fully qualified name of the type to import
	 * @return Retuns the simple type name that can be used in the code or the
	 * fully qualified type name if an import conflict prevented the import.
	 * The type name can contain dimensions.
	 */
	public String addImport(String qualifiedTypeName) {
		return fImportsStructure.addImport(qualifiedTypeName);
	}
	
	/**
	 * Adds a new import declaration that is sorted in the structure using
	 * a best match algorithm. If an import already exists, the import is
	 * not added.  The type binding can be an array binding. No import is added for unnamed
	 * types (local or anonymous types)
	 * @param binding The type binding of the type to be added
	 * @return Retuns the simple type name that can be used in the code or the
	 * fully qualified type name if an import conflict prevented the import.
	 */
	public String addImport(ITypeBinding binding) {
		return fImportsStructure.addImport(binding);
	}	

	/**
	 * Removes an import declaration if it exists. Does not touch on-demand imports.
	 * @param binding The type binding of the type to be removed as import
	 * @return Returns true if an import for the given type existed.
	 */
	public boolean removeImport(ITypeBinding binding) {
		return fImportsStructure.removeImport(binding);
	}
	
	/**
	 * Removes an import declaration for a type or an on-demand import.
	 * @param binding The qualified name the type to be removed as import
	 * @return Returns true if an import for the given type existed.
	 */
	public boolean removeImport(String qualifiedTypeName) {
		return fImportsStructure.removeImport(qualifiedTypeName);
	}
	
	/**
	 * Returns <code>true</code> if the import edit will not change the import
	 * container; otherwise <code>false</code> is returned.
	 * 
	 * @return <code>true</code> if the import edit will not change the import
	 * 	container; otherwise <code>false</code> is returned
	 */
	public boolean isEmpty() {
		return !fImportsStructure.hasChanges();
	}
}

