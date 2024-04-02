package com.project.application.dto;

public class UploadResponse {
    private int totalRecords;
    private int importedRecords;
    private int failedRecords;

    public UploadResponse() {}

    public UploadResponse(int totalRecords, int importedRecords, int failedRecords) {
        this.totalRecords = totalRecords;
        this.importedRecords = importedRecords;
        this.failedRecords = failedRecords;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getImportedRecords() {
        return importedRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }
}
