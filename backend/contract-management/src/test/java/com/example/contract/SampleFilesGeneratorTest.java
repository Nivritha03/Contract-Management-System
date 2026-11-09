package com.example.contract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

class SampleFilesGeneratorTest {

    @Test
    void generateSampleFiles() throws Exception {
        File dir = new File("sample-files");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 1. Generate sample PDF
        File pdfFile = new File(dir, "sample-agreement.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                content.newLineAtOffset(50, 700);
                content.showText("SERVICES AGREEMENT");
                content.newLineAtOffset(0, -30);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.showText("This Services Agreement is entered into between Acme Corp and Beta LLC.");
                content.newLineAtOffset(0, -20);
                content.showText("The payment shall be completed within 30 days of receiving the invoice.");
                content.newLineAtOffset(0, -20);
                content.showText("The agreement shall commence on January 1, 2026 and continue for one year.");
                content.newLineAtOffset(0, -20);
                content.showText("Either party may terminate this agreement upon 60 days written notice.");
                content.endText();
            }
            doc.save(pdfFile);
        }

        // 2. Generate sample DOCX
        File docxFile = new File(dir, "sample-consulting.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(docxFile)) {
            XWPFParagraph p1 = doc.createParagraph();
            XWPFRun r1 = p1.createRun();
            r1.setBold(true);
            r1.setText("CONSULTING SERVICES CONTRACT");

            XWPFParagraph p2 = doc.createParagraph();
            XWPFRun r2 = p2.createRun();
            r2.setText("The consultant shall provide software development services.");

            XWPFParagraph p3 = doc.createParagraph();
            XWPFRun r3 = p3.createRun();
            r3.setText("Confidential information shall be kept secret for 5 years after termination.");

            XWPFParagraph p4 = doc.createParagraph();
            XWPFRun r4 = p4.createRun();
            r4.setText("The governing law of this contract shall be the State of California.");

            doc.write(out);
        }

        System.out.println("Generated sample files successfully in: " + dir.getAbsolutePath());
    }
}

