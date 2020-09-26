package util;

import java.util.List;

import model.Record;

public class RecordAnalysis {

	public static boolean hasPrevious(List<Record> records, Record current) {
		boolean has = false;
		int n = records.indexOf(current);
		for(int i = 0; i< n; i++) {
			Record r = records.get(i);
			if(r.step.equals(current.step)) {
				has = true;
				break;
			}		
		}
		return has;
	}
	
	public static boolean isFirst(List<Record> records, Record current) {
		boolean isFirst = false;
		boolean hasPrevious = RecordAnalysis.hasPrevious(records, current);
		if(records.get(0).step.equals(current.step) && !hasPrevious) 
			isFirst = true;
		return isFirst;
	}
	
	public static boolean isLast(List<Record> records, Record current) {
		boolean isLast = false;
		int rLength = records.size();
		if(records.get(rLength -1 ).equals(current))
			isLast = true;
		
		return isLast;
 	}
}
