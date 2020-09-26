package model;
import java.time.OffsetDateTime;

public class DiscussionRecord extends Record{
	public final String type; // to see DisConstant
	public final String id;
	public final String sepId;
	public DiscussionRecord (OffsetDateTime time, String session, DiscussionStep d, String type, String sepId) {
		super(time, session, d);
		this.type = type;
		this.id = d.getDiscussionId();
		this.sepId = sepId;
	}
}