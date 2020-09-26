package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import model.Student;

public class SurveyParser {

	public static  List<String>  getSurveyQuestions(List<File> surveyFiles,  Map<String, Student> uid2Student,  Set<Integer> userIdsToIgnore  ) throws FileNotFoundException, IOException {
		List<String> surveyQuestions = new ArrayList<>();
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

					}
				}
		
		return surveyQuestions;
	}
}
