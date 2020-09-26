package model;

import java.io.Serializable;

/** this is for store the attribute that will be used in the clustering algorithm */
public class ClusterStudent implements Serializable{
	public final int userId;
	private double expectedMovement = 0;
	private double backwardMovement = 0;
	private double forwardMovement = 0;
	private double visited = 0;
	private double revisited = 0;
	private double viewByMovement = 0;
	private double postByStatement = 0 ;
	private double statementByView = 0;
	
	/**Constructor*/
	public ClusterStudent(int userId) {
		this.userId = userId;
	}

	/**getter and setter*/
	public double getExpectedMovement() {
		return expectedMovement;
	}

	public void setExpectedMovement(double expectedMovement) {
		this.expectedMovement = expectedMovement;
	}
	
	public double getBackwardMovement() {
		return backwardMovement;
	}


	public void setBackwardMovement(double backwardMovement) {
		this.backwardMovement = backwardMovement;
	}

	public double getForwardMovement() {
		return forwardMovement;
	}

	public void setForwardMovement(double forwardMovement) {
		this.forwardMovement = forwardMovement;
	}

	public double getVisited() {
		return visited;
	}

	public void setVisited(double visited) {
		this.visited = visited;
	}

	public double getRevisited() {
		return revisited;
	}

	public void setRevisited(double revisited) {
		this.revisited = revisited;
	}

	public double getViewByMovement() {
		return viewByMovement;
	}

	public void setViewByMovement(double viewByMovement) {
		this.viewByMovement = viewByMovement;
	}

	public double getPostByStatement() {
		return postByStatement;
	}

	public void setPostByStatement(double postByStatement) {
		this.postByStatement = postByStatement;
	}

	public double getStatementByView() {
		return statementByView;
	}

	public void setStatementByView(double statementByView) {
		this.statementByView = statementByView;
	}

	public int getUserId() {
		return userId;
	}
	
}
