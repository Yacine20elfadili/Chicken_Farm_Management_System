package ma.farm.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Personnel Model - Represents farm workers/employees
 * 
 * This class encapsulates all information related to farm personnel including:
 * - Basic information (name, age, contact details)
 * - Employment details (job title, shift, salary, hire date)
 * - Status management (active/inactive)
 * - Emergency contact information
 * 
 * The model supports two types of employees:
 * - Trackers: Supervisory positions
 * - Workers: Regular farm workers
 * 
 * Used in: Personnel management page
 * Associated DAO: PersonnelDAO
 * 
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class Personnel {

    // Fields
    private int id;                         // Database ID
    private String fullName;                // Worker full name
    private int age;                        // Worker age
    private String phone;                   // Contact phone number
    private String email;                   // Contact email (unique)
    private String jobTitle;                // Job title: "tracker" or "worker"
    private LocalDate hireDate;             // Date hired
    private double salary;                  // Monthly salary in MAD
    private String shift;                   // Work shift: "morning", "evening", "night"
    private boolean isActive;               // Employment status (true = currently employed)
    private String address;                 // Home address
    private String emergencyContact;        // Emergency contact name and phone

    /**
     * Default constructor
     * Creates an empty Personnel object
     */
    public Personnel() {
    }

    /**
     * Constructor for new personnel (MVP - minimal required fields)
     * 
     * This constructor is used when creating new personnel with only basic information.
     * Other fields should be set via setters.
     * 
     * @param fullName the full name of the personnel
     * @param age the age of the personnel
     * @param phone the phone number
     * @param email the email address (must be unique)
     * @param jobTitle the job title ("tracker" or "worker")
     */
    public Personnel(String fullName, int age, String phone, String email, String jobTitle) {
        this.fullName = fullName;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.jobTitle = jobTitle;
    }

    /**
     * Full constructor
     * 
     * Creates a complete Personnel object with all fields populated.
     * Used primarily by DAO when retrieving records from the database.
     * 
     * @param id the database ID
     * @param fullName the full name
     * @param age the age
     * @param phone the phone number
     * @param email the email address
     * @param jobTitle the job title
     * @param hireDate the hire date
     * @param salary the monthly salary
     * @param shift the work shift
     * @param isActive whether the employee is currently active
     * @param address the home address
     * @param emergencyContact the emergency contact information
     */
    public Personnel(int id, String fullName, int age, String phone, String email,
                     String jobTitle, LocalDate hireDate, double salary, String shift,
                     boolean isActive, String address, String emergencyContact) {
        this.id = id;
        this.fullName = fullName;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.jobTitle = jobTitle;
        this.hireDate = hireDate;
        this.salary = salary;
        this.shift = shift;
        this.isActive = isActive;
        this.address = address;
        this.emergencyContact = emergencyContact;
    }

    /**
     * Gets the database ID of this personnel
     * @return the personnel ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the database ID of this personnel
     * @param id the personnel ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the full name
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name
     * @param fullName the full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the age
     * @return the age in years
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the age
     * @param age the age in years
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Gets the phone number
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number
     * @param phone the phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the email address
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the job title
     * @return the job title ("tracker" or "worker")
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the job title
     * @param jobTitle the job title ("tracker" or "worker")
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * Gets the hire date
     * @return the hire date
     */
    public LocalDate getHireDate() {
        return hireDate;
    }

    /**
     * Sets the hire date
     * @param hireDate the hire date
     */
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    /**
     * Gets the monthly salary
     * @return the salary in MAD
     */
    public double getSalary() {
        return salary;
    }

    /**
     * Sets the monthly salary
     * @param salary the salary in MAD
     */
    public void setSalary(double salary) {
        this.salary = salary;
    }

    /**
     * Gets the work shift
     * @return the shift name ("morning", "evening", or "night")
     */
    public String getShift() {
        return shift;
    }

    /**
     * Sets the work shift
     * @param shift the shift name ("morning", "evening", or "night")
     */
    public void setShift(String shift) {
        this.shift = shift;
    }

    /**
     * Gets the employment status
     * @return true if the employee is currently active, false if inactive
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the employment status
     * @param active true if the employee is currently active, false if inactive
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Gets the home address
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the home address
     * @param address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the emergency contact information
     * @return the emergency contact (typically "Name PhoneNumber")
     */
    public String getEmergencyContact() {
        return emergencyContact;
    }

    /**
     * Sets the emergency contact information
     * @param emergencyContact the emergency contact (typically "Name PhoneNumber")
     */
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    /**
     * Calculates years of service for this personnel
     * 
     * Computes the number of complete years between hire date and today.
     * 
     * @return Number of years employed, or 0 if hire date is not set
     */
    public int getYearsOfService() {
        if (hireDate == null) {
            return 0;
        }
        return Math.toIntExact(java.time.temporal.ChronoUnit.YEARS.between(hireDate, LocalDate.now()));
    }

    /**
     * Checks if this worker is a tracker (supervisor)
     * 
     * @return true if job title is "tracker" (case-insensitive), false otherwise
     */
    public boolean isTracker() {
        return "Tracker".equalsIgnoreCase(this.jobTitle);
    }

    /**
     * Checks if this worker is a regular worker
     * 
     * @return true if job title is "worker" (case-insensitive), false otherwise
     */
    public boolean isWorker() {
        return "Worker".equalsIgnoreCase(this.jobTitle);
    }

    /**
     * Compares two Personnel objects for equality based on ID
     * 
     * @param o the object to compare
     * @return true if both objects have the same ID, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Personnel personnel = (Personnel) o;
        return id == personnel.id;
    }

    /**
     * Generates a hash code based on the ID
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the Personnel object
     * 
     * @return string representation containing all fields
     */
    @Override
    public String toString() {
        return "Personnel{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", hireDate=" + hireDate +
                ", salary=" + salary +
                ", shift='" + shift + '\'' +
                ", isActive=" + isActive +
                ", address='" + address + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                '}';
    }
}
