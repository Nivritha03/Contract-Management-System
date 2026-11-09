package com.example.contract.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DocumentParserService {

    /**
     * Extracts plain text from an input stream according to the file extension.
     * Supported formats: PDF, DOC, DOCX.
     */
    public String extractText(InputStream inputStream, String filename) throws IOException {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename: missing extension.");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "pdf":
                return extractTextFromPdf(inputStream);
            case "docx":
                return extractTextFromDocx(inputStream);
            case "doc":
                return extractTextFromDoc(inputStream);
            default:
                throw new IllegalArgumentException("Unsupported file format: ." + extension +
                        ". Allowed formats: PDF, DOC, DOCX.");
        }
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            return text != null ? text.trim() : "";
        }
    }

    private String extractTextFromDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument doc = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(doc)) {
            String text = extractor.getText();
            return text != null ? text.trim() : "";
        }
    }
}

