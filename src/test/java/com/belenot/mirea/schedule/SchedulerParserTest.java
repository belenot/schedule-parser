package com.belenot.mirea.schedule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import com.belenot.mirea.schedule.model.Schedule;
import com.belenot.mirea.schedule.model.ScheduledSubject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance( Lifecycle.PER_CLASS )
@TestMethodOrder( OrderAnnotation.class )
public class SchedulerParserTest {

    private Logger logger = LogManager.getLogger(this);

    private String workBookFile = "/kbisp.xlsx";
    private ScheduleParser schedulerParser;

    @BeforeAll
    public void init() {
	schedulerParser = new ScheduleParser(getClass().getResourceAsStream(workBookFile));
    }
    
    @Test
    @Order( 1 )
    @Disabled
    public void getGroupNamesTest() {
	List<String> groupNames = schedulerParser.getGroupNames();
	groupNames.stream().forEach( s -> logger.info(s));
	assertTrue(groupNames.size() > 0);	
    }

    @Test
    @Order( 2 )
    @Disabled
    public void getGroupIndexTest() {
	int id = assertDoesNotThrow( () -> schedulerParser.getGroupIndex("БАСО-02-16"));
	logger.info(id);
	assertTrue(id > -1);
    }

    @Test
    @Order( 3 )
    @Disabled
    public void parseScheduledSubjectsRowsTest() throws JsonProcessingException {
        List<ScheduledSubjectsRow> scheduledSubjectsRows = schedulerParser.parseScheduledSubjectsRows("БАСО-02-16");
	ObjectWriter writer = new ObjectMapper().writer();
	for (ScheduledSubjectsRow row : scheduledSubjectsRows) {
	    logger.info(writer.withDefaultPrettyPrinter().writeValueAsString(row));
	}
	assertTrue(scheduledSubjectsRows.size() > 0);
    }

    @Test
    @Order( 4 )
    @Disabled
    public void retrieveWeeksTest() {
	List<ScheduledSubjectsRow> scheduledSubjectsRows = schedulerParser.parseScheduledSubjectsRows("БАСО-02-16");
	for (ScheduledSubjectsRow scheduledSubjectsRow : scheduledSubjectsRows) {
	    Map<String, List<Integer>> weeks = schedulerParser.retrieveWeeks(scheduledSubjectsRow.getSubjectTitle(), scheduledSubjectsRow.isWeekParity());
	    assertTrue(weeks.size() > 0);
	    logger.info(scheduledSubjectsRow.getDayOfWeek() + ":" + weeks);
	}
	
    }

    @Test
    @Order( 5 )
    @Disabled
    public void generateScheduledSubjectsTest() {
	List<ScheduledSubjectsRow> scheduledSubjectsRows = schedulerParser.parseScheduledSubjectsRows("БАСО-02-16");
	for (ScheduledSubjectsRow scheduledSubjectsRow : scheduledSubjectsRows) {
	    List<ScheduledSubject> scheduledSubjects = schedulerParser.generateScheduledSubjects(scheduledSubjectsRow);
	    for (ScheduledSubject scheduledSubject : scheduledSubjects) {
		logger.info(scheduledSubject);
	    }
	}
    }

    @Test
    @Order( 6 )
    public void parseScheduleTest() throws JsonProcessingException {
	ObjectWriter writer = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd-MM-yyyy")).writer();
	Schedule schedule = schedulerParser.parseSchedule("БАСО-02-16");
	assertTrue(schedule.getSubjects().size() > 0);
	logger.info(writer.withDefaultPrettyPrinter().writeValueAsString(schedule));
    }

    @AfterAll
    public void destroy() {
	schedulerParser.close();
    }
    

}
