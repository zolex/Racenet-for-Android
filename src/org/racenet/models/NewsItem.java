package org.racenet.models;

public class NewsItem {

	private int id;
	private String title;
	private String body;
	
	public void setId(int i) {
		
		id = i;
	}
	
	public int getId() {
		
		return id;
	}
	
	public void setTitle(String t) {
		
		title = t;
	}
	
	public String getTitle() {
		
		return title;
	}
	
	public void setBody(String b) {
		
		body = b;
	}
	
	public String getBody() {
		
		return body;
	}
}
