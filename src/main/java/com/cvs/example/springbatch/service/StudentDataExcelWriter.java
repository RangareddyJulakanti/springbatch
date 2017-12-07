package com.cvs.example.springbatch.service;

import com.cvs.example.springbatch.model.ExamResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
@Component("studentDataExcelWriter")
public class StudentDataExcelWriter implements ItemWriter<ExamResult> {
    private static final String[] HEADERS = {"STUDENT_NAME", "PERCENTAGE", "DATE_OF_BIRTH"};

    private String outputFilename;
    private Workbook workbook;
    private CellStyle dataCellStyle;
    private int currRow = 0;

    private void addHeaders(Sheet sheet) {

        Workbook wb = sheet.getWorkbook();

        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();

        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(font);

        Row row = sheet.createRow(2);
        int col = 0;

        for (String header : HEADERS) {
            Cell cell = row.createCell(col);
            cell.setCellValue(header);
            cell.setCellStyle(style);
            col++;
        }
        currRow++;
    }

    private void addTitleToSheet(Sheet sheet) {

        Workbook wb = sheet.getWorkbook();

        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();

        font.setFontHeightInPoints((short) 14);
        font.setFontName("Arial");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(font);

        Row row = sheet.createRow(currRow);
        row.setHeightInPoints(16);


        Cell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
       // cell.setCellValue("Stock Data as of " + Calendar.getInstance().getTime());
        cell.setCellStyle(style);

        CellRangeAddress range = new CellRangeAddress(0, 0, 0, 7);
        sheet.addMergedRegion(range);
        currRow++;
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputFilename);
        workbook.write(fos);
        fos.close();
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Calling beforeStep");
        outputFilename= new FileSystemResource("csv/examResult.xlsx").getFile().getAbsolutePath();
        workbook = new SXSSFWorkbook(100);
        Sheet sheet = workbook.createSheet("Testing");
        sheet.createFreezePane(0, 3, 0, 3);
        sheet.setDefaultColumnWidth(20);
        addTitleToSheet(sheet);
        currRow++;
        addHeaders(sheet);
        initDataStyle();
    }

    private void initDataStyle() {
        dataCellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        dataCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        dataCellStyle.setFont(font);
    }

    @Override
    public void write(List<? extends ExamResult> list) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        list.forEach(examResult -> {
                    currRow++;
                    Row row = sheet.createRow(currRow);
                    createStringCell(row, examResult.getStudentName(), 0);
                    createNumericCell(row, examResult.getPercentage().doubleValue(), 1);
                    createStringCell(row, examResult.getDob().toString(), 2);
                }

        );
    }

    private void createStringCell(Row row, String val, int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(val);
    }

    private void createNumericCell(Row row, Double val, int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(val);
    }
}
