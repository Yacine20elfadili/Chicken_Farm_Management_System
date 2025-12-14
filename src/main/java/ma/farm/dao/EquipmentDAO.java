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
 */
public class EquipmentDAO {

    private final DatabaseConnection dbConnection;

    public EquipmentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    private Equipment mapResultSetToEquipment(ResultSet rs) throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setId(rs.getInt("id"));
        equipment.setName(rs.getString("name"));
        equipment.setCategory(rs.getString("category"));
        equipment.setQuantity(rs.getInt("quantity"));
        equipment.setStatus(rs.getString("status"));
        
        String purchaseDateStr = rs.getString("purchaseDate");
        if (purchaseDateStr != null) {
            equipment.setPurchaseDate(LocalDate.parse(purchaseDateStr));
        }
        
        equipment.setPurchasePrice(rs.getDouble("purchasePrice"));
        
        String lastMaintenanceDateStr = rs.getString("lastMaintenanceDate");
        if (lastMaintenanceDateStr != null) {
            equipment.setLastMaintenanceDate(LocalDate.parse(lastMaintenanceDateStr));
        }
        
        String nextMaintenanceDateStr = rs.getString("nextMaintenanceDate");
        if (nextMaintenanceDateStr != null) {
            equipment.setNextMaintenanceDate(LocalDate.parse(nextMaintenanceDateStr));
        }
        
        equipment.setLocation(rs.getString("location"));
        equipment.setNotes(rs.getString("notes"));
        
        return equipment;
    }

    /**
     * Add new equipment to the database
     */
    public boolean addEquipment(Equipment equipment) {
        String sql = "INSERT INTO equipment (name, category, quantity, status, purchaseDate, purchasePrice, lastMaintenanceDate, nextMaintenanceDate, location, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            System.out.println("Equipment insert: " + rowsAffected + " rows affected");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding equipment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all equipment from the database
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
     */
    public List<Equipment> getBrokenEquipment() {
        return getEquipmentByStatus("Broken");
    }

    /**
     * Get equipment due for maintenance
     */
    public List<Equipment> getEquipmentDueForMaintenance() {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE nextMaintenanceDate IS NOT NULL " +
                     "AND nextMaintenanceDate <= date('now', '+7 days') " +
                     "AND nextMaintenanceDate >= date('now') " +
                     "ORDER BY nextMaintenanceDate";
        
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
     */
    public boolean updateEquipment(Equipment equipment) {
        String sql = "UPDATE equipment SET name = ?, category = ?, quantity = ?, status = ?, " +
                     "purchaseDate = ?, purchasePrice = ?, lastMaintenanceDate = ?, " +
                     "nextMaintenanceDate = ?, location = ?, notes = ? " +
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
     */
    public boolean updateLastMaintenanceDate(int equipmentId, LocalDate maintenanceDate) {
        String sql = "UPDATE equipment SET lastMaintenanceDate = ? WHERE id = ?";
        
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
     */
    public double getTotalEquipmentValue() {
        String sql = "SELECT SUM(purchasePrice * quantity) as total FROM equipment";
        
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
