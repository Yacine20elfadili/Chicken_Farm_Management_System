package ma.farm.dao;

import ma.farm.model.Personnel;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PersonnelDAO - Data Access Object for Personnel management
 * 
 * Provides database operations for the Personnel model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Advanced queries (filtering by job title, shift, age range, etc.)
 * - Personnel statistics and counts
 * - Email validation
 * - Personnel activation/deactivation
 * 
 * All database operations use prepared statements to prevent SQL injection.
 * Foreign key lookups handle conversions between job title/shift names and their database IDs.
 * 
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class PersonnelDAO {
    private final DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public PersonnelDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Retrieves job title name from its database ID
     * 
     * @param jobTitleId the database ID of the job title
     * @return the name of the job title (e.g., "tracker", "worker"), or null if not found
     */
    private String getJobTitleName(int jobTitleId) {
        String jobTitle = null;
        String query = "SELECT name FROM jobTitles WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setInt(1, jobTitleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                jobTitle = rs.getString("name");
            }
        } catch (SQLException e) {
            System.err.println("Error getting job title: " + e.getMessage());
        }
        return jobTitle;
    }

    /**
     * Retrieves job title ID from its name
     * 
     * @param jobTitle the name of the job title (e.g., "tracker", "worker")
     * @return the database ID of the job title, or -1 if not found
     */
    private int getJobTitleId(String jobTitle) {
        int jobTitleId = -1;
        String query = "SELECT id FROM jobTitles WHERE name = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setString(1, jobTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                jobTitleId = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting job title id: " + e.getMessage());
        }
        return jobTitleId;
    }

    /**
     * Retrieves shift name from its database ID
     * 
     * @param shiftId the database ID of the shift
     * @return the name of the shift (e.g., "morning", "evening", "night"), or null if not found
     */
    private String getShiftName(int shiftId) {
        String shift = null;
        String query = "SELECT name FROM shifts WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setInt(1, shiftId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shift = rs.getString("name");
            }
        } catch (SQLException e) {
            System.err.println("Error getting shift: " + e.getMessage());
        }
        return shift;
    }

    /**
     * Retrieves shift ID from its name
     * 
     * @param shift the name of the shift (e.g., "morning", "evening", "night")
     * @return the database ID of the shift, or -1 if not found
     */
    private int getShiftId(String shift) {
        int shiftId = -1;
        String query = "SELECT id FROM shifts WHERE name = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setString(1, shift);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shiftId = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting shift id: " + e.getMessage());
        }
        return shiftId;
    }

    /**
     * Creates a new personnel record in the database
     * 
     * Validates that the job title and shift are valid before insertion.
     * Automatically retrieves and sets the generated ID on the personnel object.
     * 
     * @param personnel the Personnel object to create (must have fullName, age, phone, email, jobTitle, shift)
     * @return true if the personnel was created successfully, false otherwise
     * @throws RuntimeException if the job title or shift is invalid
     */
    // Create personnel
    public boolean createPersonnel(Personnel personnel) {
        String sql = "INSERT INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2, personnel.getAge());
            stmt.setString(3, personnel.getPhone());
            stmt.setString(4, personnel.getEmail());
            
            int jobTitleId = getJobTitleId(personnel.getJobTitle());
            if (jobTitleId == -1) {
                System.err.println("Invalid job title: " + personnel.getJobTitle());
                return false;
            }
            stmt.setInt(5, jobTitleId);
            
            if (personnel.getHireDate() != null) {
                stmt.setDate(6, java.sql.Date.valueOf(personnel.getHireDate()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            
            stmt.setDouble(7, personnel.getSalary());
            
            int shiftId = getShiftId(personnel.getShift());
            if (shiftId == -1) {
                System.err.println("Invalid shift: " + personnel.getShift());
                return false;
            }
            stmt.setInt(8, shiftId);
            
            stmt.setBoolean(9, personnel.isActive());
            stmt.setString(10, personnel.getAddress());
            stmt.setString(11, personnel.getEmergencyContact());

            int rows = stmt.executeUpdate();
            if (rows == 0) return false;

            // SQLite: get inserted ID
            try (Statement idStmt = dbConnection.getConnection().createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() AS id")) {
                if (rs.next()) {
                    personnel.setId(rs.getInt("id"));
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error creating personnel: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing personnel record in the database
     * 
     * All personnel fields are updated including job title and shift lookups.
     * 
     * @param personnel the Personnel object with updated values (must have id set)
     * @return true if the personnel was updated successfully, false otherwise
     */
    // Update personnel
    public boolean updatePersonnel(Personnel personnel) {
        String sql = "UPDATE personnel SET fullName = ?, age = ?, phone = ?, email = ?, jobTitle = ?, hireDate = ?, salary = ?, shift = ?, isActive = ?, address = ?, emergencyContact = ? WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2, personnel.getAge());
            stmt.setString(3, personnel.getPhone());
            stmt.setString(4, personnel.getEmail());
            
            int jobTitleId = getJobTitleId(personnel.getJobTitle());
            if (jobTitleId == -1) {
                System.err.println("Invalid job title: " + personnel.getJobTitle());
                return false;
            }
            stmt.setInt(5, jobTitleId);
            
            if (personnel.getHireDate() != null) {
                stmt.setDate(6, java.sql.Date.valueOf(personnel.getHireDate()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            
            stmt.setDouble(7, personnel.getSalary());
            
            int shiftId = getShiftId(personnel.getShift());
            if (shiftId == -1) {
                System.err.println("Invalid shift: " + personnel.getShift());
                return false;
            }
            stmt.setInt(8, shiftId);
            
            stmt.setBoolean(9, personnel.isActive());
            stmt.setString(10, personnel.getAddress());
            stmt.setString(11, personnel.getEmergencyContact());
            stmt.setInt(12, personnel.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating personnel: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a personnel record from the database
     * 
     * @param id the ID of the personnel to delete
     * @return true if the personnel was deleted successfully, false otherwise
     */
    // Delete
    public boolean deletePersonnel(int id) {
        String sql = "DELETE FROM personnel WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a personnel record by ID
     * 
     * @param id the ID of the personnel to retrieve
     * @return a Personnel object with all fields populated, or null if not found
     */
    // Get personnel by id
    public Personnel getPersonnelById(int id) {
        String sql = "SELECT * FROM personnel WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                return new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting personnel: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all personnel records from the database
     * 
     * @return a List of all Personnel objects, or an empty list if no records found
     */
    // Get all personnel
    public List<Personnel> getAllPersonnel() {
        String sql = "SELECT * FROM personnel";
        List<Personnel> personnelList = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                
                Personnel personnel = new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println("Error getting personnel list: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Retrieves only active personnel records
     * 
     * @return a List of active Personnel objects (isActive = true)
     */
    // Get active personnel only
    public List<Personnel> getActivePersonnel() {
        String sql = "SELECT * FROM personnel WHERE isActive = 1";
        List<Personnel> personnelList = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                Personnel personnel = new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active personnel: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Retrieves personnel filtered by job title
     * 
     * @param jobTitle the job title to filter by (e.g., "tracker", "worker")
     * @return a List of Personnel with the specified job title
     */
    // Get personnel by job title
    public List<Personnel> getPersonnelByJobTitle(String jobTitle) {
        int jobTitleId = getJobTitleId(jobTitle);
        if (jobTitleId == -1) {
            System.err.println("Invalid job title: " + jobTitle);
            return new ArrayList<>();
        }

        String sql = "SELECT * FROM personnel WHERE jobTitle = ?";
        List<Personnel> personnelList = new ArrayList<>();
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, jobTitleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                Personnel personnel = new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println("Error getting personnel by job title: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Retrieves personnel assigned to a specific shift
     * 
     * @param shift the shift name (e.g., "morning", "evening", "night")
     * @return a List of Personnel assigned to the specified shift
     */
    // Get personnel by shift
    public List<Personnel> getPersonnelByShift(String shift) {
        int shiftId = getShiftId(shift);
        if (shiftId == -1) {
            System.err.println("Invalid shift: " + shift);
            return new ArrayList<>();
        }

        String sql = "SELECT * FROM personnel WHERE shift = ?";
        List<Personnel> personnelList = new ArrayList<>();
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, shiftId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                Personnel personnel = new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println("Error getting personnel by shift: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Retrieves personnel by email address
     * 
     * Email addresses are unique in the system.
     * 
     * @param email the email address to search for
     * @return the Personnel object if found, null otherwise
     */
    // Get personnel by email
    public Personnel getPersonnelByEmail(String email) {
        String sql = "SELECT * FROM personnel WHERE email = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                return new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting personnel by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all personnel with "tracker" job title (supervisors)
     * 
     * @return a List of Personnel with tracker job title
     */
    // Get all trackers
    public List<Personnel> getTrackers() {
        return getPersonnelByJobTitle("tracker");
    }

    /**
     * Retrieves all personnel with "worker" job title
     * 
     * @return a List of Personnel with worker job title
     */
    // Get all workers
    public List<Personnel> getWorkers() {
        return getPersonnelByJobTitle("worker");
    }

    /**
     * Returns the total count of all personnel in the system
     * 
     * @return the total number of personnel records
     */
    // Get total count of personnel
    public int getTotalPersonnelCount() {
        String sql = "SELECT COUNT(*) AS count FROM personnel";
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting personnel: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns the count of active personnel
     * 
     * @return the number of personnel with isActive = true
     */
    // Get active personnel count
    public int getActivePersonnelCount() {
        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE isActive = 1";
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting active personnel: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if an email address is already in use
     * 
     * Useful for validation before creating new personnel.
     * 
     * @param email the email address to check
     * @return true if the email exists in the database, false otherwise
     */
    // Check if email exists
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM personnel WHERE email = ? LIMIT 1";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deactivates a personnel record (soft delete)
     * 
     * Sets isActive to false but keeps the record in the database.
     * 
     * @param id the ID of the personnel to deactivate
     * @return true if the personnel was deactivated successfully, false otherwise
     */
    // Deactivate personnel
    public boolean deactivatePersonnel(int id) {
        String sql = "UPDATE personnel SET isActive = 0 WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating personnel: " + e.getMessage());
        }
        return false;
    }

    /**
     * Activates a personnel record
     * 
     * Sets isActive to true.
     * 
     * @param id the ID of the personnel to activate
     * @return true if the personnel was activated successfully, false otherwise
     */
    // Activate personnel
    public boolean activatePersonnel(int id) {
        String sql = "UPDATE personnel SET isActive = 1 WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error activating personnel: " + e.getMessage());
        }
        return false;
    }

    /**
     * Searches for personnel by full name (case-insensitive, partial match)
     * 
     * Uses SQL LIKE operator for flexible searching.
     * 
     * @param fullName the name or partial name to search for
     * @return a List of Personnel matching the search criteria
     */
    // Search personnel by name
    public List<Personnel> searchByName(String fullName) {
        String sql = "SELECT * FROM personnel WHERE LOWER(fullName) LIKE LOWER(?)";
        List<Personnel> personnelList = new ArrayList<>();
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + fullName + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                Personnel personnel = new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println("Error searching personnel by name: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Retrieves personnel within a specified age range
     * 
     * @param minAge the minimum age (inclusive)
     * @param maxAge the maximum age (inclusive)
     * @return a List of Personnel within the age range
     */
    // Get personnel by age range
    public List<Personnel> getPersonnelByAgeRange(int minAge, int maxAge) {
        String sql = "SELECT * FROM personnel WHERE age BETWEEN ? AND ?";
        List<Personnel> personnelList = new ArrayList<>();
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, minAge);
            stmt.setInt(2, maxAge);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate hireDate = null;
                try {
                    String hireDateStr = rs.getString("hireDate");
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        hireDate = LocalDate.parse(hireDateStr);
                    }
                } catch (Exception e) {
                    // Date parsing error - skip
                }
                Personnel personnel = new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        getJobTitleName(rs.getInt("jobTitle")),
                        hireDate,
                        rs.getDouble("salary"),
                        getShiftName(rs.getInt("shift")),
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact")
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println("Error getting personnel by age range: " + e.getMessage());
        }
        return personnelList;
    }

    // Main method for testing
    public static void main(String[] args) {
        PersonnelDAO dao = new PersonnelDAO();

        System.out.println("===== PERSONNEL DAO TEST =====\n");

        // Test 1: Get all personnel
        System.out.println("TEST 1: Get all personnel");
        List<Personnel> allPersonnel = dao.getAllPersonnel();
        System.out.println("Total personnel: " + allPersonnel.size());
        for (Personnel p : allPersonnel) {
            System.out.println("  - " + p.getFullName() + " (" + p.getJobTitle() + ")");
        }
        System.out.println();

        // Test 2: Get personnel by ID
        System.out.println("TEST 2: Get personnel by ID (id=1)");
        Personnel personnel = dao.getPersonnelById(1);
        if (personnel != null) {
            System.out.println("  Name: " + personnel.getFullName());
            System.out.println("  Email: " + personnel.getEmail());
            System.out.println("  Job Title: " + personnel.getJobTitle());
            System.out.println("  Shift: " + personnel.getShift());
            System.out.println("  Salary: " + personnel.getSalary());
            System.out.println("  Active: " + personnel.isActive());
        } else {
            System.out.println("  Personnel not found");
        }
        System.out.println();

        // Test 3: Get active personnel
        System.out.println("TEST 3: Get active personnel");
        List<Personnel> activePersonnel = dao.getActivePersonnel();
        System.out.println("Active personnel count: " + activePersonnel.size());
        for (Personnel p : activePersonnel) {
            System.out.println("  - " + p.getFullName() + " (Active: " + p.isActive() + ")");
        }
        System.out.println();

        // Test 4: Get all trackers
        System.out.println("TEST 4: Get all trackers");
        List<Personnel> trackers = dao.getTrackers();
        System.out.println("Trackers count: " + trackers.size());
        for (Personnel p : trackers) {
            System.out.println("  - " + p.getFullName());
        }
        System.out.println();

        // Test 5: Get all workers
        System.out.println("TEST 5: Get all workers");
        List<Personnel> workers = dao.getWorkers();
        System.out.println("Workers count: " + workers.size());
        for (Personnel p : workers) {
            System.out.println("  - " + p.getFullName());
        }
        System.out.println();

        // Test 6: Get personnel by shift
        System.out.println("TEST 6: Get personnel by shift (morning)");
        List<Personnel> morningShift = dao.getPersonnelByShift("morning");
        System.out.println("Morning shift personnel: " + morningShift.size());
        for (Personnel p : morningShift) {
            System.out.println("  - " + p.getFullName() + " (" + p.getShift() + ")");
        }
        System.out.println();

        // Test 7: Search by name
        System.out.println("TEST 7: Search personnel by name (john)");
        List<Personnel> searchResults = dao.searchByName("john");
        System.out.println("Search results: " + searchResults.size());
        for (Personnel p : searchResults) {
            System.out.println("  - " + p.getFullName());
        }
        System.out.println();

        // Test 8: Get personnel by email
        System.out.println("TEST 8: Get personnel by email");
        Personnel byEmail = dao.getPersonnelByEmail("john.doe@farm.ma");
        if (byEmail != null) {
            System.out.println("  Found: " + byEmail.getFullName());
        } else {
            System.out.println("  Not found");
        }
        System.out.println();

        // Test 9: Email exists check
        System.out.println("TEST 9: Check if email exists");
        boolean exists = dao.emailExists("john.doe@farm.ma");
        System.out.println("  john.doe@farm.ma exists: " + exists);
        exists = dao.emailExists("nonexistent@farm.ma");
        System.out.println("  nonexistent@farm.ma exists: " + exists);
        System.out.println();

        // Test 10: Get personnel by age range
        System.out.println("TEST 10: Get personnel by age range (25-35)");
        List<Personnel> ageRange = dao.getPersonnelByAgeRange(25, 35);
        System.out.println("Personnel in age range: " + ageRange.size());
        for (Personnel p : ageRange) {
            System.out.println("  - " + p.getFullName() + " (Age: " + p.getAge() + ")");
        }
        System.out.println();

        // Test 11: Count statistics
        System.out.println("TEST 11: Personnel statistics");
        System.out.println("  Total personnel: " + dao.getTotalPersonnelCount());
        System.out.println("  Active personnel: " + dao.getActivePersonnelCount());
        System.out.println();

        // Test 12: Create new personnel (commented out to preserve data)
        System.out.println("TEST 12: Create new personnel (COMMENTED - uncomment to test)");
        /*
        Personnel newPersonnel = new Personnel(
            "Test Worker",
            30,
            "0612345678",
            "test@farm.ma",
            "worker"
        );
        newPersonnel.setHireDate(LocalDate.now());
        newPersonnel.setSalary(3500.00);
        newPersonnel.setShift("evening");
        newPersonnel.setActive(true);
        newPersonnel.setAddress("Test Address");
        newPersonnel.setEmergencyContact("Test Contact 0600000000");

        if (dao.createPersonnel(newPersonnel)) {
            System.out.println("  Personnel created successfully with ID: " + newPersonnel.getId());
        } else {
            System.out.println("  Failed to create personnel");
        }
        */
        System.out.println("  (Skipped to preserve existing data)\n");

        System.out.println("===== TEST COMPLETED =====");
    }

}
