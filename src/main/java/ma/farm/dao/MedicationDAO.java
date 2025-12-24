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
 */
public class MedicationDAO {

    private final DatabaseConnection dbConnection;

    public MedicationDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    private Medication mapResultSetToMedication(ResultSet rs) throws SQLException {
        Medication medication = new Medication();
        medication.setId(rs.getInt("id"));
        medication.setName(rs.getString("name"));
        medication.setType(rs.getString("type"));
        medication.setQuantity(rs.getInt("quantity"));
        medication.setUnit(rs.getString("unit"));
        medication.setPricePerUnit(rs.getDouble("pricePerUnit"));
        medication.setSupplier(rs.getString("supplier"));
        
        String purchaseDateStr = rs.getString("purchaseDate");
        if (purchaseDateStr != null) {
            medication.setPurchaseDate(LocalDate.parse(purchaseDateStr));
        }
        
        String expiryDateStr = rs.getString("expiryDate");
        if (expiryDateStr != null) {
            medication.setExpiryDate(LocalDate.parse(expiryDateStr));
        }
        
        medication.setMinStockLevel(rs.getInt("minStockLevel"));
        medication.setUsage(rs.getString("usage"));
        
        return medication;
    }

    /**
     * Add a new medication to the database
     */
    public boolean addMedication(Medication medication) {
        String sql = "INSERT INTO medications (name, type, quantity, unit, pricePerUnit, supplier, purchaseDate, expiryDate, minStockLevel, usage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            System.out.println("Medication insert: " + rowsAffected + " rows affected");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding medication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all medications from the database
     */
    public List<Medication> getAllMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medications ORDER BY name";
        
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
     */
    public Medication getMedicationById(int id) {
        String sql = "SELECT * FROM medications WHERE id = ?";
        
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
     * Get medication by name
     */
    public Medication getMedicationByName(String name) {
        String sql = "SELECT * FROM medications WHERE name = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedication(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving medication by name: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get medications by type
     */
    public List<Medication> getMedicationByType(String type) {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medications WHERE type = ? ORDER BY name";
        
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
     */
    public List<Medication> getLowStockMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medications WHERE quantity < minStockLevel ORDER BY name";
        
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
     */
    public List<Medication> getExpiringMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medications WHERE expiryDate IS NOT NULL " +
                     "AND expiryDate <= date('now', '+30 days') " +
                     "AND expiryDate >= date('now') " +
                     "ORDER BY expiryDate";
        
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
     */
    public List<Medication> getExpiredMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String sql = "SELECT * FROM medications WHERE expiryDate IS NOT NULL " +
                     "AND expiryDate < date('now') " +
                     "ORDER BY expiryDate DESC";
        
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
     */
    public boolean updateMedication(Medication medication) {
        String sql = "UPDATE medications SET name = ?, type = ?, quantity = ?, unit = ?, pricePerUnit = ?, " +
                     "supplier = ?, purchaseDate = ?, expiryDate = ?, minStockLevel = ?, usage = ? " +
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
     */
    public boolean updateQuantity(int medicationId, double newQuantity) {
        String sql = "UPDATE medications SET quantity = ? WHERE id = ?";
        
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
     */
    public boolean deleteMedication(int medicationId) {
        String sql = "DELETE FROM medications WHERE id = ?";
        
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
     */
    public double getTotalMedicationValue() {
        String sql = "SELECT SUM(quantity * pricePerUnit) as total FROM medications";
        
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
     */
    public int getLowStockMedicationCount() {
        String sql = "SELECT COUNT(*) as count FROM medications WHERE quantity < minStockLevel";
        
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
