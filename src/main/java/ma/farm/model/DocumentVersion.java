package ma.farm.model;

import java.time.LocalDateTime;

public class DocumentVersion {
    private int id;
    private int documentId;
    private int versionNumber;
    private String pdfContent; // Base64
    private String formData; // JSON
    private LocalDateTime createdAt;

    public DocumentVersion() {
    }

    public DocumentVersion(int id, int documentId, int versionNumber, String pdfContent,
            String formData, LocalDateTime createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.versionNumber = versionNumber;
        this.pdfContent = pdfContent;
        this.formData = formData;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(String pdfContent) {
        this.pdfContent = pdfContent;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
