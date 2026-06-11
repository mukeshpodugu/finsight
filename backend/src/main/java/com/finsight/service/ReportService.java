package com.finsight.service;

import com.finsight.entity.Transaction;
import com.finsight.entity.User;
import com.finsight.repository.TransactionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public byte[] generateMonthlyPDFReport(User user, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Transaction> transactions = transactionRepository.findAllByUserAndTransactionDateBetween(user, start, end);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Color primaryColor = new Color(79, 70, 229);
            Color incomeColor = new Color(16, 185, 129);
            Color expenseColor = new Color(239, 68, 68);
            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, primaryColor);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            Paragraph title = new Paragraph("FinSight Monthly Financial Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Paragraph meta = new Paragraph(
                    String.format("Report Scope: %s %d\n" +
                                  "Generated For: %s %s (%s)\n" +
                                  "Developer Name: PODUGU MUKESH\n" +
                                  "Contact: mukeshpodugu123@gmail.com | Phone: +91 8143999463 | Location: Srikakulam\n" +
                                  "Date Generated: %s",
                            start.getMonth().name(), year, user.getFirstName(), user.getLastName(), user.getEmail(),
                            LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))),
                    FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)
            );
            meta.setSpacingAfter(20);
            document.add(meta);

            Paragraph summaryHeading = new Paragraph("Financial Summary", sectionFont);
            summaryHeading.setSpacingAfter(10);
            document.add(summaryHeading);

            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            PdfPCell cell = new PdfPCell(new Phrase("Total Income", boldFont));
            cell.setBackgroundColor(new Color(243, 244, 246));
            cell.setPadding(8);
            summaryTable.addCell(cell);

            cell = new PdfPCell(new Phrase("Total Expense", boldFont));
            cell.setBackgroundColor(new Color(243, 244, 246));
            cell.setPadding(8);
            summaryTable.addCell(cell);

            cell = new PdfPCell(new Phrase("Net Savings", boldFont));
            cell.setBackgroundColor(new Color(243, 244, 246));
            cell.setPadding(8);
            summaryTable.addCell(cell);

            cell = new PdfPCell(new Phrase("₹" + totalIncome, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, incomeColor)));
            cell.setPadding(8);
            summaryTable.addCell(cell);

            cell = new PdfPCell(new Phrase("₹" + totalExpense, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, expenseColor)));
            cell.setPadding(8);
            summaryTable.addCell(cell);

            BigDecimal net = totalIncome.subtract(totalExpense);
            Color netColor = net.compareTo(BigDecimal.ZERO) >= 0 ? incomeColor : expenseColor;
            cell = new PdfPCell(new Phrase("₹" + net, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, netColor)));
            cell.setPadding(8);
            summaryTable.addCell(cell);

            document.add(summaryTable);

            Paragraph txHeading = new Paragraph("Transactions History", sectionFont);
            txHeading.setSpacingAfter(10);
            document.add(txHeading);

            PdfPTable txTable = new PdfPTable(5);
            txTable.setWidthPercentage(100);
            txTable.setWidths(new float[]{1.5f, 2f, 1.2f, 1.5f, 3.8f});

            String[] columns = {"Date", "Category", "Type", "Amount", "Description"};
            for (String col : columns) {
                PdfPCell hCell = new PdfPCell(new Phrase(col, boldFont));
                hCell.setBackgroundColor(primaryColor);
                hCell.setPadding(6);
                hCell.getPhrase().getFont().setColor(Color.WHITE);
                txTable.addCell(hCell);
            }

            for (Transaction t : transactions) {
                txTable.addCell(new PdfPCell(new Phrase(t.getTransactionDate().toString(), normalFont)));
                txTable.addCell(new PdfPCell(new Phrase(t.getCategory().getName(), normalFont)));
                
                Color tColor = "INCOME".equals(t.getType()) ? incomeColor : expenseColor;
                PdfPCell typeCell = new PdfPCell(new Phrase(t.getType(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, tColor)));
                txTable.addCell(typeCell);

                txTable.addCell(new PdfPCell(new Phrase("₹" + t.getAmount(), normalFont)));
                txTable.addCell(new PdfPCell(new Phrase(t.getDescription() != null ? t.getDescription() : "", normalFont)));
            }

            document.add(txTable);

            Paragraph footer = new Paragraph("\n\nReport generated by FinSight platform, powered by Gemini AI API.",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Color.LIGHT_GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }

    public byte[] generateExcelReport(User user, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<Transaction> transactions = transactionRepository.findAllByUserAndTransactionDateBetween(user, start, end);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("FinSight Transactions");

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] columns = {"ID", "Date", "Category", "Type", "Amount (₹)", "Description", "Recurring"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            CellStyle incomeStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font incFont = workbook.createFont();
            incFont.setColor(IndexedColors.GREEN.getIndex());
            incFont.setBold(true);
            incomeStyle.setFont(incFont);

            CellStyle expenseStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font expFont = workbook.createFont();
            expFont.setColor(IndexedColors.RED.getIndex());
            expFont.setBold(true);
            expenseStyle.setFont(expFont);

            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

            int rowIdx = 1;
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());

                Cell dateCell = row.createCell(1);
                dateCell.setCellValue(t.getTransactionDate().toString());
                
                row.createCell(2).setCellValue(t.getCategory().getName());

                Cell typeCell = row.createCell(3);
                typeCell.setCellValue(t.getType());
                typeCell.setCellStyle("INCOME".equals(t.getType()) ? incomeStyle : expenseStyle);

                row.createCell(4).setCellValue(t.getAmount().doubleValue());
                row.createCell(5).setCellValue(t.getDescription() != null ? t.getDescription() : "");
                row.createCell(6).setCellValue(t.isRecurring() ? "YES" : "NO");

                if ("INCOME".equals(t.getType())) {
                    totalIncome = totalIncome.add(t.getAmount());
                } else {
                    totalExpense = totalExpense.add(t.getAmount());
                }
            }

            rowIdx++;
            Row summaryTitleRow = sheet.createRow(rowIdx++);
            Cell titleCell = summaryTitleRow.createCell(3);
            titleCell.setCellValue("ANNUAL SUMMARY");
            
            CellStyle boldStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font bFont = workbook.createFont();
            bFont.setBold(true);
            boldStyle.setFont(bFont);
            titleCell.setCellStyle(boldStyle);

            Row summaryRow1 = sheet.createRow(rowIdx++);
            summaryRow1.createCell(3).setCellValue("Total Income:");
            summaryRow1.createCell(4).setCellValue(totalIncome.doubleValue());
            summaryRow1.getCell(3).setCellStyle(boldStyle);

            Row summaryRow2 = sheet.createRow(rowIdx++);
            summaryRow2.createCell(3).setCellValue("Total Expense:");
            summaryRow2.createCell(4).setCellValue(totalExpense.doubleValue());
            summaryRow2.getCell(3).setCellStyle(boldStyle);

            Row summaryRow3 = sheet.createRow(rowIdx++);
            summaryRow3.createCell(3).setCellValue("Net Savings:");
            summaryRow3.createCell(4).setCellValue(totalIncome.subtract(totalExpense).doubleValue());
            summaryRow3.getCell(3).setCellStyle(boldStyle);

            rowIdx += 2;
            Row developerRow = sheet.createRow(rowIdx);
            developerRow.createCell(0).setCellValue("Report Developer: PODUGU MUKESH | Srikakulam | mukeshpodugu123@gmail.com | 8143999463");

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel report: " + e.getMessage());
        }
    }
}
