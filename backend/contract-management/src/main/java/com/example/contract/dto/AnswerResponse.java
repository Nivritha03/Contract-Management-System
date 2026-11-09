package com.example.contract.dto;

public class AnswerResponse {

    private Long contractId;
    private String question;
    private String answer;

    public AnswerResponse() {
    }

    public AnswerResponse(Long contractId, String question, String answer) {
        this.contractId = contractId;
        this.question = question;
        this.answer = answer;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

