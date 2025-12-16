package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Personnel;
import ma.farm.model.PersonnelType;
import ma.farm.model.AdminPosition;

/**
 * PersonnelDAO - Data Access Object for Personnel management
 *
 * Provides database operations for the new Personnel structure:
 * - ADMINISTRATION: Farm Owner, Cashier, Admin Staff (with positions)
 * - FARM: Supervisors (Veterinary, Inventory, Farmhand) and their Subordinates
 *
 * Key Features:
 * - CRUD operations for all personnel types
 * - Department-based queries (administration vs farm)
 * - Supervisor/subordinate relationship management
 * - Position management for admin staff
 * - Singleton enforcement (only one owner, cashier, or supervisor per type)
 *
 * @author Chicken Farm Management System
 * @version 2.0
 */
public class PersonnelDAO {

    private final DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public PersonnelDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // ============================================================
    // JOB TITLE HELPER METHODS
    // ============================================================

    /**
     * Retrieves job title ID from its name
     *
     * @param jobTitle the name of the job title (e.g., "farm_owner", "veterinary_supervisor")
     * @return the database ID of the job title, or -1 if not found
     */
    public int getJobTitleId(String jobTitle) {
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            return -1;
        }

        String query = "SELECT id FROM jobTitles WHERE name = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setString(1, jobTitle.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting job title ID for '" + jobTitle + "': " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves job title name from its database ID
     *
     * @param jobTitleId the database ID of the job title
     * @return the name of the job title, or null if not found
     */
    public String getJobTitleName(int jobTitleId) {
        String query = "SELECT name FROM jobTitles WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setInt(1, jobTitleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting job title name for ID " + jobTitleId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets the department for a job title
     *
     * @param jobTitle the job title name
     * @return "administration" or "farm", or null if not found
     */
    public String getDepartmentForJobTitle(String jobTitle) {
        String query = "SELECT department FROM jobTitles WHERE name = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setString(1, jobTitle.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("department");
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting department for job title '" + jobTitle + "': " + e.getMessage());
        }
        return null;
    }

    // ============================================================
    // RESULT SET TO PERSONNEL MAPPING
    // ============================================================

    /**
     * Maps a ResultSet row to a Personnel object
     *
     * @param rs the ResultSet positioned at the current row
     * @return Personnel object populated with data from the row
     */
    private Personnel mapResultSetToPersonnel(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String fullName = rs.getString("fullName");
        int age = rs.getInt("age");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        int jobTitleId = rs.getInt("jobTitle");
        String jobTitle = getJobTitleName(jobTitleId);
        String department = rs.getString("department");
        String positions = rs.getString("positions");

        LocalDate hireDate = null;
        String hireDateStr = rs.getString("hireDate");
        if (hireDateStr != null && !hireDateStr.isEmpty()) {
            try {
                hireDate = LocalDate.parse(hireDateStr);
            } catch (Exception e) {
                System.err.println("WARNING: Could not parse hire date: " + hireDateStr);
            }
        }

        double salary = rs.getDouble("salary");
        boolean isActive = rs.getBoolean("isActive");
        String address = rs.getString("address");
        String emergencyContact = rs.getString("emergencyContact");

        Integer supervisorId = null;
        Object supervisorIdObj = rs.getObject("supervisorId");
        if (supervisorIdObj != null) {
            String strValue = supervisorIdObj.toString().trim();
            if (!strValue.isEmpty()) {
                try {
                    supervisorId = Integer.parseInt(strValue);
                } catch (NumberFormatException e) {
                    System.err.println("WARNING: Invalid supervisorId format: " + strValue);
                }
            }
        }

        String createdAt = rs.getString("created_at");
        String updatedAt = rs.getString("updated_at");

        return new Personnel(id, fullName, age, phone, email, jobTitleId, jobTitle,
                department, positions, hireDate, salary, isActive, address,
                emergencyContact, supervisorId, createdAt, updatedAt);
    }

    // ============================================================
    // CREATE OPERATIONS
    // ============================================================

    /**
     * Creates a new personnel record in the database
     *
     * @param personnel the Personnel object to create
     * @return true if creation was successful, false otherwise
     */
    public boolean createPersonnel(Personnel personnel) {
        System.out.println("DEBUG: Creating personnel: " + personnel.getFullName() +
                           " [" + personnel.getJobTitle() + "]");

        // Get job title ID
        int jobTitleId = getJobTitleId(personnel.getJobTitle());
        if (jobTitleId == -1) {
            System.err.println("ERROR: Invalid job title: " + personnel.getJobTitle());
            return false;
        }

        // Check singleton constraints
        PersonnelType personnelType = PersonnelType.fromCode(personnel.getJobTitle());
        if (personnelType != null && personnelType.isSingleton()) {
            if (existsByJobTitle(personnel.getJobTitle())) {
                System.err.println("ERROR: A " + personnel.getJobTitle() + " already exists (singleton constraint)");
                return false;
            }
        }

        String sql = "INSERT INTO personnel (fullName, age, phone, email, jobTitle, department, " +
                     "positions, hireDate, salary, isActive, address, emergencyContact, supervisorId) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2, personnel.getAge());
            stmt.setString(3, personnel.getPhone());
            stmt.setString(4, personnel.getEmail());
            stmt.setInt(5, jobTitleId);
            stmt.setString(6, personnel.getDepartment());
            stmt.setString(7, personnel.getPositions());

            if (personnel.getHireDate() != null) {
                stmt.setString(8, personnel.getHireDate().toString());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            stmt.setDouble(9, personnel.getSalary());
            stmt.setBoolean(10, personnel.isActive());
            stmt.setString(11, personnel.getAddress());
            stmt.setString(12, personnel.getEmergencyContact());

            if (personnel.getSupervisorId() != null) {
                stmt.setInt(13, personnel.getSupervisorId());
            } else {
                stmt.setNull(13, Types.INTEGER);
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // SQLite: Use last_insert_rowid() to get the generated ID
                try (Statement idStmt = dbConnection.getConnection().createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        personnel.setId(rs.getInt(1));
                        System.out.println("SUCCESS: Created personnel with ID: " + personnel.getId());
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR creating personnel: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ============================================================
    // READ OPERATIONS
    // ============================================================

    /**
     * Gets a personnel by their ID
     *
     * @param id the personnel ID
     * @return Personnel object, or null if not found
     */
    public Personnel getPersonnelById(int id) {
        String sql = "SELECT * FROM personnel WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPersonnel(rs);
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting personnel by ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets a personnel by their email
     *
     * @param email the email address
     * @return Personnel object, or null if not found
     */
    public Personnel getPersonnelByEmail(String email) {
        String sql = "SELECT * FROM personnel WHERE email = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPersonnel(rs);
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting personnel by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets all personnel
     *
     * @return List of all personnel
     */
    public List<Personnel> getAllPersonnel() {
        List<Personnel> personnelList = new ArrayList<>();
        String sql = "SELECT * FROM personnel ORDER BY department, fullName";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                personnelList.add(mapResultSetToPersonnel(rs));
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting all personnel: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Gets all administration personnel (Farm Owner, Cashier, Admin Staff)
     *
     * @return List of administration personnel
     */
    public List<Personnel> getAdministrationPersonnel() {
        List<Personnel> personnelList = new ArrayList<>();
        String sql = "SELECT * FROM personnel WHERE department = 'administration' ORDER BY " +
                     "CASE jobTitle " +
                     "WHEN (SELECT id FROM jobTitles WHERE name = 'farm_owner') THEN 1 " +
                     "WHEN (SELECT id FROM jobTitles WHERE name = 'cashier') THEN 2 " +
                     "ELSE 3 END, fullName";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                personnelList.add(mapResultSetToPersonnel(rs));
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting administration personnel: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Gets all farm personnel (Supervisors and Subordinates)
     *
     * @return List of farm personnel
     */
    public List<Personnel> getFarmPersonnel() {
        List<Personnel> personnelList = new ArrayList<>();
        String sql = "SELECT * FROM personnel WHERE department = 'farm' ORDER BY jobTitle, fullName";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                personnelList.add(mapResultSetToPersonnel(rs));
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting farm personnel: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Gets personnel by job title
     *
     * @param jobTitle the job title code (e.g., "farm_owner", "veterinary_supervisor")
     * @return List of personnel with that job title
     */
    public List<Personnel> getPersonnelByJobTitle(String jobTitle) {
        List<Personnel> personnelList = new ArrayList<>();
        int jobTitleId = getJobTitleId(jobTitle);
        if (jobTitleId == -1) {
            return personnelList;
        }

        String sql = "SELECT * FROM personnel WHERE jobTitle = ? ORDER BY fullName";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, jobTitleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                personnelList.add(mapResultSetToPersonnel(rs));
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting personnel by job title: " + e.getMessage());
        }
        return personnelList;
    }

    /**
     * Gets the Farm Owner (singleton)
     *
     * @return Farm Owner personnel, or null if not set
     */
    public Personnel getFarmOwner() {
        List<Personnel> owners = getPersonnelByJobTitle("farm_owner");
        return owners.isEmpty() ? null : owners.get(0);
    }

    /**
     * Gets the Cashier (singleton)
     *
     * @return Cashier personnel, or null if not set
     */
    public Personnel getCashier() {
        List<Personnel> cashiers = getPersonnelByJobTitle("cashier");
        return cashiers.isEmpty() ? null : cashiers.get(0);
    }

    /**
     * Gets all Admin Staff
     *
     * @return List of admin staff personnel
     */
    public List<Personnel> getAdminStaff() {
        return getPersonnelByJobTitle("admin_staff");
    }

    /**
     * Gets the FIRST admin staff (the one who holds all positions by default)
     * The first admin staff is the one with the lowest ID (earliest created)
     *
     * @return First admin staff, or null if none exists
     */
    public Personnel getFirstAdminStaff() {
        String sql = "SELECT * FROM personnel p JOIN jobTitles j ON p.jobTitle = j.id " +
                     "WHERE j.name = 'admin_staff' ORDER BY p.id ASC LIMIT 1";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPersonnel(rs);
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting first admin staff: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if this admin staff is the first one (cannot be deleted if others exist)
     *
     * @param personnelId the ID to check
     * @return true if this is the first admin staff
     */
    public boolean isFirstAdminStaff(int personnelId) {
        Personnel first = getFirstAdminStaff();
        return first != null && first.getId() == personnelId;
    }

    /**
     * Gets the positions still held by the first admin staff (available for delegation)
     * These are the positions that new admin staff can choose from
     *
     * @return array of AdminPosition that the first admin staff still holds
     */
    public AdminPosition[] getPositionsAvailableForDelegation() {
        Personnel firstStaff = getFirstAdminStaff();
        if (firstStaff == null) {
            // No first admin staff yet - all positions available for the first one
            return AdminPosition.values();
        }

        // Return positions that the first admin staff still holds
        String positions = firstStaff.getPositions();
        if (positions == null || positions.isEmpty()) {
            return new AdminPosition[0];
        }

        List<AdminPosition> available = new ArrayList<>();
        for (AdminPosition pos : AdminPosition.values()) {
            if (positions.contains(pos.getCode())) {
                available.add(pos);
            }
        }
        return available.toArray(new AdminPosition[0]);
    }

    /**
     * Removes a position from the first admin staff (called when delegating to new staff)
     *
     * @param position the position to remove
     * @return true if successful
     */
    public boolean removePositionFromFirstAdminStaff(AdminPosition position) {
        Personnel firstStaff = getFirstAdminStaff();
        if (firstStaff == null) {
            return false;
        }

        String currentPositions = firstStaff.getPositions();
        if (currentPositions == null || !currentPositions.contains(position.getCode())) {
            return false;
        }

        // Remove the position from the comma-separated list
        List<String> positionList = new ArrayList<>();
        for (String pos : currentPositions.split(",")) {
            if (!pos.trim().equals(position.getCode())) {
                positionList.add(pos.trim());
            }
        }
        String newPositions = String.join(",", positionList);

        String sql = "UPDATE personnel SET positions = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newPositions.isEmpty() ? null : newPositions);
            stmt.setInt(2, firstStaff.getId());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("DEBUG: Removed position '" + position.getCode() + "' from first admin staff. Remaining: " + newPositions);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR removing position from first admin staff: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns positions to the first admin staff (called when deleting other admin staff)
     *
     * @param positions comma-separated positions to return
     * @return true if successful
     */
    public boolean returnPositionsToFirstAdminStaff(String positions) {
        if (positions == null || positions.isEmpty()) {
            return true;
        }

        Personnel firstStaff = getFirstAdminStaff();
        if (firstStaff == null) {
            return false;
        }

        String currentPositions = firstStaff.getPositions();
        List<String> positionList = new ArrayList<>();

        // Add existing positions
        if (currentPositions != null && !currentPositions.isEmpty()) {
            for (String pos : currentPositions.split(",")) {
                if (!pos.trim().isEmpty()) {
                    positionList.add(pos.trim());
                }
            }
        }

        // Add returned positions (avoid duplicates)
        for (String pos : positions.split(",")) {
            if (!pos.trim().isEmpty() && !positionList.contains(pos.trim())) {
                positionList.add(pos.trim());
            }
        }

        String newPositions = String.join(",", positionList);

        String sql = "UPDATE personnel SET positions = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newPositions);
            stmt.setInt(2, firstStaff.getId());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("DEBUG: Returned positions '" + positions + "' to first admin staff. New positions: " + newPositions);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR returning positions to first admin staff: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes an admin staff and returns their positions to the first admin staff
     * The first admin staff cannot be deleted if other admin staff exist
     *
     * @param personnelId the ID of the admin staff to delete
     * @return true if successful, false if deletion is blocked or fails
     */
    public boolean deleteAdminStaff(int personnelId) {
        // Check if this is the first admin staff
        if (isFirstAdminStaff(personnelId)) {
            int count = getAdminStaffCount();
            if (count > 1) {
                System.err.println("ERROR: Cannot delete first admin staff while other admin staff exist (" + (count - 1) + " others)");
                return false;
            }
        }

        // Get the personnel to retrieve their positions before deletion
        Personnel personnel = getPersonnelById(personnelId);
        if (personnel == null) {
            return false;
        }

        String positionsToReturn = personnel.getPositions();

        // Delete the personnel
        String sql = "DELETE FROM personnel WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, personnelId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // If not the first admin staff, return positions to first
                if (!isFirstAdminStaff(personnelId) && positionsToReturn != null && !positionsToReturn.isEmpty()) {
                    returnPositionsToFirstAdminStaff(positionsToReturn);
                }
                System.out.println("SUCCESS: Deleted admin staff ID: " + personnelId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR deleting admin staff: " + e.getMessage());
        }
        return false;
    }

    /**
     * Gets a supervisor by type
     *
     * @param supervisorType one of: "veterinary_supervisor", "inventory_supervisor", "farmhand_supervisor"
     * @return Supervisor personnel, or null if not exists
     */
    public Personnel getSupervisorByType(String supervisorType) {
        List<Personnel> supervisors = getPersonnelByJobTitle(supervisorType);
        return supervisors.isEmpty() ? null : supervisors.get(0);
    }

    /**
     * Gets all supervisors (all three types)
     *
     * @return List of all supervisors
     */
    public List<Personnel> getAllSupervisors() {
        List<Personnel> supervisors = new ArrayList<>();
        supervisors.addAll(getPersonnelByJobTitle("veterinary_supervisor"));
        supervisors.addAll(getPersonnelByJobTitle("inventory_supervisor"));
        supervisors.addAll(getPersonnelByJobTitle("farmhand_supervisor"));
        return supervisors;
    }

    /**
     * Gets subordinates for a specific supervisor
     *
     * @param supervisorId the ID of the supervisor
     * @return List of subordinates under this supervisor
     */
    public List<Personnel> getSubordinatesBySupervisorId(int supervisorId) {
        List<Personnel> subordinates = new ArrayList<>();
        String sql = "SELECT * FROM personnel WHERE supervisorId = ? ORDER BY fullName";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, supervisorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subordinates.add(mapResultSetToPersonnel(rs));
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting subordinates: " + e.getMessage());
        }
        return subordinates;
    }

    /**
     * Gets all subordinates of a specific type
     *
     * @param subordinateType one of: "veterinary_subordinate", "inventory_subordinate", "farmhand_subordinate"
     * @return List of subordinates of that type
     */
    public List<Personnel> getSubordinatesByType(String subordinateType) {
        return getPersonnelByJobTitle(subordinateType);
    }

    // ============================================================
    // UPDATE OPERATIONS
    // ============================================================

    /**
     * Updates an existing personnel record
     *
     * @param personnel the Personnel object with updated values
     * @return true if update was successful, false otherwise
     */
    public boolean updatePersonnel(Personnel personnel) {
        System.out.println("DEBUG: Updating personnel ID: " + personnel.getId());

        int jobTitleId = getJobTitleId(personnel.getJobTitle());
        if (jobTitleId == -1) {
            System.err.println("ERROR: Invalid job title: " + personnel.getJobTitle());
            return false;
        }

        String sql = "UPDATE personnel SET fullName = ?, age = ?, phone = ?, email = ?, " +
                     "jobTitle = ?, department = ?, positions = ?, hireDate = ?, salary = ?, " +
                     "isActive = ?, address = ?, emergencyContact = ?, supervisorId = ? " +
                     "WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2, personnel.getAge());
            stmt.setString(3, personnel.getPhone());
            stmt.setString(4, personnel.getEmail());
            stmt.setInt(5, jobTitleId);
            stmt.setString(6, personnel.getDepartment());
            stmt.setString(7, personnel.getPositions());

            if (personnel.getHireDate() != null) {
                stmt.setString(8, personnel.getHireDate().toString());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            stmt.setDouble(9, personnel.getSalary());
            stmt.setBoolean(10, personnel.isActive());
            stmt.setString(11, personnel.getAddress());
            stmt.setString(12, personnel.getEmergencyContact());

            if (personnel.getSupervisorId() != null) {
                stmt.setInt(13, personnel.getSupervisorId());
            } else {
                stmt.setNull(13, Types.INTEGER);
            }

            stmt.setInt(14, personnel.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("SUCCESS: Updated personnel ID: " + personnel.getId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR updating personnel: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates only the positions of an admin staff member
     *
     * @param personnelId the ID of the admin staff
     * @param positions comma-separated positions (e.g., "accounting,hr")
     * @return true if update was successful
     */
    public boolean updateAdminStaffPositions(int personnelId, String positions) {
        String sql = "UPDATE personnel SET positions = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, positions);
            stmt.setInt(2, personnelId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR updating admin staff positions: " + e.getMessage());
        }
        return false;
    }

    /**
     * Drops (clears) personnel info - resets to "missing" state
     * Used for Farm Owner, Cashier, and default Admin Staff
     * Does NOT delete the record, just clears the data
     *
     * @param personnelId the ID of the personnel to drop
     * @return true if drop was successful
     */
    public boolean dropPersonnel(int personnelId) {
        // For admin singletons, we actually delete to allow re-creation
        return deletePersonnel(personnelId);
    }

    // ============================================================
    // DELETE OPERATIONS
    // ============================================================

    /**
     * Deletes a personnel record
     * Note: Cannot delete a supervisor who has subordinates (RESTRICT)
     *
     * @param id the personnel ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deletePersonnel(int id) {
        // Check if this is a supervisor with subordinates
        if (hasSubordinates(id)) {
            System.err.println("ERROR: Cannot delete supervisor with subordinates. Delete subordinates first.");
            return false;
        }

        String sql = "DELETE FROM personnel WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("SUCCESS: Deleted personnel ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR deleting personnel: " + e.getMessage());
        }
        return false;
    }

    // ============================================================
    // COUNT AND STATISTICS
    // ============================================================

    /**
     * Gets total personnel count
     *
     * @return total number of personnel
     */
    public int getTotalPersonnelCount() {
        String sql = "SELECT COUNT(*) AS count FROM personnel";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting total personnel count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets administration personnel count (Owner + Cashier + All Admin Staff)
     *
     * @return count of administration personnel
     */
    public int getAdminCount() {
        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE department = 'administration'";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting admin count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts personnel by job title
     *
     * @param jobTitle the job title code
     * @return count of personnel with that job title
     */
    public int countByJobTitle(String jobTitle) {
        int jobTitleId = getJobTitleId(jobTitle);
        if (jobTitleId == -1) {
            return 0;
        }

        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE jobTitle = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, jobTitleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("ERROR counting by job title: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets subordinate count for a supervisor
     *
     * @param supervisorId the supervisor's ID
     * @return number of subordinates
     */
    public int getSubordinateCount(int supervisorId) {
        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE supervisorId = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, supervisorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting subordinate count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets subordinate count by supervisor type
     *
     * @param supervisorType the supervisor job title (e.g., "veterinary_supervisor")
     * @return number of subordinates under that supervisor type
     */
    public int getSubordinateCountByType(String supervisorType) {
        Personnel supervisor = getSupervisorByType(supervisorType);
        if (supervisor == null) {
            return 0;
        }
        return getSubordinateCount(supervisor.getId());
    }

    // ============================================================
    // VALIDATION AND EXISTENCE CHECKS
    // ============================================================

    /**
     * Checks if an email already exists in the database
     *
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM personnel WHERE email = ? LIMIT 1";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("ERROR checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if an email exists for a different personnel (for update validation)
     *
     * @param email the email to check
     * @param excludeId the personnel ID to exclude from check
     * @return true if email exists for another personnel
     */
    public boolean emailExistsForOther(String email, int excludeId) {
        String sql = "SELECT 1 FROM personnel WHERE email = ? AND id != ? LIMIT 1";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("ERROR checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if a personnel with given job title exists
     *
     * @param jobTitle the job title to check
     * @return true if at least one personnel with this job title exists
     */
    public boolean existsByJobTitle(String jobTitle) {
        return countByJobTitle(jobTitle) > 0;
    }

    /**
     * Checks if a supervisor has subordinates
     *
     * @param supervisorId the supervisor's ID
     * @return true if the supervisor has at least one subordinate
     */
    public boolean hasSubordinates(int supervisorId) {
        return getSubordinateCount(supervisorId) > 0;
    }

    /**
     * Checks if Farm Owner exists
     *
     * @return true if Farm Owner is set
     */
    public boolean hasFarmOwner() {
        return existsByJobTitle("farm_owner");
    }

    /**
     * Checks if Cashier exists
     *
     * @return true if Cashier is set
     */
    public boolean hasCashier() {
        return existsByJobTitle("cashier");
    }

    /**
     * Checks if default Admin Staff exists
     *
     * @return true if at least one Admin Staff exists
     */
    public boolean hasAdminStaff() {
        return existsByJobTitle("admin_staff");
    }

    /**
     * Checks if a supervisor type exists
     *
     * @param supervisorType the supervisor job title
     * @return true if that supervisor exists
     */
    public boolean hasSupervisor(String supervisorType) {
        return existsByJobTitle(supervisorType);
    }

    // ============================================================
    // POSITION MANAGEMENT (Admin Staff)
    // ============================================================

    /**
     * Gets all positions currently assigned to admin staff
     *
     * @return comma-separated string of all used positions
     */
    public String getAllUsedPositions() {
        StringBuilder usedPositions = new StringBuilder();
        List<Personnel> adminStaff = getAdminStaff();

        for (Personnel staff : adminStaff) {
            if (staff.getPositions() != null && !staff.getPositions().isEmpty()) {
                if (usedPositions.length() > 0) {
                    usedPositions.append(",");
                }
                usedPositions.append(staff.getPositions());
            }
        }
        return usedPositions.toString();
    }

    /**
     * Gets available positions (positions held by first admin staff that can be delegated)
     * For the FIRST admin staff being created: returns all 4 positions
     * For subsequent admin staff: returns positions still held by first admin staff
     *
     * @return array of available AdminPosition enums
     */
    public AdminPosition[] getAvailablePositions() {
        int adminStaffCount = getAdminStaffCount();

        if (adminStaffCount == 0) {
            // No admin staff yet - all positions for the first one
            System.out.println("DEBUG: No admin staff yet - all 4 positions available for first");
            return AdminPosition.values();
        } else {
            // Get positions from first admin staff that can be delegated
            AdminPosition[] available = getPositionsAvailableForDelegation();
            System.out.println("DEBUG: " + adminStaffCount + " admin staff exist. Positions available for delegation: " + available.length);
            return available;
        }
    }

    /**
     * Checks if this is the first admin staff being added (no existing admin staff)
     *
     * @return true if no admin staff exist yet
     */
    public boolean isAddingFirstAdminStaff() {
        return getAdminStaffCount() == 0;
    }

    /**
     * Gets the number of admin staff (max 4)
     *
     * @return count of admin staff
     */
    public int getAdminStaffCount() {
        return countByJobTitle("admin_staff");
    }

    /**
     * Checks if more admin staff can be added (max 4)
     *
     * @return true if less than 4 admin staff exist
     */
    public boolean canAddMoreAdminStaff() {
        return getAdminStaffCount() < 4;
    }

    // ============================================================
    // ACTIVATION/DEACTIVATION
    // ============================================================

    /**
     * Activates a personnel
     *
     * @param id the personnel ID
     * @return true if activation was successful
     */
    public boolean activatePersonnel(int id) {
        String sql = "UPDATE personnel SET isActive = 1 WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR activating personnel: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deactivates a personnel
     *
     * @param id the personnel ID
     * @return true if deactivation was successful
     */
    public boolean deactivatePersonnel(int id) {
        String sql = "UPDATE personnel SET isActive = 0 WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR deactivating personnel: " + e.getMessage());
        }
        return false;
    }

    // ============================================================
    // LEGACY COMPATIBILITY (for other parts of the system)
    // ============================================================

    /**
     * Gets all operations personnel (farm workers)
     * Legacy method for compatibility with existing code
     *
     * @return List of all farm personnel
     */
    public List<Personnel> getOperationsPersonnel() {
        return getFarmPersonnel();
    }

    /**
     * Gets personnel by supervisor ID
     * Legacy method name for compatibility
     *
     * @param supervisorId the supervisor's ID
     * @return List of subordinates
     */
    public List<Personnel> getPersonnelBySupervisorId(int supervisorId) {
        return getSubordinatesBySupervisorId(supervisorId);
    }
}
