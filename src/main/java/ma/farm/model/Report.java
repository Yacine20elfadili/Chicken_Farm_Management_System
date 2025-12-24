package ma.farm.model;

import java.time.LocalDate;

/**
 * Report Model - Represents a generated analytical report.
 */
public class Report {

    public enum ReportType {
        Financial, Production, Inventory, Consumption, Summary
    }

    public enum ReportFormat {
        View, PDF, Excel
    }

    private int id;
    private String title;
    private ReportType type;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate generatedDate;
    private ReportFormat format;
    private String filePath;
    private Integer createdBy; // User ID
    private String notes;
    private String createdAt;

    // Constructors
    public Report() {
    }

    public Report(String title, ReportType type, LocalDate periodStart, LocalDate periodEnd) {
        this.title = title;
        this.type = type;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.generatedDate = LocalDate.now();
        this.format = ReportFormat.View;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public void setFormat(ReportFormat format) {
        this.format = format;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
