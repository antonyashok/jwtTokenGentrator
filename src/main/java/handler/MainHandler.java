package handler;

import java.io.BufferedReader;

import java.io.Console;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import helpHandler.ActiveTimeGraph;
import helpHandler.CSVHandler;
import helpHandler.MarimekkoHandler;
import helpHandler.SankeyHandler;
import javafx.scene.chart.PieChart.Data;
import storage.DataManager;

public class MainHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(!request.getMethod().equals("POST"))	//ignore non-POST requests
			return;
		//ignore requests without content-type application/json
		
		String contentType = request.getContentType();
		if(contentType==null || !contentType.contains("application/json"))
			return;

		//parse json in request body
		JSONObject requestjson=null;
		try {
			BufferedReader reqBody = request.getReader();	//can throw IOException
			JSONParser parser=new JSONParser();
			requestjson = (JSONObject) parser.parse(reqBody);	//can throw ParseException or IOException
		} catch (IOException e) {
			System.err.println("MainHandler IOException : " + e.getMessage());
			return;
		} catch (org.json.simple.parser.ParseException e) {
			System.err.println("MainHandler parseException : " + e.getMessage());
			return;
		}
		if(requestjson.containsKey("course")) {
			String courseName = DataManager.getDataInstance().getCourseName();
			Map<Integer, List<Integer>> clusterResult = DataManager.getDataInstance().getClusterResult();
			int hasCluster = 0;
			if(!clusterResult.isEmpty()) {
				hasCluster = clusterResult.keySet().size();
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			JSONObject obj = new JSONObject();
			obj.put("courseName", courseName);
			obj.put("hasCluster", hasCluster);
			response.getWriter().write(obj.toJSONString());
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
		}else if(requestjson.containsKey("graphtype")) {
			switch((String) requestjson.get("graphtype")) {
			case "VDSankey":
				System.out.print(requestjson);
				SankeyHandler.hanlderMethod(target, baseRequest, request, response, requestjson);
				break;
			case "marimekko":
				MarimekkoHandler.handlerMethod(target, baseRequest, request, response, requestjson);	
				break;	
			case "funnel":
				CSVHandler.map2CSVHandler(DataManager.getDataInstance().getStepFrequencies(), "Step", "Students who Viewed this Step", response, baseRequest);
				break;
			case "funneldecreasing":
				CSVHandler.map2CSVHandler(DataManager.getDataInstance().getParticipationFunnel(), "Step", "Students who Viewed this or a Higher Step", response, baseRequest);
				break;
			case "exitchrono":
				CSVHandler.map2CSVHandler(DataManager.getDataInstance().getExitStepsChrono(), "Last Step Viewed", "Number of Students", response, baseRequest);
				break;
			case "exithighest":
				CSVHandler.map2CSVHandler(DataManager.getDataInstance().getExitStepsHighest(), "Highest Step Reached", "Number of Students", response, baseRequest);
				break;
			case "activetime":
				List<String> r1 = new ArrayList<String>();
				List<Integer> r2 = new ArrayList<Integer>();
				ActiveTimeGraph.getActiveTimeLists(r1, r2);	
				@SuppressWarnings("rawtypes") List[] lists = {r1,r2};
				String[] colNames = {"Present at Date+Time (UTC)", "Number of Students"};
				CSVHandler.lists2CSVHandler(lists, colNames, response, baseRequest);
				break;
			case "discussionSummary":
				CSVHandler.discussionMap2CSVHandler(
						DataManager.getDataInstance().getDiscussionMap(), 
						"Discussion Category",
						"Viewed","ViewedByStudent",
						"Post", "PostByStudent",
						"Comment", "CommentByStudent",
						"Vote", "VoteByStudent",						
						response, baseRequest);

			}
		}else if(requestjson.containsKey("data")) {
			switch((String) requestjson.get("data")) {
			case "startDate":
				//set response headers
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");		//content-type: application/json; charset=utf-8
				
				//write response body
				OffsetDateTime startDate = DataManager.getDataInstance().getCourseStart();
				ArrayList<Integer> startDateFields = new ArrayList<>();
				startDateFields.add(startDate.getYear());
				startDateFields.add(startDate.getMonthValue() - 1);	//-1 because in Javascript, January is 0
				startDateFields.add(startDate.getDayOfMonth());
				
				response.getWriter().write(JSONArray.toJSONString(startDateFields));
				
				//finish response
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				
				break;
			case "endDate":
				//set response headers
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");		//content-type: application/json; charset=utf-8
				
				//write response body
				OffsetDateTime endDate = DataManager.getDataInstance().getCourseEnd();
				ArrayList<Integer> endDateFields = new ArrayList<>();
				endDateFields.add(endDate.getYear());
				endDateFields.add(endDate.getMonthValue() - 1);	//-1 because in Javascript, January is 0
				endDateFields.add(endDate.getDayOfMonth());
				
				response.getWriter().write(JSONArray.toJSONString(endDateFields));
				
				//finish response
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				
				break;
			case "surveyQuestions":
				//set response headers
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");		//content-type: application/json; charset=utf-8
				
				//write response body
				response.getWriter().write(JSONArray.toJSONString(DataManager.getDataInstance().getSurveyQuestions()));
			
				
				//finish response
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				
				break;
			}
		}
	}



}
