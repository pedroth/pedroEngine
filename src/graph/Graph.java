package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import algebra.Matrix;

/**
 * 
 * @author pedro
 * 
 *         based on https://sites.google.com/site/indy256/algo/graph 17:29
 *         11/03/2015
 * 
 */
public class Graph {
	private Map<Integer, HashSet<Integer>> edges = new HashMap<Integer, HashSet<Integer>>(7);
	private int numVertex;
	/*
	 * Adjacency matrix, will be created using lazy creation
	 */
	private Matrix adjacencyMatrix;
	private boolean isGraphChanged = false;

	public Graph() {
		numVertex = 0;
	}

	public Graph(Matrix adjacencyMatrix) {
		if (adjacencyMatrix.getRows() != adjacencyMatrix.getColumns())
			throw new RuntimeException("matrix must have the same number of rows and columns");

		int n = adjacencyMatrix.getColumns();
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= n; j++) {
				if (adjacencyMatrix.getXY(i, j) > 0) {
					addEdge(i, j);
					addEdge(j, i);
				}
			}
		}
	}

	public void addVertex(int u) {
		toggleIsGraphChanged();

		if (!edges.containsKey(u)) {
			numVertex++;
			edges.put(u, new HashSet<Integer>());
		}
	}

	public void removeVertex(int u) {
		toggleIsGraphChanged();

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
		toggleIsGraphChanged();

		addVertex(u);
		addVertex(v);
		edges.get(u).add(v);
	}

	public void removeEdge(int u, int v) {
		toggleIsGraphChanged();
		edges.get(u).remove(v);
	}

	public Set<Integer> getEdges(int u) {
		return edges.get(u);
	}

	public int getNumVertex() {
		return numVertex;
	}

	private void toggleIsGraphChanged() {
		if (!isGraphChanged)
			isGraphChanged = !isGraphChanged;
	}

	public Matrix getAdjacencyMatrix() {
		isGraphChanged = false;

		if (adjacencyMatrix == null || isGraphChanged)
			return generateAdjacencyMatrix();
		else
			return adjacencyMatrix;
	}

	public List<Integer> getDegreeValues() {
		ArrayList<Integer> degreeValues = new ArrayList<Integer>(numVertex);
		Set<Integer> keys = edges.keySet();
		for (Integer i : keys) {
			degreeValues.add(edges.get(i).size());
		}
		return degreeValues;
	}

	public List<Double> getClusterCoefficients() {
		List<Double> clusterCoeffsValues = new ArrayList<Double>(numVertex);
		Set<Integer> keys = edges.keySet();
		for (Integer i : keys) {
			clusterCoeffsValues.add(clusterCoefficient(i));
		}
		return clusterCoeffsValues;
	}

	public double clusterCoefficient(int u) {
		Set<Integer> neighbors = edges.get(u);
		double links = 0;
		for (Integer v : neighbors) {
			Set<Integer> vNeighbors = edges.get(v);
			for (Integer w : vNeighbors) {
				if (neighbors.contains(w))
					links += 0.5;
			}
		}
		double k = neighbors.size() ;
		return k < 1 ? 0.0 : (2 * links / (k * (k - 1)));
	}

	private Matrix generateAdjacencyMatrix() {
		if (numVertex == 0)
			return null;
		adjacencyMatrix = new Matrix(numVertex, numVertex);
		Set<Integer> keys = edges.keySet();
		Integer[] keyArray = keys.toArray(new Integer[0]);
		for (int i = 1; i <= numVertex; i++) {
			for (int j = 1; j <= numVertex; j++) {
				adjacencyMatrix.setXY(i, j, edges.get(keyArray[i - 1]).contains(keyArray[j - 1]) ? 1.0 : 0.0);
			}
		}
		return adjacencyMatrix;
	}
	
	public String toStringGephi() {
		String acc = "Source Target \n";
		Set<Integer> key = edges.keySet(); 
		for (Integer u : key) {
			Set<Integer> neighbors = edges.get(u);
			for (Integer v : neighbors) {
				acc+= u + " " + v + String.format("%n");
			}
		}
		return acc;
	}
}