package edu.rit.goal.sdg.interpreter.stmt;

import edu.rit.goal.sdg.graph.Vertex;
import edu.rit.goal.sdg.statement.Stmt;

public class Vc implements Stmt {

    public Vertex v;

    public Vc(final Vertex v) {
	super();
	this.v = v;
    }

    @Override
    public String toString() {
	return "vc " + v;
    }

}