package ma.farm.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a company entity in the Chicken Farm Management System.
 * Contains company details such as identification, business field, contact info, and creation date.
 */
public class Company {
	private int id;
	private String companyName;
	private int patent;
	private int fiscalId;
	private int declarationNumber;
	private String businessField;
	private String workAddress;
	private String bankRIB;
	private String phoneNumber;
	private LocalDateTime createdAt;

	/**
	 * Default constructor.
	 */
	public Company() {
	}

	/**
	 * Constructor without id (for creating new companies).
	 * @param companyName the name of the company
	 * @param patent the patent number
	 * @param fiscalId the fiscal ID
	 * @param declarationNumber the declaration number
	 * @param businessField the business field
	 * @param workAddress the work address
	 * @param bankRIB the bank RIB
	 * @param phoneNumber the phone number
	 */
	public Company(String companyName, int patent, int fiscalId, int declarationNumber, String businessField, String workAddress, String bankRIB, String phoneNumber) {
		this.companyName = companyName;
		this.patent = patent;
		this.fiscalId = fiscalId;
		this.declarationNumber = declarationNumber;
		this.businessField = businessField;
		this.workAddress = workAddress;
		this.bankRIB = bankRIB;
		this.phoneNumber = phoneNumber;
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * Full constructor with all fields.
	 * @param id the company ID
	 * @param companyName the name of the company
	 * @param patent the patent number
	 * @param fiscalId the fiscal ID
	 * @param declarationNumber the declaration number
	 * @param businessField the business field
	 * @param workAddress the work address
	 * @param bankRIB the bank RIB
	 * @param phoneNumber the phone number
	 * @param createdAt the creation date
	 */
	public Company(int id, String companyName, int patent, int fiscalId, int declarationNumber, String businessField, String workAddress, String bankRIB, String phoneNumber, LocalDateTime createdAt) {
		this.id = id;
		this.companyName = companyName;
		this.patent = patent;
		this.fiscalId = fiscalId;
		this.declarationNumber = declarationNumber;
		this.businessField = businessField;
		this.workAddress = workAddress;
		this.bankRIB = bankRIB;
		this.phoneNumber = phoneNumber;
		this.createdAt = createdAt;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getPatent() {
		return patent;
	}

	public void setPatent(int patent) {
		this.patent = patent;
	}

	public int getFiscalId() {
		return fiscalId;
	}

	public void setFiscalId(int fiscalId) {
		this.fiscalId = fiscalId;
	}

	public int getDeclarationNumber() {
		return declarationNumber;
	}

	public void setDeclarationNumber(int declarationNumber) {
		this.declarationNumber = declarationNumber;
	}

	public String getBusinessField() {
		return businessField;
	}

	public void setBusinessField(String businessField) {
		this.businessField = businessField;
	}

	public String getWorkAddress() {
		return workAddress;
	}

	public void setWorkAddress(String workAddress) {
		this.workAddress = workAddress;
	}

	public String getBankRIB() {
		return bankRIB;
	}

	public void setBankRIB(String bankRIB) {
		this.bankRIB = bankRIB;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Checks equality based on id and fiscalId.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Company company = (Company) o;
		return id == company.id && fiscalId == company.fiscalId;
	}

	/**
	 * Generates hash code using id and fiscalId.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, fiscalId);
	}

	/**
	 * Returns a string representation of the company (excluding sensitive data).
	 */
	@Override
	public String toString() {
		return "Company{" +
				"id=" + id +
				", companyName='" + companyName + '\'' +
				", patent=" + patent +
				", fiscalId=" + fiscalId +
				", declarationNumber=" + declarationNumber +
				", businessField='" + businessField + '\'' +
				", workAddress='" + workAddress + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", createdAt=" + createdAt +
				'}';
	}
}
