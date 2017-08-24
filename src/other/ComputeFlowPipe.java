package other;

import algorithms.QuickSortWithPermutation;
import graph.Graph;
import inputOutput.TextIO;
import javafx.util.Pair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ComputeFlowPipe {
    public static int main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("An input file address must be passed as an argument.");
            return 0;
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
        List<List<Integer>> disconnectedTopologicOrder = topologicalOrder(graph);
    }

    private static List<List<Integer>> topologicalOrder(Graph graph) {
        int time = 0;
        LinkedList<Integer> stack = new LinkedList<>();
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
                int w = stack.pollFirst();
                graph.putVertexProperty(w, "start", time++);
                for (Integer v : graph.getAdjVertex(u)) {
                    if ((int) graph.getVertexProperty(v, "start") > 0 && (int) graph.getVertexProperty(v, "end") < 0) {
                        throw new RuntimeException("The graph has cycles ... please put a DAG( direct acyclic graph ).");
                    }
                    //v.end < 0 whenever v.start < 0 by construction
                    if ((int) graph.getVertexProperty(v, "start") < 0) {
                        stack.push(v);
                    }
                }
                if (isFinished(w, graph)) {
                    graph.putVertexProperty(w, "end", ++time);
                } else {
                    stack.add(w);
                }
            }
        }
        final Integer[] vertexArray = graph.getVertexSet().toArray(new Integer[graph.getVertexSet().size()]);
        QuickSortWithPermutation quickSortWithPermutation = new QuickSortWithPermutation();
        quickSortWithPermutation.sort(vertexArray);
        final int[] permutation = quickSortWithPermutation.getPermutation();
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
