package com.example.contract.controller;

import com.example.contract.dto.AnswerResponse;
import com.example.contract.dto.ContractResponse;
import com.example.contract.dto.QuestionRequest;
import com.example.contract.dto.StatusUpdateRequest;
import com.example.contract.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "*")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    /**
     * Upload contract in PDF, DOC, or DOCX format.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractResponse> uploadContract(@RequestParam("file") MultipartFile file) {
        ContractResponse response = contractService.uploadContract(file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieve an individual contract by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContractById(@PathVariable("id") Long id) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve all contracts.
     */
    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAllContracts() {
        List<ContractResponse> response = contractService.getAllContracts();
        return ResponseEntity.ok(response);
    }

    /**
     * Update contract status (allowed: DRAFT -> REVIEW, REVIEW -> APPROVED).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContractResponse> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        ContractResponse response = contractService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Ask a question about the contract text.
     */
    @PostMapping("/{id}/ask")
    public ResponseEntity<AnswerResponse> askQuestion(
            @PathVariable("id") Long id,
            @Valid @RequestBody QuestionRequest request) {
        AnswerResponse response = contractService.askQuestion(id, request);
        return ResponseEntity.ok(response);
    }
}

