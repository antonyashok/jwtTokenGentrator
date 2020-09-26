package helpHandler;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.eclipse.jetty.server.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import model.Step;
import model.Student;
import storage.DataManager;

public class MarimekkoHandler {

	/**Structure for storing statistical data related to one rectangle*/
	private static class StatsData{
		public Double pValue;	//using Double instead of double lets us store nulls
		public double phi;	//phi coefficient

		public StatsData(Double pValue, double phi) {
			this.pValue = pValue;
			this.phi = phi;
		}
	}

	private static final int MIN_EXPECTED = 10;	//minimum expected value required for Chi2 test
	/**Lower bounds of each age group. Last group is "65 +".*/
	private static final int[] AGE_BOUNDS = {0, 18, 25, 30, 35, 40, 45, 50, 55, 60, 65};


	/**Respond to a http request for marimekko chart data.
	 * @param response 
	 * @param request 
	 * @param baseRequest 
	 * @param target 
	 * @param requestjson	JSONObject of the JSON string in the request body.
	 * @throws IOException */
	public static void handlerMethod(String targetAddress, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, JSONObject requestjson) throws IOException{
		
		Map<Integer,Student> id2Student = DataManager.getDataInstance().getId2Student();

		//requestjson looks something like: {"graphtype":"marimekko", vars:["age", "education"], omitColumns:{...}, omitRows:{...}}
		JSONArray vars = (JSONArray) requestjson.get("vars");
		String var0 = (String) vars.get(0);
		String var1 = (String) vars.get(1);
		//accessors will contain a method named 'apply' that takes a Student and returns a String.
		// The String describes a 'group' that a student is in.
		final Function<Student,String> accessor0 = fieldName2Accessor(var0);
		final Function<Student,String> accessor1 = fieldName2Accessor(var1);
		
		
		//make a copy of the id2Students Map to filter Students out of; deep-copy mutable objects
		Map<Integer,Student> id2StudentFiltered = new HashMap<>();
		id2Student.forEach( (Integer id, Student student) -> {
			id2StudentFiltered.put(id, new Student(student));
		});
		
		//fetch sets of keys (group names) to omit
		JSONObject omitColumns = (JSONObject) requestjson.get("omitColumns");
		JSONObject omitRows = (JSONObject) requestjson.get("omitRows");
		
		//remove any Student in one of the groups indicated by requestjson.omitColumns or requestjson.omitRows
		id2StudentFiltered.values().removeIf(student -> {
			String key0 = accessor0.apply(student);
			if(omitColumns.containsKey(key0))
				return true;
			
			String key1 = accessor1.apply(student);
			if(omitRows.containsKey(key1))
				return true;
			
			return false;
		});
		
		
		int grandTotal = id2StudentFiltered.size();	//number of students left after filter operation
		
		//Table of (2 group names) to (number of students in both of those groups) 
		Table<String,String,Integer> mekkoData = HashBasedTable.create();
		
		id2StudentFiltered.values().forEach(student -> {
			String key0 = accessor0.apply(student);
			String key1 = accessor1.apply(student);
			
			//increment count in Table cell
			Integer count = mekkoData.get(key0, key1);
			if(count==null)
				mekkoData.put(key0, key1, 1);
			else
				mekkoData.put(key0, key1, ++count);
		});
		
		
		//matching Table for statistical data (p-values and phi coefficient)
		Table<String,String,StatsData> statsData = HashBasedTable.create();
		
		mekkoData.cellSet().forEach((Cell<String,String,Integer> cell) -> {
			//1st cell of contingency table
			int inBothGroups = cell.getValue();	//number of students in one rectangle of the Mekko chart
			
			//note: columns in the Mekko chart correspond to rows in the Table (com.google.common.collect.Table)
			// and vice versa
			
			//subtotal: number of students in this column of the Mekko chart
			int inMekkoColumn = 0;
			for( int val : mekkoData.row(cell.getRowKey()).values() )
				inMekkoColumn += val;
			
			//subtotal: number of students in this row (series of rectangles with same color) of the Mekko chart
			int inMekkoRow = 0;
			for( int val : mekkoData.column(cell.getColumnKey()).values() )
				inMekkoRow += val;
			
			//remaining 3 cells of contingency table
			int inOuterGroupButNotInnerGroup = inMekkoColumn - inBothGroups;
			int inInnerGroupButNotOuterGroup = inMekkoRow - inBothGroups;
			int outOfBothGroups = grandTotal - inBothGroups - inOuterGroupButNotInnerGroup - inInnerGroupButNotOuterGroup;
			
			//other two subtotals
			int notInMekkoColumn = grandTotal - inMekkoColumn;
			int notInMekkoRow = grandTotal - inMekkoRow;
			
			//Check for positive or negative correlation between these two groups.
			// Positive means that if a student is in one group, they are more (but not necessarily over 50%) likely to
			// also be in the other group.
			// Casting to long should prevent overflow.
			double phi = ((long)inBothGroups*outOfBothGroups - (long)inOuterGroupButNotInnerGroup*inInnerGroupButNotOuterGroup)
					/ Math.sqrt((long)inMekkoColumn*inMekkoRow*notInMekkoColumn*notInMekkoRow);
			
			Double pValue = null;
			
			//check minimum expected values
			if(inMekkoColumn*inMekkoRow/grandTotal >= MIN_EXPECTED
					&& inMekkoColumn*notInMekkoRow/grandTotal >= MIN_EXPECTED
					&& notInMekkoColumn*inMekkoRow/grandTotal >= MIN_EXPECTED
					&& notInMekkoColumn*notInMekkoRow/grandTotal >= MIN_EXPECTED) {
				//if all expected values are high enough, compute p-value for this rectangle of the Mekko chart
				long[][] contTable = {{inBothGroups, inInnerGroupButNotOuterGroup},
						{inOuterGroupButNotInnerGroup, outOfBothGroups}};
				
				ChiSquareTest X = new ChiSquareTest();
				pValue = X.chiSquareTest(contTable);
			}
			
			//put p-value and phi coefficient into Table
			statsData.put(cell.getRowKey(), cell.getColumnKey(), new StatsData(pValue, phi));
		});
		
		
		//output mekko data Table, p-values and phi coefficients as csv
		// set response headers
		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");		//content-type: text/csv; charset=utf-8
		
		// write response body
		//  print column headers
		final CSVPrinter printer;
		if(var0.equals(var1)) {
			printer = CSVFormat.DEFAULT
					.withHeader(var0, "value", "pValue", "phi")
					.print(response.getWriter());	//may throw IOException
		}
		else {
			printer = CSVFormat.DEFAULT
					.withHeader(var0, var1, "value", "pValue", "phi")
					.print(response.getWriter());	//may throw IOException
		}
		
		
		//  print data
		for(Cell<String,String,Integer> cell : mekkoData.cellSet()) {
			String rowKey = cell.getRowKey();
			String colKey = cell.getColumnKey();
			
			printer.print(rowKey);
			if(!var0.equals(var1))
				printer.print(colKey);
			
			printer.print(cell.getValue());
			
			StatsData x = statsData.get(rowKey, colKey);
			
			printer.print(x.pValue!=null ? x.pValue : "");
			printer.print(x.phi);
			
			printer.println();
		}
		
		printer.flush();
		
		// finish response
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}
	
	
	/**Given the name of a field of a Student object, return a Function that takes a Student and returns their group name for that field.
	 * ex. gender is stored as a char: 'm', 'f', or 'u'. These map to the group names "Male", "Female" and "Unknown".
	 * ex. age is stored as an int, with -1 indicating an unknown age. These map to Strings describing age ranges.
	 * 	ex. age 68 would map to the "65 +" group.
	 *  ex. age 31 would map to the "30 - 35" group.*/
	private static Function<Student,String> fieldName2Accessor(String fieldName){
		Function<Student,String> accessor=null;
		
		switch(fieldName) {
		case "gender":
			accessor = student -> {
				switch(student.getGender()) {
				case "m": return "Male";
				case "f": return "Female";
				default: return "Unknown";
				}
			};
			break;
		case "age":
			accessor = student -> {	//return String describing range that age falls into
				if(student.getAge() < 0)
					return "Unknown";
				else if(student.getAge() >= AGE_BOUNDS[AGE_BOUNDS.length-1])
					return AGE_BOUNDS[AGE_BOUNDS.length-1] + " +";
				
				int b=1;
				for(b=1; b < AGE_BOUNDS.length; b++) {
					if(student.getAge() < AGE_BOUNDS[b])
						break;
				}
				
				return AGE_BOUNDS[b-1] + " - " + (AGE_BOUNDS[b]-1);
			};
			break;
		case "education":
			accessor = student -> student.getEducation();	//return long String of education
			break;
		case "paying":
			accessor = student -> student.isPaying()? "Paid" : "Not Paid";
			break;
		case "enrolldate":
			//work out how many weeks late this student enrolled (rounded up)
			//-1 = 1-2 weeks early, 0 = 0-1 weeks early, 1 = 0-1 weeks late
			accessor = student -> {
				int weeksLate = (int) ChronoUnit.WEEKS.between(DataManager.getDataInstance().getCourseStart(), student.getEnrolledAt());
				if(student.getEnrolledAt().isAfter(DataManager.getDataInstance().getCourseStart()))
					weeksLate++;
				
				int weeksLateAbs = weeksLate > 0 ? weeksLate : -weeksLate;
				return new StringBuilder().append(weeksLateAbs)
						.append(weeksLateAbs == 1 ? " week" : " weeks")
						.append(weeksLate > 0 ? " late" : " early")
						.toString();
			};
			break;
		case "higheststep":
			accessor = student -> {
				Step highestStep = student.getHighestStepSeen();
				return highestStep==null ? "0 (None)" : highestStep.toString();
			};
			break;
		case "highestchapter":
			accessor = student -> {
				Step highestChapter = student.getHighestChapterSeen();
				return highestChapter==null ? "0 (None)" : highestChapter.toString();
			};
			break;
		case "numberofsteps":
			accessor = student -> {
				return Integer.toString(student.getNumberOfStepsSeen());
			};
			break;
		case "country":
			accessor = student -> student.getCountryName();
			break;
		}
		
		
		//If fieldName is anything else, assume it's a question string.
		// This is a request to classify students based on their response to a question.
		if(accessor==null) {
			accessor = student -> {
				if(student.surveyMap==null)
					return "(Did not do any surveys)";
				
				String answer = student.surveyMap.get(fieldName);
				return answer==null ? "(No answer)" : answer;
			};
		}
		
		return accessor;
	}


}
