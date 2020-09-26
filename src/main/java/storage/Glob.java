package storage;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Table;

import model.ClusterStudent;
import model.Discussion;
import model.DiscussionStep;
import model.Step;
import model.Student;

/**Structure to be stored in file as serialized object. Contains all data structures required to respond 
 * to requests, except DataManager.userIdsInCustomFilter, which should be provided every time by the user.*/
public class Glob implements java.io.Serializable{
	
	public final String courseName;
	public final String courseID;
	public final OffsetDateTime courseStart;
	public final OffsetDateTime courseEnd;
	
	
	public final Map<String, Step> hashcode2Chapter;
	public final Map<String, Step> hashcode2Sequential;
	public final Map<String, Step> hashcode2Vertical;
	public final Map<String, List<String>> sequentialToVerticalMap;
	public final Map<String, DiscussionStep> id2Discussion;
	
	public final List<Step> allChapters;
	public final List<Step> allSequentials;
	public final List<Step> allVerticals;
	public final List<DiscussionStep> allDiscussions;
	public final List<Step> verticalsAndDiscussions;
	public final List<String> surveyQuestions;

	public final Map<Integer, Student> id2Student;
	public final Set<Integer> userIdsToIgnore;
	public final Map<String, Student> uid2Student;
	
	public final List<ClusterStudent> clusterData;
	
	/**prepare table for sankey diagram*/
	public final Table<Integer, Integer, Integer> linksTableVD;
	public final Table<Integer,Integer,Integer> linksTableUniqueVD;
	
	/**Map of Discussion Step to Discussion detail which conclude numbers of each event and the number of the student. */
	public final Map<Step, Discussion> discussionMap;
    /**Map of timeslots to Sets of Students who had a logged event in that timeslot.
	 * Each "timeslot" is an OffsetDateTime object representing the start of the timeslot.*/
	public final ConcurrentMap<OffsetDateTime,Set<Student>> activeTimeMap;
	/**Map of Steps (sequentials) to # of students who saw that step last*/
	public final SortedMap<Step,Integer> exitStepsChrono;
	/**Map of Steps (sequentials) to # of students for whom that was the highest step they saw*/
	public final SortedMap<Step,Integer> exitStepsHighest;
	/**Map of Steps (sequentials) to # of Students who viewed that Step OR any higher Step*/
	public final SortedMap<Step,Integer> participationFunnel;
	/**Map of Steps (sequentials) to # of Students who viewed that Step*/
	public final SortedMap<Step,Integer> stepFrequencies;
	
	
	
	public Glob(String courseName, String courseID,  OffsetDateTime courseStart, OffsetDateTime courseEnd,
			 Map<String, Step> hashcode2Chapter, Map<String, Step> hashcode2Sequential, Map<String, Step> hashcode2Vertical,
			 Map<String, List<String>> sequentialToVerticalMap,  Map<String, DiscussionStep> id2Discussion,
			 List<Step> allChapters, List<Step> allSequentials, List<Step> allVerticals,
			 List<DiscussionStep> allDiscussions, List<Step> verticalsAndDiscussions, List<String> surveyQuestions,
			 Map<Integer, Student> id2Student, Set<Integer> userIdsToIgnore, Map<String, Student> uid2Student,
			 List<ClusterStudent> clusterData,
			 Table<Integer, Integer, Integer> linksTableVD, Table<Integer,Integer,Integer> linksTableUniqueVD,
			 Map<Step, Discussion> discussionMap, ConcurrentMap<OffsetDateTime,Set<Student>> activeTimeMap,
			 SortedMap<Step,Integer> exitStepsChrono,  SortedMap<Step,Integer> exitStepsHighest,
			 SortedMap<Step,Integer> participationFunnel, SortedMap<Step,Integer> stepFrequencies
			) { 
		
		this.courseName = courseName;
		this.courseID = courseID;
		this.courseStart = courseStart;
		this.courseEnd = courseEnd;
		
		this.hashcode2Chapter = hashcode2Chapter;
		this.hashcode2Sequential = hashcode2Sequential;
		this.hashcode2Vertical = hashcode2Vertical;
		this.sequentialToVerticalMap = sequentialToVerticalMap;
		this.id2Discussion = id2Discussion;
		
		this.allChapters = allChapters;
		this.allSequentials = allSequentials;
		this.allVerticals = allVerticals;
		this.allDiscussions = allDiscussions;
		this.verticalsAndDiscussions = verticalsAndDiscussions;
		this.surveyQuestions = surveyQuestions;
		
		this.id2Student = id2Student;
		this.userIdsToIgnore = userIdsToIgnore;
		this.uid2Student = uid2Student;
		
		this.clusterData = clusterData;
		
		this.linksTableVD = linksTableVD;
		this.linksTableUniqueVD = linksTableUniqueVD;
		
		this.discussionMap = discussionMap;
		this.activeTimeMap = activeTimeMap;
		this.exitStepsChrono = exitStepsChrono;
		this.exitStepsHighest = exitStepsHighest;
		this.participationFunnel = participationFunnel;
		this.stepFrequencies = stepFrequencies;
		
	}
	
}
