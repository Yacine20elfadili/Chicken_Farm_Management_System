package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Medication;

/**
 * MedicationDAO - Data Access Object for Medication inventory management
 *
 * Provides database operations for the Medication model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Filtering by type and stock levels
 * - Low stock alerts and expiry tracking
 *
 * All database operations use prepared statements to prevent SQL injection.
 *
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class MedicationDAO {

    private final DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public MedicationDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Maps a ResultSet row to a Medication object
     *
     * @param rs the ResultSet positioned at the current row
     * @return a Medication object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Medication mapResultSetToMedication(ResultSet rs) throws SQLException {
        Medication medication = new Medication();
        medication.setId(rs.getInt("id"));
        medication.setName(rs.getString("name"));
        medication.setType(rs.getString("type"));
        medication.setQuantity(rs.getInt("quantity"));
        medication.setUnit(rs.getString("unit"));
        medication.setPricePerUnit(rs.getDouble("price_per_unit"));
        medication.setSupplier(rs.getString("supplier"));
        
        String purchaseDateStr = rs.getString("purchase_date");
        if (purchaseDateStr != null) {
            medication.setPurchaseDate(LocalDate.parse(purchaseDateStr));
        }
        
        String expiryDateStr = rs.getString("expiry_date");
        if (expiryDateStr != null) {
            medication.setExpiryDate(LocalDate.parse(expiryDateStr));
        }
        
        medication.setMinStockLevel(rs.getInt("min_stock_level"));
        medication.setUsage(rs.getString("usage"));
        
        return medication;
    }

    /**
     * Add a new medication to the database
     *
     * @param medication the Medication object to add
     * @return true if successful, false otherwise
     */
    public boolean addMedication(Medication medication) {
        String sql = "INSERT INTO medication (name, type, quantity, unit, price_per_unit, supplier, purchase_date, expiry_date, min_stock_level, usage) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, medication.getName());
            stmt.setString(2, medication.getType());
            stmt.setInt(3, medication.getQuantity());
            stmt.setString(4, medication.getUnit());
            stmt.setDouble(5, medication.getPricePerUnit());
            stmt.setString(6, medication.getSupplier());
            stmt.setString(7, medication.getPurchaseDate() != null ? medication.getPurchaseDate().toString() : null);
            stmt.setString(8, medication.getExpiryDate() != null ? medication.getExpiryDate().toString() : null);
            stmt.setInt(9, medication.getMinStockLevel());
            stmt.setString(10, medication.getUsage());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding medication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all medications from the database
     *
     * @return a list of all Medication objects
     */
    public List<Medication> getAllMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medication ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicationList.add(mapResultSetToMedication(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all medications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicationList;
    }

    /**
     * Get medication by ID
     *
     * @param id the medication ID
     * @return the Medication object, or null if not found
     */
    public Medication getMedicationById(int id) {
        String sql = "SELECT * FROM medication WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedication(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving medication by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get medications by type
     *
     * @param type the medication type
     * @return a list of Medication objects matching the type
     */
    public List<Medication> getMedicationByType(String type) {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medication WHERE type = ? ORDER BY name";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicationList.add(mapResultSetToMedication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving medications by type: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicationList;
    }

    /**
     * Get all medications with low stock
     *
     * @return a list of Medication objects with quantity below minimum level
     */
    public List<Medication> getLowStockMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medication WHERE quantity < min_stock_level ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicationList.add(mapResultSetToMedication(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving low stock medications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicationList;
    }

    /**
     * Get all expiring medications (within 30 days)
     *
     * @return a list of Medication objects expiring soon
     */
    public List<Medication> getExpiringMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medication WHERE expiry_date IS NOT NULL " +
                     "AND expiry_date <= date('now', '+30 days') " +
                     "AND expiry_date >= date('now') " +
                     "ORDER BY expiry_date";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicationList.add(mapResultSetToMedication(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving expiring medications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicationList;
    }

    /**
     * Get expired medications
     *
     * @return a list of expired Medication objects
     */
    public List<Medication> getExpiredMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medication WHERE expiry_date IS NOT NULL " +
                     "AND expiry_date < date('now') " +
                     "ORDER BY expiry_date DESC";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicationList.add(mapResultSetToMedication(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving expired medications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicationList;
    }

    /**
     * Update medication information
     *
     * @param medication the Medication object with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateMedication(Medication medication) {
        String sql = "UPDATE medication SET name = ?, type = ?, quantity = ?, unit = ?, price_per_unit = ?, " +
                     "supplier = ?, purchase_date = ?, expiry_date = ?, min_stock_level = ?, usage = ? " +
                     "WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, medication.getName());
            stmt.setString(2, medication.getType());
            stmt.setInt(3, medication.getQuantity());
            stmt.setString(4, medication.getUnit());
            stmt.setDouble(5, medication.getPricePerUnit());
            stmt.setString(6, medication.getSupplier());
            stmt.setString(7, medication.getPurchaseDate() != null ? medication.getPurchaseDate().toString() : null);
            stmt.setString(8, medication.getExpiryDate() != null ? medication.getExpiryDate().toString() : null);
            stmt.setInt(9, medication.getMinStockLevel());
            stmt.setString(10, medication.getUsage());
            stmt.setInt(11, medication.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update medication quantity (for usage/consumption)
     *
     * @param medicationId the medication ID
     * @param newQuantity the new quantity
     * @return true if successful, false otherwise
     */
    public boolean updateQuantity(int medicationId, double newQuantity) {
        String sql = "UPDATE medication SET quantity = ? WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, (int) newQuantity);
            stmt.setInt(2, medicationId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medication quantity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete medication from the database
     *
     * @param medicationId the medication ID
     * @return true if successful, false otherwise
     */
    public boolean deleteMedication(int medicationId) {
        String sql = "DELETE FROM medication WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, medicationId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting medication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get total value of all medication inventory
     *
     * @return the total value in currency
     */
    public double getTotalMedicationValue() {
        String sql = "SELECT SUM(quantity * price_per_unit) as total FROM medication";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total medication value: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Get count of low stock medications
     *
     * @return the number of medications with low stock
     */
    public int getLowStockMedicationCount() {
        String sql = "SELECT COUNT(*) as count FROM medication WHERE quantity < min_stock_level";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting low stock medications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
}
