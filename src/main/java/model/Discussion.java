package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Discussion implements Serializable{

	private  Step discussionStep;
	private  int viewedNum =0;
	private  List<Student> viewedNumByStudents = new ArrayList<>();
	private  int postNum = 0;
	private  List<Student> postNumByStudents =  new ArrayList<>();
	private  int postVote = 0;
	private  List<Student> postVoteByStudents =  new ArrayList<>();
	private  int commentNum = 0;
	private  List<Student> commentNumByStudents =  new ArrayList<>();
	private  int responseNum = 0;
	private  List<Student> responseNumByStudents =  new ArrayList<>();
	private  int reponseVote = 0;
	private  List<Student> responseVoteByStudents =  new ArrayList<>();
	
	public Discussion(Step discussionStep) {
		this.discussionStep = discussionStep;
	}

	public Step getDiscussionStep() {
		return discussionStep;
	}

	public void setDiscussionStep(Step discussionStep) {
		this.discussionStep = discussionStep;
	}

	public int getViewedNum() {
		return viewedNum;
	}

	public void setViewedNum(int viewedNum) {
		this.viewedNum = viewedNum;
	}


	public int getPostNum() {
		return postNum;
	}

	public void setPostNum(int postNum) {
		this.postNum = postNum;
	}


	public int getCommentNum() {
		return commentNum;
	}

	public void setCommentNum(int commentNum) {
		this.commentNum = commentNum;
	}

	public int getResponseNum() {
		return responseNum;
	}

	public void setResponseNum(int responseNum) {
		this.responseNum = responseNum;
	}

	public int getReponseVote() {
		return reponseVote;
	}

	public void setReponseVote(int reponseVote) {
		this.reponseVote = reponseVote;
	}
	
	public List<Student> getViewedNumByStudents() {
		return viewedNumByStudents;
	}

	public void setViewedNumByStudents(List<Student> viewedNumByStudents) {
		this.viewedNumByStudents = viewedNumByStudents;
	}

	public List<Student> getPostNumByStudents() {
		return postNumByStudents;
	}

	public void setPostNumByStudents(List<Student> postNumByStudents) {
		this.postNumByStudents = postNumByStudents;
	}

	public List<Student> getCommentNumByStudents() {
		return commentNumByStudents;
	}

	public void setCommentNumByStudents(List<Student> commentNumByStudents) {
		this.commentNumByStudents = commentNumByStudents;
	}

	public List<Student> getResponseNumByStudents() {
		return responseNumByStudents;
	}

	public void setResponseNumByStudents(List<Student> responseNumByStudents) {
		this.responseNumByStudents = responseNumByStudents;
	}

	public List<Student> getResponseVoteByStudents() {
		return responseVoteByStudents;
	}

	public void setResponseVoteByStudents(List<Student> responseVoteByStudents) {
		this.responseVoteByStudents = responseVoteByStudents;
	}
	public int getPostVote() {
		return postVote;
	}

	public void setPostVote(int postVote) {
		this.postVote = postVote;
	}
	public List<Student> getPostVoteByStudents() {
		return postVoteByStudents;
	}
	public void setPostVoteByStudents(List<Student> postVoteByStudents) {
		this.postVoteByStudents = postVoteByStudents;
	}
	
	
}
