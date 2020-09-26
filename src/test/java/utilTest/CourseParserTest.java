package utilTest;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import model.Step;
import util.CourseParser;

public class CourseParserTest {

	static JSONObject structFileRootObj = null;
	@Before
	public void setUpBeforeClass() throws Exception {
		File file = new File("C:\\DataVisual\\tree\\RJ101x\\VictoriaX-RJ101x-2T2018-course_structure-prod-analytics.json");
		JSONParser jsonParser=new JSONParser();
		structFileRootObj = (JSONObject) jsonParser.parse(new BufferedReader(new FileReader(file )));
	}

	@Test
	public void getCourseNameTest() {
		List<String> assertValue = getAssertValue("getCourseNameTest");
		String courseName = CourseParser.getCourseName(structFileRootObj);
		assertEquals(assertValue.get(0),courseName);
	}
	
	@Test
	public void getCourseIdTest() {
		List<String> assertValue = getAssertValue("getCourseIdTest");
		String courseId = CourseParser.getCourseId(structFileRootObj);
		assertEquals(assertValue.get(0),courseId);
	}
	
	@Test
	public void mapModuleId2ChapterTest() {
		List<String> assertValue = getAssertValue("mapModuleId2ChapterTest");
		Map<String,Step> moduleId2Chapter = CourseParser.mapModuleId2Chapter(structFileRootObj);
		int number = moduleId2Chapter.keySet().size();
		int convertedAssertValue  = convertNumricValue(assertValue.get(0));
		assertEquals(convertedAssertValue, number);
	}
	
	@Test
	public void mapHashcode2ChapterTest() {
		List<String> assertValue = getAssertValue("mapHashcode2ChapterTest");
		Map<String,Step> moduleId2Chapter = CourseParser.mapHashcode2Chapter(structFileRootObj);
		int number = moduleId2Chapter.keySet().size();
		int convertedAssertValue  = convertNumricValue(assertValue.get(0));
		assertEquals(convertedAssertValue, number);
	}
	
	@Test
	public void mapModuleId2SequentialTest() {
		List<String> assertValue = getAssertValue("mapModuleId2SequentialTest");
		Map<String,Step> moduleId2Chapter = CourseParser.mapModuleId2Sequential(structFileRootObj);
		int number = moduleId2Chapter.keySet().size();
		int convertedAssertValue  = convertNumricValue(assertValue.get(0));
		assertEquals(convertedAssertValue, number);
	}
	
	@Test
	public void mapHashcode2SequentialTest() {
		List<String> assertValue = getAssertValue("mapHashcode2SequentialTest");
		Map<String,Step> moduleId2Chapter = CourseParser.mapHashcode2Sequential(structFileRootObj);
		int number = moduleId2Chapter.keySet().size();
		int convertedAssertValue  = convertNumricValue(assertValue.get(0));
		assertEquals(convertedAssertValue, number);
	}
	
	@Test
	public void mapModuleId2VerticalTest() {
		List<String> assertValue = getAssertValue("mapModuleId2VerticalTest");
		Map<String,Step> moduleId2Chapter = CourseParser.mapModuleId2Vertical(structFileRootObj);
		int number = moduleId2Chapter.keySet().size();
		int convertedAssertValue  = convertNumricValue(assertValue.get(0));
		assertEquals(convertedAssertValue, number);
	}
	
	@Test
	public void mapHashcode2VerticalTest() {
		List<String> assertValue = getAssertValue("mapHashcode2VerticalTest");
		Map<String,Step> moduleId2Chapter = CourseParser.mapHashcode2Vertical(structFileRootObj);
		int number = moduleId2Chapter.keySet().size();
		int convertedAssertValue  = convertNumricValue(assertValue.get(0));
		assertEquals(convertedAssertValue, number);
	}
	
	@Test
	public void mapSequentialToVerticalTest() {
		List<String> assertValue = getAssertValue("mapSequentialToVerticalTest");
		Map<String,List<String>> moduleId2Chapter = CourseParser.mapSequentialToVertical(structFileRootObj);
		int number = moduleId2Chapter.keySet().size();
		int convertedAssertValue  = convertNumricValue(assertValue.get(0));
		assertEquals(convertedAssertValue, number);
	}
	
		
	@Test
	public void courseDataTest() {
		List<String> assertValue = getAssertValue("courseDataTest");
		List<OffsetDateTime>  courseDates = CourseParser.courseDates(structFileRootObj);
		OffsetDateTime start = courseDates.get(0);
		OffsetDateTime end = courseDates.get(1);
		assertEquals(start, OffsetDateTime.parse(assertValue.get(0)));
		assertEquals(end, OffsetDateTime.parse(assertValue.get(1)));
	}
	
	private List<String> getAssertValue(String methodName){
		List<String> assertValue = new ArrayList<>();
		if(!ReadTestDataUtil.testDataMap.isEmpty()) {
			assertValue = ReadTestDataUtil.testDataMap.get(methodName);
		}
		return assertValue;
	}
	
	private int convertNumricValue(String value) {
		return Integer.valueOf(value.replace("\"", "")) ;
	}
	

}
