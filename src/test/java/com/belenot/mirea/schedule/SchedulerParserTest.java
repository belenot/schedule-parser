package com.belenot.mirea.schedule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

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
    
    @ParameterizedTest
    @Order( 1 )
    @CsvFileSource(resources = "SchedulerParserTest.getGroupNamesTest.csv" )
    public void getGroupNamesTest(String values) {
        List<String> groupNames = schedulerParser.getGroupNames();
        String msg = String.format("%s not equal to %s", groupNames.toString(), values);
        assertTrue(groupNames.stream().anyMatch( gn -> Arrays.stream(values.split(" ")).anyMatch( v -> v.equals(gn))), msg);
        //groupNames.stream().forEach( s -> logger.info(s));
        assertTrue(groupNames.size() > 0);	
    }

    @ParameterizedTest
    @Order( 2 )
    @CsvFileSource(resources = "SchedulerParserTest.getGroupNamesTest.csv", delimiter = ' ' )
    public void getGroupIndexTest(String groupName) {
	int id = assertDoesNotThrow( () -> schedulerParser.getGroupIndex(groupName));
	logger.info(id);
	assertTrue(id > -1, String.format("%s id <= -1(%d)", groupName, id));
    }

    @ParameterizedTest
    @Order( 3 )
    @CsvFileSource( resources = "SchedulerParserTest.parseScheduledSubjectsRowsTest.csv", delimiter = '|')
    public void parseScheduledSubjectsRowsTest(int index, String groupName,
                                            String classroomNumber, String teacherShortName, String subjectTitle, 
                                            int dayOfWeek, int lessonNumber, String lessonType/*, String weeks*/) {
        List<ScheduledSubjectsRow> scheduledSubjectsRows = schedulerParser.parseScheduledSubjectsRows(groupName);
        assertEquals(scheduledSubjectsRows.get(index).getSubjectTitle(), subjectTitle);
        assertEquals(scheduledSubjectsRows.get(index).getClassroomNumber(), classroomNumber);
        assertEquals(scheduledSubjectsRows.get(index).getTeacherShortName(), teacherShortName);
        assertEquals(scheduledSubjectsRows.get(index).getLessonType(), lessonType);
        assertEquals(scheduledSubjectsRows.get(index).getDayOfWeek(), dayOfWeek);
        assertEquals(scheduledSubjectsRows.get(index).getLessonNumber(), lessonNumber);
        //assertEquals(scheduledSubjectsRows.get(0).getWeeks(), weeks);
	    assertTrue(scheduledSubjectsRows.size() > 0);
    }

    @ParameterizedTest
    @Order( 4 )
    @CsvFileSource( resources = "SchedulerParserTest.retrieveWeeksTest.csv", delimiter = '|' )
    public void retrieveWeeksTest(int index, String groupName, String subjectTitle, String assertedWeeks) {
        List<ScheduledSubjectsRow> scheduledSubjectsRows = schedulerParser.parseScheduledSubjectsRows(groupName);
        ScheduledSubjectsRow scheduledSubjectsRow = scheduledSubjectsRows.get(index);
        Map<String, List<Integer>> weeks = schedulerParser.retrieveWeeks(scheduledSubjectsRow.getSubjectTitle(), scheduledSubjectsRow.isWeekParity());
        assertNotNull(weeks.get(subjectTitle));
        String msg = String.format("%s not equals %s", weeks.get(subjectTitle).toString(), assertedWeeks);
        assertTrue(weeks.get(subjectTitle).stream().allMatch( w -> Arrays.stream(assertedWeeks.split(",")).anyMatch( aw -> ((""+w).equals(aw)))), msg);
    }

    @Test
    @Order( 5 )
    //@Disabled
    public void generateScheduledSubjectsTest() {
	List<ScheduledSubjectsRow> scheduledSubjectsRows = schedulerParser.parseScheduledSubjectsRows("БББО-01-16");
	for (ScheduledSubjectsRow scheduledSubjectsRow : scheduledSubjectsRows) {
	    List<ScheduledSubjectModel> scheduledSubjectsModels = schedulerParser.generateScheduledSubjectsModels(scheduledSubjectsRow);
	    for (ScheduledSubjectModel scheduledSubjectModel : scheduledSubjectsModels) {
		logger.info(scheduledSubjectModel);
	    }
	}
    }

    @Test
    @Order( 6 )
    //@Disabled
    public void parseScheduleTest() throws JsonProcessingException {
	//ObjectWriter writer = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd-MM-yyyy")).writer();
	for (String groupName : schedulerParser.getGroupNames()) {
	    groupName = groupName.substring(0, 10);
	    try {
		schedulerParser.parseSchedule(groupName);
	    } catch (Exception exc) {
		logger.error(groupName, exc);
		continue;
	    }
	    logger.info(groupName + ": OK");
	}
	//ScheduleModel scheduleModel = schedulerParser.parseSchedule("БАСО-02-16");
	//logger.info(writer.withDefaultPrettyPrinter().writeValueAsString(scheduleModel));
	//assertTrue(scheduleModel.getScheduledSubjectsModels().size() > 0);
    }

    @AfterAll
    public void destroy() {
	schedulerParser.close();
    }
    

}
