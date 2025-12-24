package ma.farm.dao;

import ma.farm.model.EquipmentItem;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * EquipmentItemDAO - Data Access Object for Equipment Items
 */
public class EquipmentItemDAO {

    private final DatabaseConnection dbConnection;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EquipmentItemDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Map ResultSet to EquipmentItem
     */
    private EquipmentItem mapResultSetToItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int categoryId = rs.getInt("categoryId");
        String status = rs.getString("status");
        double purchasePrice = rs.getDouble("purchasePrice");

        LocalDate purchaseDate = null;
        String purchaseDateStr = rs.getString("purchaseDate");
        if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
            try {
                purchaseDate = LocalDate.parse(purchaseDateStr);
            } catch (Exception e) {
                System.err.println("Error parsing purchaseDate: " + purchaseDateStr);
            }
        }

        LocalDate lastMaintenanceDate = null;
        String lastMaintenanceDateStr = rs.getString("lastMaintenanceDate");
        if (lastMaintenanceDateStr != null && !lastMaintenanceDateStr.isEmpty()) {
            try {
                lastMaintenanceDate = LocalDate.parse(lastMaintenanceDateStr);
            } catch (Exception e) {
                System.err.println("Error parsing lastMaintenanceDate: " + lastMaintenanceDateStr);
            }
        }

        LocalDate nextMaintenanceDate = null;
        String nextMaintenanceDateStr = rs.getString("nextMaintenanceDate");
        if (nextMaintenanceDateStr != null && !nextMaintenanceDateStr.isEmpty()) {
            try {
                nextMaintenanceDate = LocalDate.parse(nextMaintenanceDateStr);
            } catch (Exception e) {
                System.err.println("Error parsing nextMaintenanceDate: " + nextMaintenanceDateStr);
            }
        }

        LocalDateTime createdAt = null;
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                System.err.println("Error parsing createdAt: " + createdAtStr);
            }
        }

        LocalDateTime updatedAt = null;
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !updatedAtStr.isEmpty()) {
            try {
                updatedAt = LocalDateTime.parse(updatedAtStr, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                System.err.println("Error parsing updatedAt: " + updatedAtStr);
            }
        }

        int supplierId = rs.getInt("supplierId");

        // Use full constructor
        return new EquipmentItem(id, categoryId, status, purchaseDate, purchasePrice,
                lastMaintenanceDate, nextMaintenanceDate, supplierId, createdAt, updatedAt);
    }

    /**
     * Add a new equipment item
     */
    public boolean addItem(EquipmentItem item) {
        String sql = "INSERT INTO equipment_items (categoryId, status, purchaseDate, purchasePrice, " +
                "lastMaintenanceDate, nextMaintenanceDate, supplierId) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, item.getCategoryId());
            stmt.setString(2, item.getStatus());
            stmt.setString(3, item.getPurchaseDate() != null ? item.getPurchaseDate().toString() : null);
            stmt.setDouble(4, item.getPurchasePrice());
            stmt.setString(5, item.getLastMaintenanceDate() != null ? item.getLastMaintenanceDate().toString() : null);
            stmt.setString(6, item.getNextMaintenanceDate() != null ? item.getNextMaintenanceDate().toString() : null);
            if (item.getSupplierId() > 0) {
                stmt.setInt(7, item.getSupplierId());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Equipment item insert: " + rowsAffected + " rows affected");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding equipment item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all items
     */
    public List<EquipmentItem> getAllItems() {
        List<EquipmentItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment_items ORDER BY id";

        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all items: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Get item by ID
     */
    public EquipmentItem getItemById(int id) {
        String sql = "SELECT * FROM equipment_items WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving item by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get all items for a specific category
     */
    public List<EquipmentItem> getItemsByCategory(int categoryId) {
        List<EquipmentItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment_items WHERE categoryId = ? ORDER BY id";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving items by category: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Get items by status
     */
    public List<EquipmentItem> getItemsByStatus(String status) {
        List<EquipmentItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment_items WHERE status = ? ORDER BY id";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving items by status: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Get broken items
     */
    public List<EquipmentItem> getBrokenItems() {
        return getItemsByStatus("Broken");
    }

    /**
     * Get items due for maintenance
     */
    public List<EquipmentItem> getItemsDueForMaintenance() {
        List<EquipmentItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment_items " +
                "WHERE nextMaintenanceDate IS NOT NULL " +
                "AND nextMaintenanceDate <= date('now') " +
                "ORDER BY nextMaintenanceDate";

        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving maintenance items: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Update equipment item
     */
    public boolean updateItem(EquipmentItem item) {
        String sql = "UPDATE equipment_items SET categoryId = ?, status = ?, purchaseDate = ?, " +
                "purchasePrice = ?, lastMaintenanceDate = ?, nextMaintenanceDate = ?, supplierId = ? WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, item.getCategoryId());
            stmt.setString(2, item.getStatus());
            stmt.setString(3, item.getPurchaseDate() != null ? item.getPurchaseDate().toString() : null);
            stmt.setDouble(4, item.getPurchasePrice());
            stmt.setString(5, item.getLastMaintenanceDate() != null ? item.getLastMaintenanceDate().toString() : null);
            stmt.setString(6, item.getNextMaintenanceDate() != null ? item.getNextMaintenanceDate().toString() : null);
            if (item.getSupplierId() > 0) {
                stmt.setInt(7, item.getSupplierId());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            stmt.setInt(8, item.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating equipment item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete equipment item
     */
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM equipment_items WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting equipment item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get count of items by status in a category
     */
    public int getItemCountByStatus(int categoryId, String status) {
        String sql = "SELECT COUNT(*) as count FROM equipment_items WHERE categoryId = ? AND status = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setString(2, status);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting items: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}