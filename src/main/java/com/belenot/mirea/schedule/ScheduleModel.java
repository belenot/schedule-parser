package com.belenot.mirea.schedule;

import java.util.List;

public class ScheduleModel {
    private String groupName;
    private List<ScheduledSubjectModel> scheduledSubjectsModels;
    public String getGroupName() {
	return groupName;
    }
    public void setGroupName(String groupName) {
	this.groupName = groupName;
    }
    public List<ScheduledSubjectModel> getScheduledSubjectsModels() {
	return scheduledSubjectsModels;
    }
    public void setScheduledSubjectsModels(List<ScheduledSubjectModel> scheduledSubjectsModels) {
	this.scheduledSubjectsModels = scheduledSubjectsModels;
    }
}
