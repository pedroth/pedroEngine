package graph;

import algebra.src.Matrix;
import javafx.util.Pair;

import java.util.*;

/**
 * The type Graph.
 *
 * @author pedro <p> based on https://sites.google.com/site/indy256/algo/graph 17:29 11/03/2015</p>
 */
public class Graph {
    /**
     * The constant EDGE_WEIGHT_KEY.
     */
    public static final String EDGE_WEIGHT_KEY = "weight";
    private Map<Integer, HashSet<Integer>> edges = new HashMap<>(7);
    private Map<Integer, Map<String, Object>> vertexProperties = new HashMap<>(7);
    private Map<Pair<Integer, Integer>, Map<String, Object>> edgeProperties = new HashMap<>(7);
    private int numVertex;

    /*
     * Adjacency matrix and weightedGraphMatrix will be created using lazy creation
     */
    private Matrix adjacencyMatrix;
    private Matrix weightedGraphMatrix;

    private boolean isGraphChanged = false;

    /**
     * Instantiates a new Graph.
     */
    public Graph() {
        numVertex = 0;
    }

    /**
     * Copy constructor, does not copy properties map, just edge information.
     *
     * @param graph the graph
     */
    public Graph(Graph graph) {
        numVertex = 0;
        for (Map.Entry<Integer, HashSet<Integer>> entry : graph.edges.entrySet()) {
            Integer u = entry.getKey();
            for (Integer v : entry.getValue()) {
                this.addEdge(u, v);
            }
        }
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
            addVertex(i);
        }
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
     * Gets adj vertex.
     *
     * @param u the u
     * @return the adj vertex
     */
    public Set<Integer> getAdjVertex(int u) {
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
            this.adjacencyMatrix = generateAdjacencyMatrix();
            return adjacencyMatrix;
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
     * Gets degree.
     *
     * @param u the u
     * @return the degree
     */
    public int getDegree(int u) {
        HashSet<Integer> adjU = edges.get(u);
        if (adjU != null) {
            return adjU.size();
        }
        return 0;
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
        return k < 2 ? 0.0 : (2 * links / (k * (k - 1)));
    }

    private Matrix generateAdjacencyMatrix() {
        if (numVertex == 0) {
            return null;
        }
        adjacencyMatrix = new Matrix(numVertex, numVertex);
        Integer[] keyArray = getKeyIndex();
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

    /**
     * Put vertex property.
     *
     * @param <T> the type parameter
     * @param i   the i
     * @param str the str
     * @param obj the obj
     */
    public <T> void putVertexProperty(Integer i, String str, T obj) {
        if (!vertexProperties.containsKey(i)) {
            vertexProperties.put(i, new HashMap<>());
        }
        vertexProperties.get(i).put(str, obj);
    }

    /**
     * Gets vertex property.
     *
     * @param i   the i
     * @param str the str
     * @return the vertex property
     */
    public <T> T getVertexProperty(Integer i, String str) {
        Map<String, Object> stringObjectMap = vertexProperties.get(i);
        if (stringObjectMap == null) {
            return null;
        }
        return (T) stringObjectMap.get(str);
    }

    /**
     * Put edge property.
     *
     * @param <T>  the type parameter
     * @param pair the pair
     * @param str  the str
     * @param obj  the obj
     */
    public <T> void putEdgeProperty(Pair<Integer, Integer> pair, String str, T obj) {
        if (edges.get(pair.getKey()).contains(pair.getValue())) {
            if (!edgeProperties.containsKey(pair)) {
                edgeProperties.put(pair, new HashMap<>());
            }
            edgeProperties.get(pair).put(str, obj);
        }
    }

    /**
     * Gets edge property.
     *
     * @param pair the pair
     * @param str  the str
     * @return the edge property
     */
    public <T> T getEdgeProperty(Pair<Integer, Integer> pair, String str) {
        Map<String, Object> stringObjectMap = edgeProperties.get(pair);
        if (stringObjectMap == null) {
            return null;
        }
        return (T) stringObjectMap.get(str);
    }

    /**
     * Gets weight matrix.
     *
     * @param defaultValue the default value for undefined weights
     * @return the weight matrix
     */
    public Matrix getWeightMatrix(double defaultValue) {
        isGraphChanged = false;

        if (weightedGraphMatrix == null || isGraphChanged) {
            this.weightedGraphMatrix = generateWeightedGraphMatrix(defaultValue);
            return this.weightedGraphMatrix;
        } else {
            return weightedGraphMatrix;
        }
    }

    private Matrix generateWeightedGraphMatrix(double defaultValue) {
        Matrix w = new Matrix(numVertex, numVertex);

        Integer[] keyArray = getKeyIndex();

        for (int i = 1; i <= numVertex; i++) {
            for (int j = 1; j <= numVertex; j++) {
                Pair<Integer, Integer> key = new Pair<>(keyArray[i - 1], keyArray[j - 1]);
                w.setXY(i, j, (Double) (edgeProperties.containsKey(key) ? (edgeProperties.get(key).containsKey(EDGE_WEIGHT_KEY) ? edgeProperties.get(key).get(EDGE_WEIGHT_KEY) : defaultValue) : defaultValue));
            }
        }
        return w;
    }

    /**
     * Get key index.
     *
     * @return the integer [ ]
     */
    public Integer[] getKeyIndex() {
        Set<Integer> keys = edges.keySet();
        return keys.toArray(new Integer[keys.size()]);
    }

}