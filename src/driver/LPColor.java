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
		colors.add(Color.MAGENTA);
		colors.add(Color.ORANGE);
		colors.add(Color.PINK);
		colors.add(Color.RED);
		colors.add(Color.WHITE);
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
	    		lp.setColName(node.getID()*colors.size()+i, "x_"+node+","+i);
	    	}
	    }
	    for (int i = 0; i < colors.size(); i++) {
    		lp.setColName(graph.getVertexCount()*colors.size()+i, "w_"+i);
    	}

    	lp.setAddRowmode(true);  /* makes building the model faster if it is done rows by row */

    	// force all vertices to have a color
    	for (int m = 0; m < graph.getVertexCount(); m++) {
    		
	    	j = 0;
	    	for (int n = 0; n < colors.size(); n++) {
	    		colno[j] = m*colors.size()+n; // first column
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
    				
    	    		colno[j] = node1.getID()*colors.size()+n; // first column
    	    		row[j++] = 1;
    	    		
    	    		colno[j] = node2.getID()*colors.size()+n; // first column
    	    		row[j++] = 1;
    	    		
    	    		// add the row to lpsolve
    		    	lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
    	    	}
    			
    			// for each color, enforce that w_j is set when necessary
    			for (int n = 0; n < colors.size(); n++) {
    				j = 0;
    				
    	    		colno[j] = node1.getID()*colors.size()+n; // first column
    	    		row[j++] = 1;
    	    		
    	    		colno[j] = node2.getID()*colors.size()+n; // first column
    	    		row[j++] = 1;
    	    		
    	    		colno[j] = graph.getVertexCount()*colors.size()+n; // third column
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
	    		colno[j] = node.getID()*colors.size()+n; // first column
	    		row[j++] = 1;
	    		colno[j] = graph.getVertexCount()*colors.size()+n; // first column
	    		row[j++] = -1;
	    		lp.addConstraintex(j, row, colno, LpSolve.LE, 0);
	    	}
    	}
	
    	lp.setAddRowmode(false); // rowmode should be turned off again when done building the model

    	// set the objective function (sum of w_j values)
    	j = 0;
    	for (int n = 0; n < colors.size(); n++) {
    		colno[j] = graph.getVertexCount()*colors.size()+n; // first column
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
			System.out.println(lp.getColName(j + 1) + ": " + row[j]);
    	}
    	
    	// actually color the nodes
    	for (Node node : graph.getVertices()) {
	    	for (int n = 0; n < colors.size(); n++) {
	    		j = node.getID()*(colors.size()-1)+n;
	    		if (row[j] > 0) {
	    			System.out.println(node+","+n);
	    			node.setColor(colors.get(n));
	    		}
	    	}
    	}
    	
    	System.out.println("COLORS: "+colors.size());
	
	    /* clean up such that all used memory by lpsolve is freed */
	    if(lp.getLp() != 0)
	    	lp.deleteLp();
	
	    return 0;
	}
	
//	public int execute() throws LpSolveException {
//	    LpSolve lp;
//	    int Ncol, j, ret = 0;
//	
//	    /* We will build the model row by row
//	       So we start with creating a model with 0 rows and 2 columns */
//	    Ncol = 2; /* there are two variables in the model */
//
//	    /* create space large enough for one row */
//	    int[] colno = new int[Ncol];
//	    double[] row = new double[Ncol];
//	
//	    lp = LpSolve.makeLp(0, Ncol);
//	    if(lp.getLp() == 0)
//	    	ret = 1; /* couldn't construct a new model... */
//	
//	    if(ret == 0) {
//	    	/* let us name our variables. Not required, but can be useful for debugging */
//	    	lp.setColName(1, "x");
//	    	lp.setColName(2, "y");
//	
//	    	lp.setAddRowmode(true);  /* makes building the model faster if it is done rows by row */
//	
//	    	/* construct first row (120 x + 210 y <= 15000) */
//	    	j = 0;
//	
//	    	colno[j] = 1; /* first column */
//	    	row[j++] = 120;
//	
//	    	colno[j] = 2; /* second column */
//	    	row[j++] = 210;
//	
//	    	/* add the row to lpsolve */
//	    	lp.addConstraintex(j, row, colno, LpSolve.LE, 15000);
//	    }
//	
//	    if(ret == 0) {
//	    	/* construct second row (110 x + 30 y <= 4000) */
//	    	j = 0;
//	
//	    	colno[j] = 1; /* first column */
//	    	row[j++] = 110;
//	
//	    	colno[j] = 2; /* second column */
//	    	row[j++] = 30;
//	
//	    	/* add the row to lpsolve */
//	    	lp.addConstraintex(j, row, colno, LpSolve.LE, 4000);
//	    }
//	
//	    if(ret == 0) {
//	    	/* construct third row (x + y <= 75) */
//	    	j = 0;
//	
//	    	colno[j] = 1; /* first column */
//	    	row[j++] = 1;
//	
//	    	colno[j] = 2; /* second column */
//	    	row[j++] = 1;
//	
//	    	/* add the row to lpsolve */
//	    	lp.addConstraintex(j, row, colno, LpSolve.LE, 75);
//	    }
//	
//	    if(ret == 0) {
//	    	lp.setAddRowmode(false); /* rowmode should be turned off again when done building the model */
//	
//	    	/* set the objective function (143 x + 60 y) */
//	    	j = 0;
//	
//	    	colno[j] = 1; /* first column */
//	    	row[j++] = 143;
//	
//	    	colno[j] = 2; /* second column */
//	    	row[j++] = 60;
//	
//	    	/* set the objective in lpsolve */
//	    	lp.setObjFnex(j, row, colno);
//	    }
//	
//	    if(ret == 0) {
//	    	/* set the object direction to maximize */
//	    	lp.setMaxim();
//	
//	    	/* just out of curioucity, now generate the model in lp format in file model.lp */
//	    	lp.writeLp("model.lp");
//	
//	    	/* I only want to see important messages on screen while solving */
//	    	lp.setVerbose(LpSolve.IMPORTANT);
//	
//	    	/* Now let lpsolve calculate a solution */
//	    	ret = lp.solve();
//	    	if(ret == LpSolve.OPTIMAL)
//	    		ret = 0;
//	    	else
//	    		ret = 5;
//	    }
//	
//	    if(ret == 0) {
//	    	/* a solution is calculated, now lets get some results */
//	
//	    	/* objective value */
//	    	System.out.println("Objective value: " + lp.getObjective());
//	
//	    	/* variable values */
//	    	lp.getVariables(row);
//	    	for(j = 0; j < Ncol; j++)
//	    		System.out.println(lp.getColName(j + 1) + ": " + row[j]);
//	
//	    	/* we are done now */
//	    }
//	
//	    /* clean up such that all used memory by lpsolve is freed */
//	    if(lp.getLp() != 0)
//	    	lp.deleteLp();
//	
//	    return(ret);
//	}

}
