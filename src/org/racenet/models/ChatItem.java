package org.racenet.models;

public class ChatItem {

	private int id;
	private String name;
	private String text;
	
	public void setId(int i) {
		
		id = i;
	}
	
	public int getId() {
		
		return id;
	}
	
	public void setName(String n) {
		
		name = n;
	}
	
	public String getName() {
		
		return name;
	}
	
	public void setText(String t) {
		
		text = t;
	}
	
	public String getText() {
		
		return text;
	}
}
