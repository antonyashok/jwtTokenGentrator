package utilTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import constant.DisConstant;
import constant.NavigationConstant;
import model.LogRowData;

public class LogReaderXls {
	public static int numLogs;

	public static AtomicInteger rowNum = new AtomicInteger(0);
	public static AtomicInteger listCount = new AtomicInteger(0);

	// Reference :: https://www.callicoder.com/java-write-excel-file-apache-poi/
	private static String[] columns = { "File Name", "Line No", "Event Name", "Status" };

	public static void main(String[] args) throws IOException {
		System.out.println("Started");
		File logsDir = new File("C:\\DataVisual\\tree\\RJ101x\\logs");
		getLogFiles(logsDir, "course-v1:VictoriaX+RJ101x+2T2018");
	}

	public static void getLogFiles(File logsDir, String courseID) throws IOException {
		Map<String, List<LogRowData>> logRowDataMap = new HashMap<>();
		List<LogRowData> logRowDataList = new ArrayList<>();
		File[] logFiles = logsDir.listFiles(file -> file.getName().endsWith(".log"));
		numLogs = logFiles.length;
		Arrays.stream(logFiles).forEach(logFile -> {
			if (logFile.getName().endsWith(".log")) {
				if (logFile.isFile()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
						LogReaderXls.readLogFile(reader, courseID, logRowDataList, logFile.getName());
					} catch (IOException x) {
						System.err.format("IOException: %s%n", x);
					}
				} 
			}
			if (logRowDataList.size() > 900000) {
				System.out.println(logRowDataList.size());
				logRowDataMap.put("Log File Validation " + listCount.getAndAdd(1), logRowDataList.stream().collect(Collectors.toList()));
				logRowDataList.clear();
			}
			rowNum.set(0);
		});
		if (logRowDataList.size() > 0) {
			System.out.println(logRowDataList.size());
			logRowDataMap.put("Log File Validation" + listCount.getAndAdd(1), logRowDataList.stream().collect(Collectors.toList()));
			logRowDataList.clear();
		}
		generateSheet(logRowDataMap);
		System.out.println("Done");
	}

	public static void readLogFile(BufferedReader reader, String courseId, List<LogRowData> logRowDataList,
			String fileName) {

		final JSONParser parser = new JSONParser();

		reader.lines().forEach(line -> {
			LogRowData logRowData = new LogRowData();
			logRowData.setFileName(fileName);
			logRowData.setRowNum(rowNum.addAndGet(1));
			
			JSONObject rootObj;
			try {
				rootObj = (JSONObject) parser.parse(line);
			} catch (org.json.simple.parser.ParseException e) {
				System.err.println(e.getMessage());
				logRowData.setEventName("JSON Parser Error");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				return;
			}
			
			if (null == ((JSONObject) rootObj.get("context"))) {
				logRowData.setEventName("Context not available");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				return;
			}
			
			
			if (!((JSONObject) rootObj.get("context")).get("course_id").equals(courseId)) {
				logRowData.setEventName("Course Id not available"+ ((JSONObject) rootObj.get("context")).get("course_id"));
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				return; // this is to check whether the line is relevant to the course.
			}

			// check user_id on event, skip if user_id is null, or if user_id is in Set of
			// user_ids to ignore
			// http://edx.readthedocs.io/projects/devdata/en/latest/internal_data_formats/tracking_logs/common_fields.html#context-member-fields-common-to-all-events
			Long userIdLong = (Long) ((JSONObject) rootObj.get("context")).get("user_id");

			if (userIdLong == null) {
				logRowData.setEventName("User Id not available");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				return;
			}

			// get Student object for the student in this JSON string, skip if user_id
			// didn't match any known Student

			// check username on event, skip if username is blank
			// http://edx.readthedocs.io/projects/devdata/en/latest/internal_data_formats/tracking_logs/common_fields.html#username-field
			if (rootObj.get("username").equals("")) {
				logRowData.setEventName("User Name Blank");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				return;
			}

			// begin to analyze the event
			// check type of event, don't proceed further if not a type of interest (one of
			// the course navigation events)

			if (rootObj.get("name") != null) {
				String eventString = (String) rootObj.get("name");
				if (!NavigationConstant.navigationEvents().contains(eventString)
						&& (!DisConstant.discussionEvents().contains(eventString))) {
					logRowData.setEventName("Irrelvant Event Type : name" );
					logRowData.setStatus("In Valid");
					logRowDataList.add(logRowData);
					return;
				}
			} else if (rootObj.get("event_type") != null) {
				String eventString = (String) rootObj.get("event_type");
				// if event_type is not in eventTypes Set
				if ((!NavigationConstant.navigationEvents().contains(eventString))
						&& (!DisConstant.discussionEvents().contains(eventString))) {
					logRowData.setEventName("Irrelvant Event Type : event type");
					logRowData.setStatus("In Valid");
					logRowDataList.add(logRowData);
					return;
				}
			} else {
				logRowData.setEventName("Event Type Not Available");
				logRowData.setStatus("In Valid");
				logRowDataList.add(logRowData);
				return;
			}
			
			logRowData.setEventName("");
			logRowData.setStatus("Valid");
			
			logRowDataList.add(logRowData);
		});
	}

	private static void generateSheet(Map<String, List<LogRowData>> logRowDataMap)
			throws FileNotFoundException, IOException {
		String file = "C:\\Users\\Dell\\Desktop\\Log.xlsx";
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		for (Map.Entry<String, List<LogRowData>> entry : logRowDataMap.entrySet()) {
			// Create a Sheet
			Sheet sheet = workbook.createSheet(entry.getKey());
			// Create a Font for styling header cells
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());
			// Create a CellStyle with the font
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			// Create a Row
			Row headerRow = sheet.createRow(0);
			// Create cells
			for (int k = 0; k < columns.length; k++) {
				Cell cell = headerRow.createCell(k);
				cell.setCellValue(columns[k]);
				cell.setCellStyle(headerCellStyle);
			}
			// Create Cell Style for formatting Date
			// Create Other rows and cells with logRowData data
			entry.getValue().stream().forEach(logRowData -> {
				if (null != logRowData) {
					Row row = sheet.createRow(rowNum.addAndGet(1));
					row.createCell(0).setCellValue(logRowData.getFileName());
					row.createCell(1).setCellValue(logRowData.getRowNum());
					row.createCell(2).setCellValue(logRowData.getEventName());
					row.createCell(3).setCellValue(logRowData.getStatus());
				}
			});

			// Resize all columns to fit the content size
			sheet.setColumnWidth(0, 15 * 256);
			sheet.setColumnWidth(1, 15 * 256);
			sheet.setColumnWidth(2, 25 * 256);
			sheet.setColumnWidth(3, 20 * 256);

			System.out.println("Data Loaded");
			rowNum.set(0);
		}
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		fileOut.close();
		workbook.close();
		
		
	}

}
