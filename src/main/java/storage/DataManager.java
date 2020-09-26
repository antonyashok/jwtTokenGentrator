package storage;

import java.io.BufferedInputStream;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import constant.FileConstant;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.ClusterStudent;
import model.Data;
import model.Discussion;
import model.DiscussionRecord;
import model.DiscussionStep;
import model.Record;
import model.Step;
import model.Student;
import util.ClusterImp;
import util.ClusterPrepare;
import util.CourseParser;
import util.DiscussionAnalysis;
import util.DiscussionParser;
import util.FileGetter;
import util.LogReader;
import util.SankeyTable;
import util.StepAnalysis;
import util.StudentsGetter;
import util.SurveyParser;

public class DataManager {

	private static Data dataInstance;
	
	private DataManager() {
		
	}
	public static synchronized Data getDataInstance() {
		if(dataInstance == null) {
			dataInstance= new Data();
		}
		return dataInstance;
	}
	
	public static synchronized void deleteDataInstance() {
		dataInstance = null;
	}
	
	
	//Data structures required to respond to requests
	public static SimpleIntegerProperty logsDone = new SimpleIntegerProperty(0);

	/**Read raw edX data and compile it into data structures used to respond to http requests.
	 * @param	courseDir	folder containing the files for one course
	 * @param	logsDir		folder containing the log files (compressed or not) for one course
	 * @param	surveyFiles	(optional) List of Files containing Qualtrics survey data
	 * @param	newCacheFile	(optional) File to save processed data to
	 * @param	usernamesToIgnoreFile	(optional) text file with list of usernames to ignore
	 * @param	extFilterFile	(optional) text file with list of student ids that the user wants to use for filtering*/
	public static void extractData(File courseDir, File logsDir, List<File> surveyFiles, File newCacheFile,
			File usernamesToIgnoreFile, File extFilterFile) throws Exception{

		File outFile = new File("Data/out");

		//check that required input Files exist
		if(!courseDir.isDirectory())
			throw new IOException("Folder '"+courseDir.getAbsolutePath()+"' not found.");

		if(!logsDir.isDirectory())
			throw new IOException("Folder '"+logsDir.getAbsolutePath()+"' not found.");

		if(surveyFiles==null)
			surveyFiles = new ArrayList<>(1);//insert empty list in its place
		else
			for(File surveyFile : surveyFiles)
				if(!surveyFile.isFile()) throw new FileNotFoundException("Survey file not found.");

		if(usernamesToIgnoreFile!=null && !usernamesToIgnoreFile.isFile())
			throw new FileNotFoundException("List of usernames to ignore not found.");

		if(extFilterFile!=null && !extFilterFile.isFile())
			throw new FileNotFoundException("Custom filter file not found.");
		//if file of user IDs to use in custom filter was provided, add to set
		if(extFilterFile!=null) {
			try(BufferedReader in = new BufferedReader(new FileReader(extFilterFile))){
				//each line should have one user_id
				in.lines().forEach(line -> {
					System.out.println("line"+ line);
					try {dataInstance.getUserIdsInCustomFilter().add(new Integer(line));
					}
					catch(NumberFormatException nfe) {
					}	//just continue
				});
			}
		}
		
		

		File structFile = FileGetter.getFileEndingWith(courseDir, FileConstant.courseStructure);
		JSONParser jsonParser=new JSONParser();
		JSONObject structFileRootObj = (JSONObject) jsonParser.parse(new BufferedReader(new FileReader(structFile)));

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
		DataManager.getDataInstance().setSurveyQuestions(SurveyParser.getSurveyQuestions(surveyFiles, DataManager.getDataInstance().getUid2Student(), DataManager.getDataInstance().getUserIdsToIgnore()));
		LogReader.getLogFiles(logsDir, DataManager.getDataInstance().getCourseID());
		//DataManager.getDataInstance().setActiveTimeMap(LogReader.activeTimeMap);//to do

		/**after building id2Student data structure, we need to remove the consecutive one */
		DataManager.getDataInstance().getId2Student().values().parallelStream().forEach(student -> {
			List<Record> stepRecord= student.getVerticalAndDiscussionRecords();
			List<Record> verticalRecord = student.getVerticalRecords();
			List<DiscussionRecord> discussionRecord = student.getDiscussionRecords();
			student.sortListChrono(stepRecord);
			student.deleteConsecutiveDuplicates(stepRecord);
			student.sortListChrono(verticalRecord);
			student.deleteConsecutiveDuplicates(verticalRecord);
			student.sortDiscussionChrono(discussionRecord);
			student.deleteDiscussionConsecutiveDuplicates(discussionRecord);

		});

		DataManager.getDataInstance().setDiscussionMap( DiscussionAnalysis.getDiscussionMap(DataManager.getDataInstance().getId2Student()));

		DataManager.getDataInstance().setLinksTableVD(SankeyTable.computeLinksVD(DataManager.getDataInstance().getId2Student().values()));
		DataManager.getDataInstance().setLinksTableUniqueVD(SankeyTable.computeLinksUniqueVD(DataManager.getDataInstance().getId2Student().values()));
		DataManager.getDataInstance().setExitStepsChrono(StepAnalysis.exitStepsChrono(DataManager.getDataInstance().getId2Student()));
		DataManager.getDataInstance().setExitStepsHighest(StepAnalysis.exitStepsHighest(DataManager.getDataInstance().getId2Student()));
		DataManager.getDataInstance().setParticipationFunnel(StepAnalysis.participationFunnel(DataManager.getDataInstance().getId2Student(), DataManager.getDataInstance().getExitStepsHighest()));
		DataManager.getDataInstance().setStepFrequencies(StepAnalysis.stepFrequencies(DataManager.getDataInstance().getId2Student()));

		DataManager.getDataInstance().setClusterData(ClusterPrepare.prepareClusterData(DataManager.getDataInstance().getId2Student()));
		
		//ClusterPrepare.printClusterStudentCSVFile(outFile,clusterData);  // the method to print the clustering data into CSV
		
		System.out.println("DONE BUILDING DATA STRUCTURES");

		if(newCacheFile != null) {
			Glob cachedData = new Glob(DataManager.getDataInstance().getCourseName(), DataManager.getDataInstance().getCourseID(), DataManager.getDataInstance().getCourseStart(), DataManager.getDataInstance().getCourseEnd(),
					DataManager.getDataInstance().getHashcode2Chapter(), DataManager.getDataInstance().getHashcode2Sequential(), DataManager.getDataInstance().getHashcode2Vertical(),
					DataManager.getDataInstance().getSequentialToVerticalMap(), DataManager.getDataInstance().getId2Discussion(),
					DataManager.getDataInstance().getAllChapters(), DataManager.getDataInstance().getAllSequentials(),DataManager.getDataInstance().getAllVerticals(),
					DataManager.getDataInstance().getAllDiscussions(), DataManager.getDataInstance().getVerticalsAndDiscussions(), DataManager.getDataInstance().getSurveyQuestions(),
					DataManager.getDataInstance().getId2Student(), DataManager.getDataInstance().getUserIdsToIgnore(), DataManager.getDataInstance().getUid2Student(),
					DataManager.getDataInstance().getClusterData(),
					DataManager.getDataInstance().getLinksTableVD(), DataManager.getDataInstance().getLinksTableUniqueVD(),
					DataManager.getDataInstance().getDiscussionMap(), DataManager.getDataInstance().getActiveTimeMap(),
					DataManager.getDataInstance().getExitStepsChrono(),DataManager.getDataInstance().getExitStepsHighest(),
					DataManager.getDataInstance().getParticipationFunnel(), DataManager.getDataInstance().getStepFrequencies()
					);

			try(ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(newCacheFile)))){
				out.writeObject(cachedData);
				System.out.println("Saved processed data to "+newCacheFile.getName());
			}
		}

	}

	/**Read processed data from serialized object file
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException */
	public static void loadProcessedData(File cacheFile, File extFilterFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		//load data structures from file
		try(ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)))){
			Glob glob = (Glob) in.readObject();
			DataManager.getDataInstance().setCourseName(glob.courseName);
			DataManager.getDataInstance().setCourseID(glob.courseID);
			DataManager.getDataInstance().setCourseStart(glob.courseStart);
			DataManager.getDataInstance().setCourseEnd(glob.courseEnd);
	
			DataManager.getDataInstance().setHashcode2Chapter(glob.hashcode2Chapter);
			DataManager.getDataInstance().setHashcode2Sequential(glob.hashcode2Sequential);
			DataManager.getDataInstance().setHashcode2Vertical(glob.hashcode2Vertical);
			DataManager.getDataInstance().setSequentialToVerticalMap(glob.sequentialToVerticalMap);
			DataManager.getDataInstance().setId2Discussion(glob.id2Discussion);

			DataManager.getDataInstance().setAllSequentials(glob.allSequentials);
			DataManager.getDataInstance().setAllChapters(glob.allChapters);
			DataManager.getDataInstance().setAllVerticals(glob.allVerticals);
			DataManager.getDataInstance().setAllDiscussions(glob.allDiscussions);
			DataManager.getDataInstance().setVerticalsAndDiscussions(glob.verticalsAndDiscussions);
			DataManager.getDataInstance().setSurveyQuestions(glob.surveyQuestions);

			DataManager.getDataInstance().setId2Student(glob.id2Student);
			DataManager.getDataInstance().setUserIdsToIgnore(glob.userIdsToIgnore);
			DataManager.getDataInstance().setUid2Student(glob.uid2Student);


			DataManager.getDataInstance().setClusterData(glob.clusterData);
			DataManager.getDataInstance().setLinksTableVD(glob.linksTableVD);
			DataManager.getDataInstance().setLinksTableUniqueVD(glob.linksTableUniqueVD);
		
			DataManager.getDataInstance().setDiscussionMap(glob.discussionMap);
			
			DataManager.getDataInstance().setActiveTimeMap(glob.activeTimeMap);
			
			
			DataManager.getDataInstance().setParticipationFunnel(glob.participationFunnel);
			DataManager.getDataInstance().setStepFrequencies(glob.stepFrequencies);
			DataManager.getDataInstance().setExitStepsHighest(glob.exitStepsHighest);
			DataManager.getDataInstance().setExitStepsChrono(glob.exitStepsChrono);
			
		}

		//if file of user IDs to use in custom filter was provided, add to set
		if(extFilterFile!=null) {
			try(BufferedReader in = new BufferedReader(new FileReader(extFilterFile))){
				//each line should have one user_id
				in.lines().forEach(line -> {
					try {DataManager.getDataInstance().getUserIdsInCustomFilter().add(new Integer(line));}
					catch(NumberFormatException nfe) {}	//just continue
				});
			}
		}
	}
	
	/** a method to call the clustering implementation method*/
	public static Map<Integer, List<Integer>> clusterImp(String algorithm, int clusterNum, List<String> choosenAttr) throws Exception {
	
		Map<Integer, List<Integer>> result = ClusterImp.clusterChooserForList(algorithm, clusterNum, choosenAttr);
		DataManager.getDataInstance().setClusterResult(result);
		
		return DataManager.getDataInstance().getClusterResult();
	}

	
}
