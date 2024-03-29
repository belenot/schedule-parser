package com.belenot.mirea.schedule;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.belenot.mirea.schedule.model.Classroom;
import com.belenot.mirea.schedule.model.Schedule;
import com.belenot.mirea.schedule.model.ScheduledSubject;
import com.belenot.mirea.schedule.model.ScheduledSubject.LessonTime;
import com.belenot.mirea.schedule.model.ScheduledSubject.LessonType;
import com.belenot.mirea.schedule.model.Subject;
import com.belenot.mirea.schedule.model.Teacher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ScheduleParser implements Closeable, AutoCloseable {

    private Logger logger = LogManager.getLogger(this);
    
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    private int groupNamesRowIndex = 1;
    private int subjectRowIndexOffset = 2;
    private int subjectRowCount = 72;
    private String groupNamePattern = "^\\p{L}{4}-\\d{2}-\\d{2}.*$";
    private int sheetIndex = 0;
    private int weeksAmount = 17;
    private int rowsPerDay = 12;
    private int year = 2019;
    private int firstMonth = 9;
    
    public ScheduleParser() {};
    public ScheduleParser(InputStream in) {
	try {
	    workbook = new XSSFWorkbook(in);
	    sheet  = workbook.getSheetAt(sheetIndex);
	} catch (IOException exc) {
	    String msg = String.format("Can't create workbook(inputStream)");
	    logger.error(msg, exc);
	}
    }

    public List<String> getGroupNames() {
	List<String> strings = new ArrayList<>(32);
	for (Cell cell : sheet.getRow(groupNamesRowIndex)) {
	    if (!cell.getCellType().equals(CellType.STRING)) continue;
	    String cellValue = cell.getStringCellValue();
	    if (!cellValue.matches(groupNamePattern)) continue;
	    strings.add(cell.getStringCellValue());
	}
	return strings;
    }

    protected int getGroupIndex(String groupName) {
	for (Cell cell : sheet.getRow(groupNamesRowIndex)) {
	    if (!cell.getCellType().equals(CellType.STRING)) continue;
	    String cellValue = cell.getStringCellValue();
	    if (cellValue.matches(String.format("^%s.*$", groupName))) {
		return cell.getColumnIndex();
	    }
	}
	return -1;	
    }

    public Schedule parseSchedule(String groupName) {
	Schedule schedule = new Schedule();
	schedule.setGroupName(groupName);
	List<ScheduledSubject> subjects = new ArrayList<>(128);
	List<ScheduledSubjectsRow> scheduledSubjectsRows = parseScheduledSubjectsRows(groupName);
	for (ScheduledSubjectsRow scheduledSubjectsRow : scheduledSubjectsRows) {
	    subjects.addAll(generateScheduledSubjects(scheduledSubjectsRow));
	}
	schedule.setSubjects(subjects);
	return schedule;
    }

    protected List<ScheduledSubjectsRow> parseScheduledSubjectsRows(String groupName) {
	int columnIndex = getGroupIndex(groupName);
	int rowIndexFirst = groupNamesRowIndex + subjectRowIndexOffset;
	int rowCount = subjectRowCount;
	int lessonNumberOffset = -4;
	int weekParityOffset = -1;
	int lessonTypeOffset = 1;
	int teacherOffset = 2;
	int classroomOffset = 3;
	
	List<ScheduledSubjectsRow> scheduledSubjectsRows = new ArrayList<>(80);
	for (int rowIndex = rowIndexFirst; rowIndex < rowCount + rowIndexFirst; rowIndex++) {
	    Row row = sheet.getRow(rowIndex);
	    ScheduledSubjectsRow scheduledSubjectsRow = new ScheduledSubjectsRow();
	    
	    Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (cell == null || !cell.getCellType().equals(CellType.STRING)) continue;
	    scheduledSubjectsRow.setSubjectTitle(cell.getStringCellValue());
	    
	    cell = row.getCell(columnIndex + lessonTypeOffset);
	    if (cell.getCellType().equals(CellType.STRING)) {
		scheduledSubjectsRow.setLessonType(cell.getStringCellValue());
	    }

	    cell = row.getCell(columnIndex + teacherOffset);
	    if (cell.getCellType().equals(CellType.STRING)){
		scheduledSubjectsRow.setTeacherShortName(cell.getStringCellValue());
	    }

	    cell = row.getCell(columnIndex + classroomOffset, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	    String number = "";
	    switch (cell.getCellType()) {
	    case NUMERIC: number = String.valueOf(cell.getNumericCellValue()); break;
	    case STRING: number = cell.getStringCellValue(); break;
	    default: number = null;
	    }
	    scheduledSubjectsRow.setClassroomNumber(number);

	    cell = row.getCell(columnIndex + weekParityOffset, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	    boolean weekParity = (rowIndex - rowIndexFirst) % 2 == 1;
	    switch (cell.getCellType()) {
	    case STRING: weekParity = cell.getStringCellValue().equals("II")
		    ? true : cell.getStringCellValue().equals("I") ? false : weekParity;
		break;
	    case NUMERIC: weekParity = cell.getNumericCellValue() == 2
		    ? true : cell.getNumericCellValue() == 1 ? false : weekParity;
		break;
	    default: break;
	    }
	    scheduledSubjectsRow.setWeekParity(weekParity);
	    
	    //Assuming that subject number cell merged between two rows
	    if ((rowIndex - rowIndexFirst) % 2 == 0) {
		cell = row.getCell(columnIndex + lessonNumberOffset);
	    } else {
		cell = sheet.getRow(rowIndex - 1).getCell(columnIndex + lessonNumberOffset);
	    }
	    if (cell == null || !cell.getCellType().equals(CellType.NUMERIC)) continue;
	    scheduledSubjectsRow.setLessonNumber((int)cell.getNumericCellValue());

	    int dayOfWeek = (int) ((rowIndex - rowIndexFirst) / rowsPerDay);
	    scheduledSubjectsRow.setDayOfWeek(dayOfWeek);
	    
	    scheduledSubjectsRows.add(scheduledSubjectsRow);
	}
	return scheduledSubjectsRows;
    }

    protected List<ScheduledSubject> generateScheduledSubjects(ScheduledSubjectsRow ssr) {
	List<ScheduledSubject> scheduledSubjects = new ArrayList<>();
	Map<String, List<Integer>> weekMap = retrieveWeeks(ssr.getSubjectTitle(),ssr.isWeekParity());
	for (String subtitle : weekMap.keySet()) {
	    for (int weekNumber : weekMap.get(subtitle)) {
		ScheduledSubject scheduledSubject = new ScheduledSubject();
		Subject subject = new Subject().setTitle(subtitle);
		Classroom classroom = ssr.getClassroomNumber() == null ? null :
		    new Classroom().setNumber(ssr.getClassroomNumber());
		Teacher teacher = ssr.getTeacherShortName() == null ? null :
		    new Teacher().setShortName(ssr.getTeacherShortName());
		LessonType lessonType = ssr.getLessonType() == null ? null :
		    LessonType.byString(ssr.getLessonType());
		LessonTime lessonTime = ssr.getLessonNumber() < 1 || ssr.getLessonNumber() > 6 ?
		    null : LessonTime.byNumber(ssr.getLessonNumber());
		Date date = new GregorianCalendar(year, firstMonth, weekNumber * 7 + ssr.getDayOfWeek()).getTime();
		scheduledSubject.setSubject(subject).setClassroom(classroom)
		    .setTeacher(teacher).setLessonType(lessonType)
		    .setLessonTime(lessonTime).setDate(date);
		scheduledSubjects.add(scheduledSubject);
	    }
	}
	return scheduledSubjects;
    }

    protected Map<String, List<Integer>> retrieveWeeks(String title, boolean weekParity) {
	Map<String, List<Integer>> weekNumbersMap = new HashMap<>();
	Pattern ordinalSubjectPattern = Pattern.compile("(?<subject>\\p{L}{2,}[ \\p{L}]+[ \\p{L}\\d]*)");
	Pattern weekEnumerationPattern = Pattern.compile("(?<weeks>(\\d*?, ?)*?(\\d+)) ?н");
	Pattern krWeekEnumerationPattern = Pattern.compile("(?<krWeeks>кр ?(\\d*?, ?)*?(\\d+)) ?н");
	Matcher ordinalSubjectMatcher = ordinalSubjectPattern.matcher(title);
	int ordinalSubjectPrevIndex = 0;
	int ordinalSubjectNextIndex = 0;
	while (ordinalSubjectMatcher.find(ordinalSubjectNextIndex)) {
	    ordinalSubjectPrevIndex = ordinalSubjectNextIndex;
	    ordinalSubjectNextIndex = ordinalSubjectMatcher.end("subject");
	    String subjectName = ordinalSubjectMatcher.group("subject");
	    Matcher weekEnumerationMatcher = weekEnumerationPattern.matcher(title.substring(ordinalSubjectPrevIndex, ordinalSubjectNextIndex));
	    Matcher krWeekEnumerationMatcher = krWeekEnumerationPattern.matcher(title.substring(ordinalSubjectPrevIndex, ordinalSubjectNextIndex));
	    List<Integer> weekNumbers = new ArrayList<>();
	    if (weekEnumerationMatcher.find() && !krWeekEnumerationMatcher.find()) {
		for (String strWeekNumbers : weekEnumerationMatcher.group("weeks").split(", ?")) {
		    weekNumbers.add(Integer.parseInt(strWeekNumbers));
		}
	    } else {
		for (int weekNumber = weekParity ? 2 : 1; weekNumber < weeksAmount; weekNumber += 2) {
		    weekNumbers.add(weekNumber);
		}
	    }
	    weekNumbersMap.put(subjectName, weekNumbers);
	}
	    
	return weekNumbersMap;
    }
    

    public void close() {
	try {
	    workbook.close();
	} catch (IOException exc) {
	    logger.error("Cannot close workbook");
	}
    }

    public class Configurer {
	private ScheduleParser sp = ScheduleParser.this;

	public Configurer groupNamesRowIndex(int groupNamesRowIndex) {
	    sp.groupNamesRowIndex = groupNamesRowIndex;
	    return this;
	}
	public Configurer subjectRowIndexOffset(int subjectRowIndexOffset) {
	    sp.subjectRowIndexOffset = subjectRowIndexOffset;
	    return this;
	}
	public Configurer subjectRowCount(int subjectRowCount) {
	    sp.subjectRowCount = subjectRowCount;
	    return this;
	}
	public Configurer sheetIndex(int sheetIndex) {
	    sp.sheetIndex = sheetIndex;
	    return this;
	}
	public Configurer weeksAmount(int weeksAmount) {
	    sp.weeksAmount = weeksAmount;
	    return this;
	}
	public Configurer rowsPerDay(int rowsPerDay) {
	    sp.rowsPerDay = rowsPerDay;
	    return this;
	}
	public Configurer year(int year) {
	    sp.year =  year;
	    return this;
	}
	public Configurer firstMonth(int firstMonth) {
	    sp.firstMonth = firstMonth;
	    return this;
	}
    }
}

