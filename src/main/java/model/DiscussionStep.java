package model;



public class DiscussionStep extends Step {
	private String discussionId;
	private String category;
	private String subCategory;

	public DiscussionStep(String displayName ,String hashcode, String discussionId,Step parent, int i) {
		super(displayName ,hashcode, parent.getStepNumInts()[0],  parent.getStepNumInts()[1], parent.getStepNumInts()[2], i );
		this.discussionId = discussionId;
	}

	public String getDiscussionId() {
		return discussionId;
	}

	public void setDiscussionId(String discussionId) {
		this.discussionId = discussionId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
}