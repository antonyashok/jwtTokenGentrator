package model;

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

import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import javafx.beans.property.ReadOnlyIntegerProperty;

public class Data {

	public Data() {}
	
	private String courseName = null;
	private String courseID = null;
	private OffsetDateTime courseStart = null;
	private OffsetDateTime courseEnd = null;
	private  Map<String, Step> hashcode2Chapter = new HashMap<>();
	private  Map<String, Step> hashcode2Sequential = new HashMap<>();
	private  Map<String, Step> hashcode2Vertical = new HashMap<>();
	private  Map<String, DiscussionStep> id2Discussion = new HashMap<>();
	private  List<Step> allChapters = null;
	private  List<Step> allSequentials = null;
	private  List<Step> allVerticals = null;
	private  List<DiscussionStep> allDiscussions = null;
	private  List<Step> verticalsAndDiscussions = null;
	private  Map<String, List<String>> sequentialToVerticalMap = null;
	private  List<String> surveyQuestions = new ArrayList<>();
	private  Map<Integer, Student> id2Student = new HashMap<>();
	private  Set<Integer> userIdsToIgnore = new HashSet<>();;
	private  Map<String, Student> uid2Student = new HashMap<>();

	/**Set of user IDs input via initial user interface.
	 * If some other tool identifies an interesting group of students, we can filter for/out these students by putting them in this set.*/
	private Set<Integer> userIdsInCustomFilter = new HashSet<Integer>();

	/** this is to prepare the clustering data by building data structure in ClusterStudent*/
	private List<ClusterStudent> clusterData;
	
	private Table<Integer, Integer, Integer> linksTableVD = null;
	private Table<Integer,Integer,Integer> linksTableUniqueVD = null;
	private Map<Step, Discussion> discussionMap= null;

	/**Map of timeslots to Sets of Students who had a logged event in that timeslot.
	 * Each "timeslot" is an OffsetDateTime object representing the start of the timeslot.*/
	private ConcurrentMap<OffsetDateTime,Set<Student>> activeTimeMap = null;

	/**Map of Steps (sequentials) to # of students who saw that step last*/
	private SortedMap<Step,Integer> exitStepsChrono = new TreeMap<>();
	/**Map of Steps (sequentials) to # of students for whom that was the highest step they saw*/
	private SortedMap<Step,Integer> exitStepsHighest = new TreeMap<>();
	/**Map of Steps (sequentials) to # of Students who viewed that Step OR any higher Step*/
	private SortedMap<Step,Integer> participationFunnel = new TreeMap<>();
	/**Map of Steps (sequentials) to # of Students who viewed that Step*/
	private SortedMap<Step,Integer> stepFrequencies  = new TreeMap<>();
	
	private Map<Integer, List<Integer>> clusterResult = new HashMap<>(); 

	//getter
	public Map<Integer, List<Integer>> getClusterResult() {
		return clusterResult;
	}
	
	public Set<Integer> getUserIdsToIgnore() {
		return userIdsToIgnore;
	}
	public String getCourseName() {
		return courseName;
	}
	public String getCourseID() {
		return courseID;
	}
	public Map<String, Step> getHashcode2Vertical() {
		return hashcode2Vertical;
	}
	public Map<String, DiscussionStep> getId2Discussion() {
		return id2Discussion;
	}
	public List<Step> getAllVerticals() {
		return allVerticals;
	}
	public List<DiscussionStep> getAllDiscussion() {
		return allDiscussions;
	}
	public List<Step> getVerticalsAndDiscussions() {
		return verticalsAndDiscussions;
	}
	public Map<Integer, Student> getId2Student() {
		return id2Student;
	}
	
	public ConcurrentMap<OffsetDateTime, Set<Student>> getActiveTimeMap() {
		return activeTimeMap;
	}
	public  Map<String, Step> getHashcode2Sequential() {
		return hashcode2Sequential;
	}
	public List<Step> getAllSequentials() {
		return allSequentials;
	}
	public Map<String, List<String>> getSequentialToVerticalMap() {
		return sequentialToVerticalMap;
	}
	public OffsetDateTime getCourseStart() {
		return courseStart;
	}
	public OffsetDateTime getCourseEnd() {
		return courseEnd;
	}
	public Table<Integer, Integer, Integer> getLinksTableVD() {
		return linksTableVD;
	}
	public Table<Integer, Integer, Integer> getLinksTableUniqueVD() {
		return linksTableUniqueVD;
	}
	public Map<String, Step> getHashcode2Chapter() {
		return hashcode2Chapter;
	}
	public List<Step> getAllChapters() {
		return allChapters;
	}
	public SortedMap<Step, Integer> getExitStepsChrono() {
		return exitStepsChrono;
	}
	public SortedMap<Step, Integer> getExitStepsHighest() {
		return exitStepsHighest;
	}
	public SortedMap<Step, Integer> getParticipationFunnel() {
		return participationFunnel;
	}
	public SortedMap<Step, Integer> getStepFrequencies() {
		return stepFrequencies;
	}
	public Map<Step, Discussion> getDiscussionMap() {
		return discussionMap;
	}
	public List<DiscussionStep> getAllDiscussions() {
		return allDiscussions;
	}
	public List<String> getSurveyQuestions() {
		return surveyQuestions;
	}
	public Map<String, Student> getUid2Student() {
		return uid2Student;
	}
	/**Number of log files that have been read.*/
	public Set<Integer> getUserIdsInCustomFilter() {
		return userIdsInCustomFilter;
	}
	public List<ClusterStudent> getClusterData() {
		return clusterData;
	}
	
	
	//setter
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	public void setCourseStart(OffsetDateTime courseStart) {
		this.courseStart = courseStart;
	}

	public void setCourseEnd(OffsetDateTime courseEnd) {
		this.courseEnd = courseEnd;
	}

	public void setHashcode2Chapter(Map<String, Step> hashcode2Chapter) {
		this.hashcode2Chapter = hashcode2Chapter;
	}

	public void setHashcode2Sequential(Map<String, Step> hashcode2Sequential) {
		this.hashcode2Sequential = hashcode2Sequential;
	}

	public void setHashcode2Vertical(Map<String, Step> hashcode2Vertical) {
		this.hashcode2Vertical = hashcode2Vertical;
	}

	public void setId2Discussion(Map<String, DiscussionStep> id2Discussion) {
		this.id2Discussion = id2Discussion;
	}

	public void setAllChapters(List<Step> allChapters) {
		this.allChapters = allChapters;
	}

	public void setAllSequentials(List<Step> allSequentials) {
		this.allSequentials = allSequentials;
	}

	public void setAllVerticals(List<Step> allVerticals) {
		this.allVerticals = allVerticals;
	}

	public void setAllDiscussions(List<DiscussionStep> allDiscussions) {
		this.allDiscussions = allDiscussions;
	}

	public void setVerticalsAndDiscussions(List<Step> verticalsAndDiscussions) {
		this.verticalsAndDiscussions = verticalsAndDiscussions;
	}

	public void setSequentialToVerticalMap(Map<String, List<String>> sequentialToVerticalMap) {
		this.sequentialToVerticalMap = sequentialToVerticalMap;
	}

	public void setSurveyQuestions(List<String> surveyQuestions) {
		this.surveyQuestions = surveyQuestions;
	}

	public void setId2Student(Map<Integer, Student> id2Student) {
		this.id2Student = id2Student;
	}

	public void setUserIdsToIgnore(Set<Integer> userIdsToIgnore) {
		this.userIdsToIgnore = userIdsToIgnore;
	}

	public void setUid2Student(Map<String, Student> uid2Student) {
		this.uid2Student = uid2Student;
	}

	public void setUserIdsInCustomFilter(Set<Integer> userIdsInCustomFilter) {
		this.userIdsInCustomFilter = userIdsInCustomFilter;
	}

	public void setClusterData(List<ClusterStudent> clusterData) {
		this.clusterData = clusterData;
	}

	public void setLinksTableVD(Table<Integer, Integer, Integer> linksTableVD) {
		this.linksTableVD = linksTableVD;
	}

	public void setLinksTableUniqueVD(Table<Integer, Integer, Integer> linksTableUniqueVD) {
		this.linksTableUniqueVD = linksTableUniqueVD;
	}

	public void setDiscussionMap(Map<Step, Discussion> discussionMap) {
		this.discussionMap = discussionMap;
	}

	public void setActiveTimeMap(ConcurrentMap<OffsetDateTime, Set<Student>> activeTimeMap) {
		this.activeTimeMap = activeTimeMap;
	}

	public void setExitStepsChrono(SortedMap<Step, Integer> exitStepsChrono) {
		this.exitStepsChrono = exitStepsChrono;
	}

	public void setExitStepsHighest(SortedMap<Step, Integer> exitStepsHighest) {
		this.exitStepsHighest = exitStepsHighest;
	}

	public void setParticipationFunnel(SortedMap<Step, Integer> participationFunnel) {
		this.participationFunnel = participationFunnel;
	}

	public void setStepFrequencies(SortedMap<Step, Integer> stepFrequencies) {
		this.stepFrequencies = stepFrequencies;
	}

	public void setClusterResult(Map<Integer, List<Integer>> clusterResult) {
		this.clusterResult = clusterResult;
	}
}
