package graph;

public class Edge {
	private int inVertex, outVertex;

	public Edge(int inVertex, int outVertex) {
		super();
		this.inVertex = inVertex;
		this.outVertex = outVertex;
	}

	public int getInVertex() {
		return inVertex;
	}

	public void setInVertex(int inVertex) {
		this.inVertex = inVertex;
	}

	public int getOutVertex() {
		return outVertex;
	}

	public void setOutVertex(int outVertex) {
		this.outVertex = outVertex;
	}
	
}
