package util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.parser.ParseException;

import constant.FileConstant;
import model.Student;
public class StudentsGetter {

	
	public static void getStudents(File courseDir, File usernamesToIgnoreFile, List<File> surveyFiles,
			Map<Integer, Student> id2Student, Set<Integer> userIdsToIgnore,
			Map<String, Student> uid2Student
			) throws IOException{
		//read auth_user table, add Students to id2Student Map, and also fill username2Id Map
		File authuserFile = FileGetter.getFileEndingWith(courseDir, FileConstant.authuserFile);
		 Map<String, Integer> username2Id = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(authuserFile))){
			Iterable<CSVRecord> rows = CSVFormat.MYSQL.withFirstRecordAsHeader().parse(reader);
			rows.forEach(row -> {	//each row represents one student
				Integer userId = new Integer(row.get("id"));
				id2Student.put(userId, new Student(userId, row.get("username")));
				username2Id.put(row.get("username"), new Integer(row.get("id")));
			});
		}
		

		//if file of usernames to ignore was provided, map to user ids, then add to the set of user ids to ignore
		if(usernamesToIgnoreFile!=null) {
			try (BufferedReader in = new BufferedReader(new FileReader(usernamesToIgnoreFile))) {
				//each line in file has one username to ignore
				in.lines().forEach(username -> {
					Integer id = username2Id.get(username);
					if(id!=null)
						userIdsToIgnore.add(id);
				});
			}
			catch (IOException e) {throw e;}
		}



		//read course access role file; add ids of users with any privileged role to the Set of user ids to ignore
		File roleFile = FileGetter.getFileEndingWith(courseDir, FileConstant.accessroleFile);
		try(BufferedReader reader = new BufferedReader(new FileReader(roleFile))){
			Iterable<CSVRecord> rows = CSVFormat.MYSQL.withFirstRecordAsHeader().parse(reader);

			rows.forEach(row -> {
				//this table only contains users with a privileged role; no normal students; so just add everyone
				userIdsToIgnore.add(new Integer(row.get("user_id")));
				
			});
		}


		//read django comment client role file
		//add ids of users with any privileged roles in the discussion forums to the Set of user ids to ignore
		File commentRoleFile = FileGetter.getFileEndingWith(courseDir, FileConstant.commentRole);
		try(BufferedReader reader = new BufferedReader(new FileReader(commentRoleFile))){
			Iterable<CSVRecord> rows = CSVFormat.MYSQL.withFirstRecordAsHeader().parse(reader);

			//TODO: optimization: table seems to sort privileged roles to the top,
			/// so as soon as a normal student role is encountered, program can stop reading?
			rows.forEach(row -> {
				//if this user has a role other than "Student", add their user id to the ignored Set
				if(!row.get("name").equals("Student"))
					userIdsToIgnore.add(new Integer(row.get("user_id")));
			});
		}


		//read course enrollment file, add data to id2Student Map
		File enrollFile = FileGetter.getFileEndingWith(courseDir, FileConstant.enrollFile);
		try(BufferedReader reader = new BufferedReader(new FileReader(enrollFile))){
			Iterable<CSVRecord> rows = CSVFormat.MYSQL.withFirstRecordAsHeader().parse(reader);
			rows.forEach(row -> {
				Student student = id2Student.get(new Integer(row.get("user_id")));
				if(student!=null) {
					student.setPaying(row.get("mode").equals("verified"));
					student.setEnrolledAt(OffsetDateTime.parse(row.get("created")+"Z",
							DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")));
				}
			});
		}


		//read certificate file, add data to id2Student Map
		File certificateFile = FileGetter.getFileEndingWith(courseDir, FileConstant.certificateFile);
		try(BufferedReader reader = new BufferedReader(new FileReader(certificateFile))){
			Iterable<CSVRecord> rows = CSVFormat.MYSQL.withFirstRecordAsHeader().parse(reader);

			rows.forEach(row -> {
				Student student = id2Student.get(new Integer(row.get("user_id")));
				if(student!=null) {
					student.setCertificated(row.get("status").equals("downloadable"));
				}
			});
		}


		//read auth_userprofile file, add data to id2Student Map
		File authuserprofileFile = FileGetter.getFileEndingWith(courseDir, FileConstant.authuserProFile);
		try(BufferedReader reader = new BufferedReader(new FileReader(authuserprofileFile))){
			Iterable<CSVRecord> rows = CSVFormat.MYSQL.withFirstRecordAsHeader().parse(reader);

			rows.forEach(row -> {
				Student student = id2Student.get(new Integer(row.get("user_id")));
				if(student!=null) {
					if(row.get("gender").equals("m"))
						student.setGender("m");
					else if(row.get("gender").equals("f"))
						student.setGender("f");

					//calculating it with only the year of birth, age may be off by one year

					try {student.setAge(student.getEnrolledAt().getYear() - Integer.parseInt(row.get("year_of_birth")));}
					catch (NumberFormatException nfe) {} //thrown if string could not be parsed as number; just continue

					student.setEducation(row.get("level_of_education").intern());	//.intern() won't do much since the strings are so short
					student.setCountry(row.get("country").intern());
				}
			});
		}
		


		File userIdMapFile = FileGetter.getFileEndingWith(courseDir, FileConstant.userIdMapFile);
		try(BufferedReader reader = new BufferedReader(new FileReader(userIdMapFile))){
			Iterable<CSVRecord> rows = CSVFormat.MYSQL.withFirstRecordAsHeader().parse(reader);

			rows.forEach(row -> {
				Integer userId = new Integer(row.get("id"));
				uid2Student.put(row.get("hash_id"), id2Student.get(userId));
			});
		}
		
		StudentsGetter.addSurveyToStudent(surveyFiles, uid2Student, userIdsToIgnore);
		
		id2Student.keySet().removeIf(userId -> userIdsToIgnore.contains(userId));
	}

	public static List<String> addSurveyToStudent(List<File> surveyFiles, Map<String, Student> uid2Student, Set<Integer> userIdsToIgnore ) {
		List<String> surveyQuestions = new ArrayList<String>();
		//read all survey result files, add data to surveyMap fields in Student objects, and to surveyQuestions List
		for(File surveyFile : surveyFiles) {
			try(BufferedReader reader = new BufferedReader(new FileReader(surveyFile))){
				CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

				int firstCol=17;	//question/answer data always starts at column with index 17 ('R' column in Excel)
				int lastCol=parser.getHeaderMap().get("uid")-1;	//question/answer data always ends just before column with header "uid"

				Iterator<CSVRecord> it = parser.iterator();

				CSVRecord row2 = it.next();//full question text in this row

				CSVRecord row3 = it.next();//this row indicates whether questions are multi-choice or open-answer

				//add indices of columns of interest to a List
				//also add question text to surveyQuestions List
				ArrayList<Integer> cols = new ArrayList<>();
				for(int col=firstCol; col<=lastCol; col++) {
					//if question is _probably_ multi-choice, single-answer
					// (can't tell for sure whether a multi-choice question is single-answer or multiple-answer)
					if( !( row3.get(col).contains("_TEXT") || row2.get(col).toLowerCase().contains("all that apply")) ) {
						cols.add(col);	//add this column to the list of columns to record questions and answers from
						surveyQuestions.add(row2.get(col));	//add this column's question text to the List
					}
				}

				it.forEachRemaining(row -> {
					String uid = row.get("uid");
					//skip if uid is blank
					if(uid.isEmpty())
						return;

					Student student = uid2Student.get(uid);
					//skip if uid did not match a Student
					// OR, if the Student's userId is in the Set to ignore (staff)
					if(student==null || userIdsToIgnore.contains(student.userId))
						return;

					//if Student's surveyMap is null, construct it
					if(student.surveyMap == null)
						student.surveyMap = new HashMap<>();

					//for each column with a multi-choice (and usually, single-select) question
					for(int col : cols) {
						if(row.get(col).isEmpty())	//if answer is blank, skip adding the mapping
							continue;

						//add entry to Student's surveyMap
						student.surveyMap.put(row2.get(col).intern(), row.get(col).intern());
					}
				});

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return surveyQuestions;

	}
	

}
