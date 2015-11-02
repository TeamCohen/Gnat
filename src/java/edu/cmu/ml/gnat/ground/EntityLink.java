package edu.cmu.ml.gnat.ground;

public class EntityLink {
	private static final char TAB = '\t';
	private String title;
	private String enType;
	private double score;
	private String mention;
	

	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		sb.append(TAB).append(enType);
		sb.append(TAB).append(score);
		sb.append(TAB).append(mention);
		return sb.toString();
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEnType() {
		return enType;
	}
	public void setEnType(String enType) {
		this.enType = enType;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getMention() {
		return mention;
	}
	public void setMention(String mention) {
		this.mention = mention;
	}
}
