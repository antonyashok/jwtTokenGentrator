package model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * This class is to store the record that students' behavior .
 * @author akistar
 *
 */
public class Record implements java.io.Serializable{
	
	public final OffsetDateTime time;
	public final String session;
	public final Step step;
	
	public Record(OffsetDateTime time, String session, Step step) {
		this.time = time;
		this.session = session;
		this.step = step;
	}
}