package com.belenot.mirea.schedule.model;

import java.util.List;

public class Schedule {
    private int id;
    private String groupName;
    private List<ScheduledSubject> subjects;
    public int getId() {
	return id;
    }
    public Schedule setId(int id) {
	this.id = id;
	return this;
    }
    public String getGroupName() {
	return groupName;
    }
    public Schedule setGroupName(String groupName) {
	this.groupName = groupName;
	return this;
    }
    public List<ScheduledSubject> getSubjects() {
	return subjects;
    }
    public Schedule setSubjects(List<ScheduledSubject> subjects) {
	this.subjects = subjects;
	return this;
    }
    
}
