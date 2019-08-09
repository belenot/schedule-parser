package com.belenot.mirea.schedule.model;

public class Classroom {
    private int id;
    private String location;
    private String number;
    public int getId() {
	return id;
    }
    public Classroom setId(int id) {
	this.id = id;
	return this;
    }
    public String getLocation() {
	return location;
    }
    public Classroom setLocation(String location) {
	this.location = location;
	return this;
    }
    public String getNumber() {
	return number;
    }
    public Classroom setNumber(String number) {
	this.number = number;
	return this;
    }
    
}
