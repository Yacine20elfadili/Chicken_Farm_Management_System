package ma.farm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Task model - Represents daily tasks for workers
 * Used in: Tasks page
 */
public class Task {

    // Fields
    private int id;
    private String description; // Task description
    private String status; // Done, Pending, Missed
    private LocalDate dueDate; // When task should be completed
    private LocalDateTime completedAt; // When task was actually completed
    private String assignedTo; // Worker name/id
    private int houseId; // Related house (optional)
    private String category; // Cleaning, Feeding, Collection, Medical
    private int crackedEggs; // For egg collection tasks
    private String notes; // Additional details
    private String priority; // High, Medium, Low

    // Default constructor
    public Task() {}

    // Constructor for new task
    public Task(
        String description,
        LocalDate dueDate,
        String assignedTo,
        String category
    ) {
        this.description = description;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.category = category;
        this.status = "Pending";
    }

    // Full constructor
    public Task(
        int id,
        String description,
        String status,
        LocalDate dueDate,
        LocalDateTime completedAt,
        String assignedTo,
        int houseId,
        String category,
        int crackedEggs,
        String notes,
        String priority
    ) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.completedAt = completedAt;
        this.assignedTo = assignedTo;
        this.houseId = houseId;
        this.category = category;
        this.crackedEggs = crackedEggs;
        this.notes = notes;
        this.priority = priority;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCrackedEggs() {
        return crackedEggs;
    }

    public void setCrackedEggs(int crackedEggs) {
        this.crackedEggs = crackedEggs;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    // Business methods

    /**
     * Check if task is overdue
     * @return true if past due date and not completed
     */
    public boolean isOverdue() {
        if (dueDate == null || "Done".equals(status)) {
            return false;
        }
        return dueDate.isBefore(LocalDate.now());
    }

    /**
     * Check if task is completed
     * @return true if status is "Done"
     */
    public boolean isCompleted() {
        return "Done".equals(status);
    }

    /**
     * Mark task as done
     */
    public void markAsComplete() {
        this.status = "Done";
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Get status badge color for UI
     * @return CSS color class name
     */
    public String getStatusColor() {
        if (status == null) {
            return "secondary";
        }
        switch (status) {
            case "Done":
                return "success";
            case "Pending":
                return "warning";
            case "Missed":
                return "danger";
            default:
                return "secondary";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (
            "Task{" +
            "id=" +
            id +
            ", description='" +
            description +
            '\'' +
            ", status='" +
            status +
            '\'' +
            ", dueDate=" +
            dueDate +
            ", completedAt=" +
            completedAt +
            ", assignedTo='" +
            assignedTo +
            '\'' +
            ", houseId=" +
            houseId +
            ", category='" +
            category +
            '\'' +
            ", crackedEggs=" +
            crackedEggs +
            ", notes='" +
            notes +
            '\'' +
            ", priority='" +
            priority +
            '\'' +
            '}'
        );
    }
}
