package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import constant.DisConstant;
import model.Discussion;
import model.DiscussionRecord;
import model.Step;
import model.Student;

public class DiscussionAnalysis {

	public static  Map<Step, Discussion> getDiscussionMap(Map<Integer, Student> id2Student) {
		Map<Step, Discussion> discussionMap = new ConcurrentHashMap<>();
		
		id2Student.values().stream()
		.forEach(student -> {
			List<DiscussionRecord> discussionRecords = student.getDiscussionRecords();
			if(discussionRecords.isEmpty()) return;

			discussionRecords.forEach(discussionRecord -> {
				String type = discussionRecord.type;
				Step step = discussionRecord.step;
				Discussion discussion = null;
				
				if(discussionMap.get(step) == null) {
					discussion = new Discussion(step);
					discussionMap.put(step, discussion);
				}
				else
					discussion = discussionMap.get(step);

				if(type.equals(DisConstant.disView)) {
					discussion.setViewedNum(discussion.getViewedNum()+1);
					if(!discussion.getViewedNumByStudents().contains(student))
						discussion.getViewedNumByStudents().add(student);
				}
				if(type.equals(DisConstant.thrVot))	{
					discussion.setPostVote(discussion.getPostVote()+1);
					if(!discussion.getPostVoteByStudents().contains(student))
						discussion.getPostVoteByStudents().add(student);
				}
				if(type.equals(DisConstant.thrCre))	{
					discussion.setPostNum(discussion.getPostNum()+1);
					if(!discussion.getPostNumByStudents().contains(student))
						discussion.getPostNumByStudents().add(student);
				}
				if(type.equals(DisConstant.comCre)) {
					discussion.setCommentNum(discussion.getCommentNum()+1);
					if(!discussion.getCommentNumByStudents().contains(student))
						discussion.getCommentNumByStudents().add(student);
				}
				if(type.equals(DisConstant.resCre)) {
					discussion.setResponseNum(discussion.getResponseNum()+1);
					if(!discussion.getResponseNumByStudents().contains(student))
						discussion.getResponseNumByStudents().add(student);
				}
				if(type.equals(DisConstant.resVot)) {
					discussion.setReponseVote(discussion.getReponseVote()+1);	
					if(!discussion.getResponseVoteByStudents().contains(student))
						discussion.getResponseVoteByStudents().add(student);
				}

			});
		});

		return discussionMap;
	}

}
