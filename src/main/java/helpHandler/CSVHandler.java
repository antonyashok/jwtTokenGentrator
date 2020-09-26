package helpHandler;

import java.io.IOException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jetty.server.Request;

import model.Discussion;
import model.Step;
import model.Student;

public class CSVHandler {

	
	/**Helper method to output Map entries as 2-column CSV. Output goes straight to the response Object's writer.
	 * @param	map			Map to output as 2-column CSV. Keys and values will be converted to Strings with their .toString() method.
	 * @param	col0Name	name of key column
	 * @param	col1Name	name of value column*/
	public static <K,V> void map2CSVHandler(Map<K,V> map, String col0Name, String col1Name,
			HttpServletResponse response, Request baseRequest) throws IOException{
		//set response headers
		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");		//content-type: text/csv; charset=utf-8
		//write response body
		// print headers
		final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(col0Name, col1Name).print(response.getWriter());
		
		// print data
		for(Map.Entry<K, V> entry : map.entrySet()) {
			printer.print(entry.getKey().toString());
			printer.print(entry.getValue().toString());
			printer.println();
		}
		
		printer.flush();
		
		//finish response
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	} 
	
	/**Helper method to output entries from 2 lists as 2-column CSV. Output goes straight to the response Object's writer.
	 * @param	data to put in body of CSV; each List corresponds to a column; all Lists should have the same size.
	 * @param	column headers; this array should be the same length as colData.*/
	public static void lists2CSVHandler(@SuppressWarnings("rawtypes") List[] colData, String[] colNames, 
			HttpServletResponse response, Request baseRequest) throws IOException{
		//TODO: check lengths/sizes
			
		//set response headers
		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");		//content-type: text/csv; charset=utf-8
		
		//write response body
		// print headers
		final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(colNames).print(response.getWriter());
		
		// print data
		for(int i=0; i<colData[0].size(); i++) {
			for(int j=0; j<colData.length; j++) {
				printer.print(colData[j].get(i).toString());
			}
			printer.println();
		}
		
		printer.flush();
		
		//finish response
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}

	
	public static <K,V> void discussionMap2CSVHandler(Map<Step, Discussion> discussionMap, 
			String col0Name, 
			String col1Name, String col2Name,
			String col3Name, String col4Name,
			String col5Name, String col6Name,
			String col7Name, String col8Name,
			HttpServletResponse response, Request baseRequest
			) throws IOException {
		
		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");
		final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(col0Name,
				col1Name, col2Name, 
				col3Name, col4Name,
				col5Name, col6Name, 
				col7Name, col8Name
				).print(response.getWriter());
		
		
		discussionMap.forEach((k, v) -> {
			Set<Student> commentStudent = new HashSet<Student>();
			commentStudent.addAll(v.getCommentNumByStudents());
			commentStudent.addAll(v.getResponseNumByStudents());
			
			Set<Student> voteStudent = new HashSet<Student>();
			voteStudent.addAll(v.getResponseVoteByStudents());
			voteStudent.addAll(v.getPostVoteByStudents());
			try {
				printer.printRecord(k.toString(),
						v.getViewedNum(), v.getViewedNumByStudents().size(), 
						v.getPostNum(), v.getPostNumByStudents().size(),
						v.getCommentNum()+v.getResponseNum(), commentStudent.size(),
						v.getReponseVote()+v.getPostVote(), voteStudent.size()
						);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		System.out.println("the data "+ printer);
		printer.flush();
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	} 
}
