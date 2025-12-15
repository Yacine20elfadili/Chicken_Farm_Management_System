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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
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
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
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
        String sql =
                "INSERT INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact, supervisorId) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                    "Invalid job title: " + personnel.getJobTitle()
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
                System.err.println("Invalid shift: " + personnel.getShift());
                return false;
            }
            stmt.setInt(8, shiftId);

            stmt.setBoolean(9, personnel.isActive());
            stmt.setString(10, personnel.getAddress());
            stmt.setString(11, personnel.getEmergencyContact());
            stmt.setObject(12, personnel.getSupervisorId(), java.sql.Types.INTEGER);

            int rows = stmt.executeUpdate();
            if (rows == 0) return false;

            // SQLite: get inserted ID
            try (
                Statement idStmt = dbConnection
                    .getConnection()
                    .createStatement();
                ResultSet rs = idStmt.executeQuery(
                    "SELECT last_insert_rowid() AS id"
                )
            ) {
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
        String sql =
                "UPDATE personnel SET fullName = ?, age = ?, phone = ?, email = ?, jobTitle = ?, hireDate = ?, salary = ?, shift = ?, isActive = ?, address = ?, emergencyContact = ?, supervisorId = ? WHERE id = ?";

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
                    "Invalid job title: " + personnel.getJobTitle()
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
                System.err.println("Invalid shift: " + personnel.getShift());
                return false;
            }
            stmt.setInt(8, shiftId);

            stmt.setBoolean(9, personnel.isActive());
            stmt.setString(10, personnel.getAddress());
            stmt.setString(11, personnel.getEmergencyContact());
            stmt.setObject(12, personnel.getSupervisorId(), java.sql.Types.INTEGER);
            stmt.setInt(13, personnel.getId());

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
                    rs.getString("emergencyContact"),
                    rs.getObject("supervisorId", Integer.class)
                );
                personnelList.add(personnel);
            }
        } catch (SQLException e) {
            System.err.println(
                "Error getting personnel list: " + e.getMessage()
            );
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
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM personnel WHERE email = ? LIMIT 1";
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println(
                "Error checking email existence: " + e.getMessage()
            );
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
    public List<Personnel> getOperationsPersonnel() {
        String sql = "SELECT * FROM personnel WHERE jobTitle IN " +
                "(SELECT id FROM jobTitles WHERE name IN ('veterinary', 'inventory_tracker', 'supervisor', 'farmhand'))";
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
                    "Error getting operations personnel: " + e.getMessage()
            );
        }
        return personnelList;
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
