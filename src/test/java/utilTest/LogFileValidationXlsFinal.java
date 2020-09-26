package utilTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import constant.DisConstant;
import constant.NavigationConstant;
import model.DiscussionStep;
import model.LogRowData;
import model.Step;
import storage.DataManager;
import util.CourseParser;
import util.DiscussionParser;
import util.TabAnalysis;

public class LogFileValidationXlsFinal {
	public static int numLogs;

	public static AtomicInteger rowNum = new AtomicInteger(0);
	public static AtomicInteger notePadCount = new AtomicInteger(0);
	public static AtomicInteger rowNum1 = new AtomicInteger(0);
	public static AtomicInteger listCount = new AtomicInteger(0);
	public static AtomicInteger recordCount = new AtomicInteger(0);
	public static AtomicInteger logNameCount = new AtomicInteger(0);
	public static AtomicInteger logResultCount = new AtomicInteger(0);	
	private static String[] columns = { "File Name", "Line No", "Event Name", "Status" };

	
	
	public static void main(String[] args) throws IOException, ParseException {
		System.out.println("Started");
		File logsDir = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		File structFile = new File("C:\\DataVisual\\tree\\RJ101x\\VictoriaX-RJ101x-2T2018-course_structure-prod-analytics.json");
		JSONParser jsonParser=new JSONParser();
		JSONObject structFileRootObj = (JSONObject) jsonParser.parse(new BufferedReader(new FileReader(structFile)));
		Map<String, DiscussionStep> id2Discussion = DiscussionParser.mapId2Discussion(structFileRootObj);
		Map<String, Step> hashcode2Vertical = CourseParser.mapHashcode2Vertical(structFileRootObj);  //169
		Map<String, Step> hashcode2Sequential = CourseParser.mapHashcode2Sequential(structFileRootObj);  //49  list of steps
		List<Step> allSequentials = new ArrayList<>(hashcode2Sequential.values()); //49  -->no hashcode  //1.1 Welcome to RJ101x, 1.2 Meet Your Instructors, 1.3 How the Course Works,
		Map<String, List<String>> sequentialToVerticalMap = CourseParser.mapSequentialToVertical(structFileRootObj); //59cbda5a42424e06b110fe6708997f8a=[c9b0e8059e52419e8029cddbd2995d10,
		DataManager.getDataInstance().setHashcode2Sequential(hashcode2Sequential);
		DataManager.getDataInstance().setHashcode2Vertical(hashcode2Vertical);
		DataManager.getDataInstance().setId2Discussion(id2Discussion);
		DataManager.getDataInstance().setAllSequentials(allSequentials);
		DataManager.getDataInstance().setSequentialToVerticalMap(sequentialToVerticalMap); // helper structure to analysis the vertical index

		getLogFiles(logsDir, "course-v1:VictoriaX+RJ101x+2T2018");
	}
	
	// gets all logfiles and validate them.

	public static void getLogFiles(File logsDir, String courseID) throws IOException {
		Map<String, List<LogRowData>> logRowDataMap = new HashMap<>();   // for creating map for workbook with sheet name and records list
		List<LogRowData> logRowDataList = new ArrayList<>();  // add list of record details to list 
		File[] logFiles = logsDir.listFiles(file -> file.getName().endsWith(".log"));
		List<LogRowData> notePadList = new ArrayList<>();  // add list of record details to list 
		numLogs = logFiles.length;
		Arrays.stream(logFiles).forEach(logFile -> {
			if (logFile.getName().endsWith(".log")) {
				if (logFile.isFile()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
						LogFileValidationXls.readLogFile(reader, courseID, logRowDataList, logFile.getName());
						System.out.println("recordCount :: "+recordCount);
					} catch (IOException | ParseException x) {
						System.err.format("IOException: %s%n", x);
					}
				} 
			}
			if (logRowDataList.size() > 900000) {
				System.out.println(logRowDataList.size());
				//copy, mutable, 
//				List<LogRowData> temp = new ArrayList<>();
//				temp = logRowDataList;
//				logRowDataMap.put("Log File Validation " + listCount.getAndAdd(1), temp);
				logRowDataMap.put("Log File Validation " + listCount.getAndAdd(1), logRowDataList.stream().collect(Collectors.toList())); //Java8 clone
				System.out.println("***********************************************Test-1 "+logRowDataList.size());
				notePadList.addAll(logRowDataList);
				logRowDataList.clear();
			}
			rowNum.set(0);  // end of the file make line number(rownumber) as zero.
		});
		System.out.println("***********************************************Test0");
		if (logRowDataList.size() > 0) {
			System.out.println("***********************************************Test");
			System.out.println(logRowDataList.size());
//			List<LogRowData> temp = new ArrayList<>();
//			temp = logRowDataList;
//			logRowDataMap.put("Log File Validation " + listCount.getAndAdd(1), temp);
			logRowDataMap.put("Log File Validation" + listCount.getAndAdd(1), logRowDataList.stream().collect(Collectors.toList()));
			notePadList.addAll(logRowDataList);
			logRowDataList.clear();
		}
		//generateXlsSheet(logRowDataMap);
		//generateTextFile(logRowDataMap);
		generateTextFileFinal(notePadList);
		System.out.println("Done");
	}

	public static void readLogFile(BufferedReader reader, String courseId, List<LogRowData> logRowDataList, String fileName) throws IOException, ParseException {
		
		final JSONParser parser = new JSONParser();
		Map<String, DiscussionStep> id2Discussion = DataManager.getDataInstance().getId2Discussion();
		Map<String, Step> hashcode2Vertical = DataManager.getDataInstance().getHashcode2Vertical();
		Map<String, Step> hashcode2Sequential = DataManager.getDataInstance().getHashcode2Sequential();
		List<Step> allSequentials = DataManager.getDataInstance().getAllSequentials();
		
		Map<String, List<String>> sequentialToVerticalMap = DataManager.getDataInstance().getSequentialToVerticalMap();

		reader.lines().forEach(line -> {
			LogRowData logRowData = new LogRowData(); 
			//add1: add filename and rownumber details to logRowData object.
			System.out.println(fileName);
			System.out.println(rowNum.addAndGet(1));
			logRowData.setFileName(fileName);
			logRowData.setRowNum(rowNum.addAndGet(1));
			
			JSONObject rootObj;
			try {
				rootObj = (JSONObject) parser.parse(line);
			} catch (org.json.simple.parser.ParseException e) {
				
				System.err.println(e.getMessage());
				//add2: unable to parse the line (update with Json Parser Error for not a parser object)
				logRowData.setEventName("JSON Parser Error");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				recordCount.addAndGet(1);
				return;
			}			
			if (((JSONObject) rootObj.get("context"))==null) {
				//add3: Context not available
				logRowData.setEventName("Context not available");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				recordCount.addAndGet(1);
				return;
			}				
			if (!((JSONObject) rootObj.get("context")).get("course_id").equals(courseId)) {
				//add4: Course Id is not same
				String addCourseText="Course Id not available:: "+((JSONObject)rootObj.get("context")).get("course_id");
				logRowData.setEventName(addCourseText);
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				recordCount.addAndGet(1);
				return; // this is to check whether the line is relevant to the course.
			}			
			if (!((JSONObject) rootObj.get("context")).get("org_id").equals("VictoriaX")) {
				//add4: Course Id is not same
				logRowData.setEventName("Org_id is mismatched");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				recordCount.addAndGet(1);
				return; // this is to check whether the line is relevant to the course.
			}

			// check user_id on event, skip if user_id is null, or if user_id is in Set of
			// user_ids to ignore:
			//----------------------------------------------------------------------------------
			//Occasionally, an event is recorded with a missing or blank context.user_id value. This can occur when a user logs out, or the login session times out, 
			//while a browser window remains open. Subsequent actions are logged, but the system cannot supply the user identifier. 
			//EdX recommends that you ignore these events during analysis.
			//------------------------------------------------------------------------------------
			// http://edx.readthedocs.io/projects/devdata/en/latest/internal_data_formats/tracking_logs/common_fields.html#context-member-fields-common-to-all-events
			Long userIdLong = (Long) ((JSONObject) rootObj.get("context")).get("user_id");

			if (userIdLong == null) {
				//add5: User Id not available
				logRowData.setEventName("User Id not available");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				recordCount.addAndGet(1);
				return;
			}

			// get Student object for the student in this JSON string, skip if user_id  ??
			// didn't match any known Student

			// check username on event, skip if username is blank
			//-------------------------------------------------------------------------------------------------------------------------
			//Occasionally, an event is recorded with a blank username value. This can occur when a user logs out, or the login session times out, 
			//while a browser window remains open. Subsequent actions are logged, but the system cannot supply the user identifier. EdX recommends
			//that you ignore these events during analysis.
			//-------------------------------------------------------------------------------------------------------------------------
			// http://edx.readthedocs.io/projects/devdata/en/latest/internal_data_formats/tracking_logs/common_fields.html#username-field
			//-------------------------------------------------------------------------------------------------------------------------
			if (rootObj.get("username").equals("")) {
				// add6: Username is Blank
				logRowData.setEventName("UserName-Blank");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				recordCount.addAndGet(1);
				return;
			}

			// begin to analyze the event
			// check type of event, don't proceed further if not a type of interest (one of
			// the course navigation events)
			// begin to analyze the event
			String eventType = null;
			if (rootObj.get("name") != null) {                                  //-->root
				String eventString = (String) rootObj.get("name");
				if (!NavigationConstant.navigationEvents().contains(eventString)
						&& (!DisConstant.discussionEvents().contains(eventString))) {
					//add7: Irrelvant Event Type-name
					logRowData.setEventName("Irrelvant Event Type-name ");
					logRowData.setStatus("In Valid");
					logRowDataList.add(logRowData);
					recordCount.addAndGet(1);
					return;
				}
				eventType = eventString;
			} else if (rootObj.get("event_type") != null) {
				String eventString = (String) rootObj.get("event_type");
				// if event_type is not in eventTypes Set
				if ((!NavigationConstant.navigationEvents().contains(eventString))
						&& (!DisConstant.discussionEvents().contains(eventString))) {
					//add7: Irrelvant Event Type-event_type
					logRowData.setEventName("Irrelvant Event Type-event_type");
					logRowData.setStatus("In Valid");
					logRowDataList.add(logRowData);
					recordCount.addAndGet(1);
					return;
				}
				eventType = eventString;
			} else {
				//add9: Event Type Not Available
				logRowData.setEventName("Event Type Not Available");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				recordCount.addAndGet(1);
				return;
			}			
			if(DisConstant.discussionEvents().contains(eventType)) {
				// analyze the discussion behavior
				JSONObject eventObj;
				try {
					eventObj = (JSONObject) parser.parse(rootObj.get("event").toString());
					String session = (String)rootObj.get("session"); // check with ip 
					if(session==null || session.isEmpty()){
//						if(rootObj.get("ip")!=""){//in the case of mobile and
						//if(event_source=='mobile')// if the event source is Mobile
							LogRowData logRowData1 = new LogRowData(); 
							logRowData1.setFileName(fileName);
							logRowData1.setRowNum(rowNum.get());
							logRowData1.setEventName("Session not available");
							logRowDataList.add(logRowData1);  //+1    line num 100   objnum 1000
							recordCount.addAndGet(1);
							// else if(event_source=='server')
							// else return							
//						return;							
//						}
					}
//						missingSessions.incrementAndGet();
					String discussionId = eventObj.get("commentable_id").toString(); // this id is to match the discussion id;
					DiscussionStep discussion = id2Discussion.get(discussionId);
					if(discussion == null) {
						logRowData.setEventName("unrecognisedHashcode");
						logRowData.setStatus("In Valid");
						logRowDataList.add(logRowData);					//+2	line num 100   objnum 1000
//						unrecognisedHashcode.incrementAndGet();
						recordCount.addAndGet(1);
						return;
					}						
					
				} catch (org.json.simple.parser.ParseException e) {
					e.printStackTrace();
					logRowData.setEventName("JSON Parser Event Error");
					logRowData.setStatus("In Valid");
					logRowDataList.add(logRowData);
					recordCount.addAndGet(1);
					return;
				}
			} else if(NavigationConstant.navigationEvents().contains(eventType)) {
				// analyze the vertical behavior
				Step step=null;
				String pageUrl=(String) rootObj.get("page");
				if(pageUrl!=null){
					if(eventType.equals(NavigationConstant.navLink)) {
						String verticalHash = null;
						JSONObject eventObj;
						try {
							eventObj = (JSONObject) parser.parse((String) rootObj.get("event"));
							String targetUrl = (String) eventObj.get("target_url");
							int jump_to_index = targetUrl.indexOf("jump_to");
							int block_index = targetUrl.indexOf("vertical+block");
							int jump_to_vertical = targetUrl.indexOf("jump_to_id");
							if(jump_to_index!=-1 && block_index!=-1 && jump_to_index < block_index) {
								verticalHash = targetUrl.substring(targetUrl.length()-32);
							}else if(jump_to_vertical!=-1) {
								verticalHash = targetUrl.substring(targetUrl.length()-32);
							}
							if(verticalHash == null) {
								logRowData.setEventName("Hashcode null in sequencial ");
								logRowData.setStatus("In Valid");
								logRowDataList.add(logRowData);
								recordCount.addAndGet(1);
								return;
							}
							step = hashcode2Vertical.get(verticalHash);
							if(step==null){
								//unrecognisedHashcode.incrementAndGet();	//probably results from deleted or modified course content ?
								logRowData.setEventName("Invalid Hashcode in sequencial ".concat(verticalHash));
								logRowData.setStatus("In Valid");
								logRowDataList.add(logRowData);
								recordCount.addAndGet(1);
								return;
							}
						} catch (org.json.simple.parser.ParseException e) {
							e.printStackTrace();
							logRowData.setEventName("Invalid Hashcode in sequencial ".concat(verticalHash));
							logRowData.setStatus("In Valid");
							logRowDataList.add(logRowData);
							recordCount.addAndGet(1);
							return;
						}
					}else {
						int x=pageUrl.indexOf("/courseware/");
						if(x!=-1){
							try {
								//throws out of bounds exception if pageUrl is too short and does not contain the sequential hashcode
								//returns null if a hashcode was found, but not recognized
								String sequentialHash = pageUrl.substring(x+45, x+77); 
								Step sequentialObj = hashcode2Sequential.get(sequentialHash);
								int currentSeqIndex = allSequentials.indexOf(sequentialObj);
								String preSeqHash = null;
								String nextSeqHash = null;
								if(currentSeqIndex-1>=0) {
									preSeqHash= allSequentials.get(currentSeqIndex -1).getHashcode();						
								}
								if(currentSeqIndex+1<allSequentials.size()) {
									nextSeqHash = allSequentials.get(currentSeqIndex+1).getHashcode();
								}

								String verticalHash = TabAnalysis.verticalHash(rootObj, eventType, sequentialHash, sequentialToVerticalMap, preSeqHash, nextSeqHash);
								if(verticalHash == null) {
									logRowData.setEventName("Hashcode null in sequencial ");
									logRowData.setStatus("In Valid");
									logRowDataList.add(logRowData);
									recordCount.addAndGet(1);
									return;
								}
								step=hashcode2Vertical.get(verticalHash);
								if(step==null){
									//unrecognisedHashcode.incrementAndGet();	//probably results from deleted or modified course content ?
									logRowData.setEventName("Invalid Hashcode in sequencial ".concat(verticalHash));
									logRowData.setStatus("In Valid");
									logRowDataList.add(logRowData);
									recordCount.addAndGet(1);
									return;
								}
							}
							catch (java.lang.StringIndexOutOfBoundsException e) {	//if sequential hashcode was not found in pageUrl
								logRowData.setEventName("Invalid Hashcode in sequencial ");
								logRowData.setStatus("In Valid");
								logRowDataList.add(logRowData);
								recordCount.addAndGet(1);
								return;
								//fall back to event.id field; see below
							} catch (org.json.simple.parser.ParseException e) {
								e.printStackTrace();
								logRowData.setEventName("Invalid Hashcode in sequencial ");
								logRowData.setStatus("In Valid");
								recordCount.addAndGet(1);
								logRowDataList.add(logRowData);
							}
						}else{
							//coursewareNotFound.incrementAndGet();
							logRowData.setEventName("Courseware not available");
							logRowData.setStatus("In Valid");
							logRowDataList.add(logRowData);
							recordCount.addAndGet(1);
							//this seems to happen when the student viewed the course's 'table of contents' (?)
							//so, the student isn't really viewing any Step now
							return;
						}
					}
				} else {
					//pageFieldNotFound.incrementAndGet();
					logRowData.setEventName("Page field not avaliable for sequencial");
					logRowData.setStatus("In Valid");
					logRowDataList.add(logRowData);
					recordCount.addAndGet(1);
					return;
				}	
				//if using the page field didn't work, try the event.id field
				//this field should be present for next_selected, previous_selected and tab_selected events
				//look for "type@sequential+block@", then look ahead for sequential hashcode

				if(step==null) {	//if step could not be determined from the page field
					//parse event json string
					JSONObject eventObj=null;
					try {
						eventObj = (JSONObject) parser.parse((String) rootObj.get("event"));
					}
					catch (org.json.simple.parser.ParseException e) {
						System.err.println(e.getMessage());
						logRowData.setEventName("JSON Parser Event Error");
						logRowData.setStatus("In Valid");
						logRowDataList.add(logRowData);
						recordCount.addAndGet(1);
						return;
					}

					String eventId = (String) eventObj.get("id");
					if(eventId!=null) {
						int y=eventId.indexOf("type@sequential+block@");
						if(y!=-1) {
							String sequentialHash  = eventId.substring(y+22);
							step = hashcode2Vertical.get(eventId.substring(y+22));	//TODO: put this in a try block to catch outofboundsexception?

							Step sequentialObj = hashcode2Sequential.get(sequentialHash);
							int currentSeqIndex = allSequentials.indexOf(sequentialObj);
							String preSeqHash = null;
							String nextSeqHash = null;
							if(currentSeqIndex-1>=0) {
								preSeqHash= allSequentials.get(currentSeqIndex -1).getHashcode();						
							}
							if(currentSeqIndex+1<allSequentials.size()) {
								nextSeqHash = allSequentials.get(currentSeqIndex+1).getHashcode();
							}

							String verticalHash;
							try {
								verticalHash = TabAnalysis.verticalHash(rootObj, eventType, sequentialHash, sequentialToVerticalMap, preSeqHash, nextSeqHash);
								if(verticalHash == null) {
									logRowData.setEventName("verticalHash is null");//add++
									logRowData.setStatus("In Valid");
									recordCount.addAndGet(1);
									logRowDataList.add(logRowData);
									return;
								}
								step=hashcode2Vertical.get(verticalHash);
								if(step==null){
									//unrecognisedHashcode.incrementAndGet();	//probably results from deleted or modified course content ?
									logRowData.setEventName("Hashcode null in sequencial ");
									logRowData.setStatus("In Valid");
									recordCount.addAndGet(1);
									logRowDataList.add(logRowData);
									return;
								}
							} catch (java.lang.StringIndexOutOfBoundsException e) {
								e.printStackTrace();
								logRowData.setEventName("Hashcode null in sequencial ");
								logRowData.setStatus("In Valid");
								recordCount.addAndGet(1);
								logRowDataList.add(logRowData);
								return;
							} catch (org.json.simple.parser.ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								logRowData.setEventName("Hashcode null in sequencial ");
								logRowData.setStatus("In Valid");
								recordCount.addAndGet(1);
								logRowDataList.add(logRowData);
								return;
							}

						}
						else {
							System.err.println("problem string 2: "+eventId);
							//hashcodeNotFoundInEventIdField.incrementAndGet();
							logRowData.setEventName("Hashcode not available in sequencial");
							logRowData.setStatus("In Valid");
							recordCount.addAndGet(1);
							logRowDataList.add(logRowData);
							return;
						}
					}
					else {
						//eventIdNotFound.incrementAndGet();
						logRowData.setEventName("Event id not available in sequencial");
						logRowData.setStatus("In Valid");
						recordCount.addAndGet(1);
						logRowDataList.add(logRowData);
						return;
					}
				}
			
			}
			logRowData.setEventName("");
			logRowData.setStatus("Valid");
			logRowData.setContent(line);
			recordCount.addAndGet(1);
			logRowDataList.add(logRowData);		
			});
	}


	// Reference :: https://www.callicoder.com/java-write-excel-file-apache-poi/
	//https://poi.apache.org/apidocs/dev/org/apache/poi/xssf/streaming/SXSSFWorkbook.html
	private static void generateXlsSheet(Map<String, List<LogRowData>> logRowDataMap)
			throws FileNotFoundException, IOException {
		String file = "C:\\DharaniCh\\SWEN589_Project\\datastore\\Log.xlsx";
		SXSSFWorkbook workbook = new SXSSFWorkbook(); 
		for (Map.Entry<String, List<LogRowData>> entry : logRowDataMap.entrySet()) {// sheetname and record deatils(list)
			// Create a Sheet
			Sheet sheet = workbook.createSheet(entry.getKey());
			// Create a Font for styling header cells   --> headerFont
			Font headerFont = workbook.createFont();                  
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());
			// Create a CellStyle with the font
			CellStyle headerCellStyle = workbook.createCellStyle();// 3rd party object // headerFont--> headerCellStyle
			headerCellStyle.setFont(headerFont);
			// Create a Row
			Row headerRow = sheet.createRow(0);   // 0th row
			// Create cells
			for (int k = 0; k < columns.length; k++) {
				Cell cell = headerRow.createCell(k);
				cell.setCellValue(columns[k]);
				cell.setCellStyle(headerCellStyle);
			}
			// Create Cell Style for formatting Date
			// Create Other rows and cells with logRowData data
			entry.getValue().stream().forEach(logRowData -> {
//				if (null != logRowData) {
					Row row = sheet.createRow(rowNum1.addAndGet(1));// index 1 row
					row.createCell(0).setCellValue(logRowData.getFileName());
					row.createCell(1).setCellValue(logRowData.getRowNum());
					row.createCell(2).setCellValue(logRowData.getEventName());
					row.createCell(3).setCellValue(logRowData.getStatus());
//				}
			});

			// Resize all columns to fit the content size
//			for(int i = 0; i < columns.length; i++) {  //  java.lang.IllegalStateException: Could not auto-size column. Make sure the column was tracked prior to auto-sizing the column
//	            sheet.autoSizeColumn(i);
//	        }
			
			//setColumnWidth(int columnIndex, int width) Set the width (in units of 1/256th of a character width)
			sheet.setColumnWidth(0, 15 * 256);
			sheet.setColumnWidth(1, 15 * 256);
			sheet.setColumnWidth(2, 25 * 256);
			sheet.setColumnWidth(3, 20 * 256);

			System.out.println("Data Loaded");
			rowNum1.set(0);
		}
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		fileOut.close();
		workbook.close();
	}
	
	private static void generateTextFile(Map<String,List<LogRowData>> logRowDataMap) throws IOException {
		//https://mkyong.com/java8/java-8-streams-filter-examples/
		List<LogRowData> result=null;
		for(Map.Entry<String, List<LogRowData>> entry : logRowDataMap.entrySet()){
			result = entry.getValue().stream()                // convert list to stream
	                .filter(line -> "Valid".equals(line.getStatus()))     // we dont like mkyong
	                .collect(Collectors.toList());  		
		}
		//https://stackoverflow.com/questions/30995763/java-8-partition-list
		List<String> collection = new ArrayList<>();
		// fill collection
		int chunkSize = 25000;
		List<List<LogRowData>> lists = new ArrayList<>();
		for (int i = 0; i < result.size(); i += chunkSize) {
		    int end = Math.min(result.size(), i + chunkSize);
		    lists.add(result.subList(i, end));
		}
		
		Map<String, List<LogRowData>> logDetails = new HashMap<>(); 
		for(int i=0;i<lists.size();i++)
		{
			logDetails.put("LogFile" + i, lists.get(i)); 
		}
		
		String file_name="C:\\DharaniCh\\SWEN589_Project\\datastore\\";
		
	}
	
	
	private static void generateTextFileFinal(List<LogRowData> notePadeList) throws IOException {
		//https://mkyong.com/java8/java-8-streams-filter-examples/
		
		List<LogRowData> notePadValidList = notePadeList.stream()                // convert list to stream
                .filter(line -> "Valid".equals(line.getStatus()))    // we dont like mkyong
                .collect(Collectors.toList());  
		
		List<List<LogRowData>> subSets = ListUtils.partition(notePadValidList, 25000);
        // Displaying the sublists 
        for (List<LogRowData> sublist: subSets) {
        	System.out.println("*****************listsize****************"+sublist.size());
        }
        
		subSets.stream().forEach(listData -> {
			//File file_name= new File("C:\\DharaniCh\\SWEN589_Project\\datastore\\");
			BufferedWriter writer = null;
				try {
					writer = new BufferedWriter(new FileWriter("C:\\Users\\Dell\\Desktop\\LogPad\\"+notePadCount.getAndAdd(1)));
					for(LogRowData logRowData : listData) {
						writer.write(logRowData.getContent() + System.lineSeparator());
					}
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		});
		
	}
	
	/*
	private static void generateTextFile(Map<String,List<LogRowData>> logRowDataMap) throws IOException {
	//https://mkyong.com/java8/java-8-streams-filter-examples/
	List<LogRowData> result=null;
	for(Map.Entry<String, List<LogRowData>> entry : logRowDataMap.entrySet()){
		result = entry.getValue().stream()                // convert list to stream
                .filter(line -> "Valid".equals(line.getStatus()))     // we dont like mkyong
                .collect(Collectors.toList());  		
	}
	System.out.println("Valid"+result.size());
	//Get the file reference
	String file_name="C:\\DharaniCh\\SWEN589_Project\\datastore\\Log"+logNameCount.getAndAdd(1)+".log";
//	Path path = Paths.get(file_name);
	File file = new File(file_name);
	 FileWriter fw = new FileWriter(file.getAbsoluteFile());
		try(BufferedWriter writer = new BufferedWriter(fw)){
	for (LogRowData resultobj : result)
	{
		if(logResultCount.get()==25000){
			file_name="C:\\DharaniCh\\SWEN589_Project\\datastore\\Log"+logNameCount.getAndAdd(1)+".log";	
			logResultCount.set(0);
			file = new File(file_name);

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
           	
				NotePadCount.addAndGet(1);	
				writer.write(resultobj.getContent() + System.lineSeparator());
				//	writeToLogFile(path,resultobj,writer);						
				//  logResultCount.addAndGet(1);							
					
			
		}
		else{// If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//			try(BufferedWriter writer = new BufferedWriter(fw)){					
					NotePadCount.addAndGet(1);												
					writer.write(resultobj.getContent() + System.lineSeparator());
					logResultCount.addAndGet(1);
					//writeToLogFile(path,resultobj,writer);	
					//logResultCount.addAndGet(1);	
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			//writeToLogFile(path,resultobj,writer);				
		}
	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	System.out.println("NotePadCount"+NotePadCount);
}
	
	private static void writeToLogFile(Path path,LogRowData resultobj,BufferedWriter writer) throws IOException {
		
		try{		
						if(!(null==resultobj.getContent()))
						{
							NotePadCount.addAndGet(1);	
//							logResultCount.addAndGet(1);							
							writer.write(resultobj.getContent() + System.lineSeparator());
						}
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	}
			
		
	
	*/
}
