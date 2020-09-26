package util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import model.Student;
import storage.DataManager;
import model.Record;
import model.Step;
/**
 * prepare Table structure for Sankey diagram
 * @author akistar
 */
public class SankeyTable {
	/**Generate a Table showing number of traversals between each pair of steps. Includes start and exit nodes.
	 * row headers = source node indices; col headers = target node indices; body cells = # of traversals
	 * For row/col headers:
	 * n = number of steps
	 * 0 = start node; 1,3,5...2n-1 = 1st time step nodes; 2,4,6...2n = 2nd+ time step nodes; 2n+1 = exit node
	 * @param	students 	populate the Table using only these Students' Lists of Records*/
	public static Table<Integer, Integer, Integer> computeLinksVD(Collection<Student> students) {
		List<Step> verticalsAndDiscussions = DataManager.getDataInstance().getVerticalsAndDiscussions();

		Table<Integer,Integer,Integer> linksTable = HashBasedTable.create();
		students.stream().forEach(student -> {student.sortListChrono(student.getVerticalAndDiscussionRecords());});

		students.stream()
		.map(student -> student.getVerticalAndDiscussionRecords())
		.forEach(list -> {	//for each Student's List of Records
			if(list.isEmpty())
				return;

			//Set of Steps previously visited by this Student
			Set<Step> stepsVisited=new HashSet<>();

			//increment value for link between start node and 1st step in list
			int source=0;	//source = start node
			//int target=allSequentials.indexOf(list.get(0).step)*2+1;
			int target=verticalsAndDiscussions.indexOf(list.get(0).step)*2+1;
			Integer count=linksTable.get(source,target);
			if(count==null)
				linksTable.put(source, target, 1);
			else
				linksTable.put(source, target, ++count);

			stepsVisited.add(list.get(0).step);

			//for each adjacent pair of Records in list, increment the correct value in linksTable
			for(int i=1; i<list.size(); i++){
				source=target;	//source = previous target
				//target=allSequentials.indexOf(list.get(i).step)*2+1;
				target=verticalsAndDiscussions.indexOf(list.get(i).step)*2+1;
				
				if(stepsVisited.contains(list.get(i).step))
					target++;	//target 2nd+ visit node instead
				else
					stepsVisited.add(list.get(i).step);

				count=linksTable.get(source, target);
				if(count==null)
					linksTable.put(source, target, 1);
				else
					linksTable.put(source, target, ++count);
			}

			//increment value for link between last step done and exit node
			source=target;	//source = last target
			//target=allSequentials.size()*2+1;	//target = exit node
			target=verticalsAndDiscussions.size()*2+1;
			count=linksTable.get(source, target);
			if(count==null)
				linksTable.put(source, target, 1);
			else
				linksTable.put(source, target, ++count);
		});
		return linksTable;
	}
	
	/**Generate a Table showing number of students who browsed between each pair of steps.
	 * Includes start and exit nodes.
	 * row headers = source node indices; col headers = target node indices; body cells = # of students
	 * For row/col headers:
	 * n = number of steps
	 * 0 = start node; 1,3,5...2n-1 = 1st time step nodes; 2,4,6...2n = 2nd+ time step nodes; 2n+1 = exit node
	 * @param	students 	populate the Table using only these Students' Lists of Records*/
	public static Table<Integer, Integer, Integer> computeLinksUniqueVD(Collection<Student> students) {
		//create a table structure to store data.
		List<Step> verticalsAndDiscussions = DataManager.getDataInstance().getVerticalsAndDiscussions();

		Table<Integer,Integer,Integer> linksTable = HashBasedTable.create();

		students.stream().forEach(student -> {student.sortListChrono(student.getVerticalAndDiscussionRecords());});

		students.stream()
		.map(student -> student.getVerticalAndDiscussionRecords())
		.forEach(list -> {	//for each Student's List of Records
			if(list.isEmpty())
				return;

			//Set of Steps previously visited by this Student
			Set<Step> stepsVisited=new HashSet<>();

			//Table to record which links this student traversed
			Table<Integer,Integer,Boolean> thisStudentsLinks = HashBasedTable.create();

			//add link between start node and 1st step in list
			int source=0;	//source = start node
			//int target=allSequentials.indexOf(list.get(0).step)*2+1;
			int target=verticalsAndDiscussions.indexOf(list.get(0).step)*2+1;
			thisStudentsLinks.put(source, target, true);

			stepsVisited.add(list.get(0).step);

			//for each adjacent pair of Records in list, add the link
			for(int i=1; i<list.size(); i++){
				source=target;	//source = previous target

				//target=allSequentials.indexOf(list.get(i).step)*2+1;
				target=verticalsAndDiscussions.indexOf(list.get(i).step)*2+1;
				if(stepsVisited.contains(list.get(i).step))
					target++;	//target 2nd+ visit node instead
				else
					stepsVisited.add(list.get(i).step);

				thisStudentsLinks.put(source, target, true);
			}

			//add link between last step done and exit node
			source=target;	//source = last target
			//target=allSequentials.size()*2+1;	//target = end node
			target=verticalsAndDiscussions.size()*2+1;
			thisStudentsLinks.put(source, target, true);

			//finally, increment link values in linksTable
			thisStudentsLinks.cellSet().forEach(cell -> {
				Integer count=linksTable.get(cell.getRowKey(), cell.getColumnKey());
				if(count==null)
					linksTable.put(cell.getRowKey(), cell.getColumnKey(), 1);
				else
					linksTable.put(cell.getRowKey(), cell.getColumnKey(), ++count);
			});
		});
		return linksTable;	
	}

	
	/**Generate a Map of Steps to the number of students who viewed that Step more than once.
	 * Normally the sizes of nodes on the Sankey diagram can be calculated just by adding the values of their links.
	 * However, for the repeat-nodes, when the links are counting students, that approach would double-count students 
	 * who browsed to the repeat-node from more than one other node, or browsed from the repeat-node to more than one 
	 * other node.
	 * So, repeat-node size needs to be calculated here.*/
	public static Map<Step,Integer> computeRepeatNodeValuesUniqueVD(Collection<Student> students) {
		List<Step> verticalsAndDiscussions = DataManager.getDataInstance().getVerticalsAndDiscussions();

		Map<Step,Integer> step2RepeatNodeValue = new HashMap<>();
		for(Step step : verticalsAndDiscussions)
			step2RepeatNodeValue.put(step, 0);

		students.stream()
		.map(student -> student.getVerticalAndDiscussionRecords())
		.forEach(list -> {
			if(list.isEmpty())
				return;

			//Set of Steps seen by this Student
			Set<Step> stepsVisited=new HashSet<>();

			//Set of Steps seen twice or more by this student
			Set<Step> stepsRevisited=new HashSet<>();

			for(Record record : list) {
				if(stepsVisited.contains(record.step)) 
					stepsRevisited.add(record.step);
				else
					stepsVisited.add(record.step);
			}

			//finally, increment values in step2RepeatNodeValue Map
			for(Step step : stepsRevisited) {
				Integer count = step2RepeatNodeValue.get(step);
				step2RepeatNodeValue.put(step, ++count);
			}
		});

		return step2RepeatNodeValue;
	}

	// when filter need time, have to calculate the start node again.
		public static Table<Integer, Integer, Integer> computeTimeLinksVD(Collection<Student> students, Collection<Student> filteredStudents){
			 List<Step> verticalsAndDiscussions = DataManager.getDataInstance().getVerticalsAndDiscussions();

			Table<Integer,Integer,Integer> linksTable = HashBasedTable.create();

			Map<Student, List<Record>> studentToRecords = new HashMap<>();
			List<Integer> ids = new ArrayList<>(); // store the filtered student id

			filteredStudents.stream().map(student -> student.userId).forEach(id -> ids.add(id));

			students
			.stream()
			.filter(student -> ids.contains(student.userId))
			.forEach(student ->{
				Student s1 = filteredStudents.stream().filter(s -> s.userId == student.userId).findFirst().get();
				studentToRecords.put(s1, student.getVerticalAndDiscussionRecords()); // get the filted student records
			});


			Set<Map.Entry<Student, List<Record>>> set = studentToRecords.entrySet();

			set.stream().forEach(entry ->{
				Student currentStudent = entry.getKey();
				List<Record> currentRecords = currentStudent.getVerticalAndDiscussionRecords();
				List<Record> allRecords = entry.getValue();

				if(currentRecords.isEmpty())
					return;
				int source=0;
				int target=verticalsAndDiscussions.indexOf(currentRecords.get(0).step)*2+1;
				Integer count= linksTable.get(source, target);

				if(RecordAnalysis.isFirst(allRecords, currentRecords.get(0))) {
					if(count == null) {
						linksTable.put(source, target, 1);
					}else {
						linksTable.put(source, target, ++count);
					}
				}

				//check whether it is a repeat node
				if(RecordAnalysis.hasPrevious(allRecords, currentRecords.get(0)))
					target++;

				for(int i=1; i<currentRecords.size(); i++){
					source=target;	
					target=verticalsAndDiscussions.indexOf(currentRecords.get(i).step)*2+1;
					if(RecordAnalysis.hasPrevious(allRecords, currentRecords.get(i)))
						target++;

					count=linksTable.get(source, target);
					if(count==null)
						linksTable.put(source, target, 1);
					else
						linksTable.put(source, target, ++count);
				}

				Record lastRecord = currentRecords.get(currentRecords.size()-1);
				if(RecordAnalysis.isLast(allRecords, lastRecord)) {
					source = target;
					target = verticalsAndDiscussions.size()*2+1;
					count=linksTable.get(source, target);
					if(count==null)
						linksTable.put(source, target, 1);
					else
						linksTable.put(source, target, ++count);
				}


			});		
			return linksTable;
		}
		
		// when filter need time, have to calculate the start node again.
		public static Table<Integer, Integer, Integer> computeTimeLinksUniqueVD(Collection<Student> students, Collection<Student> filteredStudents){
			 List<Step> verticalsAndDiscussions = DataManager.getDataInstance().getVerticalsAndDiscussions();

			Table<Integer,Integer,Integer> linksTable = HashBasedTable.create();

			Map<Student, List<Record>> studentToRecords = new HashMap<>();
			List<Integer> ids = new ArrayList<>(); // store the filtered student id

			filteredStudents.stream().map(student -> student.userId).forEach(id -> ids.add(id));

			students
			.stream()
			.filter(student -> ids.contains(student.userId))
			.forEach(student ->{
				Student s1 = filteredStudents.stream().filter(s -> s.userId == student.userId).findFirst().get();
				studentToRecords.put(s1, student.getVerticalAndDiscussionRecords()); // get the filted student records
			});


			Set<Map.Entry<Student, List<Record>>> set = studentToRecords.entrySet();

			set.stream().forEach(entry ->{
				Student currentStudent = entry.getKey();
				List<Record> currentRecords = currentStudent.getVerticalAndDiscussionRecords();
				List<Record> allRecords = entry.getValue();

				Table<Integer,Integer,Boolean> thisStudentsLinks = HashBasedTable.create();

				if(currentRecords.isEmpty())
					return;
				int source=0;
				int target=verticalsAndDiscussions.indexOf(currentRecords.get(0).step)*2+1;

				if(RecordAnalysis.isFirst(allRecords, currentRecords.get(0))) {
					thisStudentsLinks.put(source, target, true);
				}

				//check whether it is a repeat node
				if(RecordAnalysis.hasPrevious(allRecords, currentRecords.get(0))) {
					target++;				
				}

				for(int i=1; i<currentRecords.size(); i++){
					source=target;	
					target=verticalsAndDiscussions.indexOf(currentRecords.get(i).step)*2+1;
					if(RecordAnalysis.hasPrevious(allRecords, currentRecords.get(i)))
						target++;
					thisStudentsLinks.put(source, target, true);
				}

				Record lastRecord = currentRecords.get(currentRecords.size()-1);
				if(RecordAnalysis.isLast(allRecords, lastRecord)) {
					source = target;
					target = verticalsAndDiscussions.size()*2+1;
					thisStudentsLinks.put(source, target, true);
				}

				thisStudentsLinks.cellSet().forEach(cell -> {
					Integer count = linksTable.get(cell.getRowKey(), cell.getColumnKey());
					if(count == null)
						linksTable.put(cell.getRowKey(), cell.getColumnKey(), 1);
					else
						linksTable.put(cell.getRowKey(), cell.getColumnKey(), ++count);
				});

			});		
			return linksTable;
		}

}
