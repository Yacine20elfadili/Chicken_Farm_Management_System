// Mortality.java
package ma.farm.model;

import java.time.LocalDate;

public class Mortality {
    private Integer id;
    private Integer houseId;
    private LocalDate deathDate;
    private Integer count;
    private String cause;
    private String symptoms;
    private Boolean isOutbreak;
    private String recordedBy;
    private String notes;
    private String recordedAt;

    // Constructors
    public Mortality() {}

    public Mortality(Integer id, Integer houseId, LocalDate deathDate, Integer count,
                     String cause, String symptoms, Boolean isOutbreak,
                     String recordedBy, String notes, String recordedAt) {
        this.id = id;
        this.houseId = houseId;
        this.deathDate = deathDate;
        this.count = count;
        this.cause = cause;
        this.symptoms = symptoms;
        this.isOutbreak = isOutbreak;
        this.recordedBy = recordedBy;
        this.notes = notes;
        this.recordedAt = recordedAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getHouseId() { return houseId; }
    public void setHouseId(Integer houseId) { this.houseId = houseId; }

    public LocalDate getDeathDate() { return deathDate; }
    public void setDeathDate(LocalDate deathDate) { this.deathDate = deathDate; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public String getCause() { return cause; }
    public void setCause(String cause) { this.cause = cause; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public Boolean getIsOutbreak() { return isOutbreak; }
    public void setIsOutbreak(Boolean outbreak) { isOutbreak = outbreak; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }

    @Override
    public String toString() {
        return "Mortality{" +
                "id=" + id +
                ", houseId=" + houseId +
                ", deathDate=" + deathDate +
                ", count=" + count +
                ", cause='" + cause + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", isOutbreak=" + isOutbreak +
                ", recordedBy='" + recordedBy + '\'' +
                ", notes='" + notes + '\'' +
                ", recordedAt='" + recordedAt + '\'' +
                '}';
    }
}