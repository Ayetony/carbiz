package com.mp.generator.utils;

import com.mp.generator.entity.AlibabaProductInfoPo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ExcelProcess {

    static Logger logger = LoggerFactory.getLogger(ExcelProcess.class);

    public static void main(String[] args) throws IOException {
//        format("src/test_product.xlsx");
        itemsSkuToExcel(new ArrayList<AlibabaProductInfoPo>());
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

    public static File itemsSkuToExcel(List<AlibabaProductInfoPo> list) throws IOException {

        String path = "alibaba_product_info_" + new Date().getTime() +  ".xlsx";
        File file = new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        Workbook wb=new SXSSFWorkbook();
        Sheet sheet = wb.createSheet();
        AtomicInteger rowIndex = new AtomicInteger();
        AtomicReference<Row> row = new AtomicReference<>(sheet.createRow(rowIndex.get()));
        //标题
        row.get().createCell(0).setCellValue("id");
        row.get().createCell(1).setCellValue("source_site");
        row.get().createCell(2).setCellValue("parent_catalog");
        row.get().createCell(3).setCellValue("child_catalog");
        row.get().createCell(4).setCellValue("sku");
        row.get().createCell(5).setCellValue("product_i_id_source_site");
        row.get().createCell(6).setCellValue("product_img_link");
        row.get().createCell(7).setCellValue("product_name");
        row.get().createCell(8).setCellValue("cross_border_pro");
        row.get().createCell(9).setCellValue("product_detail");
        row.get().createCell(10).setCellValue("current_price");
        row.get().createCell(11).setCellValue("shop_name");
        row.get().createCell(12).setCellValue("shop_ref");
        row.get().createCell(13).setCellValue("delivery_address");
        row.get().createCell(14).setCellValue("fast_shipping_fee");
        row.get().createCell(15).setCellValue("total_sale_this_month");
        row.get().createCell(16).setCellValue("size_price_stock");
        row.get().createCell(17).setCellValue("keyword");
        row.get().createCell(18).setCellValue("update_time");

        logger.info("Format the excel Names : " + file.getName() + "  行数:" + list.size());

        list.forEach( po ->{
            rowIndex.getAndIncrement();
            row.set(sheet.createRow(rowIndex.get()));
            row.get().createCell(0).setCellValue(po.getId());
            row.get().createCell(1).setCellValue(po.getSourceSite());
            row.get().createCell(2).setCellValue(po.getParentCatalog());
            row.get().createCell(3).setCellValue(po.getChildCatalog());
            row.get().createCell(4).setCellValue(po.getSku());
            row.get().createCell(5).setCellValue(po.getProductIDInSourceSite());
            row.get().createCell(6).setCellValue(po.getProductImgLink());
            row.get().createCell(7).setCellValue(po.getProductName());
            row.get().createCell(8).setCellValue(po.getCrossBorderPro());
            row.get().createCell(9).setCellValue(po.getProductDetail());
            row.get().createCell(10).setCellValue(po.getCurrentPrice());
            row.get().createCell(11).setCellValue(po.getShopName());
            row.get().createCell(12).setCellValue(po.getShopRef());
            row.get().createCell(13).setCellValue(po.getDeliveryAddress());
            row.get().createCell(14).setCellValue(po.getFastShippingFee());
            row.get().createCell(15).setCellValue(po.getTotalSaleThisMonth());
            row.get().createCell(16).setCellValue(po.getSizePriceStock());
            row.get().createCell(17).setCellValue(po.getKeyword());
            row.get().createCell(18).setCellValue(po.getUpdateTime().toString());
        });

        FileOutputStream fis = new FileOutputStream(file);
        wb.write(fis);
        fis.close();
        return file;
    }
}
