package driver;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class LPColor {
	
	Graph graph;
	List<Color> colors = new ArrayList<Color>();
	
	public LPColor(Graph graph) {
		this.graph = graph;
		initializeColors();
		formulate();
	}
	
	private void initializeColors() {
		colors.add(Color.BLACK);
		colors.add(Color.BLUE);
		//colors.add(Color.CYAN);
		//colors.add(Color.DARK_GRAY);
		//colors.add(Color.GRAY);
		colors.add(Color.GREEN);
		//colors.add(Color.LIGHT_GRAY);
		//colors.add(Color.MAGENTA);
		//colors.add(Color.ORANGE);
		//colors.add(Color.PINK);
		colors.add(Color.RED);
		//colors.add(Color.WHITE);
		colors.add(Color.YELLOW);
	}
	
	private void formulate() {
		
	}
	
	public void color() {
		try {
			System.out.println("RETURN VALUE: "+execute());
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}
	
	public int execute() throws LpSolveException {
	    LpSolve lp;
	    int Ncol, j;
	
	    /* We will build the model row by row
	       So we start with creating a model with 0 rows and 2 columns */
	    Ncol = graph.getVertexCount() * colors.size() + colors.size(); /* there are two variables in the model */

	    /* create space large enough for one row */
	    int[] colno = new int[Ncol];
	    double[] row = new double[Ncol];
	
	    lp = LpSolve.makeLp(0, Ncol);
	    if (lp.getLp() == 0) {
	    	System.out.println("ERROR: couldn't construct a new model...");
	    	return 1;
	    }
	
	    
    	/* let us name our variables. Not required, but can be useful for debugging */
	    for (Node node : graph.getVertices()) {
	    	for (int i = 0; i < colors.size(); i++) {
	    		lp.setColName(getIndex(node, i), "x_"+node+","+i);
	    	}
	    }
	    for (int i = 0; i < colors.size(); i++) {
    		lp.setColName(getColorIndex(i), "w_"+i);
    	}

    	lp.setAddRowmode(true);  /* makes building the model faster if it is done rows by row */

    	// force all vertices to have a color
    	for (Node node : graph.getVertices()) {
    		
	    	j = 0;
	    	for (int n = 0; n < colors.size(); n++) {
	    		colno[j] = getIndex(node, n); // first column
	    		row[j++] = 1;
	    	}
	
	    	// add the row to lpsolve
	    	lp.addConstraintex(j, row, colno, LpSolve.EQ, 1);
    	}
    	
    	// disallow neighboring nodes to have the same color
    	List<Node> constrainedNodes = new ArrayList<>();
    	for (Node node1 : graph.getVertices()) {
    		for (Node node2 : graph.getNeighbors(node1)) {
    			// skip these
    			if (node2 == node1 || constrainedNodes.contains(node2))
    				continue;
    			
    			// for each color, make constraint that the two adjacent nodes cannot be the same
    			for (int n = 0; n < colors.size(); n++) {
    				j = 0;
    				
    	    		colno[j] = getIndex(node1, n); // first column
    	    		row[j++] = 1;
    	    		
    	    		colno[j] = getIndex(node2, n); // first column
    	    		row[j++] = 1;
    	    		
    	    		// add the row to lpsolve
    		    	lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
    	    	}
    			
    			// for each color, enforce that w_j is set when necessary
    			for (int n = 0; n < colors.size(); n++) {
    				j = 0;
    				
    	    		colno[j] = getIndex(node1, n); // first column
    	    		row[j++] = 1;
    	    		
    	    		colno[j] = getIndex(node2, n); // first column
    	    		row[j++] = 1;
    	    		
    	    		colno[j] = getColorIndex(n); // third column
    	    		row[j++] = -1;
    	    		
    	    		// add the row to lpsolve
    		    	lp.addConstraintex(j, row, colno, LpSolve.LE, 0);
    	    	}
    		}
    		constrainedNodes.add(node1);
    	}
    	
    	// if node j is colored, set w_j to true
    	for (Node node : graph.getVertices()) {
	    	for (int n = 0; n < colors.size(); n++) {
	    		j = 0;
	    		colno[j] = getIndex(node, n); // first column
	    		row[j++] = 1;
	    		colno[j] = getColorIndex(n); // first column
	    		row[j++] = -1;
	    		lp.addConstraintex(j, row, colno, LpSolve.LE, 0);
	    	}
    	}
	
    	lp.setAddRowmode(false); // rowmode should be turned off again when done building the model

    	// set the objective function (sum of w_j values)
    	j = 0;
    	for (int n = 0; n < colors.size(); n++) {
    		colno[j] = getColorIndex(n); // first column
    		row[j++] = 1;
    	}
    	/* set the objective in lpsolve */
    	lp.setObjFnex(j, row, colno);
	
    	/* set the object direction to minimize */
    	lp.setMinim();

    	/* just out of curioucity, now generate the model in lp format in file model.lp */
    	lp.writeLp("model.lp");

    	/* I only want to see important messages on screen while solving */
    	lp.setVerbose(LpSolve.IMPORTANT);

    	/* Now let lpsolve calculate a solution */
    	int soln = lp.solve();
    	if(soln != LpSolve.OPTIMAL) {
    		System.out.println("ERROR: calculating solution...");
    		return 5;
    	}
	
    	/* a solution is calculated, now lets get some results */

    	/* objective value */
    	System.out.println("Objective value: " + lp.getObjective());

    	/* variable values */
    	lp.getVariables(row);
    	for(j = 0; j < Ncol; j++) {
			//System.out.println(lp.getColName(j + 1) + ": " + row[j]);
    	}
    	
    	// actually color the nodes
    	for (Node node : graph.getVertices()) {
	    	for (int n = 0; n < colors.size(); n++) {
	    		j = getIndex(node, n) - 1;
	    		if (row[j] >  0) {
	    			System.out.println("row["+j+"]   "+lp.getColName(j + 1)+"     "+node+","+n);
	    			//System.out.println("COLOR: "+n);
	    			node.setColor(colors.get(n));
	    		}
	    	}
    	}
	
	    /* clean up such that all used memory by lpsolve is freed */
	    if(lp.getLp() != 0)
	    	lp.deleteLp();
	
	    return 0;
	}
	
	private int getIndex(Node node, int color) {
		return node.getID()*colors.size()+color+1;
	}
	
	private int getColorIndex(int color) {
		return graph.getVertexCount()*colors.size()+color+1;
	}

}
