package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import model.Step;
public class CourseParser {

	public static String getCourseName(JSONObject structFileRootObj) {
		@SuppressWarnings("unchecked")
		Set<Map.Entry<String,JSONObject>> rootObjSet = structFileRootObj.entrySet();
		//find course-category object in JSON file
		JSONObject courseObj = (JSONObject) rootObjSet.stream()
				//filter for map.entries with course-category objects; should be only one
				.filter(entry -> entry.getValue().get("category").equals("course"))
				.map(entry -> entry.getValue())
				.findFirst()
				.get();	//throws NoSuchElementException if Optional was empty
		String courseName =  ((JSONObject)(courseObj.get("metadata"))).get("display_name").toString();
		return courseName;
	}

	public static String getCourseId(JSONObject structFileRootObj) {
		//view root object as Set of Map.Entries
		@SuppressWarnings("unchecked")	//warning unavoidable since JSONObjects lack generic types
		Set<Map.Entry<String,JSONObject>> rootObjSet = structFileRootObj.entrySet();

		//find course-category object in JSON file
		String courseModuleID= rootObjSet.stream()
				//filter for map.entries with course-category objects; should be only one
				.filter(entry -> entry.getValue().get("category").equals("course"))
				.map(entry -> entry.getKey())
				.findFirst()
				.get();	//throws NoSuchElementException if Optional was empty

		int after = courseModuleID.indexOf(":")+1;
		int before = courseModuleID.indexOf("+type");
		String courseID = courseModuleID.substring(after, before);
		return courseID;
	}

	public static Map<String, Step> mapModuleId2Chapter(JSONObject structFileRootObj){
		Map<String,Step> moduleId2Chapter=new HashMap<>();
		//view root object as Set of Map.Entries
		@SuppressWarnings("unchecked")	//warning unavoidable since JSONObjects lack generic types
		Set<Map.Entry<String,JSONObject>> rootObjSet = structFileRootObj.entrySet();

		JSONObject courseObj = (JSONObject) rootObjSet.stream()
				//filter for map.entries with course-category objects; should be only one
				.filter(entry -> entry.getValue().get("category").equals("course"))
				.map(entry -> entry.getValue())
				.findFirst()
				.get();	//throws NoSuchElementException if Optional was empty

		List<String> chapterModuleIds = (List<String>) courseObj.get("children");


		int n=1;
		for(String moduleId : chapterModuleIds){
			String hashcode = moduleId.substring( moduleId.length()-32);
			String displayName =((JSONObject) ((JSONObject) structFileRootObj.get(moduleId)).get("metadata")).get("display_name").toString();
			Boolean b = (Boolean)((JSONObject) ((JSONObject) structFileRootObj.get(moduleId)).get("metadata")).get("visible_to_staff_only");
			if(b!= null && b == true)
				break;
			Step step = new Step(displayName, hashcode, n);			
			List<String> children = (List<String>) ((JSONObject) structFileRootObj.get(moduleId)).get("children");
			step.setChildren(children);
			moduleId2Chapter.put(moduleId, step);
			n++;
		}	
		return moduleId2Chapter;
	}
	

	public static Map<String, Step> mapHashcode2Chapter(JSONObject structFileRootObj){
		Map<String, Step> hashcode2Chapter = new HashMap<>();
		Map<String, Step> moduleId2Chapter = CourseParser.mapModuleId2Chapter(structFileRootObj);

		moduleId2Chapter.forEach((moduleId, vertical) -> {
			int moduleIdLen = moduleId.length();
			String hashcode = moduleId.substring(moduleIdLen-32);
			hashcode2Chapter.put(hashcode, vertical);

		});
		return hashcode2Chapter;
	}
	
	public static Map<String, Step> mapModuleId2Sequential(JSONObject structFileRootObj) {
		Map<String,Step> moduleId2Sequential=new HashMap<>();

		@SuppressWarnings("unchecked")	//warning unavoidable since JSONObjects lack generic types
		Set<Map.Entry<String,JSONObject>> rootObjSet = structFileRootObj.entrySet();

		Map<String, Step> moduleId2Chapter = CourseParser.mapModuleId2Chapter(structFileRootObj);

		moduleId2Chapter.entrySet()
		.stream()
		.map(entry -> entry.getValue())
		.forEach(chapterStep ->{
			List<String> sequentialIds  = chapterStep.getChildren();
			int n=1;
			for(String moduleId : sequentialIds){
				String hashcode = moduleId.substring( moduleId.length()-32);
				String displayName =((JSONObject) ((JSONObject) structFileRootObj.get(moduleId)).get("metadata")).get("display_name").toString();
				Boolean b = (Boolean)((JSONObject) ((JSONObject) structFileRootObj.get(moduleId)).get("metadata")).get("visible_to_staff_only");
				if(b!= null && b == true)
					break;
				Step step = new Step(displayName, hashcode, chapterStep.getStepNumInts()[0], n);			
				List<String> children = (List<String>) ((JSONObject) structFileRootObj.get(moduleId)).get("children");
				step.setChildren(children);
				moduleId2Sequential.put(moduleId, step);
				n++;
			}	
		});
		return moduleId2Sequential;
	}

	public static Map<String, Step> mapHashcode2Sequential(JSONObject structFileRootObj){
		Map<String, Step> hashcode2Sequential = new HashMap<>();
		Map<String, Step> moduleId2Sequential = CourseParser.mapModuleId2Sequential(structFileRootObj);

		moduleId2Sequential.forEach((moduleId, vertical) -> {
			int moduleIdLen = moduleId.length();
			String hashcode = moduleId.substring(moduleIdLen-32);
			hashcode2Sequential.put(hashcode, vertical);

		});
		return hashcode2Sequential;
	}
	
	public static Map<String, Step> mapModuleId2Vertical(JSONObject structFileRootObj){
		Map<String,Step> moduleId2Vertical=new HashMap<>();

		@SuppressWarnings("unchecked")	//warning unavoidable since JSONObjects lack generic types
		Set<Map.Entry<String,JSONObject>> rootObjSet = structFileRootObj.entrySet();

		Map<String, Step> moduleId2Sequential = CourseParser.mapModuleId2Sequential(structFileRootObj);

		moduleId2Sequential.entrySet()
		.stream()
		.map(entry -> entry.getValue())
		.forEach(sequentialStep ->{
			List<String> verticalIds = sequentialStep.getChildren();
			int n =1;
			for(String moduleId : verticalIds){
				String hashcode = moduleId.substring( moduleId.length()-32);
				String displayName =((JSONObject) ((JSONObject) structFileRootObj.get(moduleId)).get("metadata")).get("display_name").toString();
				Boolean b = (Boolean)((JSONObject) ((JSONObject) structFileRootObj.get(moduleId)).get("metadata")).get("visible_to_staff_only");
				if(b!= null && b == true)
					break;
				Step step = new Step(displayName, hashcode, sequentialStep.getStepNumInts()[0], sequentialStep.getStepNumInts()[1], n);			
				List<String> children = (List<String>) ((JSONObject) structFileRootObj.get(moduleId)).get("children");
				step.setChildren(children);
				moduleId2Vertical.put(moduleId, step);
				n++;
			}	
		});
		return moduleId2Vertical;
	}
	
	public static Map<String, Step> mapHashcode2Vertical(JSONObject structFileRootObj){
		Map<String, Step> hashcode2Vertical = new HashMap<>();
		Map<String, Step> moduleId2Vertical = CourseParser.mapModuleId2Vertical(structFileRootObj);

		moduleId2Vertical.forEach((moduleId, vertical) -> {
			int moduleIdLen = moduleId.length();
			String hashcode = moduleId.substring(moduleIdLen-32);
			hashcode2Vertical.put(hashcode, vertical);

		});
		return hashcode2Vertical;
	}
	
	public static Map<String, List<String>> mapSequentialToVertical(JSONObject structFileRootObj){
		Map<String, List<String>> sequentialToVerticalMap = new HashMap<String, List<String>>();
		Map<String, Step> hashcode2Sequential = CourseParser.mapHashcode2Sequential(structFileRootObj);

		hashcode2Sequential.forEach((seqHashcode, sequentialStep) -> {
			List<String> verticalHashs = new ArrayList<String>();
			List<String> verticalModuleIds = sequentialStep.getChildren();
			verticalModuleIds.forEach(id ->{
				int idLen = id.length();
				verticalHashs.add(id.substring(idLen - 32));
			});
			sequentialToVerticalMap.put(seqHashcode, verticalHashs);
		});
		return sequentialToVerticalMap;
	}

	public static void main(String[] args) {
		File structFile = new File("src/main/VictoriaX-ICE101x-1T2017-course_structure-prod-analytics.json");
		JSONParser jsonParser=new JSONParser();
		try {
			JSONObject structFileRootObj = (JSONObject) jsonParser.parse(new BufferedReader(new FileReader(structFile)));
			CourseParser.mapModuleId2Chapter(structFileRootObj);
			CourseParser.mapModuleId2Sequential(structFileRootObj);
			Map<String, Step> mapModuleId2Vertical = CourseParser.mapModuleId2Vertical(structFileRootObj);
			 mapModuleId2Vertical.entrySet().stream().forEach(entry -> System.out.println(entry.getValue().toString()));
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<OffsetDateTime> courseDates(JSONObject structFileRootObj){
		List<OffsetDateTime>  courseDates = new ArrayList<OffsetDateTime>();
		//view root object as Set of Map.Entries
		@SuppressWarnings("unchecked")	//warning unavoidable since JSONObjects lack generic types
		Set<Map.Entry<String,JSONObject>> rootObjSet = structFileRootObj.entrySet();
		
		//find course-category object in JSON file
		JSONObject courseObj = (JSONObject) rootObjSet.stream()
				//filter for map.entries with course-category objects; should be only one
				.filter(entry -> entry.getValue().get("category").equals("course"))
				.map(entry -> entry.getValue())
				.findFirst()
				.get();	//throws NoSuchElementException if Optional was empty

		String startDateTimeString = (String) ((JSONObject) courseObj.get("metadata")).get("start");
		courseDates.add(OffsetDateTime.parse(startDateTimeString));

		String endDateTimeString = (String) ((JSONObject) courseObj.get("metadata")).get("end");
		courseDates.add(OffsetDateTime.parse(endDateTimeString));
		
		return courseDates;
	}

}
