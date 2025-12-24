package ma.farm.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Personnel Model - Represents all farm personnel (Admin and Farm workers)
 *
 * This class encapsulates all information related to farm personnel including:
 * - Basic information (name, age, contact details)
 * - Department classification (administration or farm)
 * - Job title and positions (for admin staff)
 * - Employment details (hire date, salary, status)
 * - Supervisor relationship (for subordinates)
 *
 * Personnel Types:
 * ADMINISTRATION:
 * - Farm Owner / General Manager (singleton)
 * - Cashier (singleton)
 * - Admin Staff (1-4 people with positions: accounting, hr, legal, sales)
 *
 * FARM:
 * - Supervisors: Veterinary, Inventory, Farmhand (singletons)
 * - Subordinates: Work under their respective supervisors
 *
 * @author Chicken Farm Management System
 * @version 2.0
 */
public class Personnel {

    // ============================================================
    // FIELDS
    // ============================================================

    // Primary Key
    private int id;

    // Basic Information
    private String fullName;
    private int age;
    private String phone;
    private String email;

    // Job Information
    private int jobTitleId;              // Foreign key to jobTitles table
    private String jobTitle;             // Job title name (e.g., "farm_owner", "veterinary_supervisor")
    private String department;           // "administration" or "farm"

    // Admin Staff Positions (only for admin_staff job title)
    // Stored as comma-separated: "accounting,hr,legal,sales"
    private String positions;

    // Employment Details
    private LocalDate hireDate;
    private double salary;
    private boolean isActive;

    // Contact Information
    private String address;
    private String emergencyContact;

    // Supervisor Relationship (for subordinates)
    private Integer supervisorId;

    // Timestamps
    private String createdAt;
    private String updatedAt;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    /**
     * Default constructor
     * Creates an empty Personnel object
     */
    public Personnel() {
        this.isActive = true;
    }

    /**
     * Constructor for new personnel (minimal required fields)
     *
     * @param fullName the full name of the personnel
     * @param age the age of the personnel
     * @param phone the phone number
     * @param email the email address (must be unique)
     * @param jobTitle the job title code (e.g., "farm_owner", "veterinary_supervisor")
     * @param department "administration" or "farm"
     */
    public Personnel(String fullName, int age, String phone, String email,
                     String jobTitle, String department) {
        this.fullName = fullName;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.isActive = true;
    }

    /**
     * Full constructor for database retrieval
     *
     * @param id the database ID
     * @param fullName the full name
     * @param age the age
     * @param phone the phone number
     * @param email the email address
     * @param jobTitleId the job title foreign key
     * @param jobTitle the job title name
     * @param department the department
     * @param positions comma-separated positions (for admin_staff only)
     * @param hireDate the hire date
     * @param salary the monthly salary
     * @param isActive whether the employee is currently active
     * @param address the home address
     * @param emergencyContact the emergency contact information
     * @param supervisorId the supervisor's ID (null if no supervisor)
     * @param createdAt creation timestamp
     * @param updatedAt last update timestamp
     */
    public Personnel(int id, String fullName, int age, String phone, String email,
                     int jobTitleId, String jobTitle, String department, String positions,
                     LocalDate hireDate, double salary, boolean isActive,
                     String address, String emergencyContact, Integer supervisorId,
                     String createdAt, String updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.jobTitleId = jobTitleId;
        this.jobTitle = jobTitle;
        this.department = department;
        this.positions = positions;
        this.hireDate = hireDate;
        this.salary = salary;
        this.isActive = isActive;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.supervisorId = supervisorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getJobTitleId() {
        return jobTitleId;
    }

    public void setJobTitleId(int jobTitleId) {
        this.jobTitleId = jobTitleId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public Integer getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Integer supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Gets the PersonnelType enum for this personnel
     * @return the PersonnelType, or null if not found
     */
    public PersonnelType getPersonnelType() {
        return PersonnelType.fromCode(this.jobTitle);
    }

    /**
     * Gets the AdminPosition array for this personnel (admin_staff only)
     * @return array of AdminPosition, or empty array if not admin_staff
     */
    public AdminPosition[] getAdminPositions() {
        if (!"admin_staff".equals(this.jobTitle) || this.positions == null) {
            return new AdminPosition[0];
        }
        return AdminPosition.fromCommaSeparatedString(this.positions);
    }

    /**
     * Sets admin positions from an array
     * @param adminPositions array of AdminPosition enums
     */
    public void setAdminPositions(AdminPosition[] adminPositions) {
        this.positions = AdminPosition.toCommaSeparatedString(adminPositions);
    }

    /**
     * Checks if this personnel has a supervisor
     * @return true if supervisorId is not null
     */
    public boolean hasSupervisor() {
        return supervisorId != null;
    }

    /**
     * Checks if this personnel is in administration department
     * @return true if department is "administration"
     */
    public boolean isAdministration() {
        return "administration".equals(this.department);
    }

    /**
     * Checks if this personnel is in farm department
     * @return true if department is "farm"
     */
    public boolean isFarm() {
        return "farm".equals(this.department);
    }

    /**
     * Checks if this personnel is a supervisor (can have subordinates)
     * @return true for veterinary_supervisor, inventory_supervisor, farmhand_supervisor
     */
    public boolean isSupervisor() {
        return "veterinary_supervisor".equals(this.jobTitle) ||
               "inventory_supervisor".equals(this.jobTitle) ||
               "farmhand_supervisor".equals(this.jobTitle);
    }

    /**
     * Checks if this personnel is a subordinate (requires a supervisor)
     * @return true for veterinary_subordinate, inventory_subordinate, farmhand_subordinate
     */
    public boolean isSubordinate() {
        return "veterinary_subordinate".equals(this.jobTitle) ||
               "inventory_subordinate".equals(this.jobTitle) ||
               "farmhand_subordinate".equals(this.jobTitle);
    }

    /**
     * Checks if this personnel is the farm owner
     * @return true if job title is "farm_owner"
     */
    public boolean isFarmOwner() {
        return "farm_owner".equals(this.jobTitle);
    }

    /**
     * Checks if this personnel is the cashier
     * @return true if job title is "cashier"
     */
    public boolean isCashier() {
        return "cashier".equals(this.jobTitle);
    }

    /**
     * Checks if this personnel is admin staff
     * @return true if job title is "admin_staff"
     */
    public boolean isAdminStaff() {
        return "admin_staff".equals(this.jobTitle);
    }

    /**
     * Calculates years of service for this personnel
     * @return number of years employed, or 0 if hire date is not set
     */
    public int getYearsOfService() {
        if (hireDate == null) {
            return 0;
        }
        return Math.toIntExact(ChronoUnit.YEARS.between(hireDate, LocalDate.now()));
    }

    /**
     * Checks if this personnel has a specific admin position
     * @param position the AdminPosition to check
     * @return true if personnel has this position
     */
    public boolean hasPosition(AdminPosition position) {
        if (positions == null || position == null) {
            return false;
        }
        return positions.contains(position.getCode());
    }

    /**
     * Gets the count of positions this admin staff has
     * @return number of positions, or 0 if not admin_staff
     */
    public int getPositionCount() {
        if (positions == null || positions.trim().isEmpty()) {
            return 0;
        }
        return positions.split(",").length;
    }

    // ============================================================
    // EQUALS, HASHCODE, TOSTRING
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Personnel personnel = (Personnel) o;
        return id == personnel.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Personnel{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", department='" + department + '\'' +
                ", positions='" + positions + '\'' +
                ", hireDate=" + hireDate +
                ", salary=" + salary +
                ", isActive=" + isActive +
                ", supervisorId=" + supervisorId +
                '}';
    }
}
