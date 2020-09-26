package model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import constant.DisConstant;
import constant.StudentConstant;
import storage.DataManager;

public class Student implements Serializable{

	public final int userId;
	public final String username;
	
	private String gender = "u";
	private boolean paying=false;
	private OffsetDateTime enrolledAt=null;
	private int age=-1;
	private String countryCode = "Unknown";
	private String countryName = "Unknown";
	private boolean isCertificated = false;
	public HashMap<String,String> surveyMap = null;
	private String education = null;
	private List<Record> verticalRecords;
	private List<Record> verticalAndDiscussionRecords;
	private List<DiscussionRecord> discussionRecords; // detailed discussion record;

	public Student(int userId, String username) {
		this.userId = userId;
		this.username = username;
		this.verticalRecords = Collections.synchronizedList(new LinkedList<Record>());
		this.verticalAndDiscussionRecords = Collections.synchronizedList(new LinkedList<Record>());
		this.discussionRecords= Collections.synchronizedList(new LinkedList<DiscussionRecord>());
	}

	/**Copying constructor. Explicitly creates a new object for any mutable, non-primitive fields.*/
	public Student(Student orig) {
		this.userId=orig.userId;
		this.username=orig.username;

		this.gender=orig.gender;
		this.education=orig.education;
		this.isCertificated = orig.isCertificated;
		this.paying=orig.paying;
		this.enrolledAt=orig.enrolledAt;
		this.age=orig.age;
		this.countryCode=orig.countryCode;
		this.countryName=orig.countryName;
		
		//modifying this Student's ArrayList won't tamper with the original Student's ArrayList
		this.verticalRecords = new ArrayList<Record>(orig.verticalRecords);
		this.discussionRecords = new ArrayList<DiscussionRecord>(orig.discussionRecords);
		this.verticalAndDiscussionRecords = new ArrayList<Record>(orig.verticalAndDiscussionRecords);
		//modifications to this Student's HashMap won't affect the original Student's HashMap
		if(orig.surveyMap!=null)
			this.surveyMap=new HashMap<String,String>(orig.surveyMap);
		//else, this.surveyMap keeps its default initial value: null
	}

	public List<Record> getVerticalAndDiscussionRecords() {
		return verticalAndDiscussionRecords;
	}

	public void setVerticalAndDiscussionRecords(List<Record> verticalAndDiscussionRecords) {
		this.verticalAndDiscussionRecords = verticalAndDiscussionRecords;
	}

	public List<DiscussionRecord> getDiscussionRecords() {
		return discussionRecords;
	}

	public void setDiscussionRecords(List<DiscussionRecord> discussionRecord) {
		this.discussionRecords = discussionRecord;
	}
	public List<Record> getVerticalRecords() {
		return verticalRecords;
	}


	public void setVerticalRecords(List<Record> verticalRecords) {
		this.verticalRecords = verticalRecords;
	}


	/**
	 * @return	'u'=unknown, 'm'=male, 'f'=female*/
	public String getGender() {
		return gender;
	}

	/**
	 * @param	gender	'm'=male, 'f'=female, anything else = unknown*/
	public void setGender(String gender) {
		if(gender=="m"|| gender=="f")
			this.gender=gender;
		else
			this.gender="u";
	}

	public boolean isPaying() {
		return paying;
	}

	public void setPaying(boolean paying) {
		this.paying = paying;
	}

	public OffsetDateTime getEnrolledAt() {
		return enrolledAt;
	}

	public void setEnrolledAt(OffsetDateTime enrolledAt) {
		this.enrolledAt = enrolledAt;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public boolean isCertificated() {
		return isCertificated;
	}

	public void setCertificated(boolean isCertificated) {
		this.isCertificated = isCertificated;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}
    
	/**Sets countryCode and countryName fields. countryName automatically inferred from countryCode using Locale class.
	 * @param	countryCode	2-character string representing the student's country.*/
	public void setCountry(String countryCode) {
		if(countryCode.length() != 2)
			return;	//reject input if not 2 characters long

		this.countryCode = countryCode;

		//derived from https://www.mkyong.com/java/display-a-list-of-countries-in-java/
		Locale loc = new Locale("", countryCode);
		this.countryName = loc.getDisplayCountry().intern();
	}
	
	/**Sort this Student's List of Records into chronological order*/
	public void sortListChrono(List<Record> records){
		synchronized(records) {
			records.sort((r0, r1) -> {
				if(r0.time.isBefore(r1.time))
					return -1;
				else if(r1.time.isBefore(r0.time))
					return 1;
				else
					return 0;
			});
		}
	}
	
	public void sortDiscussionChrono(List<DiscussionRecord> records) {
		synchronized(records) {
			records.sort((r0, r1) -> {
				if(r0.time.isBefore(r1.time))
					return -1;
				else if(r1.time.isBefore(r0.time))
					return 1;
				else
					return 0;
			});
		}	
	}
	
	public void deleteDiscussionConsecutiveDuplicates(List<DiscussionRecord> records) {
		synchronized(records) {
			//remove consecutive duplicates
			//can't avoid adding consecutive duplicates because events aren't logged in chronological order
			Iterator<DiscussionRecord> it = records.iterator();
			if(!it.hasNext())
				return;

			DiscussionRecord preDr  = it.next();
			Step previousStep= preDr.step;
			String previousId = preDr.sepId;
			String preType = preDr.type;
			
			while(it.hasNext()){
				DiscussionRecord dr = it.next();
				Step currentStep=dr.step;
				String currentId = dr.sepId;
				String currentType = dr.type;

				if(currentStep == previousStep && currentId.equals(previousId)&&currentType.equals(preType)) {
					it.remove();	//removes Record with currentStep from List
				}
				else {
					previousStep = currentStep;
					previousId = currentId;
					preType = currentType;
				}
			}
		}
	}
	/**Iterate through this Student's List of Records and remove consecutive Records with the same Step.*/
	public void deleteConsecutiveDuplicates(List<Record> records){
		synchronized(records) {
			//remove consecutive duplicates
			//can't avoid adding consecutive duplicates because events aren't logged in chronological order
			Iterator<Record> it = records.iterator();
			if(!it.hasNext())
				return;

			Step previousStep=it.next().step;

			while(it.hasNext()){
				Step currentStep=it.next().step;
				if(currentStep == previousStep)
					it.remove();	//removes Record with currentStep from List
				else
					previousStep = currentStep;
			}
		}
	}
	
	/**
	 * @return	Highest vertical this student viewed, or null if they didn't view any.*/
	public Step getHighestStepSeen() {
		if(verticalRecords.isEmpty())
			return null;
		return verticalRecords.stream()
				.map(record -> record.step)
				.max((Step step0, Step step1) -> step0.compareTo(step1))
				.get();	//shouldn't throw exception since we checked earlier if records was empty.
	}
	
	/**
	 * @return Highest chapter this student viewed, or null if they didn't view any.*/
	public Step getHighestChapterSeen() {
		if(verticalAndDiscussionRecords.isEmpty())
			return null;

		int chapterNum = verticalAndDiscussionRecords.stream()
				.map(record -> record.step)
				.max((Step step0, Step step1) -> step0.compareTo(step1))
				.get()	//shouldn't throw exception since we checked earlier if records was empty.
				.getStepNumInts()[0];

		return DataManager.getDataInstance().getAllChapters().get(chapterNum-1);	//-1 because chapter 1 is stored at index 0
	}
	
	/**
	 * @return	Number of unique steps (discussion and verticals) this student viewed.*/
	public int getNumberOfStepsSeen() {
		if(verticalAndDiscussionRecords.isEmpty())
			return 0;

		HashSet<Step> stepsSeen = new HashSet<>();
		for(Record record :verticalAndDiscussionRecords)
			stepsSeen.add(record.step);

		return stepsSeen.size();
	}
	
	
	
	
}
