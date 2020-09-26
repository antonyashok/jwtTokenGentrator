package utilTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This program read date values from XLSX file in Java using Apache POI.
 * 
 * @author WINDOWS 8
 * https://javarevisited.blogspot.com/2015/06/how-to-read-write-excel-file-java-poi-example.html
 */
public class ReadTestDataUtil {
	
	public static Map<String,List<String>> testDataMap = new HashMap<>();

	static{
		try {
			readFromExcel("C:\\Users\\Dell\\Desktop\\EDX_TEST_DATA.xlsx");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readFromExcel(String file) throws IOException {
		XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);
        Iterator<Row> rowIterator = myExcelSheet.iterator();
        ArrayList<String> columndata = null;
        String rowData = "";
        
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            columndata = new ArrayList<>();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                if(row.getRowNum() > 0){ //To filter column headings
                	System.out.println(cell.getColumnIndex());
                	System.out.println(cell.getStringCellValue());
                    if(cell.getColumnIndex() == 0) {
                        	rowData = cell.getStringCellValue();
                    } else {
                    		columndata.add(cell.getStringCellValue());
                    }
                }
            }
            if(!rowData.equals("")) {
            	testDataMap.put(rowData,columndata);
            }
        }
        myExcelBook.close();
        System.out.println(testDataMap);

	}

}
