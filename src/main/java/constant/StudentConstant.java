package constant;

import java.util.HashMap;
import java.util.Map;

public class StudentConstant {

	public static Map<String, String> educationMap() {
		Map<String,String> eduMap = new HashMap<>();
		eduMap.put("p", "Doctorate");
		eduMap.put("m", "Master Degree");
		eduMap.put("b", "Bachelor Degree");
		eduMap.put("a", "Associate Degree");
		eduMap.put("hs", "High School");
		eduMap.put("jhs", "Junior High School");
		eduMap.put("el", "Primary School");
		eduMap.put("none", "None");
		eduMap.put("other", "Other");
		eduMap.put(null, "Unknown");
		return eduMap;
	}; 
	
	public static Map<String, String> genderMap(){
		Map<String,String> genderMap = new HashMap<>();
		genderMap.put("m", "male");
		genderMap.put("f", "female");
		genderMap.put("u", "unknown");
		return genderMap;
	}
	public StudentConstant() {
		// TODO Auto-generated constructor stub
	}
	
	

}
