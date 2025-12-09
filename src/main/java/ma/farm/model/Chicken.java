package ma.farm.model;

import java.time.LocalDate;

public class Chicken {
    private Integer id;
    private Integer houseId;
    private String batchNumber;
    private Integer quantity;
    private LocalDate arrivalDate;
    private Integer ageInDays;
    private String gender;
    private String healthStatus;
    private Double averageWeight;
    private LocalDate nextTransferDate;

    // Constructors
    public Chicken() {}

    public Chicken(Integer id, Integer houseId, String batchNumber, Integer quantity,
                   LocalDate arrivalDate, Integer ageInDays, String gender,
                   String healthStatus, Double averageWeight, LocalDate nextTransferDate) {
        this.id = id;
        this.houseId = houseId;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.arrivalDate = arrivalDate;
        this.ageInDays = ageInDays;
        this.gender = gender;
        this.healthStatus = healthStatus;
        this.averageWeight = averageWeight;
        this.nextTransferDate = nextTransferDate;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getHouseId() { return houseId; }
    public void setHouseId(Integer houseId) { this.houseId = houseId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }

    public Integer getAgeInDays() { return ageInDays; }
    public void setAgeInDays(Integer ageInDays) { this.ageInDays = ageInDays; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public Double getAverageWeight() { return averageWeight; }
    public void setAverageWeight(Double averageWeight) { this.averageWeight = averageWeight; }

    public LocalDate getNextTransferDate() { return nextTransferDate; }
    public void setNextTransferDate(LocalDate nextTransferDate) { this.nextTransferDate = nextTransferDate; }

    @Override
    public String toString() {
        return "Chicken{" +
                "id=" + id +
                ", houseId=" + houseId +
                ", batchNumber='" + batchNumber + '\'' +
                ", quantity=" + quantity +
                ", arrivalDate=" + arrivalDate +
                ", ageInDays=" + ageInDays +
                ", gender='" + gender + '\'' +
                ", healthStatus='" + healthStatus + '\'' +
                ", averageWeight=" + averageWeight +
                ", nextTransferDate=" + nextTransferDate +
                '}';
    }
}