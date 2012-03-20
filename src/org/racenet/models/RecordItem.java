package org.racenet.models;

public class RecordItem {

	private int id;
	private String player;
	private String map;
	private int time;
	private int oldPoints;
	private int newPoints;
	
	public void setId(int i) {
		
		id = i;
	}
	
	public int getId() {
		
		return id;
	}
	
	public void setPlayer(String p) {
		
		player = p;
	}
	
	public String getPlayer() {
		
		return player;
	}
	
	public void setMap(String m) {
		
		map = m;
	}
	
	public String getMap() {
		
		return map;
	}
	
	public void setTime(int t) {
		
		time = t;
	}
	
	public int getTime() {
		
		return time;
	}
	
	public void setOldPoints(int p) {
		
		oldPoints = p;
	}
	
	public int getOldPoints() {
		
		return oldPoints;
	}
	
	public void setNewPoints(int p) {
		
		newPoints = p;
	}
	
	public int getNewPoints() {
		
		return newPoints;
	}
}
