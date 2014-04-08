package graph;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Node {

	// initialize parameters
	private Point2D.Double location;
	private Color color = Color.BLUE;
	private int id;
	protected List<Edge> edges = new ArrayList<>();
	
	public Node(int id) {
		this.id = id;
	}

	/**
	 * @return	The location of the node in virtual 2D space.
	 */
	public Point2D.Double getLocation() {
		return location;
	}
	
	/**
	 * @return	The virtual location of the node in list form.
	 */
	public List<Double> getLocationVector() {
		List<Double> vector = new ArrayList<Double>(2);
		vector.add(location.x);
		vector.add(location.y);
		return vector;
	}

	/**
	 * @param location	The location of the node in virtual 2D space.
	 */
	public void setLocation(Point2D.Double location) {
		this.location = location;
	}
	
	/**
	 * @param color	The color to display the node as in the visualizer.
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * @return	The color that the node is displayed with in the visualizer.
	 */
	public Color getColor() {
		return color;
	}
	
	public void addEdge(Edge edge) {
		edges.add(edge);
	}
	
	public String toString() {
		return String.valueOf(id);
	}
	
	public int getID() {
		return id;
	}

}
