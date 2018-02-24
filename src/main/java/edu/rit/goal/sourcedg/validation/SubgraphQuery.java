package edu.rit.goal.sourcedg.validation;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;

import com.github.javaparser.ast.Node;

import edu.rit.goal.sourcedg.graph.VertexType;
import edu.rit.goal.sourcedg.validation.SubgraphQuery.SubgraphQueryEdge;
import edu.rit.goal.sourcedg.validation.SubgraphQuery.SubgraphQueryNode;

public class SubgraphQuery extends DefaultDirectedGraph<SubgraphQueryNode, SubgraphQueryEdge> {
	private static final long serialVersionUID = -513017247218635783L;
	private SubgraphQueryNode mainNode;
	
	public SubgraphQuery(Class<? extends SubgraphQueryEdge> edgeClass) {
		super(edgeClass);
	}
	
	public Set<SubgraphQueryNode> getUndirectedNeighbors(SubgraphQueryNode u) {
		Set<SubgraphQueryNode> set = new HashSet<>();
		
		Set<SubgraphQueryEdge> edges = this.incomingEdgesOf(u);
		for (SubgraphQueryEdge e : edges)
			set.add(e.src);
		
		edges = this.outgoingEdgesOf(u);
		for (SubgraphQueryEdge e : edges)
			set.add(e.tgt);
		
		return set;
	}
	
	public SubgraphQueryNode addVertex(VertexType type, Node ast) {
		SubgraphQueryNode n = new SubgraphQueryNode();
		n.type = type;
		n.ast = ast;
		addVertex(n);
		return n;
	}
	
	public SubgraphQueryEdge addEdge(SubgraphQueryNode src, SubgraphQueryNode tgt, boolean isPath) {
		SubgraphQueryEdge e = new SubgraphQueryEdge(src, tgt, isPath);
		addEdge(src, tgt, e);
		return e;
	}
	
	public class SubgraphQueryNode {
		VertexType type;
		Node ast;
	}

	public class SubgraphQueryEdge {
		SubgraphQueryNode src, tgt;
		boolean isPath;

		public SubgraphQueryEdge(SubgraphQueryNode src, SubgraphQueryNode tgt, boolean isPath) {
			super();
			this.src = src;
			this.tgt = tgt;
			this.isPath = isPath;
		}
		
	}

	public SubgraphQueryNode getMainNode() {
		return mainNode;
	}
	
}