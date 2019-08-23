package com.belenot.mirea.schedule;

import java.util.Date;
import java.util.Optional;

public class ScheduledSubjectModel {
    private String subjectTitle;
    private Optional<Integer> lessonNumber = Optional.empty();
    private Optional<String> classroomNumber = Optional.empty();
    private Optional<String> lessonType = Optional.empty();
    private Optional<String> teacherShortName = Optional.empty();
    private Date date;
    private ScheduledSubjectsRow scheduledSubjectsRow;
    public String getSubjectTitle() {
	return subjectTitle;
    }
    public void setSubjectTitle(String subjectTitle) {
	this.subjectTitle = subjectTitle;
    }
    public Optional<Integer> getLessonNumber() {
	return lessonNumber;
    }
    public void setLessonNumber(Integer lessonNumber) {
	this.lessonNumber = Optional.ofNullable(lessonNumber);
    }
    public Optional<String> getClassroomNumber() {
	return classroomNumber;
    }
    public void setClassroomNumber(String classroomNumber) {
	this.classroomNumber = Optional.ofNullable(classroomNumber);
    }
    public Optional<String> getLessonType() {
	return lessonType;
    }
    public void setLessonType(String lessonType) {
	this.lessonType = Optional.ofNullable(lessonType);
    }
    public Optional<String> getTeacherShortName() {
	return teacherShortName;
    }
    public void setTeacherShortName(String teacherShortName) {
	this.teacherShortName = Optional.ofNullable(teacherShortName);
    }
    public Date getDate() {
	return date;
    }
    public void setDate(Date date) {
	this.date = date;
    }

    @Override
    public String toString() {
	String str = String.format("SubjectModel:\nDate: %s\nSubjectTitle: %s\nlessonNumber: %d\nclassroomNumber: %s\nlessonType: %s\nteacherShortName: %s",
				   date.toString(), subjectTitle, lessonNumber.orElse(0),
				   classroomNumber.orElse("N/A"), lessonType.orElse("N/A"),
				   teacherShortName.orElse("N/A"));
	return str;
    }

    public ScheduledSubjectsRow getScheduledSubjectsRow() {
        return scheduledSubjectsRow;
    }

    public void setScheduledSubjectsRow(ScheduledSubjectsRow scheduledSubjectsRow) {
        this.scheduledSubjectsRow = scheduledSubjectsRow;
    }
}
