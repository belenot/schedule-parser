package com.belenot.mirea.schedule;

import java.util.List;
import java.util.Map;

public class ScheduledSubjectsRow {
    private int dayOfWeek;		//-->Date+
    private int lessonNumber;		//-->LessonTime
    private Map<String, List<Integer>> weeks;	//-->Date+
    private boolean weekParity;		//-->Date+
    private String subjectTitle;	//-->Subject.title
    private String lessonType;		//-->LessonType
    private String teacherShortName;	//-->Teacher.shortName
    private String classroomNumber;	//-->Classroom.number
    
    public int getLessonNumber() {
	return lessonNumber;
    }
    public void setLessonNumber(int number) {
	this.lessonNumber = number;
    }
    public Map<String, List<Integer>> getWeeks() {
	return weeks;
    }
    public void setWeeks(Map<String, List<Integer>> weeks) {
	this.weeks = weeks;
    }
    public String getLessonType() {
	return lessonType;
    }
    public void setLessonType(String lessonType) {
	this.lessonType = lessonType;
    }
    public String getTeacherShortName() {
	return teacherShortName;
    }
    public void setTeacherShortName(String teacherShortName) {
	this.teacherShortName = teacherShortName;
    }
    public String getClassroomNumber() {
	return classroomNumber;
    }
    public void setClassroomNumber(String classroomNumber) {
	this.classroomNumber = classroomNumber;
    }
    public String getSubjectTitle() {
	return subjectTitle;
    }
    public void setSubjectTitle(String subjectTitle) {
	this.subjectTitle = subjectTitle;
    }
    public boolean isWeekParity() {
	return weekParity;
    }
    public void setWeekParity(boolean weekParity) {
	this.weekParity = weekParity;
    }
    public int getDayOfWeek() {
	return dayOfWeek;
    }
    public void setDayOfWeek(int dayOfWeek) {
	this.dayOfWeek = dayOfWeek;
    }

    
    
    
}
