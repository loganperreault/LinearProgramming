package graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utilities {
	
	static Random random = new Random(11235);

	public static Graph createRandomGraph(int numVertices, int numEdges) {
		int maxEdges = (numVertices * (numVertices - 1) / 2);
		if (numEdges > maxEdges) {
			System.out.println("ERROR: "+numVertices+" vertices may only have "+maxEdges+" edges.");
			System.exit(1);
		}
		List<Node> nodes = new ArrayList<>();
		Graph graph = new Graph();
		for (int i = 0; i < numVertices; i++) {
			Node node = new Node(i);
			nodes.add(node);
			graph.addVertex(node);
		}
		List<Edge> edges = new ArrayList<>();
		for (int i = 0; i < numEdges; i++) {
			Node node1, node2;
			do {
				do {
					node1 = nodes.get(random.nextInt(nodes.size()));
				} while (graph.degree(node1) == numVertices - 1);
				do {
					node2 = nodes.get(random.nextInt(nodes.size()));
				} while (node1 == node2 || graph.degree(node2) == numVertices - 1);
			} while (graph.isNeighbor(node1, node2));
			graph.addEdge(new Edge(), node1, node2);
		}
		return graph;
	}
	
}
