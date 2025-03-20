package com.wonkglorg.doc.core.interfaces;

import org.eclipse.jgit.diff.Edit;

import java.util.List;

public interface EditCalls{
	
	boolean addEdit();
	
	boolean removeEdit();
	
	/**
	 * Gets all edits in a repo
	 * @return a list of edits
	 */
	List<Edit> getEdits();
}
