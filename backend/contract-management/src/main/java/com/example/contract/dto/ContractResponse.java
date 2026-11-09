package com.example.contract.dto;

import com.example.contract.entity.Contract;
import com.example.contract.enums.ContractStatus;

import java.time.LocalDateTime;

public class ContractResponse {

    private Long id;
    private String filename;
    private String storedFilename;
    private String fileType;
    private Long fileSize;
    private String extractedText;
    private ContractStatus status;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    public ContractResponse() {
    }

    public static ContractResponse fromEntity(Contract contract) {
        ContractResponse response = new ContractResponse();
        response.setId(contract.getId());
        response.setFilename(contract.getFilename());
        response.setStoredFilename(contract.getStoredFilename());
        response.setFileType(contract.getFileType());
        response.setFileSize(contract.getFileSize());
        response.setExtractedText(contract.getExtractedText());
        response.setStatus(contract.getStatus());
        response.setUploadedAt(contract.getUploadedAt());
        response.setUpdatedAt(contract.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

