package constant;

import java.util.ArrayList;
import java.util.List;

public class NavigationConstant {
	
	public static final String navLink= "edx.ui.lms.link_clicked";
	public static final String navNext = "edx.ui.lms.sequence.next_selected";
	public static final String navPre = "edx.ui.lms.sequence.previous_selected";
	public static final String navTab ="edx.ui.lms.sequence.tab_selected";
	
	public static List<String> navigationEvents(){
		List<String> navigationEvents = new ArrayList<String>();
		
		navigationEvents.add(navLink);
		navigationEvents.add(navNext);
		navigationEvents.add(navPre);
		navigationEvents.add(navPre);
		
		return navigationEvents;
	}

//	public static List<String> videoEvents(){
//		List<String> videoEvents = new ArrayList<String>();
//		
//		videoEvents.add("play_video");
//		videoEvents.add("edx.video.played");
//		videoEvents.add("stop_video");
//		videoEvents.add("edx.video.stopped");
//		videoEvents.add("pause_video");
//		videoEvents.add("edx.video.paused");
//		videoEvents.add("seek_video");
//		videoEvents.add("edx.video.position.changed");
//		
//		return videoEvents;
//	}

}
