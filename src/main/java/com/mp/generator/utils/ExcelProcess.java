package com.mp.generator.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;

public class ExcelProcess {

    static Logger logger = LoggerFactory.getLogger(ExcelProcess.class);

    public static void main(String[] args) throws IOException {
        format("src/test_product.xlsx");
        return;
    }
    //合并字段
    public static void clean(String filePath) throws IOException {
        String extString = filePath.substring(filePath.lastIndexOf("."));
        Workbook wb = null;
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                wb = new XSSFWorkbook(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Sheet sheet = wb.getSheetAt(0);

        int rownum = sheet.getPhysicalNumberOfRows();
        //因为模板是在第四行开始读取，那么我们的直接定位到第四行
        for (int i = 1; i < rownum; i++) {
            //获取当前行
            Row row = sheet.getRow(i);
            if (row != null) {
                int cellPosition = row.getPhysicalNumberOfCells();
                String val = row.getCell(cellPosition - 1).getStringCellValue();
                row.getCell(cellPosition - 1).setCellValue(val.replace(" ", "").replace("\t", "").replace("\n", ""));
                String cells = "";
                for (int index = 6; index <= 25; index++) {
                    cells = cells + row.getCell(index).getStringCellValue();
                }
                System.out.println(cells);
                row.getCell(6).setCellValue(cells);

            }
        }
        int len = sheet.getLastRowNum();
        while (len >= 0) {
            CellRangeAddress region = new CellRangeAddress(len, len, 6, 25);
            sheet.addMergedRegion(region);
            len--;
        }

        FileOutputStream outputStream = new FileOutputStream(filePath);
        wb.write(outputStream);
        outputStream.close();
        is.close();
    }

    public static void format(String filePath) throws IOException {
        logger.info("Format the excel : " + filePath + LocalDateTime.now());
        String extString = filePath.substring(filePath.lastIndexOf("."));
        Workbook wb = null;
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                wb = new XSSFWorkbook(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Sheet sheet = wb.getSheetAt(0);

        int rownum = sheet.getPhysicalNumberOfRows();
        //因为模板是在第四行开始读取，那么我们的直接定位到第四行
        for (int i = 1; i < rownum; i++) {
            //获取当前行
            Row row = sheet.getRow(i);
            int cells = row.getPhysicalNumberOfCells();
            if (row != null) {
                for(int cellPosition=0;cellPosition<cells;cellPosition++) {
                    String val = row.getCell(cellPosition).getStringCellValue();
                    if (val != null) {
                        row.getCell(cellPosition)
                                .setCellValue(val.replace(" ", "")
                                        .replace("\t", "")
                                        .replace("\n", ""));
                    }
                }
            }
        }

        FileOutputStream outputStream = new FileOutputStream(filePath);
        wb.write(outputStream);
        outputStream.close();
        is.close();
    }

    public static void itemsToSku(){

    }
}
