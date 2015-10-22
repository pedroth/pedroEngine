package graph;

import algebra.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * The type Graph.
 *
 * @author pedro <p> based on https://sites.google.com/site/indy256/algo/graph 17:29 11/03/2015
 */
public class Graph {
    private Map<Integer, HashSet<Integer>> edges = new HashMap<Integer, HashSet<Integer>>(7);

    private int numVertex;

    /*
     * Adjacency matrix, will be created using lazy creation
     */
    private Matrix adjacencyMatrix;

    private boolean isGraphChanged = false;

    /**
     * Instantiates a new Graph.
     */
    public Graph() {
        numVertex = 0;
    }

    /**
     * Instantiates a new Graph.
     *
     * @param adjacencyMatrix the adjacency matrix
     */
    public Graph(Matrix adjacencyMatrix) {
        if (adjacencyMatrix.getRows() != adjacencyMatrix.getColumns()) {
            throw new RuntimeException("matrix must have the same number of rows and columns");
        }

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

    /**
     * Add vertex.
     *
     * @param u the u
     */
    public void addVertex(int u) {
        toggleIsGraphChanged();

        if (!edges.containsKey(u)) {
            numVertex++;
            edges.put(u, new HashSet<Integer>());
        }
    }

    /**
     * Remove vertex.
     *
     * @param u the u
     */
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

    /**
     * Add edge.
     *
     * @param u the u
     * @param v the v
     */
    public void addEdge(int u, int v) {
        toggleIsGraphChanged();

        addVertex(u);
        addVertex(v);
        edges.get(u).add(v);
    }

    /**
     * Remove edge.
     *
     * @param u the u
     * @param v the v
     */
    public void removeEdge(int u, int v) {
        toggleIsGraphChanged();
        edges.get(u).remove(v);
    }

    /**
     * Gets edges.
     *
     * @param u the u
     * @return the edges
     */
    public Set<Integer> getEdges(int u) {
        return edges.get(u);
    }

    /**
     * Gets vertex set.
     *
     * @return the vertex set
     */
    public Set<Integer> getVertexSet() {
        return edges.keySet();
    }

    /**
     * Gets num vertex.
     *
     * @return the num vertex
     */
    public int getNumVertex() {
        return numVertex;
    }

    private void toggleIsGraphChanged() {
        if (!isGraphChanged) {
            isGraphChanged = !isGraphChanged;
        }
    }

    /**
     * Gets adjacency matrix.
     *
     * @return the adjacency matrix
     */
    public Matrix getAdjacencyMatrix() {
        isGraphChanged = false;

        if (adjacencyMatrix == null || isGraphChanged) {
            return generateAdjacencyMatrix();
        } else {
            return adjacencyMatrix;
        }
    }

    /**
     * Gets degree values.
     *
     * @return the degree values
     */
    public List<Integer> getDegreeValues() {
        ArrayList<Integer> degreeValues = new ArrayList<Integer>(numVertex);
        Set<Integer> keys = edges.keySet();
        for (Integer i : keys) {
            degreeValues.add(edges.get(i).size());
        }
        return degreeValues;
    }

    /**
     * Gets cluster coefficients.
     *
     * @return the cluster coefficients
     */
    public List<Double> getClusterCoefficients() {
        List<Double> clusterCoeffsValues = new ArrayList<Double>(numVertex);
        Set<Integer> keys = edges.keySet();
        for (Integer i : keys) {
            clusterCoeffsValues.add(clusterCoefficient(i));
        }
        return clusterCoeffsValues;
    }

    /**
     * Cluster coefficient.
     *
     * @param u the u
     * @return the double
     */
    public double clusterCoefficient(int u) {
        Set<Integer> neighbors = edges.get(u);
        double links = 0;
        for (Integer v : neighbors) {
            Set<Integer> vNeighbors = edges.get(v);
            for (Integer w : vNeighbors) {
                if (neighbors.contains(w)) {
                    links += 0.5;
                }
            }
        }
        double k = neighbors.size();
        return k < 1 ? 0.0 : (2 * links / (k * (k - 1)));
    }

    private Matrix generateAdjacencyMatrix() {
        if (numVertex == 0) {
            return null;
        }
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

    /**
     * To string gephi.
     *
     * @return the string
     */
    public String toStringGephi() {
        String acc = "Source Target \n";
        Set<Integer> key = edges.keySet();
        for (Integer u : key) {
            Set<Integer> neighbors = edges.get(u);
            for (Integer v : neighbors) {
                acc += u + " " + v + String.format("%n");
            }
        }
        return acc;
    }

    /**
     * Gets distances from.
     *
     * @param u the u
     * @return the distances from
     */
    public Map<Integer, Double> getDistancesFrom(int u) {
        if (!edges.containsKey(u)) {
            return null;
        }
        Map<Integer, Double> distances = new HashMap<>(numVertex);
        for (Integer v : edges.keySet()) {
            distances.put(v, Double.MAX_VALUE);
        }
        distances.put(u, 0.0);
        Queue<Integer> queue = new LinkedList<>();
        queue.add(u);
        while (!queue.isEmpty()) {
            Integer v = queue.poll();
            double d = distances.get(v) + 1.0;
            for (Integer neigh : edges.get(v)) {
                if (distances.get(neigh) >= Double.MAX_VALUE) {
                    distances.put(neigh, d);
                    queue.add(neigh);
                }
            }
        }
        return distances;
    }
}