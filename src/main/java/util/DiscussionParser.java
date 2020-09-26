package util;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import model.DiscussionStep;
import model.Step;
public class DiscussionParser {

	public static Map<String, DiscussionStep> mapModuleId2Discussion(JSONObject structFileRootObj){
		Map<String, DiscussionStep> moduleId2Discussion = new HashMap<>();
		Map<String, Step> moduleId2Vertical = CourseParser.mapModuleId2Vertical(structFileRootObj);

		moduleId2Vertical.entrySet()
		.stream()
		.map(entry -> entry.getValue())
		.forEach(verticalStep -> {
			List<String> childrenIds = verticalStep.getChildren();
			childrenIds.forEach(moduleId -> {
				if(moduleId.contains("discussion+block")) {
					int idIndex = childrenIds.indexOf(moduleId)+1;
					JSONObject discussionMeta = ((JSONObject)((JSONObject)structFileRootObj.get(moduleId)).get("metadata"));
					String discussionId = discussionMeta.get("discussion_id").toString();
					String displayName = null;
					if(discussionMeta.get("display_name")!=null) {
						displayName = discussionMeta.get("display_name").toString();
					}else if(discussionMeta.get("discussion_target")!=null) {
						displayName = discussionMeta.get("discussion_target").toString();
					}
					String hashcode = moduleId.substring(moduleId.length() - 32);
					DiscussionStep d =new DiscussionStep(displayName,  hashcode, discussionId, verticalStep, idIndex);
					if(discussionMeta.get("discussion_category") !=null) {
						d.setCategory(discussionMeta.get("discussion_category").toString());
					}
					moduleId2Discussion.put(moduleId, d);
				}
			});
		});

		return moduleId2Discussion;
	}
	
	public static Map<String, DiscussionStep> mapHashcode2Discussion(JSONObject structFileRootObj){
		Map<String, DiscussionStep> hashcode2Discussion = new HashMap<>();
		
		Map<String, DiscussionStep> moduleId2Discussion = DiscussionParser.mapModuleId2Discussion(structFileRootObj);
		
		moduleId2Discussion.forEach((moduleId, discussion) -> {
			String hashcode = discussion.getHashcode();
		    hashcode2Discussion.put(hashcode, discussion);
		});
		
		return hashcode2Discussion;
	}
	
	public static Map<String, DiscussionStep> mapId2Discussion(JSONObject structFileRootObj){
		Map<String, DiscussionStep> id2Discussion = new HashMap<>();
		Map<String, DiscussionStep> moduleId2Discussion = DiscussionParser.mapModuleId2Discussion(structFileRootObj);
		moduleId2Discussion.forEach((moduleId, discussion) -> {
			String id = discussion.getDiscussionId();
		    id2Discussion.put(id, discussion);
		});
		return id2Discussion;
	}
	
	
}
