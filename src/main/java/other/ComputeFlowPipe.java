package other;

import graph.Graph;
import graph.TopologicalOrder;
import inputOutput.TextIO;
import javafx.util.Pair;

import java.io.IOException;

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
        Integer[] topologicalOrder = TopologicalOrder.topologicalOrder(graph);
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
}
