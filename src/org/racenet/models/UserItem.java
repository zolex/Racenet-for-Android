package org.racenet.models;

public class UserItem {

	private int id;
	private String username;
	private int playerId;
	private String name;
	private String simplified;
	
	public void setId(int i) {
		
		id = i;
	}
	
	public int getId() {
		
		return id;
	}
	
	public void setUsername(String n) {
		
		username = n;
	}
	
	public String getUsername() {
		
		return username;
	}
	
	public void setPlayerId(int i) {
		
		playerId = i;
	}
	
	public int getPlayerId() {
		
		return playerId;
	}
	
	public void setName(String n) {
		
		name = n;
	}
	
	public String getName() {
		
		return name;
	}
	
	public void setSimplified(String s) {
		
		simplified = s;
	}
	
	public String getSimplified() {
		
		return simplified;
	}
}
