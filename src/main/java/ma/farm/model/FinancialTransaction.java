package ma.farm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialTransaction {
    private int id;
    private LocalDate transactionDate;
    private String type; // Income, Expense
    private String category;
    private double amount;
    private String paymentMethod;
    private String description;
    private String relatedEntityType; // Supplier, Customer, Personnel, Other
    private int relatedEntityId;
    private String receiptImage; // Base64
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FinancialTransaction() {
    }

    // Simplified constructor for creating new transactions (without timestamps)
    public FinancialTransaction(int id, LocalDate transactionDate, String type, String category,
            double amount, String paymentMethod, String description,
            String relatedEntityType, int relatedEntityId, String receiptImage) {
        this(id, transactionDate, type, category, amount, paymentMethod, description, relatedEntityType,
                relatedEntityId, receiptImage, null, null);
    }

    public FinancialTransaction(int id, LocalDate transactionDate, String type, String category,
            double amount, String paymentMethod, String description,
            String relatedEntityType, int relatedEntityId, String receiptImage,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.transactionDate = transactionDate;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.receiptImage = receiptImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public int getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(int relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getReceiptImage() {
        return receiptImage;
    }

    public void setReceiptImage(String receiptImage) {
        this.receiptImage = receiptImage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
