package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import constant.DisConstant;
import constant.NavigationConstant;
import constant.TimeConstant;
import javafx.beans.property.SimpleIntegerProperty;
import model.DiscussionRecord;
import model.DiscussionStep;
import model.Record;
import model.Step;
import model.Student;
import storage.DataManager;

public class LogReader {
	/**Length of a timeslot in minutes. 60 must be evenly divisible by this number.*/
	private static int timeslotLength = TimeConstant.timeslotLength;
	public static int numLogs;

	public static AtomicInteger totalEventsChecked = new AtomicInteger(0);
	public static AtomicInteger totalCourseEventsChecked = new AtomicInteger(0);
	public static AtomicInteger couldNotParseJSON = new AtomicInteger(0);
	public static AtomicInteger irrelevantType = new AtomicInteger(0);	//# of events with irrelevant type
	public static AtomicInteger typeNotFound = new AtomicInteger(0);	//# of events whose type could not be found in the log - skip
	public static AtomicInteger ignoredUser = new AtomicInteger(0);;	//# of events ignored because the user had a privileged role in the course
	public static AtomicInteger usernameBlank = new AtomicInteger(0);	//# of events with blank usernames
	public static AtomicInteger nullUserIds = new AtomicInteger(0);	//# of events missing a user_id (probably also had blank username) - skip
	public static AtomicInteger unknownStudentId = new AtomicInteger(0);	//# of events with a user_id that was not found in the auth_user table
	public static AtomicInteger missingSessions = new AtomicInteger(0);	//# of events missing a session String - don't skip, just count
	public static AtomicInteger pageFieldNotFound = new AtomicInteger(0);	//# of events where the 'page' field could not be found - skip
	public static AtomicInteger coursewareNotFound = new AtomicInteger(0);	//# of events where "/courseware/" could not be found in the 'page' field - skip
	public static AtomicInteger eventIdNotFound = new AtomicInteger(0);
	public static AtomicInteger hashcodeNotFoundInEventIdField = new AtomicInteger(0);
	public static AtomicInteger unrecognisedHashcode = new AtomicInteger(0);	//# of events whose hashcode (from the 'page' field) could not be matched with a Step - skip
	//public static ConcurrentMap<OffsetDateTime,Set<Student>>  activeTimeMap = null;	//1 day per log file; 1440 minutes in a day
	public static SimpleIntegerProperty logsDone = DataManager.logsDone;


	public static int getLogFiles(File logsDir, String courseID) {
		File[] logFiles = logsDir.listFiles(file -> file.getName().endsWith(".log"));
		numLogs = logFiles.length;
		DataManager.getDataInstance().setActiveTimeMap(new ConcurrentHashMap<>(numLogs * 1440 / timeslotLength)); 
		Arrays.stream(logFiles)
		.parallel()
		.forEach(logFile -> {
			if(logFile.getName().endsWith(".log")) {
				if(logFile.isFile()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
						LogReader.readLogFile(reader, courseID);
					}
					catch (IOException x) {
						System.err.format("IOException: %s%n", x);
					}

				}else if(logFile.isDirectory()) {
					File eventLog = logFile.listFiles()[0];
					try (BufferedReader reader = new BufferedReader(new FileReader(eventLog))) {
						LogReader.readLogFile(reader, courseID);
					}
					catch (IOException x) {
						System.err.format("IOException: %s%n", x);
					}
				}
			}	
			synchronized(logsDone) {
				logsDone.set(logsDone.get()+1);
			}
		});

		return numLogs;
	}

	public static void readLogFile (BufferedReader reader, String courseId) {

		Map<String, DiscussionStep> id2Discussion = DataManager.getDataInstance().getId2Discussion();
		Map<String, Step> hashcode2Vertical = DataManager.getDataInstance().getHashcode2Vertical();
		Map<String, Step> hashcode2Sequential = DataManager.getDataInstance().getHashcode2Sequential();
		List<Step> allSequentials = DataManager.getDataInstance().getAllSequentials();
		Map<Integer, Student> id2Student = DataManager.getDataInstance().getId2Student();
		Set<Integer> userIdsToIgnore = DataManager.getDataInstance().getUserIdsToIgnore();
		
		Map<String, List<String>> sequentialToVerticalMap = DataManager.getDataInstance().getSequentialToVerticalMap();
		final JSONParser parser=new JSONParser();

		reader.lines().forEach(line -> {
			totalEventsChecked.incrementAndGet();
			if(!line.contains(courseId)) return; // this is to check whether the line is relevant to the course.
			totalCourseEventsChecked.incrementAndGet();
			JSONObject rootObj;
			try {
				rootObj = (JSONObject) parser.parse(line);
			} catch (org.json.simple.parser.ParseException e) {
				System.err.println(e.getMessage());
				couldNotParseJSON.incrementAndGet();
				return;
			}

			//check user_id on event, skip if user_id is null, or if user_id is in Set of user_ids to ignore
			//http://edx.readthedocs.io/projects/devdata/en/latest/internal_data_formats/tracking_logs/common_fields.html#context-member-fields-common-to-all-events
			Long userIdLong = (Long) ((JSONObject) rootObj.get("context")).get("user_id");
			
			if(userIdLong==null) {
				nullUserIds.incrementAndGet();
				return;
			}

			Integer userId = new Integer(userIdLong.intValue());

			if(userIdsToIgnore.contains(userId)){
				ignoredUser.incrementAndGet();
				return;
			}

			//get Student object for the student in this JSON string, skip if user_id didn't match any known Student

			Student student = id2Student.get(userId);


			if(student==null) {
				unknownStudentId.incrementAndGet();	//don't know how users can log an event on the course despite not being enrolled
				return;
			}

			//check username on event, skip if username is blank
			//http://edx.readthedocs.io/projects/devdata/en/latest/internal_data_formats/tracking_logs/common_fields.html#username-field
			if(rootObj.get("username").equals("")){
				usernameBlank.incrementAndGet();
				return;
			}

			//log student as present in this timeslot
			//compute timeslot
			OffsetDateTime timeslot = OffsetDateTime.parse((String) rootObj.get("time"))
					.with((Temporal input) -> {	//round the time down to the nearest timeslot start
						//zero second and fraction-of-second fields
						OffsetDateTime x = ((OffsetDateTime) input).truncatedTo(ChronoUnit.MINUTES);
						return x.withMinute(x.getMinute() / timeslotLength * timeslotLength);
					});

			DataManager.getDataInstance().getActiveTimeMap().putIfAbsent(timeslot, Collections.synchronizedSet(new HashSet<Student>()));	//atomic
			DataManager.getDataInstance().getActiveTimeMap().get(timeslot).add(student); //locks this timeslot's Set while adding

			// begin to analyze the event
			String eventType = null;
			//check type of event, don't proceed further if not a type of interest (one of the course navigation events)


			if(rootObj.get("name")!= null) {
				String eventString = (String) rootObj.get("name");
				if(!NavigationConstant.navigationEvents().contains(eventString) && (! DisConstant.discussionEvents().contains(eventString))) {
					irrelevantType.incrementAndGet();
					return;
				}
				eventType = eventString;
			}else if(rootObj.get("event_type")!= null) {
				String eventString = (String) rootObj.get("event_type");
				//if event_type is not in eventTypes Set
				if((!NavigationConstant.navigationEvents().contains(eventString)) && (!DisConstant.discussionEvents().contains(eventString))){
					irrelevantType.incrementAndGet();
					return;
				}
				eventType = eventString;
				System.out.println("warning: event_type field used");

			}else {
				typeNotFound.incrementAndGet();
				return;
			}

			if(DisConstant.discussionEvents().contains(eventType)) {
				// analyze the discussion behavior
				JSONObject eventObj;
				try {
					eventObj = (JSONObject) parser.parse(rootObj.get("event").toString());
					OffsetDateTime time = OffsetDateTime.parse((String) rootObj.get("time"));
					String session = (String)rootObj.get("session");
					if(session==null || session.isEmpty())
						missingSessions.incrementAndGet();
					String discussionId = eventObj.get("commentable_id").toString(); // this id is to match the discussion id;

					DiscussionStep discussion = id2Discussion.get(discussionId);
					if(discussion == null) {
						unrecognisedHashcode.incrementAndGet();
						return;
					}

					String id = null; // this is to identify the forum contribution
					if(eventObj.get("id") != "") {
						id = eventObj.get("id").toString();
					}
					String type = null;
					if(eventType.equals(DisConstant.disViewEv))
					{
						type = DisConstant.disView;
						DiscussionRecord drv = new DiscussionRecord(time, session, discussion, type, id);
						student.getVerticalAndDiscussionRecords().add(drv);
					}
					if(eventType.equals(DisConstant.resCreEv))
						type = DisConstant.resCre;
					if(eventType.equals(DisConstant.resVotEv))
						type = DisConstant.resVot;
					if(eventType.equals(DisConstant.thrCreEv))
						type = DisConstant.thrCre;
					if(eventType.equals(DisConstant.thrVotEv)) 
						type = DisConstant.thrVot;
					if(eventType.equals(DisConstant.comCreEv))
						type = DisConstant.comCre;
					DiscussionRecord dr = new DiscussionRecord(time, session, discussion, type, id);
					student.getDiscussionRecords().add(dr);
				} catch (org.json.simple.parser.ParseException e) {
					e.printStackTrace();
				}

			}else if(NavigationConstant.navigationEvents().contains(eventType)) {
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
							if(verticalHash == null) return;
							step = hashcode2Vertical.get(verticalHash);
							if(step==null){
								unrecognisedHashcode.incrementAndGet();	//probably results from deleted or modified course content ?
								return;
							}
						} catch (org.json.simple.parser.ParseException e) {
							e.printStackTrace();
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
									return;
								}
								step=hashcode2Vertical.get(verticalHash);
								if(step==null){
									unrecognisedHashcode.incrementAndGet();	//probably results from deleted or modified course content ?
									return;
								}
							}
							catch (java.lang.StringIndexOutOfBoundsException e) {	//if sequential hashcode was not found in pageUrl
								//fall back to event.id field; see below
							} catch (org.json.simple.parser.ParseException e) {
								e.printStackTrace();
							}
						}else{
							coursewareNotFound.incrementAndGet();
							//this seems to happen when the student viewed the course's 'table of contents' (?)
							//so, the student isn't really viewing any Step now
							return;
						}
					}
				} else {
					pageFieldNotFound.incrementAndGet();
					return;
				}	
				//if using the page field didn't work, try the event.id field
				//this field should be present for next_selected, previous_selected and tab_selected events
				//look for "type@sequential+block@", then look ahead for sequential hashcode

				if(step==null) {	//if step could not be determined from the page field
					//parse event json string
					JSONObject eventObj=null;
					try {eventObj = (JSONObject) parser.parse((String) rootObj.get("event"));}
					catch (org.json.simple.parser.ParseException e) {
						System.err.println(e.getMessage());
						couldNotParseJSON.getAndIncrement();
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
									return;
								}
								step=hashcode2Vertical.get(verticalHash);
								if(step==null){
									unrecognisedHashcode.incrementAndGet();	//probably results from deleted or modified course content ?
									return;
								}
							} catch (java.lang.StringIndexOutOfBoundsException e) {
								e.printStackTrace();
							} catch (org.json.simple.parser.ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						else {
							System.err.println("problem string 2: "+eventId);
							hashcodeNotFoundInEventIdField.incrementAndGet();
							return;
						}
					}
					else {
						eventIdNotFound.incrementAndGet();
						return;
					}
				}

				//get session string
				String session=(String) rootObj.get("session");
				if(session==null || session.isEmpty())
					missingSessions.incrementAndGet();

				//parse time string, assuming default ISO_OFFSET_DATE_TIME format
				OffsetDateTime time=OffsetDateTime.parse((String) rootObj.get("time"));

				//add new Record object to Student's List of Records
				student.getVerticalRecords().add(new Record(time, session.intern(), step));
				student.getVerticalAndDiscussionRecords().add(new Record(time, session.intern(), step));
			}
		});
		
	}

}
