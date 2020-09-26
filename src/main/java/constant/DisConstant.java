package constant;

import java.util.ArrayList;
import java.util.List;

public class DisConstant {
	// discussion type
	public static final String disView = "discussion viewed";
	public static final String thrCre = "thread created";
	public static final String thrVot =  "thread voted";
	public static final String resCre ="response created";
	public static final String resVot = "response voted";
	public static final String comCre = "comment created";

	
	/**The server emits discussion forum events when a user interacts with a course discussion. */
	public static final String disViewEv = "edx.forum.thread.viewed";
	public static final String thrCreEv = "edx.forum.thread.created";
	public static final String thrVotEv = "edx.forum.thread.voted";
	public static final String resCreEv = "edx.forum.response.created";
	public static final String resVotEv = "edx.forum.response.voted";
	public static final String comCreEv = "edx.forum.comment.created";
	
	
	
	public static List<String> discussionEvents(){
		List<String> discussionEvents = new ArrayList<String>();
		
		discussionEvents.add(disViewEv);
		discussionEvents.add(thrCreEv);
		discussionEvents.add(thrVotEv);
		discussionEvents.add(comCreEv);
		discussionEvents.add(resCreEv);
		discussionEvents.add(resVotEv);
		
		return discussionEvents;
	}
	
}
