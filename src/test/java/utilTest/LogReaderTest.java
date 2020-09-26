package utilTest;


import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import storage.DataManager;
import util.CourseParser;
import util.DiscussionParser;
import util.LogReader;
import util.StudentsGetter;

public class  LogReaderTest {
	private static File courseDir = new File("C:\\DataVisual\\tree\\RJ101x");
	private static File usernamesToIgnoreFile = null;
	private static List<File> surveyFiles =  new ArrayList<>(1);

	@Before
	public void setUp() throws Exception {
		DataManager.deleteDataInstance();
		JSONObject structFileRootObj = null;
		File file = new File("C:\\DataVisual\\tree\\RJ101x\\VictoriaX-RJ101x-2T2018-course_structure-prod-analytics.json");
		JSONParser jsonParser=new JSONParser();
		structFileRootObj = (JSONObject) jsonParser.parse(new BufferedReader(new FileReader(file )));
		DataManager.getDataInstance().setCourseName(CourseParser.getCourseName(structFileRootObj));
		DataManager.getDataInstance().setCourseID(CourseParser.getCourseId(structFileRootObj)); 
		List<OffsetDateTime> courseDates = CourseParser.courseDates(structFileRootObj);
		DataManager.getDataInstance().setCourseStart(courseDates.get(0));
		DataManager.getDataInstance().setCourseEnd(courseDates.get(1));
		
		DataManager.getDataInstance().setHashcode2Chapter(CourseParser.mapHashcode2Chapter(structFileRootObj));
		DataManager.getDataInstance().setHashcode2Sequential(CourseParser.mapHashcode2Sequential(structFileRootObj));
		DataManager.getDataInstance().setHashcode2Vertical(CourseParser.mapHashcode2Vertical(structFileRootObj));
		DataManager.getDataInstance().setId2Discussion(DiscussionParser.mapId2Discussion(structFileRootObj));
	

		DataManager.getDataInstance().setAllChapters(new ArrayList<>(DataManager.getDataInstance().getHashcode2Chapter().values()));
		DataManager.getDataInstance().getAllChapters().sort(null);
		
		DataManager.getDataInstance().setAllSequentials(new ArrayList<>(DataManager.getDataInstance().getHashcode2Sequential().values()));
		DataManager.getDataInstance().getAllSequentials().sort(null);
		
		DataManager.getDataInstance().setAllVerticals(new ArrayList<>(DataManager.getDataInstance().getHashcode2Vertical().values()));
		DataManager.getDataInstance().getAllVerticals().sort(null);
		
		DataManager.getDataInstance().setAllDiscussions(new ArrayList<>(DataManager.getDataInstance().getId2Discussion().values()));
		DataManager.getDataInstance().getAllDiscussions().sort(null);

	
		DataManager.getDataInstance().setVerticalsAndDiscussions(new ArrayList<>(DataManager.getDataInstance().getAllVerticals()));
		DataManager.getDataInstance().getVerticalsAndDiscussions().addAll(DataManager.getDataInstance().getAllDiscussion());
		DataManager.getDataInstance().getVerticalsAndDiscussions().sort(null);
		
		DataManager.getDataInstance().setSequentialToVerticalMap(CourseParser.mapSequentialToVertical(structFileRootObj)); // helper structure to analysis the vertical index

		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, DataManager.getDataInstance().getId2Student(),DataManager.getDataInstance().getUserIdsToIgnore(), DataManager.getDataInstance().getUid2Student());
	}
	
	@Test
	public void testStudentVerticalRecords() throws FileNotFoundException {
		List<String> assertValue = getAssertValue("testStudentVerticalRecords");
		String courseId = "VictoriaX+RJ101x+2T2018";
		File logFile = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		LogReader.getLogFiles(logFile, courseId);
		int verticalRecordsSize = DataManager.getDataInstance().getId2Student().get(18170132).getVerticalRecords().size();
		assertEquals(convertNumricValue(assertValue.get(0)),verticalRecordsSize);
		
	}
	
	@Test
	public void testStudentDiscussionRecords() throws FileNotFoundException {
		List<String> assertValue = getAssertValue("testStudentDiscussionRecords");
		String courseId = "VictoriaX+RJ101x+2T2018";
		File logFile = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		LogReader.getLogFiles(logFile, courseId);
		int discussionlRecordsSize = DataManager.getDataInstance().getId2Student().get(18170132).getDiscussionRecords().size();
		assertEquals(convertNumricValue(assertValue.get(0)),discussionlRecordsSize);
	}
	
	@Test
	public void testStudentViewedRecords() throws FileNotFoundException {
		List<String> assertValue = getAssertValue("testStudentViewedRecords");
		String courseId = "VictoriaX+RJ101x+2T2018";
		File logFile = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		LogReader.getLogFiles(logFile, courseId);
		int viewedRecordsSize = DataManager.getDataInstance().getId2Student().get(18170132).getVerticalAndDiscussionRecords().size();
		assertEquals(convertNumricValue(assertValue.get(0)),viewedRecordsSize);

	}

	@Test
	public void testUserIdsToIgnore() throws FileNotFoundException {
		List<String> assertValue = getAssertValue("testUserIdsToIgnore");
		String courseId = "VictoriaX+RJ101x+2T2018";
		File logFile = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		LogReader.getLogFiles(logFile, courseId);
		int userIdsToIgnoreSize = DataManager.getDataInstance().getUserIdsToIgnore().size();
		assertEquals(convertNumricValue(assertValue.get(0)),userIdsToIgnoreSize);
	}
	
	@Test
	public void testOverAllVerticalRecord() throws FileNotFoundException {
		List<String> assertValue = getAssertValue("testOverAllVerticalRecord");
		String courseId = "VictoriaX+RJ101x+2T2018";
		File logFile = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		LogReader.getLogFiles(logFile, courseId);
		List<Integer> overAllVerticalRecordList = new ArrayList<>();
		DataManager.getDataInstance().getId2Student().forEach((id, student)->{
			if(student.isPaying()) {
				overAllVerticalRecordList.add(student.getVerticalRecords().size());
			}
		});
		
		int overAllVerticalRecord = overAllVerticalRecordList.stream().collect(Collectors.summingInt(Integer::intValue));
		assertEquals(convertNumricValue(assertValue.get(0)),overAllVerticalRecord);
	}
	
	@Test
	public void testOverAllDiscussionRecord() throws FileNotFoundException {
		List<String> assertValue = getAssertValue("testOverAllDiscussionRecord");
		String courseId = "VictoriaX+RJ101x+2T2018";
		File logFile = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		LogReader.getLogFiles(logFile, courseId);
		List<Integer> overAllDiscussionRecordList = new ArrayList<>();
		DataManager.getDataInstance().getId2Student().forEach((id, student)->{
			if(student.isPaying()) {
				overAllDiscussionRecordList.add(student.getDiscussionRecords().size());
			}
		});
		
		int overAllDiscussionRecord = overAllDiscussionRecordList.stream().collect(Collectors.summingInt(Integer::intValue));
		assertEquals(convertNumricValue(assertValue.get(0)), overAllDiscussionRecord);
	}
	
	@Test
	public void testOverAllVerticalAndDisscusionRecord() throws FileNotFoundException {
		List<String> assertValue = getAssertValue("testOverAllVerticalAndDisscusionRecord");
		String courseId = "VictoriaX+RJ101x+2T2018";
		File logFile = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		LogReader.getLogFiles(logFile, courseId);
		List<Integer> overAllVerticalAndDisscusionRecordList = new ArrayList<>();
		DataManager.getDataInstance().getId2Student().forEach((id, student)->{
			if(student.isPaying()) {
				overAllVerticalAndDisscusionRecordList.add(student.getVerticalAndDiscussionRecords().size());
			}
		});
		
		int overAllVerticalAndDisscusionRecord = overAllVerticalAndDisscusionRecordList.stream().collect(Collectors.summingInt(Integer::intValue));
		assertEquals(convertNumricValue(assertValue.get(0)),overAllVerticalAndDisscusionRecord);
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
