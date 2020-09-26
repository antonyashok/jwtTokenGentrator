package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import constant.DisConstant;
import model.ClusterStudent;
import model.DiscussionRecord;
import model.Record;
import model.Step;
import model.Student;
import storage.DataManager;

public class ClusterPrepare {
	
	public static List<ClusterStudent> prepareClusterData(Map<Integer, Student> id2Student){
		List<Step> verticals = DataManager.getDataInstance().getAllVerticals();

		List<ClusterStudent> clusterData = new ArrayList<ClusterStudent>();
		id2Student.forEach((id, student) -> {
			//get the vertical and discussion list;
			List<Record> verticalRecords = student.getVerticalRecords();
			List<DiscussionRecord> discussionRecords = student.getDiscussionRecords();

			if(verticalRecords.isEmpty()) {
				return;
			}

			student.sortListChrono(verticalRecords);
			student.deleteConsecutiveDuplicates(verticalRecords);
			student.sortDiscussionChrono(discussionRecords);
			student.deleteDiscussionConsecutiveDuplicates(discussionRecords);

			ClusterStudent clusterStudent = new ClusterStudent(id);

			double movement = 0;
			double expectedMovement = 0;
			double forwardMovement = 0;
			double backwardMovement = 0;
			double visited = 0;
			double revisited = 0;
			double total =  0;
			double view = 0;
			double post = 0;
			double statement = 0;


			Set<Step> stepsVisited=new HashSet<>();

			int source = 0;

			int target = verticals.indexOf(verticalRecords.get(0).step)*2+1 ;

			if(target > source +1) {
				// this means student jumps to another vertical.
				forwardMovement++;
			}else{
				// this means student goes to the next vertical
				expectedMovement++;
			}
			movement++; // doing movement
			visited++; // visit the target
			stepsVisited.add(verticalRecords.get(0).step);

			for(int i =1; i< verticalRecords.size(); i++) {
				source = target;	// target become source		
				target = verticals.indexOf(verticalRecords.get(i).step)*2 +1;	 	
				if(stepsVisited.contains(verticalRecords.get(i).step)) {
					target++; // let the target equal the index of the repeated node
					revisited++; // this mean student has already seen the target
					}
				else {
					stepsVisited.add(verticalRecords.get(i).step); // adding the stepVisited record
					visited++; // first time visited the target
				}
				if(target == source+2 || target == source+1) {
					expectedMovement ++; 
				}else if(target > source+2) {
					forwardMovement ++;
				}else {
					backwardMovement++;
				}
				movement++;
			}

			for(DiscussionRecord discussionRecord: discussionRecords) {
				if(discussionRecord.type.equals(DisConstant.disView)) {
					view++;
				}if(discussionRecord.type.equals(DisConstant.thrCre)) {
					post++;
					statement++;
				}if(discussionRecord.type.equals(DisConstant.comCre)|| discussionRecord.type.equals(DisConstant.resCre)) {
					statement++;
				}
			}

			total = revisited+ visited;

//			if(view == 0 && post ==1) {
//				for(DiscussionRecord d: student.getDiscussionRecords()) {
//					System.out.println(student.username);
//					System.out.println(d.type);
//					System.out.println(d.step.toString());
//
//				}
//			}
			clusterStudent.setExpectedMovement(expectedMovement/movement);
			clusterStudent.setBackwardMovement(backwardMovement/movement);
			clusterStudent.setForwardMovement(forwardMovement/movement);
			clusterStudent.setVisited(visited/total);
			clusterStudent.setRevisited(revisited/total);
			clusterStudent.setViewByMovement(view/movement);
			clusterStudent.setPostByStatement(statement == 0? 0: post/statement);
			clusterStudent.setStatementByView(view == 0? (statement == 0? 0: -1) : statement/view);

			clusterData.add(clusterStudent);

		});


		return clusterData;
	}



	public static void printClusterStudentCSVFile (File outFile,List<ClusterStudent> clusterData) {
		try(BufferedWriter out = new BufferedWriter(new FileWriter(outFile))){
			//print headers (print method on CSVFormat prints just the headers, and, returns a CSVPrinter)
			final CSVPrinter printer = CSVFormat.RFC4180.withHeader(
					"learner_id",
					"expectedMovement",
					"forwardMovement",
					"backwardMovement",
					"visited",
					"revisited",
					"viewForMovement",
					"postForStatement",
					"statementForView"
					).print(out);	


			clusterData.forEach(student ->{

				try {
					printer.printRecord(
							student.userId, 
							student.getExpectedMovement(), 
							student.getForwardMovement(), 
							student.getBackwardMovement(), 
							student.getVisited(),
							student.getRevisited(),
							student.getViewByMovement(),
							student.getPostByStatement(),
							student.getStatementByView()
							);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
}
