/* -- JFLAP 4.0 --
 *
 * Copyright information:
 *
 * Susan H. Rodger, Thomas Finley
 * Computer Science Department
 * Duke University
 * April 24, 2003
 * Supported by National Science Foundation DUE-9752583.
 *
 * Copyright (c) 2003
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the author.  The name of the author may not be used to
 * endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
 
package grammar;

import java.io.Serializable;
import java.util.*;

/**
 * The grammar object is the root class for the representation of all
 * forms of grammars, including regular and context-free grammars.
 * This object simply maintains a structure that holds and maintains
 * the data pertinent to the definition of a grammar.
 *
 * @author Ryan Cavalcante
 */

public abstract class Grammar implements Serializable, Cloneable {
    /**
     * Creates an instance of <CODE>Grammar</CODE>.  The created
     * instance has no productions, no terminals, no variables, 
     * and specifically no start variable.
     */
    public Grammar(){
	myVariables = new HashSet();
	myTerminals = new HashSet();
	myStartVariable = null;
    }

    /**
     * Returns a copy of the Grammar object.
     * @return a copy of the Grammar object.
     */
    public Object clone() {
	Grammar g;
	try {
	    g = (Grammar) getClass().newInstance();
	} 
	catch (Throwable e) {
	    System.err.println("Warning: clone of grammar failed!");
	    return null;
	}

	HashMap map = new HashMap(); //old variables to new variables

	String[] variables = getVariables();
	for(int v = 0; v < variables.length; v++) {
	    String variable = variables[v];
	    String nvariable = new String(variables[v]);
	    map.put(variable, nvariable);
	    g.addVariable(nvariable);
	}
	
	/** set start variable. */
	g.setStartVariable((String) map.get(getStartVariable()));

	String[] terminals = getTerminals();
	for(int t = 0; t < terminals.length; t++) {
	    g.addTerminal(new String(terminals[t]));
	}
	
	ProductionRename[] productions = getProductions();
	for(int p = 0; p < productions.length; p++) {
	    String rhs = productions[p].getRHS();
	    String lhs = productions[p].getLHS();
	    g.addProduction(new ProductionRename(rhs,lhs));
	}
	
	return g;
    }

    /**
     * Changes the start variable to <CODE>variable</CODE>.
     * @param variable the new start variable.
     */
    public void setStartVariable(String variable) {
	myStartVariable = variable;
    }

    /**
     * Returns the start variable.
     * @return the start variable.
     */
    public String getStartVariable() {
	return myStartVariable;
    }
    
    /**
     * Returns true if <CODE>production</CODE> is a valid production
     * for the grammar.  This method by default calls
     * <CODE>checkProduction</CODE> and returns true if and only if
     * the method did not throw an exception.
     * @param production the production.
     * @return <CODE>true</CODE> if the production is fine,
     * <CODE>false</CODE> if it is not
     */
    public boolean isValidProduction(ProductionRename production) {
	try {
	    checkProduction(production);
	    return true;
	} catch (IllegalArgumentException e) {
	    return false;
	}
    }

    /**
     * If a production is invalid for the grammar, this method should
     * throw exceptions indicating why the production is invalid.
     * Otherwise it should do nothing.  This method will be called
     * when a production is added, and may be called by outsiders
     * wishing to check a production without adding it to a grammar.
     * @param production the production
     * @throws IllegalArgumentException if the production is in some
     * way faulty
     */
    public abstract void checkProduction(ProductionRename production);

    /**
     * Adds <CODE>production</CODE> to the set of 
     * productions in the grammar.
     * @param production the production to be added.
     * @throws IllegalArgumentException if the production is
     * unsuitable somehow
     */
    public void addProduction(ProductionRename production) {
	checkProduction(production);
	GrammarChecker gc = new GrammarChecker();
	/** if production already in grammar. */
	if(gc.isProductionInGrammar(production,this)) return;
	myProductions.add(production);
	
	/** add all new variables introduced by production to
	 * set of variables. */
	String[] variablesInProduction = production.getVariables();
	for(int k = 0; k < variablesInProduction.length; k++) {
	    if(!myVariables.contains(variablesInProduction[k])) {
		addVariable(variablesInProduction[k]);
	    }
	}
	
	/** add all new terminals introduced by production to
	 * set of terminals. */
	String[] terminalsInProduction = production.getTerminals();
	for(int i = 0; i < terminalsInProduction.length; i++) {
	    if(!myTerminals.contains(terminalsInProduction[i])) {
		addTerminal(terminalsInProduction[i]);
	    }
	}
    }
    
    /**
     * Adds <CODE>productions</CODE> to grammar by calling 
     * addProduction for each production in array.
     * @param productions the set of productions to add to grammar
     */
    public void addProductions(ProductionRename[] productions) {
	for(int k = 0; k < productions.length; k++) {
	    addProduction(productions[k]);
	}
    }

    /**
     * Removes <CODE>production</CODE> from the set of 
     * productions in the grammar.
     * @param production the production to remove.
     */
    public void removeProduction(ProductionRename production) {
	myProductions.remove(production);
	GrammarChecker gc = new GrammarChecker();
	/** Remove any variables that existed only in the
	 * production being removed. */
	String[] variablesInProduction = production.getVariables();
	for(int k = 0; k < variablesInProduction.length; k++) {
	    if(!gc.isVariableInProductions(this, variablesInProduction[k])) {
		removeVariable(variablesInProduction[k]);
	    }
	}
	
	/** Remove any terminals that existed only in the
	 * production being removed. */
	String[] terminalsInProduction = production.getTerminals();
	for(int i = 0; i < terminalsInProduction.length; i++) {
	    if(!gc.isTerminalInProductions(this, terminalsInProduction[i])) {
		removeTerminal(terminalsInProduction[i]);
	    }
	}
    }
    
    /**
     * Returns all productions in the grammar.
     * @return all productions in the grammar.
     */
    public ProductionRename[] getProductions() {
	return (ProductionRename[]) myProductions.toArray(new ProductionRename[0]);
    }
    
    /**
     * Adds <CODE>terminal</CODE> to the set of terminals 
     * in the grammar.
     * @param terminal the terminal to add.
     */
    private void addTerminal(String terminal) {
	myTerminals.add(terminal);
    }
    
    /**
     * Removes <CODE>terminal</CODE> from the set of 
     * terminals in the grammar.
     * @param terminal the terminal to remove.
     */
    private void removeTerminal(String terminal) {
	myTerminals.remove(terminal);
    }
    
    /**
     * Returns all terminals in the grammar.
     * @return all terminals in the grammar.
     */
    public String[] getTerminals() {
	return (String[]) myTerminals.toArray(new String[0]);
    }

    /**
     * Adds <CODE>variable</CODE> to the set of variables
     * in the grammar.
     * @param variable the variable to add.
     */
    private void addVariable(String variable) {
	myVariables.add(variable);
    }
    
    /**
     * Removes <CODE>variable</CODE> from the set of 
     * variables of the grammar.
     * @param variable the variable to remove.
     */
    private void removeVariable(String variable) {
	myVariables.remove(variable);
    }
    
    /**
     * Returns all variables in the grammar.
     * @return all variables in the grammar.
     */
    public String[] getVariables() {
	return (String[]) myVariables.toArray(new String[0]);
    }
    
    /**
     * Returns true if <CODE>production</CODE> is in the set
     * of productions of the grammar.
     * @param production the production.
     * @return true if <CODE>production</CODE> is in the set
     * of productions of the grammar.
     */
    public boolean isProduction(ProductionRename production) {
	return myProductions.contains(production);
    }
    
    /**
     * Returns true if <CODE>terminal</CODE> is in the set
     * of terminals in the grammar.
     * @param terminal the terminal.
     * @return true if <CODE>terminal</CODE> is in the set
     * of terminals in the grammar.
     */
    public boolean isTerminal(String terminal) {
	return myTerminals.contains(terminal);
    }
    
    /**
     * Returns true if <CODE>variable</CODE> is in the set
     * of variables in the grammar.
     * @param variable the variable.
     * @return true if <CODE>variable</CODE> is in the set
     * of variables in the grammar.
     */
    public boolean isVariable(String variable) {
	return myVariables.contains(variable);
    }
    
    /**
     * Returns a string representation of the grammar object, 
     * listing the four parts of the definition of a grammar:
     * the set of variables, the set of terminals, the start
     * variable, and the set of production rules.
     * @return a string representation of the grammar object.
     */
    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(super.toString());
	buffer.append('\n');
	/** print variables. */
	buffer.append("V: ");
	String[] variables = getVariables();
	for(int v = 0; v < variables.length; v++) {
	    buffer.append(variables[v]);
	    buffer.append(" ");
	}
	buffer.append('\n');
	
	/** print terminals. */
	buffer.append("T: ");
	String[] terminals = getTerminals();
	for(int t = 0; t < terminals.length; t++) {
	    buffer.append(terminals[t]);
	    buffer.append(" ");
	}
	buffer.append('\n');
	
	/** print start variable. */
	buffer.append("S: ");
	buffer.append(getStartVariable());
	buffer.append('\n');
	
	/** print production rules. */
	buffer.append("P: ");
	buffer.append('\n');
	ProductionRename[] productions = getProductions();
	for(int p = 0; p < productions.length; p++) {
	    buffer.append(productions[p].toString());
	    buffer.append('\n');
	}
	
	return buffer.toString();
    }
    
    /** Set of Variables. */
    protected Set myVariables;
    /** Set of Terminals. */
    protected Set myTerminals;
    /** Start variable. */
    protected String myStartVariable;
    /** Set of Production rules. */
    protected List myProductions = new ArrayList();
    
}
