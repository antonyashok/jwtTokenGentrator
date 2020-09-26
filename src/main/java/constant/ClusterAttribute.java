package constant;

import java.util.ArrayList;
import java.util.List;

public class ClusterAttribute {
	public final static String ID = "learner_id";
	public final static String EXPECTEDMOVEMENT = "expectedMovement";
	public final static String FORWARDMOVEMENT = "forwardMovement";
	public final static String BACKWARDMOVEMENT = "backwardMovement";
	public final static String VISITED = "visited";
	public final static String REVISITED = "revisited";
	public final static String VIEWBYMOVEMENT = "viewByMovement";
	public final static String POSTBYSTATEMENT = "postByStatement";
	public final static String STATEMENTBYVIEW = "statementByView";


	public final static List<String> attributes(){
		List<String> attributes = new ArrayList<String>();
		attributes.add(ID);
		attributes.add(EXPECTEDMOVEMENT);
		attributes.add(BACKWARDMOVEMENT);
		attributes.add(FORWARDMOVEMENT);
		attributes.add(VISITED);
		attributes.add(REVISITED);
		attributes.add(VIEWBYMOVEMENT);
		attributes.add(POSTBYSTATEMENT);
		attributes.add(STATEMENTBYVIEW);
		
		return attributes;
	}
}
