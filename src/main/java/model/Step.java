package model;

import java.io.Serializable;
import java.util.List;

/**Contains information about a step (a chapter, sequential or vertical in the course structure), like its number and display name.
 * Class implements Comparable interface to give Steps the correct natural order.
 * Immutable.*/
public class Step implements Comparable<Step>, Serializable{
	private String stepNum;
	private int[] stepNumInts;
	private String displayName;
	private String hashcode;
	private List<String> children;
	
	public Step(String displayName, String hashcode, int firstStepNum, int... nextStepNums) {
		StringBuilder stepNum_=new StringBuilder().append(firstStepNum);
		this.stepNumInts=new int[nextStepNums.length+1];
		this.stepNumInts[0]=firstStepNum;
		//store remaining numbers of step
		for(int i=1; i<stepNumInts.length; i++){
			stepNum_.append(".");
			stepNum_.append(nextStepNums[i-1]);
			this.stepNumInts[i]=nextStepNums[i-1];
		}
		this.stepNum=stepNum_.toString();
		this.displayName=displayName;
		this.hashcode = hashcode;
	}
	
	/**Get String representation of this Step.*/
	public String toString(){
		return stepNum+" "+displayName;
	}
	@Override
	public int compareTo(Step arg1) {
		
		int i=0;
		while(true){
			int a0=this.stepNumInts[i];
			int a1=arg1.stepNumInts[i];
			
			if(a0 > a1)
				return 1;
			else if(a0 < a1)
				return -1;
			else{
				i++;
				
				//if either step is too short for another comparison
				if(this.stepNumInts.length==i || arg1.stepNumInts.length==i){
					//if both steps are too short for the next comparison
					if(this.stepNumInts.length==i && arg1.stepNumInts.length==i)
						return 0;	//steps are equal
					else if(this.stepNumInts.length==i)//if just this step is too short
						return -1;	//the other step is higher
					else	//the other step was too short
						return 1;	//this step is higher
				}
				
			}
		}
	}
	
	//getter and setter
	public String getStepNum() {
		return stepNum;
	}
	public void setStepNum(String stepNum) {
		this.stepNum = stepNum;
	}
	public int[] getStepNumInts() {
		return stepNumInts;
	}
	public void setStepNumInts(int[] stepNumInts) {
		this.stepNumInts = stepNumInts;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getHashcode() {
		return hashcode;
	}
	public void setHashcode(String hashcode) {
		this.hashcode = hashcode;
	}
	public List<String> getChildren() {
		return children;
	}
	public void setChildren(List<String> children) {
		this.children = children;
	}

}
