package graph;


import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public abstract class AbstractGraphClustering {
    protected final static String CLASS_VERTEX_PROPERTY = "class";
    protected final KnnGraph graph;
    protected Map<Integer, List<Integer>> inverseClassification;
    protected Map<Integer, Integer> classification;

    protected AbstractGraphClustering(KnnGraph graph) {
        this.graph = graph;
    }

    /**
     * returns a map where key is the number of the class a the value is the segmented graph with similarity function on the  edges
     *
     * @return the graph
     */
    public Map<Integer, Graph> getClusteredGraph() {
        if (inverseClassification == null) {
            return null;
        }
        Map<Integer, Graph> map = new HashMap<>(inverseClassification.size());
        for (Map.Entry<Integer, List<Integer>> entry : inverseClassification.entrySet()) {
            Integer kclass = entry.getKey();
            Graph kgraph = segmentGraph(kclass);
            map.put(kclass, kgraph);
        }
        return map;
    }

    private Graph segmentGraph(Integer kclass) {
        Graph kgraph = new Graph();
        Stack<Integer> stack = new Stack<>();

        inverseClassification.get(kclass).forEach(stack::push);

        while (!stack.empty()) {
            Integer u = stack.pop();
            kgraph.addVertex(u);
            kgraph.putVertexProperty(u, CLASS_VERTEX_PROPERTY, kclass);
            for (Integer v : graph.getAdjVertex(u)) {
                if (graph.getVertexProperty(v, CLASS_VERTEX_PROPERTY) != kclass) {
                    continue;
                }
                kgraph.addEdge(u, v);
                Pair<Integer, Integer> pair = new Pair<>(u, v);
                kgraph.putEdgeProperty(pair, Graph.EDGE_WEIGHT_KEY, graph.getEdgeProperty(pair, Graph.EDGE_WEIGHT_KEY));
            }
        }
        return kgraph;
    }

    protected Map<Integer, List<Integer>> fixIndexOfInverseClassificationMap(Map<Integer, List<Integer>> map) {
        Integer[] keyIndex = this.graph.getKeyIndex();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            List<Integer> value = entry.getValue();
            for (int i = 0; i < value.size(); i++) {
                value.set(i, keyIndex[value.get(i)]);
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer kclass = entry.getKey();
            for (Integer index : entry.getValue()) {
                graph.putVertexProperty(index, CLASS_VERTEX_PROPERTY, kclass);
            }
        }
        return map;
    }

    protected Map<Integer, Integer> fixIndexOfClassificationMap(Map<Integer, Integer> map) {
        Integer[] keyIndex = this.graph.getKeyIndex();
        Map<Integer, Integer> ansMap = new HashMap<>(map.size());
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Integer key = entry.getKey();
            ansMap.put(keyIndex[key], entry.getValue());
        }
        return ansMap;
    }

    /**
     * Gets graph.
     *
     * @return the graph
     */
    public KnnGraph getGraph() {
        return graph;
    }

    /**
     * Gets inverse classification.
     *
     * @return the inverse classification
     */
    public Map<Integer, List<Integer>> getInverseClassification() {
        return inverseClassification;
    }

    /**
     * Gets classification.
     *
     * @return the classification
     */
    public Map<Integer, Integer> getClassification() {
        return classification;
    }
}
