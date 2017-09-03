package other;

import algorithms.QuickSortWithPermutation;
import graph.Graph;
import inputOutput.TextIO;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Stack;

public class ComputeFlowPipe {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("An input file address must be passed as an argument.");
            return;
        }
        TextIO textIO = new TextIO(args[0]);
        final String[] split = textIO.getText().split("\n");
        Graph graph = new Graph();
        for (int i = 0; i < split.length; i++) {
            final String[] params = split[i].split(" ");
            assert params.length == 3 : "line format is u v w, where u and v are nodes and w is the flow. Error in line " + i;
            final int u = Integer.parseInt(params[0]);
            final int v = Integer.parseInt(params[1]);
            graph.addEdge(u, v);
            graph.putEdgeProperty(new Pair<>(u, v), "flow", Integer.parseInt(params[2]));
        }
        Integer[] topologicalOrder = topologicalOrder(graph);
        for (Integer u : graph.getVertexSet()) {
            graph.putVertexProperty(u, "flowAcc", 0);
            for (Integer v : graph.getAdjVertex(u)) {
                graph.putEdgeProperty(new Pair<>(u, v), "flowAcc", 0);
            }
        }
        for (Integer u : topologicalOrder) {
            for (Integer v : graph.getAdjVertex(u)) {
                final int update = (int) graph.getVertexProperty(u, "flowAcc") + (int) graph.getEdgeProperty(new Pair<>(u, v), "flow");
                graph.putVertexProperty(v, "flowAcc", update + (int) graph.getVertexProperty(v, "flowAcc"));
                graph.putEdgeProperty(new Pair<>(u, v), "flowAcc", update);
                System.out.printf("accumulatedFlow(%d, %d) =  %d \n", u, v, update);
            }
        }
    }

    private static Integer[] topologicalOrder(Graph graph) {
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
