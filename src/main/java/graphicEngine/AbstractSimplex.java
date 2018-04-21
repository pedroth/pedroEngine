package graphicEngine;


import graphicEngine.utils.AbstractSimplexException;

import java.util.Map;

public abstract class AbstractSimplex implements DrawAble {
    private static int nextVertexId;
    private int numOfVertices;
    private int[] vertices;
    private Map<Integer, Map<String, Object>> simplexPropertyMap;

    public AbstractSimplex(int numOfVertices) {
        this.numOfVertices = numOfVertices;
    }

    public int getNumOfVertices() {
        return numOfVertices;
    }

    public int[] getVertices() {
        return vertices;
    }

    public void setVertices(int[] vertices) {
        if (vertices.length != numOfVertices) {
            throw new AbstractSimplexException("vertex array size must be of size " + numOfVertices);
        }
        this.vertices = vertices;
    }

    public Map<Integer, Map<String, Object>> getSimplexPropertyMap() {
        return simplexPropertyMap;
    }

    public Map<String, Object> getPropertyMap(int[] vertexSet) {
        if (vertexSet.length <= 0 || vertexSet.length > numOfVertices) {
            throw new AbstractSimplexException("the size of vertex set must have the range { 1, ... , " + numOfVertices + " }");
        }
        long hash = vertexSet[0];
        for (int i = 1; i < vertexSet.length - 1; i++) {
            hash += 31 * hash + vertexSet[i + 1];
        }
        return simplexPropertyMap.get(hash);
    }
}
