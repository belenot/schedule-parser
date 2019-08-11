package com.belenot.mirea.schedule;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public ScheduleModel parseSchedule(String groupName) {
	ScheduleModel scheduleModel = new ScheduleModel();
	scheduleModel.setGroupName(groupName);
	List<ScheduledSubjectModel> subjects = new ArrayList<>(128);
	List<ScheduledSubjectsRow> scheduledSubjectsRows = parseScheduledSubjectsRows(groupName);
	for (ScheduledSubjectsRow scheduledSubjectsRow : scheduledSubjectsRows) {
	    subjects.addAll(generateScheduledSubjectsModels(scheduledSubjectsRow));
	}
	scheduleModel.setScheduledSubjectsModels(subjects);
	return scheduleModel;
    }

    protected List<ScheduledSubjectsRow> parseScheduledSubjectsRows(String groupName) {
	int columnIndex = getGroupIndex(groupName); //Marks column with subject's title
	int rowIndexFirst = groupNamesRowIndex + subjectRowIndexOffset;
	int rowCount = subjectRowCount;
	int lessonNumberIndex = 1;
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
	    
	    cell = row.getCell(columnIndex + lessonTypeOffset, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	    if (cell.getCellType().equals(CellType.STRING)) {
		scheduledSubjectsRow.setLessonType(cell.getStringCellValue());
	    }

	    cell = row.getCell(columnIndex + teacherOffset, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
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
		cell = row.getCell(lessonNumberIndex);
	    } else {
		cell = sheet.getRow(rowIndex - 1).getCell(lessonNumberIndex);
	    }
	    if (cell == null || !cell.getCellType().equals(CellType.NUMERIC)) continue;
	    scheduledSubjectsRow.setLessonNumber((int)cell.getNumericCellValue());

	    int dayOfWeek = (int) ((rowIndex - rowIndexFirst) / rowsPerDay);
	    scheduledSubjectsRow.setDayOfWeek(dayOfWeek);
	    
	    scheduledSubjectsRows.add(scheduledSubjectsRow);
	}
	return scheduledSubjectsRows;
    }

    
    protected List<ScheduledSubjectModel> generateScheduledSubjectsModels(ScheduledSubjectsRow ssr) {
	List<ScheduledSubjectModel> scheduledSubjectsModels = new ArrayList<>();
	Map<String, List<Integer>> weekMap = retrieveWeeks(ssr.getSubjectTitle(),ssr.isWeekParity());
	int titleIndex = 0;
	for (String subtitle : weekMap.keySet()) {
	    for (int weekNumber : weekMap.get(subtitle)) {
		ScheduledSubjectModel scheduledSubjectModel = new ScheduledSubjectModel();
		scheduledSubjectModel.setSubjectTitle(subtitle);
		scheduledSubjectModel.setClassroomNumber(retrieveMergedValue(ssr.getClassroomNumber(), titleIndex, 0));
		scheduledSubjectModel.setTeacherShortName(retrieveMergedValue(ssr.getTeacherShortName(), titleIndex, 0));
		scheduledSubjectModel.setLessonType(retrieveMergedValue(ssr.getLessonType(), titleIndex, 0));
		scheduledSubjectModel.setLessonNumber(ssr.getLessonNumber());
		Date date = new GregorianCalendar(year, firstMonth, weekNumber * 7 + ssr.getDayOfWeek()).getTime();
		scheduledSubjectModel.setDate(date);
		scheduledSubjectsModels.add(scheduledSubjectModel);
	    }
	    titleIndex++;
	}
	return scheduledSubjectsModels;
    }

    protected Map<String, List<Integer>> retrieveWeeks(String title, boolean weekParity) {
	Map<String, List<Integer>> weekNumbersMap = new LinkedHashMap<>(); //Order is IMPORTANT
	Pattern ordinalSubjectPattern =
	    Pattern.compile("(?<subject>(\\p{IsUppercase})(\\p{IsLowercase}+|\\p{IsUpperCase})\\.?([ -](\\p{IsLowerCase}[\\p{IsLowercase}\\.]{2,}|\\p{IsUppercase}\\p{IsUppercase}+|[а-мо-я])\\.?)*((?<cut> \\d,)|( \\d)?))");
	Pattern weekEnumerationPattern = Pattern.compile("(?<weeks>(\\d+ ?, ?)+\\d+(?<cut> ?н\\.?))");
	Pattern krWeekEnumerationPattern = Pattern.compile("(?<krWeeks>кр ?(\\d+ ?, ?)+\\d+(?<cut> ?н\\.?))");	
	Pattern prWeekEnumerationPattern = Pattern.compile("(?<prWeeks>пр ?(\\d+ ?, ?)+\\d+(?<cut> ?н\\.?))");
	Matcher ordinalSubjectMatcher = ordinalSubjectPattern.matcher(title);
	int ordinalSubjectPrevIndex = 0;
	int ordinalSubjectNextIndex = 0;
	while (ordinalSubjectMatcher.find(ordinalSubjectNextIndex)) {
	    ordinalSubjectPrevIndex = ordinalSubjectNextIndex;
	    ordinalSubjectNextIndex = ordinalSubjectMatcher.end("subject");
	    String subjectName = ordinalSubjectMatcher.group("subject");
	    if (ordinalSubjectMatcher.group("cut") != null && ordinalSubjectMatcher.end("subject") == ordinalSubjectMatcher.end("cut")) {
		ordinalSubjectNextIndex = ordinalSubjectMatcher.start("cut");
		subjectName = title.substring(ordinalSubjectMatcher.start("subject"), ordinalSubjectMatcher.start("cut"));
	    }
	    String subTitle = title.substring(ordinalSubjectPrevIndex, ordinalSubjectNextIndex);
	    Matcher weekEnumerationMatcher = weekEnumerationPattern.matcher(subTitle);
	    Matcher krWeekEnumerationMatcher = krWeekEnumerationPattern.matcher(subTitle);
	    Matcher prWeekEnumerationMatcher = prWeekEnumerationPattern.matcher(subTitle);
	    List<Integer> weekNumbers = new ArrayList<>();
	    if (weekEnumerationMatcher.find() && !krWeekEnumerationMatcher.find() && !prWeekEnumerationMatcher.find()) {
		String weekEnumerationString = subTitle.substring(weekEnumerationMatcher.start("weeks"), weekEnumerationMatcher.start("cut"));
		for (String strWeekNumber : weekEnumerationString.split(" ?, ?")) {
			weekNumbers.add(Integer.parseInt(strWeekNumber));
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
    protected String retrieveMergedValue(String value, int index, int defaultIndex) {
	if (value == null) return null;
	String[] values = value.split("[\\n,/]");
	if (index < values.length) {
	    return values[index];
	}
	return defaultIndex < values.length ? values[defaultIndex] : null;
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

