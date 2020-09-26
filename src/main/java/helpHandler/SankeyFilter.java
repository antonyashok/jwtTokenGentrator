package helpHandler;

import java.time.OffsetDateTime;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

import model.Step;
import model.Student;
import storage.DataManager;

public class SankeyFilter {
	
	
	
	public static Map<Integer, Student> nodeFilter(Map<Integer, Student> id2StudentFiltered,JSONObject requestjson){
		List<Step> allSteps = DataManager.getDataInstance().getVerticalsAndDiscussions();
		
		//node filter selected; remove all students who did not pass the indicated node
		int nodeNum = ((Long) requestjson.get("node")).intValue();
		//skip if 0 (start node) or 2*allSteps.size()+1 (exit node)
		if(nodeNum==0 || nodeNum==(2*allSteps.size()+1)){
			System.out.println("Start or end node selected; skipping filter operation.");
		}
		else{
			Step step=allSteps.get((nodeNum-1)/2);	//map nodeNum to Step
			if(nodeNum%2 == 1){	//if nodeNum is odd, this is not a repeated step
				id2StudentFiltered.values().removeIf(student -> {	//remove student if student's list of records does NOT contain step
					return !student.getVerticalAndDiscussionRecords().stream().anyMatch(record -> record.step == step);
				});
			}
			else{	//if nodeNum is even, this is a repeated step
				id2StudentFiltered.values().removeIf(student -> {	//remove student if student's list of records contains step less than twice
					return ( student.getVerticalAndDiscussionRecords().stream().filter(record -> record.step == step).count() ) < 2;
				});
			}
		}
		return id2StudentFiltered;
	}
	
	public static Map<Integer, Student> linkFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		List<Step> allSteps = DataManager.getDataInstance().getVerticalsAndDiscussions();

		//link filter selected; remove all students who did not traverse the indicated link
		final int sourceNodeNum = ((Long) ((JSONObject) requestjson.get("link")).get("source")).intValue();
		final int targetNodeNum = ((Long) ((JSONObject) requestjson.get("link")).get("target")).intValue();

		//map source and target node numbers to steps; null indicates the start or end node
		final Step sourceStep = sourceNodeNum!=0 ? allSteps.get((sourceNodeNum-1)/2) : null;
		final Step targetStep = targetNodeNum!=2*allSteps.size()+1 ? allSteps.get((targetNodeNum-1)/2) : null;


		//if source AND target are null, error - should be impossible...
		//if source is null and target is not, just check if 1st step seen == target step; repeat should be impossible because 1st step
		//if target is null and source is not, check if last step seen = source step and they have/haven't seen it before
		//if neither are null, go through each adjacent pair in records list, check if any pair matches
		//use odd/evenness of sourcenum and target num + boolean flags to deal with repeats
		if(sourceStep==null && targetStep==null){	//if source is start node and target is end node
			System.out.println("Error: Cannot go from start node to leave node.");
			return null;
		}
		else if(sourceStep==null && targetStep!=null){	//if source is start node
			id2StudentFiltered.values().removeIf(student -> {	//remove student if student's 1st step is not targetStep
				return !student.getVerticalAndDiscussionRecords().get(0).step.equals(targetStep);
			});
		}
		else if(targetStep==null && sourceStep!=null){	//if target is end node
			id2StudentFiltered.values().removeIf(student -> {
				//remove student if student's last step is not sourceStep
				int size=student.getVerticalAndDiscussionRecords().size();
				if(!student.getVerticalAndDiscussionRecords().get(size-1).step.equals(sourceStep))
					return true;

				//student's last step is known to be sourceStep
				//remove student if their last step is/isn't a repeat when it shouldn't/should be
				if(sourceNodeNum%2 == 1){	//if sourceNodeNum is odd, it denotes a non-repeated step
					//if a student's list of records, minus the last, contains sourceStep, the student saw it more than once
					if(student.getVerticalAndDiscussionRecords().stream().limit(size-1).anyMatch(record -> record.step==sourceStep))
						return true;
				}
				else{	//if source node number is even, it denotes a repeated step
					//if a student's list of records, minus the last, does not contain sourceStep, the student saw it only once
					if(!student.getVerticalAndDiscussionRecords().stream().limit(size-1).anyMatch(record -> record.step==sourceStep))
						return true;
				}
				return false;
			});
		}
		else{	//if source is not start node and target is not end node
			id2StudentFiltered.values().removeIf(student -> {
				final boolean sourceIsRepeated = sourceNodeNum%2 == 0;
				final boolean targetIsRepeated = targetNodeNum%2 == 0;

				Step prevStep = null;
				Step currStep = student.getVerticalAndDiscussionRecords().get(0).step;
				boolean seenSource = false;
				boolean seenTarget = false;

				for(int i=1; i<student.getVerticalAndDiscussionRecords().size(); i++){
					prevStep=currStep;
					currStep=student.getVerticalAndDiscussionRecords().get(i).step;
					//if this pair of steps matches the selected link
					if(prevStep==sourceStep && currStep==targetStep && sourceIsRepeated==seenSource && targetIsRepeated==seenTarget)
						return false;

					if(prevStep==sourceStep)
						seenSource=true;
					if(prevStep==targetStep)
						seenTarget=true;
				}

				return true;
			});
		}
		return id2StudentFiltered;
	}
	
	public static Map<Integer, Student> payingFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		//pay filter selected; remove paying or non-paying students
		if(requestjson.get("pay").equals("paying")) {
			//remove all nonpaying students from map
			id2StudentFiltered.values().removeIf(student -> student.isPaying()==false);
		}
		else if(requestjson.get("pay").equals("nonpaying")) {
			//remove all paying students from map
			id2StudentFiltered.values().removeIf(student -> student.isPaying()==true);
		}
		return id2StudentFiltered;
	}
	public static Map<Integer, Student> certificateFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		if(requestjson.get("certificate").equals("yes")) {
			//remove all no certificated students from map
			id2StudentFiltered.values().removeIf(student -> student.isCertificated()==false);
		}
		else if(requestjson.get("certificate").equals("no")) {
			//remove all certificated students from map
			id2StudentFiltered.values().removeIf(student -> student.isCertificated()==true);
		}
		return id2StudentFiltered;
	}
	public static Map<Integer, Student> enrolledFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		//enrollment time filter; remove students that did not enroll between the 'from' date and 'to' date
		//note: students' enrollment dates are in UTC
		try {
			OffsetDateTime from = OffsetDateTime.parse(
					(String) ((JSONObject) requestjson.get("enrolled")).get("from") + "T00:00:00Z");
			//remove Students who enrolled before the 'from' date
			id2StudentFiltered.values().removeIf(student -> student.getEnrolledAt().isBefore(from));
		}
		catch (DateTimeParseException dtpe) {}
		try {
			OffsetDateTime to = OffsetDateTime.parse(
					(String) ((JSONObject) requestjson.get("enrolled")).get("to") + "T23:59:59Z");
			//remove Students who enrolled after the 'to' date
			id2StudentFiltered.values().removeIf(student -> student.getEnrolledAt().isAfter(to));
		}
		catch (DateTimeParseException dtpe) {}
		return id2StudentFiltered;
	}
	public static Map<Integer, Student> timeFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		try {
			OffsetDateTime from = OffsetDateTime.parse(
					(String) ((JSONObject) requestjson.get("time")).get("from") + "T00:00:00+00:00");
			//remove Records before the 'from' date
			//TODO: optimization: the List of Records should already be sorted in chrono-order; use this to speed up filtering
			id2StudentFiltered.values().stream().forEach(student -> {
				student.getVerticalAndDiscussionRecords().removeIf(record -> record.time.isBefore(from));
			});
		}
		catch (DateTimeParseException dtpe) {}
		try {
			OffsetDateTime to = OffsetDateTime.parse(
					(String) ((JSONObject) requestjson.get("time")).get("to") + "T23:59:59+00:00");
			//remove Records before the 'to' date
			//TODO: optimization: the List of Records should already be sorted in chrono-order; use this to speed up filtering
			id2StudentFiltered.values().parallelStream().forEach(student -> {
				student.getVerticalAndDiscussionRecords().removeIf(record -> record.time.isAfter(to));
			});
		}
		catch (DateTimeParseException dtpe) {}
		return id2StudentFiltered;
	}
	public static Map<Integer, Student> periodFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		//During the course running period
		if(requestjson.get("period").equals("in")) {
			id2StudentFiltered.values().stream().forEach(student ->{
				student.getVerticalAndDiscussionRecords().removeIf(record -> record.time.isBefore(DataManager.getDataInstance().getCourseStart()));
				student.getVerticalAndDiscussionRecords().removeIf(record -> record.time.isAfter(DataManager.getDataInstance().getCourseEnd()));
			});
		} else if(requestjson.get("period").equals("out")) {
			id2StudentFiltered.values().stream().forEach(student ->{
				student.getVerticalAndDiscussionRecords().removeIf(record -> record.time.isBefore(DataManager.getDataInstance().getCourseEnd()));
			});
		}
		return id2StudentFiltered;
	}
	public static Map<Integer, Student> genderFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		//gender filter; remove students that are not of the specified gender
		if(requestjson.get("gender").equals("male")) {
			id2StudentFiltered.values().removeIf(student -> student.getGender()!="m");
		}
		else if(requestjson.get("gender").equals("female")) {
			id2StudentFiltered.values().removeIf(student -> student.getGender()!="f");
		}
		else if(requestjson.get("gender").equals("unknown")) {
			id2StudentFiltered.values().removeIf(student -> student.getGender()!="u");
		}
		return id2StudentFiltered;
	}


	public static Map<Integer, Student> ageFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		//gender filter; remove students that are not of the specified gender
		//age filter; remove students that are not between the specified ages
		try {
			int minage = Integer.parseInt((String) ((JSONObject) requestjson.get("age")).get("min"));
			//remove students with known age less than minimum age
			id2StudentFiltered.values().removeIf(student -> student.getAge()>-1 && student.getAge() < minage);
		}
		catch (NumberFormatException nfe) {}

		try {
			int maxage = Integer.parseInt((String) ((JSONObject) requestjson.get("age")).get("max"));
			//remove students with known age over the maximum age
			id2StudentFiltered.values().removeIf(student -> student.getAge() > maxage);
		}
		catch (NumberFormatException nfe) {}

		if(!(boolean) ((JSONObject) requestjson.get("age")).get("showunknown"))
			id2StudentFiltered.values().removeIf(student -> student.getAge() < 0);
		return id2StudentFiltered;
	}
	

	public static Map<Integer, Student> educationFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		//education filter; remove students whose education level doesn't match one of the specified levels
		@SuppressWarnings("unchecked")
		Set<String> selectedLevels = new HashSet<String>((ArrayList<String>) requestjson.get("education"));

		// convert "unknown", if present in Set, to null, since that's how it's stored in the Student object
		if(selectedLevels.remove("unknown"))	//if "unknown" is present, removes it and returns true
			selectedLevels.add(null);

		id2StudentFiltered.values().removeIf(student -> !selectedLevels.contains(student.getEducation()));
		return id2StudentFiltered;
	}

	public static Map<Integer, Student> customFilter(Map<Integer, Student> id2StudentFiltered, Set<Integer> userIdsInCustomFilter,  JSONObject requestjson){
		if(requestjson.get("custom").equals("in")) {
			//remove any students who are not in the set
			id2StudentFiltered.keySet().removeIf(userId -> !userIdsInCustomFilter.contains(userId));
		}
		else if(requestjson.get("custom").equals("out")) {
			//remove any students who are in the set
			id2StudentFiltered.keySet().removeIf(userId -> userIdsInCustomFilter.contains(userId));
		}
		return id2StudentFiltered;
	}
	
	public static Map<Integer, Student> clusteringFilter(Map<Integer, Student> id2StudentFiltered, JSONObject requestjson){
		
		int clusterIndex = Integer.parseInt(requestjson.get("clustering").toString());
		
		List<Integer> idFilter = DataManager.getDataInstance().getClusterResult().get(clusterIndex);
		
		id2StudentFiltered.keySet().removeIf(userId -> !idFilter.contains(userId));
		
		return id2StudentFiltered;
	}
	
}
