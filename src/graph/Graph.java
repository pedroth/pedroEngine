package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * @author pedro
 * 
 *         based on https://sites.google.com/site/indy256/algo/graph 17:29
 *         11/03/2015
 * 
 */
public class Graph {
	private Map<Integer, Set<Integer>> edges = new HashMap<Integer, Set<Integer>>();
	private int numVertex;

	public Graph() {
		numVertex = 0;
	}

	public void addVertex(int u) {
		if (!edges.containsKey(u)) {
			numVertex++;
			edges.put(u, new HashSet<Integer>());
		}
	}

	public void removeVertex(int u) {
		if (!edges.containsKey(u)) {
			return;
		}
		for (int v : edges.get(u)) {
			edges.get(v).remove(u);
		}
		edges.remove(u);
		numVertex--;
	}

	public void addEdge(int u, int v) {
		addVertex(u);
		addVertex(v);
		edges.get(u).add(v);
	}

	public void removeEdge(int u, int v) {
		edges.get(u).remove(v);
	}

	public Set<Integer> getEdges(int u) {
		return edges.get(u);
	}

	public int getNumVertex() {
		return numVertex;
	}

}