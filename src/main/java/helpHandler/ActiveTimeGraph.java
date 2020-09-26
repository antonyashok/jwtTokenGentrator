package helpHandler;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import constant.TimeConstant;
import model.Student;
import storage.DataManager;

public class ActiveTimeGraph {
	
	/**Retrieves activeTimeMap from the DataManagerV2 class, then converts it to 2 matching Lists.
	 * One List contains JSON Strings that represent the OffsetDateTime as a JSON Array of numbers 
	 * corresponding to year, month, day, hour and minute.
	 * The other List contains Integers representing how many Students were in each Set.
	 * Can't return 2 Lists so caller should provide references to 2 empty Lists.
	 * @param	dateTimeJSONArrayStrings	provide an empty list
	 * @param	numStudents		provide an empty list*/
	public static void getActiveTimeLists(List<String> dateTimeJSONArrayStrings, List<Integer> numStudents) {
		Map<OffsetDateTime,Set<Student>> activeTimeMap = DataManager.getDataInstance().getActiveTimeMap();
		
		//make List of timeslot start times in chrono order, including timeslots with 0 students present
		// get first and last times
		OffsetDateTime firstTimeslot = activeTimeMap.keySet().stream()
			.min((o1, o2) -> o1.isBefore(o2) ? -1 : (o1.isAfter(o2) ? 1 : 0))
			.get();
		
		OffsetDateTime lastTimeslot = activeTimeMap.keySet().stream()
			.max((o1, o2) -> o1.isBefore(o2) ? -1 : (o1.isAfter(o2) ? 1 : 0))
			.get();
		
		// construct ArrayList with sufficient capacity
		int capacity = (int) Duration.between(firstTimeslot, lastTimeslot).toMinutes() / TimeConstant.timeslotLength +1;
		ArrayList<OffsetDateTime> timeslots = new ArrayList<>(capacity);
		
		// fill List
		OffsetDateTime nextTimeslot = firstTimeslot;
		do {
			timeslots.add(nextTimeslot);
			nextTimeslot = nextTimeslot.plusMinutes(TimeConstant.timeslotLength);
		}
		while(!nextTimeslot.isAfter(lastTimeslot));
		
		
		//make matching List of Set sizes (# of Students in each Set)
		if(numStudents instanceof ArrayList)
			((ArrayList<Integer>) numStudents).ensureCapacity(timeslots.size());
		
		for(OffsetDateTime timeslot : timeslots) {
			Set<Student> set = activeTimeMap.get(timeslot);
			
			if(set!=null)
				numStudents.add(set.size());
			else
				numStudents.add(0);
		}
		
		//make matching List of JSONArrays of numbers, each representing an OffsetDateTime
		if(dateTimeJSONArrayStrings instanceof ArrayList)
			((ArrayList<String>) dateTimeJSONArrayStrings).ensureCapacity(timeslots.size());
		
		for(OffsetDateTime timeslot : timeslots) {
			ArrayList<Integer> fields = new ArrayList<>();
			fields.add(timeslot.getYear());
			fields.add(timeslot.getMonthValue()-1);	//-1 because month field starts at 0 in javascript
			fields.add(timeslot.getDayOfMonth());
			fields.add(timeslot.getHour());
			fields.add(timeslot.getMinute());
			
			dateTimeJSONArrayStrings.add(JSONArray.toJSONString(fields));
		}
		
	}
}