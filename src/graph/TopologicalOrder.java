package graph;

import algorithms.QuickSortWithPermutation;

import java.util.Stack;

public final class TopologicalOrder {
    private TopologicalOrder() {
        // empty constructor
    }

    public static Integer[] topologicalOrder(Graph graph) {
        int time = 0;
        Stack<Integer> stack = new Stack<>();
        Stack<Integer> auxStack = new Stack<>();
        for (Integer u : graph.getVertexSet()) {
            graph.putVertexProperty(u, "start", Integer.MIN_VALUE);
            graph.putVertexProperty(u, "end", Integer.MIN_VALUE);
        }
        for (Integer u : graph.getVertexSet()) {
            if ((int) graph.getVertexProperty(u, "start") >= 0) {
                continue;
            }
            stack.add(u);
            // DFS
            while (!stack.isEmpty()) {
                int w = stack.pop();
                if ((int) graph.getVertexProperty(w, "start") < 0) {
                    graph.putVertexProperty(w, "start", time++);
                    for (Integer v : graph.getAdjVertex(w)) {
                        if ((int) graph.getVertexProperty(v, "start") > 0 && (int) graph.getVertexProperty(v, "end") < 0) {
                            throw new RuntimeException("The graph has cycles ... please put a DAG( direct acyclic graph ).");
                        }
                        //v.end < 0 whenever v.start < 0 by construction
                        if ((int) graph.getVertexProperty(v, "start") < 0) {
                            auxStack.push(v);
                        }
                    }
                }
                if (isFinished(w, graph)) {
                    graph.putVertexProperty(w, "end", ++time);
                } else {
                    stack.push(w);
                    while (!auxStack.empty()) {
                        stack.push(auxStack.pop());
                    }
                }
            }
        }
        QuickSortWithPermutation quickSortWithPermutation = new QuickSortWithPermutation(QuickSortWithPermutation.DECREASING_ORDER);
        Integer[] ends = graph.getVertexPropertyMap("end").toArray(new Integer[graph.getNumVertex()]);
        quickSortWithPermutation.sort(ends);
        Integer[] topologicalOrder = graph.getKeyIndex();
        return quickSortWithPermutation.permutate(topologicalOrder);
    }

    private static boolean isFinished(int u, Graph graph) {
        for (Integer v : graph.getAdjVertex(u)) {
            if ((int) graph.getVertexProperty(v, "start") < 0) {
                return false;
            }
        }
        return true;
    }
}
