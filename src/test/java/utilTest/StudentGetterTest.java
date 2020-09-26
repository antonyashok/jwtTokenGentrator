package utilTest;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import model.Student;
import util.StudentsGetter;

public class StudentGetterTest {
	File courseDir = new File("C:\\DataVisual\\tree\\RJ101x");
	File usernamesToIgnoreFile = null;
	List<File> surveyFiles =  new ArrayList<>(1);
	private  Map<Integer, Student> id2Student = new HashMap<>();
	private  Set<Integer> userIdsToIgnore = new HashSet<>();;
	private  Map<String, Student> uid2Student = new HashMap<>();
//	private static File authuserFile = null;
//	private static File roleFile = null;
//	private static File commentRoleFile = null;
//	private static File enrollFile = null;
//	private static File certificateFile = null;
//	private static File authuserprofileFile = null;
//	private static File userIdMapFile = null;


	@Before
	public void setUpBeforeClass() throws Exception {
//		authuserFile = FileGetter.getFileEndingWith(courseDir, FileConstant.authuserFile);
//		roleFile = FileGetter.getFileEndingWith(courseDir, FileConstant.accessroleFile);
//		commentRoleFile= FileGetter.getFileEndingWith(courseDir, FileConstant.commentRole);
//		enrollFile = FileGetter.getFileEndingWith(courseDir, FileConstant.enrollFile);
//		certificateFile = FileGetter.getFileEndingWith(courseDir, FileConstant.certificateFile);
//		authuserprofileFile = FileGetter.getFileEndingWith(courseDir, FileConstant.authuserProFile);
//		userIdMapFile = FileGetter.getFileEndingWith(courseDir, FileConstant.userIdMapFile);
	}

	@Test
	public void testStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testStudentNum");
		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);
		assertEquals(convertNumricValue(assertValue.get(0)),id2Student.keySet().size() );
	}
	
	@Test
	public void testVerifiedStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testVerifiedStudentNum");

		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);
	

		List<Student> verifiedStudents = new ArrayList<>();
		id2Student.forEach((id, student)->{
			if(student.isPaying()) {
				 verifiedStudents.add(student);
			}
		});
		
		assertEquals(convertNumricValue(assertValue.get(0)), verifiedStudents.size() );
	}
	
	@Test
	public void testCertificatedStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testCertificatedStudentNum");
		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);
	

		List<Student> certificatedStudents = new ArrayList<>();
		id2Student.forEach((id, student)->{
			if(student.isCertificated()) {
				certificatedStudents.add(student);
			}
		});
		assertEquals(convertNumricValue(assertValue.get(0)), certificatedStudents.size() );
	}
	
	@Test
	public void testMaleGenderStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testMaleGenderStudentNum");
		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);
	

		List<Student> maleStudents = new ArrayList<>();
		id2Student.forEach((id, student)->{
			if(student.getGender().equals("m")) {
				maleStudents.add(student);
			}
		});

		assertEquals(convertNumricValue(assertValue.get(0)), maleStudents.size());
	}

	@Test
	public void testFemaleGenderStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testFemaleGenderStudentNum");
		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);
	

		List<Student> femaleStudents = new ArrayList<>();
		id2Student.forEach((id, student)->{
			if(student.getGender().equals("f")) {
				femaleStudents.add(student);
			}
		});

		assertEquals(convertNumricValue(assertValue.get(0)), femaleStudents.size());
	}

	@Test
	public void testUnknowGenderStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testUnknowGenderStudentNum");
		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);

		List<Student> unknownStudents = new ArrayList<>();
		id2Student.forEach((id, student)->{
			if(student.getGender().equals("u")) {
				unknownStudents.add(student);
			}
		});

		assertEquals(convertNumricValue(assertValue.get(0)), unknownStudents.size());
	}

	@Test
	public void testNonVerifiedStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testNonVerifiedStudentNum");
		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);

		List<Student> nonVerifiedStudents = new ArrayList<>();
		id2Student.forEach((id, student)->{
			if(!student.isPaying()) {
				nonVerifiedStudents.add(student);
			}
		});
		
		assertEquals(convertNumricValue(assertValue.get(0)), nonVerifiedStudents.size() );
	}
	
	@Test
	public void testNonCertificatedStudentNum() throws IOException {
		List<String> assertValue = getAssertValue("testNonCertificatedStudentNum");
		StudentsGetter.getStudents(courseDir, usernamesToIgnoreFile, surveyFiles, id2Student, userIdsToIgnore, uid2Student);
	
		List<Student> nonCertificatedStudents = new ArrayList<>();
		id2Student.forEach((id, student)->{
			if(!student.isCertificated()) {
				nonCertificatedStudents.add(student);
			}
		});
		assertEquals(convertNumricValue(assertValue.get(0)), nonCertificatedStudents.size() );
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
