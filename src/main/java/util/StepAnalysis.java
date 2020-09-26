package util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import model.Record;
import model.Step;
import model.Student;
import storage.DataManager;

public class StepAnalysis {


	public static SortedMap<Step,Integer> exitStepsChrono(Map<Integer, Student> id2Student){
		List<Step> allVerticals = DataManager.getDataInstance().getAllVerticals();

		SortedMap<Step,Integer> exitStepsChrono = new TreeMap<>();
		//build data structures for exit point graphs
		for(Step vertical: allVerticals) {
			//put every step into the two Maps, with initial value 0
			exitStepsChrono.put(vertical, 0);
		}
		id2Student.values().forEach(student -> {
			int size = student.getVerticalRecords().size();
			if(size>0) {
				//get step of most recent Record in Student's List of Records
				// will be last in List since it has been sorted
				Step studentsLastStep = student.getVerticalRecords().get(size-1).step;
				//increment count of studentsLastStep in exitStepsChrono Map
				int count=exitStepsChrono.get(studentsLastStep);
				exitStepsChrono.put(studentsLastStep, ++count);
			}
		});
		return exitStepsChrono;
	} 
	
	public static SortedMap<Step,Integer> exitStepsHighest(Map<Integer, Student> id2Student){
		List<Step> allVerticals = DataManager.getDataInstance().getAllVerticals();

		SortedMap<Step,Integer> exitStepsHighest = new TreeMap<>();
		//build data structures for exit point graphs
		for(Step vertical: allVerticals) {
			//put every step into the two Maps, with initial value 0
			exitStepsHighest.put(vertical, 0);
		}
		id2Student.values().forEach(student -> {
			//get the highest Step this Student has seen; could be null
			Step highestStepSeen = student.getHighestStepSeen();
			if(highestStepSeen!=null) {
				//increment count of highestStepSeen in exitStepsHighest Map
				int count = exitStepsHighest.get(highestStepSeen);
				exitStepsHighest.put(highestStepSeen, ++count);
			}
		});
		return exitStepsHighest;
	} 
	
	
	public static SortedMap<Step,Integer> participationFunnel(Map<Integer, Student> id2Student, SortedMap<Step,Integer> exitStepsHighest){
		List<Step> allVerticals = DataManager.getDataInstance().getAllVerticals();

		 SortedMap<Step,Integer> participationFunnel = new TreeMap<>();
			int sum=0;
			for(int i=allVerticals.size()-1; i>=0; i--) {//iterate over all Steps in reverse order
				sum += exitStepsHighest.get(allVerticals.get(i));
				participationFunnel.put(allVerticals.get(i), sum);
			}
		 return participationFunnel; 
	};

	public static SortedMap<Step,Integer> stepFrequencies(Map<Integer, Student> id2Student){
		List<Step> allVerticals = DataManager.getDataInstance().getAllVerticals();

		SortedMap<Step,Integer> stepFrequencies = new TreeMap<>();
		// fill stepFrequencies Map
				for(Step step : allVerticals) {stepFrequencies.put(step, 0);}
				for(Student student : id2Student.values()) {
					try {
						//System.out.println(student.getRecords().toString());
						HashSet<Step> stepsSeen = new HashSet<>();
						for(Record record : student.getVerticalRecords())
							stepsSeen.add(record.step);
						//System.out.println(stepsSeen.toString());

						for(Step step : stepsSeen) {
							Integer count = stepFrequencies.get(step);
							stepFrequencies.put(step, ++count);
						}
					} catch(NullPointerException e) {
						HashSet<Step> stepsSeen = new HashSet<>();
						for(Record record : student.getVerticalRecords())
							stepsSeen.add(record.step);
					}
				}
		return stepFrequencies;
	} 

}
