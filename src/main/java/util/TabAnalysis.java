package util;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import constant.NavigationConstant;
public class TabAnalysis {


	/*
	"edx.ui.lms.sequence.next_selected"; means the next tab is clicked
	"edx.ui.lms.sequence.previous_selected"; means the previous tab is clicked
	"edx.ui.lms.sequence.tab_selected"; means clicking a specific tab
	 */
	public static String verticalHash(JSONObject logEvent, String eventType, String sequentialHash, Map<String, List<String>> sequentialToVerticalMap, String preSeqHash, String nextSeqHash) throws ParseException {
		String verticalHash = null;
		JSONParser parser=new JSONParser();
		JSONObject eventObj = (JSONObject) parser.parse((String) logEvent.get("event"));

		if(eventType.equals(NavigationConstant.navTab)) {
			int verticalIndex = Integer.parseInt((eventObj.get("target_tab").toString()))-1;
			List<String> verticals = sequentialToVerticalMap.get(sequentialHash);
			try {
				verticalHash =  verticals.get(verticalIndex);
			}catch(Exception e) {
				return null;
			}
			return verticalHash;
		}

		if(eventType.equals(NavigationConstant.navPre)) {
			String type = (String)logEvent.get("event_type");
			// if name and type are same, means not in the same sequential
			if(type.equals(eventType)&&preSeqHash !=null) {
				List<String> verticals = sequentialToVerticalMap.get(preSeqHash);
				verticalHash = verticals.get(verticals.size()-1);

			}else {
				int verticalIndex = Integer.parseInt(eventObj.get("current_tab").toString())-2;
				try {
					List<String> verticals = sequentialToVerticalMap.get(sequentialHash);
					verticalHash =  verticals.get(verticalIndex);
				}catch(Exception e){
					return null;
				}
			}
			return verticalHash;
		}

		if(eventType.equals(NavigationConstant.navNext)) {
			String type = (String)logEvent.get("event_type");
			// if name and type are same, means not in the same sequential
			if(type.equals(eventType)&&nextSeqHash!=null) {
				List<String> verticals = sequentialToVerticalMap.get(nextSeqHash);
				verticalHash = verticals.get(0);
			}else {
				int verticalIndex =Integer.parseInt(eventObj.get("current_tab").toString());
				try {
					List<String> verticals = sequentialToVerticalMap.get(sequentialHash);
					verticalHash =  verticals.get(verticalIndex);
				}catch(Exception e) {
					return null;
				}
			}
			return verticalHash;
		}
		return verticalHash;
	}

}
