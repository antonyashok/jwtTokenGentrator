package utilTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

public class LogFileValidation {
	
	public static AtomicInteger couldNotParseJSON = new AtomicInteger(0); 
	
	@Test
	public void checkLogFileValid() throws FileNotFoundException {
		File logsDir = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		File[] logFiles = logsDir.listFiles(file -> file.getName().endsWith(".log"));
		Arrays.stream(logFiles)
		.parallel()
		.forEach(logFile -> {
			if(logFile.getName().endsWith(".log")) {
				if(logFile.isFile()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
						final JSONParser parser=new JSONParser();
						couldNotParseJSON.set(0);
						reader.lines().forEach(line -> {
							try {
								JSONObject rootObj = (JSONObject) parser.parse(line);
								String errorMsg = checkError(rootObj);
								if(!errorMsg.equals("")) {
									//System.out.println(logFile.getName() + " "+ couldNotParseJSON.getAndIncrement() + " "+ "Suceess");
								//}
								//else {
									System.out.println(logFile.getName() + " "+  couldNotParseJSON.getAndIncrement() +" "+ "Failed" + errorMsg);
								}
							} catch (org.json.simple.parser.ParseException e) {
								System.err.println(e.getMessage());
								//couldNotParseJSON.incrementAndGet();
								System.out.println(logFile.getName() +" "+ couldNotParseJSON.getAndIncrement() + " "+ "Failed" + e.getMessage());
							}
						});
					}
					catch (IOException x) {
						System.err.format("IOException: %s%n", x);
					}

				}else if(logFile.isDirectory()) {
					File eventLog = logFile.listFiles()[0];
					try (BufferedReader reader = new BufferedReader(new FileReader(eventLog))) {
					}
					catch (IOException x) {
						System.err.format("IOException: %s%n", x);
					}
				}else if(logFile.equals("")) {
					System.out.println(logFile.getName() + " "+ "Log file is empty");
				}
			}	
		});
		if(logFiles.length == 0 ) {
			System.out.println("Log folder is empty");
		}
	}

	private String checkError(JSONObject rootObj) {
		JSONObject childObj = (JSONObject) rootObj.get("context");
		String errorMsg = "";
		if(!rootObj.get("host").equals("courses.edx.org") && !rootObj.get("host").equals("preview.edx.org") && !rootObj.get("host").equals("mitxpro.mit.edu") && !rootObj.get("host").equals("studio.edx.org")) {
			errorMsg = "Failure" +"Host is wrong";
		}
		if(!childObj.equals(null)) {
			if(childObj.get("user_id") == "") {
				errorMsg =errorMsg + "Failure" + "User Id not available";
			}
			if(!childObj.get("org_id").equals("VictoriaX")) {
				errorMsg =errorMsg + "Failure" + "Organization Id is wrong";
			}
			if(childObj.get("course_id") == "") {
				errorMsg = errorMsg +"Failure" + "Course Id is not availabe";
			}
		}else {
			errorMsg = "Failure" + "User Id not available"+",Organization Id is wrong"+",Course Id is not availabe";
		}
		return errorMsg;
	}

}
