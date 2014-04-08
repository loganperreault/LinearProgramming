package graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public class Visualizer {
	
	Layout<Node, Edge> layout;
	BasicVisualizationServer<Node, Edge> vis;
	int width, height;
	
	public Visualizer(Graph graph, int width, int height) {
		this.width = width;
		this.height = height;
		setGraph(graph);
	}
	
 	private Transformer<Node,Paint> vertexPaint = new Transformer<Node,Paint>() {
		public Paint transform(Node node) {
			return node.getColor();
		}
	};
	
	private void setGraph(Graph<Node, Edge> graph) {
		
		layout = new CircleLayout<Node, Edge>(graph);
		layout.setSize(new Dimension(width, height));
		vis = new BasicVisualizationServer<Node, Edge>(layout);
	 	vis.setPreferredSize(new Dimension(width, height));
	 	
	 	/*
	 	double visibleStep = height / visible.size();
	 	double visiblePosition = visibleStep / 2;
	 	double hiddenStep = height / hidden.size();
	 	double hiddenPosition = hiddenStep / 2;
	 	for (Object n : graph.getVertices()) {
	 		Node node = (Node) n;
	 		if (!node.hidden) {
	 			layout.setLocation(node, new Point2D.Double(width/5, visiblePosition));
	 			visiblePosition += visibleStep;
	 		} else {
	 			layout.setLocation(node, new Point2D.Double(4*width/5, hiddenPosition));
	 			hiddenPosition += hiddenStep;
	 		}
		}
		*/
	 	
	 	vis.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
	}
	
	public void showStructure() {
		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vis); 
		frame.pack();
		frame.setVisible(true); 
	}

}
