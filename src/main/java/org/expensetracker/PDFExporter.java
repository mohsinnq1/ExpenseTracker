// PDFExporter.java
package org.expensetracker;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.List;

public class PDFExporter {

    public static void exportExpensesToPDF(List<Expense> expenses, String fileName) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Expense Report");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 720);

            int yOffset = 0;
            for (Expense expense : expenses) {
                if (yOffset > 650) {
                    contentStream.endText();
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(50, 750);
                    yOffset = 0;
                }
                contentStream.showText("Name: " + expense.getName() + " | Amount: " + expense.getAmount() + " | Category: " + expense.getCategoryName());
                contentStream.newLineAtOffset(0, -20);
                yOffset += 20;
            }

            contentStream.endText();
            contentStream.close();

            document.save(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void exportCategoryExpenses(CategoryManager category) {
        List<Expense> expenses = DatabaseHelper.getExpensesByCategory(category.getId());
        String fileName = category.getName() + "_Report.pdf";
        exportExpensesToPDF(expenses, fileName);
    }

}
