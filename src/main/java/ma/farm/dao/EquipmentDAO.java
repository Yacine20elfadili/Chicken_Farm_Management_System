package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Equipment;

/**
 * EquipmentDAO - Data Access Object for Equipment inventory management
 *
 * Provides database operations for the Equipment model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Filtering by status and category
 * - Maintenance tracking
 *
 * All database operations use prepared statements to prevent SQL injection.
 *
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class EquipmentDAO {

    private final DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public EquipmentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Maps a ResultSet row to an Equipment object
     *
     * @param rs the ResultSet positioned at the current row
     * @return an Equipment object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Equipment mapResultSetToEquipment(ResultSet rs) throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setId(rs.getInt("id"));
        equipment.setName(rs.getString("name"));
        equipment.setCategory(rs.getString("category"));
        equipment.setQuantity(rs.getInt("quantity"));
        equipment.setStatus(rs.getString("status"));
        
        String purchaseDateStr = rs.getString("purchase_date");
        if (purchaseDateStr != null) {
            equipment.setPurchaseDate(LocalDate.parse(purchaseDateStr));
        }
        
        equipment.setPurchasePrice(rs.getDouble("purchase_price"));
        
        String lastMaintenanceDateStr = rs.getString("last_maintenance_date");
        if (lastMaintenanceDateStr != null) {
            equipment.setLastMaintenanceDate(LocalDate.parse(lastMaintenanceDateStr));
        }
        
        String nextMaintenanceDateStr = rs.getString("next_maintenance_date");
        if (nextMaintenanceDateStr != null) {
            equipment.setNextMaintenanceDate(LocalDate.parse(nextMaintenanceDateStr));
        }
        
        equipment.setLocation(rs.getString("location"));
        equipment.setNotes(rs.getString("notes"));
        
        return equipment;
    }

    /**
     * Add new equipment to the database
     *
     * @param equipment the Equipment object to add
     * @return true if successful, false otherwise
     */
    public boolean addEquipment(Equipment equipment) {
        String sql = "INSERT INTO equipment (name, category, quantity, status, purchase_date, purchase_price, last_maintenance_date, next_maintenance_date, location, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getCategory());
            stmt.setInt(3, equipment.getQuantity());
            stmt.setString(4, equipment.getStatus());
            stmt.setString(5, equipment.getPurchaseDate() != null ? equipment.getPurchaseDate().toString() : null);
            stmt.setDouble(6, equipment.getPurchasePrice());
            stmt.setString(7, equipment.getLastMaintenanceDate() != null ? equipment.getLastMaintenanceDate().toString() : null);
            stmt.setString(8, equipment.getNextMaintenanceDate() != null ? equipment.getNextMaintenanceDate().toString() : null);
            stmt.setString(9, equipment.getLocation());
            stmt.setString(10, equipment.getNotes());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding equipment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all equipment from the database
     *
     * @return a list of all Equipment objects
     */
    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all equipment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return equipmentList;
    }

    /**
     * Get equipment by ID
     *
     * @param id the equipment ID
     * @return the Equipment object, or null if not found
     */
    public Equipment getEquipmentById(int id) {
        String sql = "SELECT * FROM equipment WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEquipment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get equipment by category
     *
     * @param category the equipment category
     * @return a list of Equipment objects matching the category
     */
    public List<Equipment> getEquipmentByCategory(String category) {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE category = ? ORDER BY name";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return equipmentList;
    }

    /**
     * Get equipment by status
     *
     * @param status the equipment status (Good, Fair, Broken)
     * @return a list of Equipment objects with the specified status
     */
    public List<Equipment> getEquipmentByStatus(String status) {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE status = ? ORDER BY name";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return equipmentList;
    }

    /**
     * Get broken equipment
     *
     * @return a list of broken Equipment objects
     */
    public List<Equipment> getBrokenEquipment() {
        return getEquipmentByStatus("Broken");
    }

    /**
     * Get equipment due for maintenance
     *
     * @return a list of Equipment objects due for maintenance soon
     */
    public List<Equipment> getEquipmentDueForMaintenance() {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE next_maintenance_date IS NOT NULL " +
                     "AND next_maintenance_date <= date('now', '+7 days') " +
                     "AND next_maintenance_date >= date('now') " +
                     "ORDER BY next_maintenance_date";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment due for maintenance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return equipmentList;
    }

    /**
     * Update equipment information
     *
     * @param equipment the Equipment object with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateEquipment(Equipment equipment) {
        String sql = "UPDATE equipment SET name = ?, category = ?, quantity = ?, status = ?, " +
                     "purchase_date = ?, purchase_price = ?, last_maintenance_date = ?, " +
                     "next_maintenance_date = ?, location = ?, notes = ? " +
                     "WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getCategory());
            stmt.setInt(3, equipment.getQuantity());
            stmt.setString(4, equipment.getStatus());
            stmt.setString(5, equipment.getPurchaseDate() != null ? equipment.getPurchaseDate().toString() : null);
            stmt.setDouble(6, equipment.getPurchasePrice());
            stmt.setString(7, equipment.getLastMaintenanceDate() != null ? equipment.getLastMaintenanceDate().toString() : null);
            stmt.setString(8, equipment.getNextMaintenanceDate() != null ? equipment.getNextMaintenanceDate().toString() : null);
            stmt.setString(9, equipment.getLocation());
            stmt.setString(10, equipment.getNotes());
            stmt.setInt(11, equipment.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating equipment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update equipment status only
     *
     * @param equipmentId the equipment ID
     * @param newStatus the new status
     * @return true if successful, false otherwise
     */
    public boolean updateEquipmentStatus(int equipmentId, String newStatus) {
        String sql = "UPDATE equipment SET status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, equipmentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating equipment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update equipment last maintenance date
     *
     * @param equipmentId the equipment ID
     * @param maintenanceDate the maintenance date
     * @return true if successful, false otherwise
     */
    public boolean updateLastMaintenanceDate(int equipmentId, LocalDate maintenanceDate) {
        String sql = "UPDATE equipment SET last_maintenance_date = ? WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, maintenanceDate != null ? maintenanceDate.toString() : null);
            stmt.setInt(2, equipmentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating equipment maintenance date: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete equipment from the database
     *
     * @param equipmentId the equipment ID
     * @return true if successful, false otherwise
     */
    public boolean deleteEquipment(int equipmentId) {
        String sql = "DELETE FROM equipment WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, equipmentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting equipment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get total value of all equipment
     *
     * @return the total value in currency
     */
    public double getTotalEquipmentValue() {
        String sql = "SELECT SUM(purchase_price * quantity) as total FROM equipment";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total equipment value: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Get count of broken equipment
     *
     * @return the number of broken equipment items
     */
    public int getBrokenEquipmentCount() {
        String sql = "SELECT COUNT(*) as count FROM equipment WHERE status = 'Broken'";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting broken equipment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
}
