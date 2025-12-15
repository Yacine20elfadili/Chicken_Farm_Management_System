package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Personnel;

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
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(query)
        ) {
            stmt.setInt(1, jobTitleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                jobTitle = rs.getString("name");
                System.out.println("DEBUG: Found job title ID " + jobTitleId + " = " + jobTitle);
            } else {
                System.err.println("WARNING: No job title found for ID: " + jobTitleId);
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting job title ID " + jobTitleId + ": " + e.getMessage());
        }
        return jobTitle;
    }

    /**
     * Retrieves job title ID from its name
     *
     * @param jobTitle the name of the job title (e.g., "tracker", "worker")
     * @return the database ID of the job title, or -1 if not found
     */
    public int getJobTitleId(String jobTitle) {
        int jobTitleId = -1;
        String query = "SELECT id FROM jobTitles WHERE name = ?";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(query)
        ) {
            stmt.setString(1, jobTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                jobTitleId = rs.getInt(1);
                System.out.println("DEBUG: Job title '" + jobTitle + "' has ID: " + jobTitleId);
            } else {
                System.err.println("WARNING: No ID found for job title: " + jobTitle);
                // Try to insert it if it doesn't exist
                if (!insertJobTitle(jobTitle)) {
                    System.err.println("ERROR: Failed to insert job title: " + jobTitle);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting job title id for '" + jobTitle + "': " + e.getMessage());
            e.printStackTrace();
        }
        return jobTitleId;
    }

    /**
     * Insert a new job title if it doesn't exist
     */
    private boolean insertJobTitle(String jobTitle) {
        String sql = "INSERT OR IGNORE INTO jobTitles (name) VALUES (?)";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            stmt.setString(1, jobTitle);
            int rows = stmt.executeUpdate();
            System.out.println("DEBUG: Inserted job title '" + jobTitle + "', rows affected: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("ERROR inserting job title '" + jobTitle + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Improved shift lookup
     */
    private String getShiftName(int shiftId) {
        String shift = null;
        String query = "SELECT name FROM shifts WHERE id = ?";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(query)
        ) {
            stmt.setInt(1, shiftId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shift = rs.getString("name");
                System.out.println("DEBUG: Found shift ID " + shiftId + " = " + shift);
            } else {
                System.err.println("WARNING: No shift found for ID: " + shiftId);
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting shift ID " + shiftId + ": " + e.getMessage());
        }
        return shift;
    }

    /**
     * Improved shift ID lookup
     */
    private int getShiftId(String shift) {
        int shiftId = -1;
        String query = "SELECT id FROM shifts WHERE name = ?";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(query)
        ) {
            stmt.setString(1, shift);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shiftId = rs.getInt(1);
                System.out.println("DEBUG: Shift '" + shift + "' has ID: " + shiftId);
            } else {
                System.err.println("WARNING: No ID found for shift: " + shift);
                // Try to insert it if it doesn't exist
                if (!insertShift(shift)) {
                    System.err.println("ERROR: Failed to insert shift: " + shift);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting shift id for '" + shift + "': " + e.getMessage());
        }
        return shiftId;
    }

    /**
     * Insert a new shift if it doesn't exist
     */
    private boolean insertShift(String shift) {
        // Default times for new shifts
        String startTime = "06:00:00";
        String endTime = "15:00:00";

        if ("evening".equalsIgnoreCase(shift)) {
            startTime = "15:00:00";
            endTime = "00:00:00";
        }

        String sql = "INSERT OR IGNORE INTO shifts (name, startTime, endTime) VALUES (?, ?, ?)";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            stmt.setString(1, shift);
            stmt.setString(2, startTime);
            stmt.setString(3, endTime);
            int rows = stmt.executeUpdate();
            System.out.println("DEBUG: Inserted shift '" + shift + "', rows affected: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("ERROR inserting shift '" + shift + "': " + e.getMessage());
            return false;
        }
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
    /**
     * Create personnel with better error handling
     */
    public boolean createPersonnel(Personnel personnel) {
        System.out.println("DEBUG: Creating personnel: " + personnel.getFullName());

        // Check if email already exists
        if (emailExists(personnel.getEmail())) {
            System.err.println("ERROR: Email already exists: " + personnel.getEmail());
            return false;
        }

        String sql = "INSERT INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact, supervisorId) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            // Set basic fields
            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2, personnel.getAge());
            stmt.setString(3, personnel.getPhone());
            stmt.setString(4, personnel.getEmail());

            // Job Title
            int jobTitleId = getJobTitleId(personnel.getJobTitle());
            if (jobTitleId == -1) {
                System.err.println("ERROR: Invalid job title: " + personnel.getJobTitle());
                return false;
            }
            stmt.setInt(5, jobTitleId);
            System.out.println("DEBUG: Using jobTitle ID: " + jobTitleId + " for: " + personnel.getJobTitle());

            // Hire Date
            if (personnel.getHireDate() != null) {
                stmt.setDate(6, java.sql.Date.valueOf(personnel.getHireDate()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }

            // Salary
            stmt.setDouble(7, personnel.getSalary());

            // Shift
            int shiftId = getShiftId(personnel.getShift());
            if (shiftId == -1) {
                System.err.println("ERROR: Invalid shift: " + personnel.getShift());
                return false;
            }
            stmt.setInt(8, shiftId);
            System.out.println("DEBUG: Using shift ID: " + shiftId + " for: " + personnel.getShift());

            // Other fields
            stmt.setBoolean(9, personnel.isActive());
            stmt.setString(10, personnel.getAddress() != null ? personnel.getAddress() : "");
            stmt.setString(11, personnel.getEmergencyContact() != null ? personnel.getEmergencyContact() : "");

            // Supervisor
            if (personnel.getSupervisorId() != null) {
                stmt.setInt(12, personnel.getSupervisorId());
            } else {
                stmt.setNull(12, java.sql.Types.INTEGER);
            }

            // Execute insert
            int rows = stmt.executeUpdate();
            System.out.println("DEBUG: Insert executed, rows affected: " + rows);

            if (rows == 0) {
                System.err.println("ERROR: No rows inserted");
                return false;
            }

            // For SQLite, we need to get the last insert ID separately
            try (Statement idStmt = dbConnection.getConnection().createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    personnel.setId(generatedId);
                    System.out.println("DEBUG: Generated ID using last_insert_rowid(): " + generatedId);
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("SQL ERROR creating personnel: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
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
        System.out.println("DEBUG: Updating personnel ID: " + personnel.getId());

        String sql = "UPDATE personnel SET fullName = ?, age = ?, phone = ?, email = ?, jobTitle = ?, hireDate = ?, salary = ?, shift = ?, isActive = ?, address = ?, emergencyContact = ?, supervisorId = ? WHERE id = ?";

        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2, personnel.getAge());
            stmt.setString(3, personnel.getPhone());
            stmt.setString(4, personnel.getEmail());

            int jobTitleId = getJobTitleId(personnel.getJobTitle());
            if (jobTitleId == -1) {
                System.err.println(
                        "ERROR: Invalid job title: " + personnel.getJobTitle()
                );
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
                System.err.println("ERROR: Invalid shift: " + personnel.getShift());
                return false;
            }
            stmt.setInt(8, shiftId);

            stmt.setBoolean(9, personnel.isActive());
            stmt.setString(10, personnel.getAddress() != null ? personnel.getAddress() : "");
            stmt.setString(11, personnel.getEmergencyContact() != null ? personnel.getEmergencyContact() : "");
            stmt.setObject(12, personnel.getSupervisorId(), java.sql.Types.INTEGER);
            stmt.setInt(13, personnel.getId());

            int rowsUpdated = stmt.executeUpdate();
            System.out.println("DEBUG: Rows updated: " + rowsUpdated);
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("ERROR updating personnel: " + e.getMessage());
            e.printStackTrace();
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
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
        System.out.println("DEBUG: Getting personnel by ID: " + id);

        String sql = "SELECT * FROM personnel WHERE id = ?";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
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
                    System.err.println("WARNING: Error parsing hire date for ID " + id + ": " + e.getMessage());
                }

                // Get job title name
                int jobTitleId = rs.getInt("jobTitle");
                String jobTitleName = getJobTitleName(jobTitleId);
                if (jobTitleName == null) {
                    System.err.println("ERROR: Could not find job title for ID: " + jobTitleId);
                    return null;
                }

                // Get shift name
                int shiftId = rs.getInt("shift");
                String shiftName = getShiftName(shiftId);
                if (shiftName == null) {
                    System.err.println("ERROR: Could not find shift for ID: " + shiftId);
                    return null;
                }

                return new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        jobTitleName,
                        hireDate,
                        rs.getDouble("salary"),
                        shiftName,
                        rs.getBoolean("isActive"),
                        rs.getString("address"),
                        rs.getString("emergencyContact"),
                        rs.getObject("supervisorId", Integer.class)
                );
            } else {
                System.out.println("DEBUG: No personnel found with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting personnel: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all personnel records from the database
     *
     * @return a List of all Personnel objects, or an empty list if no records found
     */
    /**
     * Get all personnel with debug info
     */
    public List<Personnel> getAllPersonnel() {
        System.out.println("DEBUG: Getting all personnel");
        String sql = "SELECT * FROM personnel";
        List<Personnel> personnelList = new ArrayList<>();

        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;

            while (rs.next()) {
                count++;
                try {
                    // Debug output for each record
                    System.out.println("DEBUG: Processing personnel record #" + count);
                    System.out.println("  ID: " + rs.getInt("id"));
                    System.out.println("  Name: " + rs.getString("fullName"));
                    System.out.println("  JobTitle ID: " + rs.getInt("jobTitle"));
                    System.out.println("  Shift ID: " + rs.getInt("shift"));

                    // Parse hire date
                    LocalDate hireDate = null;
                    try {
                        String hireDateStr = rs.getString("hireDate");
                        if (hireDateStr != null && !hireDateStr.isEmpty()) {
                            hireDate = LocalDate.parse(hireDateStr);
                        }
                    } catch (Exception e) {
                        System.err.println("WARNING: Error parsing hire date for ID " + rs.getInt("id"));
                    }

                    // Get job title name
                    String jobTitleName = getJobTitleName(rs.getInt("jobTitle"));
                    if (jobTitleName == null) {
                        System.err.println("ERROR: Could not get job title for ID " + rs.getInt("jobTitle"));
                        continue; // Skip this record
                    }

                    // Get shift name
                    String shiftName = getShiftName(rs.getInt("shift"));
                    if (shiftName == null) {
                        System.err.println("ERROR: Could not get shift for ID " + rs.getInt("shift"));
                        continue; // Skip this record
                    }

                    Personnel personnel = new Personnel(
                            rs.getInt("id"),
                            rs.getString("fullName"),
                            rs.getInt("age"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            jobTitleName,
                            hireDate,
                            rs.getDouble("salary"),
                            shiftName,
                            rs.getBoolean("isActive"),
                            rs.getString("address"),
                            rs.getString("emergencyContact"),
                            rs.getObject("supervisorId", Integer.class)
                    );

                    personnelList.add(personnel);

                } catch (Exception e) {
                    System.err.println("ERROR processing personnel record #" + count + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("DEBUG: Successfully loaded " + personnelList.size() + " personnel records");

        } catch (SQLException e) {
            System.err.println("ERROR getting personnel list: " + e.getMessage());
            e.printStackTrace();
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
                    rs.getString("emergencyContact"),
                    rs.getObject("supervisorId", Integer.class)
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println(
                "Error getting active personnel: " + e.getMessage()
            );
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
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
                    rs.getString("emergencyContact"),
                    rs.getObject("supervisorId", Integer.class)
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println(
                "Error getting personnel by job title: " + e.getMessage()
            );
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
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
                    rs.getString("emergencyContact"),
                    rs.getObject("supervisorId", Integer.class)
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println(
                "Error getting personnel by shift: " + e.getMessage()
            );
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
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
                    rs.getString("emergencyContact"),
                    rs.getObject("supervisorId", Integer.class)
                );
            }
        } catch (SQLException e) {
            System.err.println(
                "Error getting personnel by email: " + e.getMessage()
            );
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
        String sql =
            "SELECT COUNT(*) AS count FROM personnel WHERE isActive = 1";
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println(
                "Error counting active personnel: " + e.getMessage()
            );
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
    /**
     * Check if email exists with debug
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM personnel WHERE email = ? LIMIT 1";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            System.out.println("DEBUG: Email '" + email + "' exists: " + exists);
            return exists;
        } catch (SQLException e) {
            System.err.println("ERROR checking email existence: " + e.getMessage());
            return false;
        }
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(
                "Error deactivating personnel: " + e.getMessage()
            );
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
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
        String sql =
            "SELECT * FROM personnel WHERE LOWER(fullName) LIKE LOWER(?)";
        List<Personnel> personnelList = new ArrayList<>();
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
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
                    rs.getString("emergencyContact"),
                    rs.getObject("supervisorId", Integer.class)
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println(
                "Error searching personnel by name: " + e.getMessage()
            );
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
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
                    rs.getString("emergencyContact"),
                    rs.getObject("supervisorId", Integer.class)
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println(
                "Error getting personnel by age range: " + e.getMessage()
            );
        }
        return personnelList;
    }

    /**
     * Retrieves all personnel who are supervisors
     *
     * @return a List of Personnel with supervisor job title
     */
    public List<Personnel> getAllSupervisors() {
        return getPersonnelByJobTitle("supervisor");
    }

    /**
     * Retrieves all farmhands (workers under supervisors)
     *
     * @return a List of Personnel with farmhand job title
     */
    public List<Personnel> getAllFarmhands() {
        return getPersonnelByJobTitle("farmhand");
    }

    /**
     * Retrieves all veterinary staff
     *
     * @return a List of Personnel with veterinary job title
     */
    public List<Personnel> getAllVeterinary() {
        return getPersonnelByJobTitle("veterinary");
    }

    /**
     * Retrieves all inventory trackers
     *
     * @return a List of Personnel with inventory_tracker job title
     */
    public List<Personnel> getAllInventoryTrackers() {
        return getPersonnelByJobTitle("inventory_tracker");
    }

    /**
     * Retrieves all personnel under a specific supervisor
     *
     * @param supervisorId the ID of the supervisor
     * @return a List of Personnel who report to this supervisor
     */
    public List<Personnel> getPersonnelBySupervisorId(int supervisorId) {
        String sql = "SELECT * FROM personnel WHERE supervisorId = ?";
        List<Personnel> personnelList = new ArrayList<>();
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            stmt.setInt(1, supervisorId);
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
                        rs.getString("emergencyContact"),
                        rs.getObject("supervisorId", Integer.class)
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println(
                    "Error getting personnel by supervisor: " + e.getMessage()
            );
        }
        return personnelList;
    }

    /**
     * Gets the count of personnel under a specific supervisor
     *
     * @param supervisorId the ID of the supervisor
     * @return the number of subordinates
     */
    public int getSubordinateCount(int supervisorId) {
        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE supervisorId = ?";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            stmt.setInt(1, supervisorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println(
                    "Error counting subordinates: " + e.getMessage()
            );
        }
        return 0;
    }

    /**
     * Checks if a supervisor has any subordinates
     *
     * @param supervisorId the ID of the supervisor
     * @return true if the supervisor has subordinates, false otherwise
     */
    public boolean hasSupervisedPersonnel(int supervisorId) {
        return getSubordinateCount(supervisorId) > 0;
    }

    /**
     * Gets all operations personnel (excludes admin and cashier)
     *
     * @return a List of Personnel excluding admin/cashier roles
     */
    /**
     * Get operations personnel with better error handling
     */
    public List<Personnel> getOperationsPersonnel() {
        System.out.println("DEBUG: Getting operations personnel");

        // First, ensure the job titles exist
        ensureJobTitlesExist();

        String sql = "SELECT p.* FROM personnel p " +
                "JOIN jobTitles j ON p.jobTitle = j.id " +
                "WHERE j.name IN ('veterinary', 'inventory_tracker', 'supervisor', 'farmhand')";

        List<Personnel> personnelList = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;

            while (rs.next()) {
                count++;
                try {
                    LocalDate hireDate = null;
                    try {
                        String hireDateStr = rs.getString("hireDate");
                        if (hireDateStr != null && !hireDateStr.isEmpty()) {
                            hireDate = LocalDate.parse(hireDateStr);
                        }
                    } catch (Exception e) {
                        // Date parsing error - skip
                    }

                    String jobTitleName = getJobTitleName(rs.getInt("jobTitle"));
                    String shiftName = getShiftName(rs.getInt("shift"));

                    if (jobTitleName == null || shiftName == null) {
                        System.err.println("WARNING: Skipping personnel record due to missing job title or shift");
                        continue;
                    }

                    Personnel personnel = new Personnel(
                            rs.getInt("id"),
                            rs.getString("fullName"),
                            rs.getInt("age"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            jobTitleName,
                            hireDate,
                            rs.getDouble("salary"),
                            shiftName,
                            rs.getBoolean("isActive"),
                            rs.getString("address"),
                            rs.getString("emergencyContact"),
                            rs.getObject("supervisorId", Integer.class)
                    );

                    personnelList.add(personnel);

                } catch (Exception e) {
                    System.err.println("ERROR processing operations personnel #" + count + ": " + e.getMessage());
                }
            }

            System.out.println("DEBUG: Loaded " + personnelList.size() + " operations personnel");

        } catch (SQLException e) {
            System.err.println("ERROR getting operations personnel: " + e.getMessage());
            e.printStackTrace();
        }
        return personnelList;
    }

    /**
     * Ensure required job titles exist in database
     */
    private void ensureJobTitlesExist() {
        System.out.println("DEBUG: Ensuring job titles exist");
        String[] requiredTitles = {
                "veterinary", "inventory_tracker", "supervisor", "farmhand",
                "administration", "cashier", "tracker", "worker"
        };

        for (String title : requiredTitles) {
            getJobTitleId(title); // This will create it if it doesn't exist
        }
    }

    /**
     * Count personnel by job title
     *
     * @param jobTitle the job title to count
     * @return the number of personnel with that job title
     */
    public int countByJobTitle(String jobTitle) {
        int jobTitleId = getJobTitleId(jobTitle);
        if (jobTitleId == -1) {
            return 0;
        }

        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE jobTitle = ?";
        try (
                PreparedStatement stmt = dbConnection
                        .getConnection()
                        .prepareStatement(sql)
        ) {
            stmt.setInt(1, jobTitleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println(
                    "Error counting personnel by job title: " + e.getMessage()
            );
        }
        return 0;
    }

}
