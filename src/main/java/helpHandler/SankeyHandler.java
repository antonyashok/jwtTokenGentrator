package helpHandler;

import java.io.IOException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.eclipse.jetty.server.Request;
import org.json.simple.JSONObject;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import model.Discussion;
import model.Step;
import model.Student;
import storage.DataManager;
import util.SankeyTable;

public class SankeyHandler {
	/** Expected values must exceed this value to run a Chi squared test */
	public static final int MIN_EXPECTED = 10;
	
	/**Structure for storing data related to one link and a Chi Squared Test of goodness of fit.*/
	private static class ChiSquareFitData{
		public double expectedInSet;
		public double expectedOutOfSet;
		public int observedInSet;
		public int observedOutOfSet;
		public double pValue;

		public ChiSquareFitData(double expectedInSet, double expectedOutOfSet, int observedInSet, int observedOutOfSet,
				double pValue) {
			this.expectedInSet = expectedInSet;
			this.expectedOutOfSet = expectedOutOfSet;
			this.observedInSet = observedInSet;
			this.observedOutOfSet = observedOutOfSet;
			this.pValue = pValue;
		}
	}

	/**Structure for storing data related to one link and a Chi Squared Test of independence.*/
	private static class ChiSquareIndepData{
		public int inSetAndOnLink;
		public int inSetAndOffLink;
		public int outOfSetAndOnLink;
		public int outOfSetAndOffLink;
		public double pValue;
		public double phi;

		public ChiSquareIndepData(int inSetAndOnLink, int inSetAndOffLink, int outOfSetAndOnLink, int outOfSetAndOffLink,
				double pValue, double phi) {
			this.inSetAndOnLink = inSetAndOnLink;
			this.inSetAndOffLink = inSetAndOffLink;
			this.outOfSetAndOnLink = outOfSetAndOnLink;
			this.outOfSetAndOffLink = outOfSetAndOffLink;
			this.pValue = pValue;
			this.phi = phi;
		}
	}


	public static void hanlderMethod(String targetAddress, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, JSONObject requestjson) {
		Map<Integer, Student> id2Student = DataManager.getDataInstance().getId2Student();
		Set<Integer> userIdsInCustomFilter = DataManager.getDataInstance().getUserIdsInCustomFilter();
		Table<Integer, Integer, Integer> linksTableVD = DataManager.getDataInstance().getLinksTableVD();
		 Table<Integer,Integer,Integer> linksTableUniqueVD = DataManager.getDataInstance().getLinksTableUniqueVD();
		Map<Integer, Student> id2StudentFiltered = new HashMap<>();

		//make a copy of the id2Student Map to filter Students out of; deep-copy mutable objects;
		id2Student.forEach((Integer id, Student student) ->{
			id2StudentFiltered.put(id, new Student(student));
		}); 


		id2StudentFiltered.values().removeIf(student -> student.getVerticalAndDiscussionRecords().isEmpty());
		

		int numParticipatingStudents = id2StudentFiltered.size();
		//filter
		if(requestjson.containsKey("node")) {
			SankeyFilter.nodeFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("links")) {
			SankeyFilter.linkFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("pay")) {
			SankeyFilter.payingFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("certificate")) {
			SankeyFilter.certificateFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("enrolled")) {
			SankeyFilter.enrolledFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("time")) {
			SankeyFilter.timeFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("period")) {
			SankeyFilter.periodFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("gender")) {
			SankeyFilter.genderFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("education")) {
			SankeyFilter.educationFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("age")) {
			SankeyFilter.ageFilter(id2StudentFiltered, requestjson);
		}
		if(requestjson.containsKey("custom")) {
			SankeyFilter.customFilter(id2StudentFiltered, userIdsInCustomFilter, requestjson);
		}
		if(requestjson.containsKey("clustering")) {
			SankeyFilter.clusteringFilter(id2StudentFiltered, requestjson);
		}

		//remove any students whose list of records became empty during the filtering process
		id2StudentFiltered.values().removeIf(student -> student.getVerticalAndDiscussionRecords().isEmpty());

		//Table of link data just for students in the filtered set
		Table<Integer,Integer,Integer> linksTableFiltered = null;

		//Map showing how many students re-viewed each step at least once
		Map<Step,Integer> step2RepeatNodeValue = null;

		//Table of structures; each structure contains data related to a Chi Squared test
		Table<Integer,Integer,Object> chiSquareDataTable = HashBasedTable.create();

		ChiSquareTest X = new ChiSquareTest();

		if(!requestjson.containsKey("mode") || requestjson.get("mode").equals("countTraversals")) {
			
			if(requestjson.containsKey("time") || requestjson.containsKey("period")) {
				linksTableFiltered = SankeyTable.computeTimeLinksVD(id2Student.values(),id2StudentFiltered.values());

			} else {
				linksTableFiltered = SankeyTable.computeLinksVD(id2StudentFiltered.values());
			}
			//p-values calculated with chi-square goodness of fit test
			
			linksTableFiltered.cellSet().forEach(cell -> {
				int observedInSet = cell.getValue();
				int observedTotal = linksTableVD.get(cell.getRowKey(), cell.getColumnKey());
				int observedOutOfSet = observedTotal - observedInSet;
				double expectedInSet = observedTotal* (double) id2StudentFiltered.size()/numParticipatingStudents;
				double expectedOutOfSet = observedTotal * (1 - (double) id2StudentFiltered.size() / numParticipatingStudents);

				if(expectedInSet > MIN_EXPECTED && expectedOutOfSet > MIN_EXPECTED) {
					double[] expected = {expectedInSet, expectedOutOfSet};
					long[] observed = {observedInSet, observedOutOfSet};
					double pValue = X.chiSquareTest(expected, observed);

					chiSquareDataTable.put(cell.getRowKey(), cell.getColumnKey(), 
							new ChiSquareFitData(expectedInSet, expectedOutOfSet, observedInSet, observedOutOfSet, pValue));
				}
			});

			
		} 
	   else if(requestjson.get("mode").equals("countStudents")){
		   if(requestjson.containsKey("time") || requestjson.containsKey("period")) {
				linksTableFiltered = SankeyTable.computeTimeLinksUniqueVD(id2Student.values(),id2StudentFiltered.values());
			} else {
				linksTableFiltered = SankeyTable.computeLinksUniqueVD(id2StudentFiltered.values());
			}
		   
		   Table<Integer,Integer,Integer> linksTableAll = DataManager.getDataInstance().getLinksTableUniqueVD();
			step2RepeatNodeValue = SankeyTable.computeRepeatNodeValuesUniqueVD(id2StudentFiltered.values());
			linksTableFiltered.cellSet().forEach(cell -> {
				int inSetAndOnLink = cell.getValue();
				int totalInSet = id2StudentFiltered.size();
				int inSetAndOffLink = totalInSet - inSetAndOnLink;
				int totalOnLink = linksTableAll.get(cell.getRowKey(), cell.getColumnKey());
				int outOfSetAndOnLink = totalOnLink - inSetAndOnLink;
				int totalOutOfSet = numParticipatingStudents - totalInSet;
				int outOfSetAndOffLink = totalOutOfSet - outOfSetAndOnLink;
				int totalOffLink = numParticipatingStudents - totalOnLink;
				int grandTotal = numParticipatingStudents;
				if(totalInSet*totalOnLink/grandTotal > MIN_EXPECTED && totalInSet*totalOffLink/grandTotal > MIN_EXPECTED
						&& totalOutOfSet*totalOnLink/grandTotal > MIN_EXPECTED && totalOutOfSet*totalOffLink/grandTotal > MIN_EXPECTED) {
					long[][] contTable = {{inSetAndOnLink, inSetAndOffLink},{outOfSetAndOnLink, outOfSetAndOffLink}};
					double pValue = X.chiSquareTest(contTable);
					double phi = ((long)inSetAndOnLink*outOfSetAndOffLink - (long)inSetAndOffLink*outOfSetAndOnLink)
							/ Math.sqrt((long)totalInSet*totalOutOfSet*totalOnLink*totalOffLink);

					chiSquareDataTable.put(cell.getRowKey(), cell.getColumnKey(), new ChiSquareIndepData(inSetAndOnLink, 
							inSetAndOffLink, outOfSetAndOnLink, outOfSetAndOffLink, pValue, phi));
				}

			});
		}
		
		 HashMap<String, Object> sankeyJSON = SankeyHandler.getSankeyJSON(step2RepeatNodeValue, linksTableFiltered, chiSquareDataTable);
		 response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			try {
				JSONObject.writeJSONString(sankeyJSON, response.getWriter());
			}
			catch(IOException e) {
				System.err.println(e.getMessage());
				return;
			}
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
	}
	
	public static HashMap<String, Object> getSankeyJSON(Map<Step,Integer> step2RepeatNodeValue, 
			Table<Integer,Integer,Integer> linksTableFiltered, 
			Table<Integer,Integer,Object> chiSquareDataTable ){
		
		 List<Step> allSteps = DataManager.getDataInstance().getVerticalsAndDiscussions();
		 Map<Step, Discussion> discussionMap = DataManager.getDataInstance().getDiscussionMap();
		HashMap<String, Object> sankeyJSON = new HashMap<>();
		ArrayList<Object> nodes = new ArrayList<>();
		ArrayList<Object> links = new ArrayList<>();
		sankeyJSON.put("nodes", nodes);
		sankeyJSON.put("links", links);

		// building node data
		HashMap<String, Object> node = new HashMap<>();
		node.put("name", "Start");
		nodes.add(node);
		for(Step step: allSteps) {
			node = new HashMap<>();
			node.put("name", step.toString());
			if(step.getStepNumInts().length == 4) {
				node.put("isDiscussion", 1);				
				Discussion discussion = discussionMap.get(step);
				node.put("post", discussion.getPostNum()+" by "+ discussion.getPostNumByStudents().size() + " students");
				node.put("postVote", discussion.getPostVote()+" by "+discussion.getPostVoteByStudents().size() + " students");
				node.put("comment", discussion.getCommentNum()+" by "+discussion.getCommentNumByStudents().size() + " students");
				node.put("response", discussion.getResponseNum()+" by "+discussion.getResponseNumByStudents().size() + " students");
				node.put("responseVote", discussion.getReponseVote()+" by "+discussion.getResponseVoteByStudents().size() + " students");
			}
			nodes.add(node);
			node = new HashMap<>();
			node.put("name", step.toString()+ " (Repeated)");
			if(step2RepeatNodeValue!=null)
				node.put("trueValue", step2RepeatNodeValue.get(step));
			if(step.getStepNumInts().length == 4) {
				node.put("isDiscussion", 1);
			}
			nodes.add(node);
		}
		node = new HashMap<>();
		node.put("name", "Leave");
		nodes.add(node);
		
		//building links data
		linksTableFiltered.cellSet().forEach(cell -> {
			HashMap<String, Object> link = new HashMap<>();
			link.put("source", cell.getRowKey());
			link.put("target", cell.getColumnKey());
			link.put("value", cell.getValue());


			Object cell2 = chiSquareDataTable.get(cell.getRowKey(), cell.getColumnKey());
			if(cell2 != null) {
				if(cell2 instanceof ChiSquareFitData) {
					ChiSquareFitData cell3 = (ChiSquareFitData) cell2;
					link.put("expectedInSet", cell3.expectedInSet);
					link.put("expectedOutOfSet", cell3.expectedOutOfSet);
					link.put("observedInSet", cell3.observedInSet);
					link.put("observedOutOfSet", cell3.observedOutOfSet);
					link.put("pValue", cell3.pValue);
				}
				else if(cell2 instanceof ChiSquareIndepData) {
					ChiSquareIndepData cell3 = (ChiSquareIndepData) cell2;
					link.put("inSetAndOnLink", cell3.inSetAndOnLink);
					link.put("inSetAndOffLink", cell3.inSetAndOffLink);
					link.put("outOfSetAndOnLink", cell3.outOfSetAndOnLink);
					link.put("outOfSetAndOffLink", cell3.outOfSetAndOffLink);
					link.put("pValue", cell3.pValue);
					link.put("phi", cell3.phi);
				}
			}
			links.add(link);
		});
		
		return sankeyJSON;
	} 


}
