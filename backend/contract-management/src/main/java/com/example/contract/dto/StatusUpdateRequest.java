package com.example.contract.dto;

import com.example.contract.enums.ContractStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ContractStatus status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(ContractStatus status) {
        this.status = status;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}

