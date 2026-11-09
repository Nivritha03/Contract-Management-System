package com.example.contract.service;

import com.example.contract.dto.AnswerResponse;
import com.example.contract.dto.ContractResponse;
import com.example.contract.dto.QuestionRequest;
import com.example.contract.dto.StatusUpdateRequest;
import com.example.contract.entity.Contract;
import com.example.contract.enums.ContractStatus;
import com.example.contract.exception.ResourceNotFoundException;
import com.example.contract.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContractService {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("pdf", "doc", "docx"));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final ContractRepository contractRepository;
    private final DocumentParserService documentParserService;
    private final QuestionAnswerService questionAnswerService;
    private final Path uploadLocation;

    public ContractService(ContractRepository contractRepository,
                           DocumentParserService documentParserService,
                           QuestionAnswerService questionAnswerService,
                           @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.contractRepository = contractRepository;
        this.documentParserService = documentParserService;
        this.questionAnswerService = questionAnswerService;
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload folder: " + this.uploadLocation, e);
        }
    }

    /**
     * Uploads and parses a contract document, persists metadata and text in MySQL,
     * and saves the file locally in uploads/.
     */
    @Transactional
    public ContractResponse uploadContract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10 MB limit.");
        }

        String rawFilename = file.getOriginalFilename();
        String originalFilename = StringUtils.cleanPath(rawFilename != null ? rawFilename : "document");

        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid filename containing path traversal characters: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: ." + extension +
                    ". Allowed types: PDF, DOC, DOCX.");
        }

        // 1. Extract text from uploaded stream BEFORE permanent storage
        String extractedText;
        try (InputStream inputStream = file.getInputStream()) {
            extractedText = documentParserService.extractText(inputStream, originalFilename);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract text from document: " + e.getMessage(), e);
        }

        // 2. Prepare local storage path with UUID
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = this.uploadLocation.resolve(storedFilename).normalize();

        // 3. Save file locally
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file on disk: " + e.getMessage(), e);
        }

        // 4. Save to database with rollback file cleanup
        try {
            Contract contract = new Contract();
            contract.setFilename(originalFilename);
            contract.setStoredFilename(storedFilename);
            contract.setFileType(extension.toUpperCase());
            contract.setFileSize(file.getSize());
            contract.setStoragePath(targetPath.toString());
            contract.setExtractedText(extractedText);
            contract.setStatus(ContractStatus.DRAFT);

            Contract saved = contractRepository.save(contract);
            return ContractResponse.fromEntity(saved);
        } catch (Exception e) {
            // Clean up uploaded file if database persistence fails
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException ignored) {
            }
            throw new RuntimeException("Failed to save contract to database: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves an individual contract by ID.
     */
    @Transactional(readOnly = true)
    public ContractResponse getContractById(Long id) {
        Objects.requireNonNull(id, "Contract ID must not be null");
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        return ContractResponse.fromEntity(contract);
    }

    /**
     * Retrieves all contracts.
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll()
                .stream()
                .map(ContractResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a contract following valid transition rules:
     * DRAFT -> REVIEW
     * REVIEW -> APPROVED
     * Reject all other transitions with HTTP 400.
     */
    @Transactional
    public ContractResponse updateStatus(Long id, StatusUpdateRequest request) {
        Objects.requireNonNull(id, "Contract ID must not be null");
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        ContractStatus currentStatus = contract.getStatus();
        ContractStatus newStatus = request.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null.");
        }

        boolean validTransition =
                (currentStatus == ContractStatus.DRAFT && newStatus == ContractStatus.REVIEW) ||
                (currentStatus == ContractStatus.REVIEW && newStatus == ContractStatus.APPROVED);

        if (!validTransition) {
            throw new IllegalArgumentException(String.format(
                    "Invalid status transition from '%s' to '%s'. Allowed transitions: DRAFT -> REVIEW, REVIEW -> APPROVED.",
                    currentStatus, newStatus
            ));
        }

        contract.setStatus(newStatus);
        Contract updated = contractRepository.save(contract);
        return ContractResponse.fromEntity(updated);
    }

    /**
     * Answers questions regarding a contract's extracted text.
     */
    @Transactional(readOnly = true)
    public AnswerResponse askQuestion(Long id, QuestionRequest request) {
        Objects.requireNonNull(id, "Contract ID must not be null");
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        String answer = questionAnswerService.answerQuestion(contract.getExtractedText(), request.getQuestion());
        return new AnswerResponse(contract.getId(), request.getQuestion(), answer);
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1);
    }
}

