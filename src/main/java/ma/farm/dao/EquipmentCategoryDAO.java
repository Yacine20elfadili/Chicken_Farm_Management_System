package ma.farm.dao;

import ma.farm.model.EquipmentCategory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * EquipmentCategoryDAO - Data Access Object for Equipment Categories
 */
public class EquipmentCategoryDAO {

    private final DatabaseConnection dbConnection;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EquipmentCategoryDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Map ResultSet to EquipmentCategory
     */
    private EquipmentCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String category = rs.getString("category");
        String location = rs.getString("location");
        String notes = rs.getString("notes");
        int itemCount = 0;
        
        // Try to get itemCount from query (if using COUNT JOIN)
        try {
            itemCount = rs.getInt("itemCount");
        } catch (SQLException e) {
            itemCount = 0;
        }

        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                System.err.println("Error parsing createdAt: " + createdAtStr);
            }
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null && !updatedAtStr.isEmpty()) {
            try {
                updatedAt = LocalDateTime.parse(updatedAtStr, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                System.err.println("Error parsing updatedAt: " + updatedAtStr);
            }
        }

        // Use full constructor
        return new EquipmentCategory(id, name, category, location, notes, itemCount, createdAt, updatedAt);
    }

    /**
     * Add a new equipment category
     */
    public boolean addCategory(EquipmentCategory category) {
        String sql = "INSERT INTO equipment_categories (name, category, location, notes) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getCategory());
            stmt.setString(3, category.getLocation());
            stmt.setString(4, category.getNotes());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Equipment category insert: " + rowsAffected + " rows affected");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding equipment category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all categories with item counts
     */
    public List<EquipmentCategory> getAllCategoriesWithCounts() {
        List<EquipmentCategory> categories = new ArrayList<>();
        String sql = "SELECT ec.*, COALESCE(COUNT(ei.id), 0) as itemCount " +
                    "FROM equipment_categories ec " +
                    "LEFT JOIN equipment_items ei ON ec.id = ei.categoryId " +
                    "GROUP BY ec.id " +
                    "ORDER BY ec.name";

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    /**
     * Get all equipment categories
     */
    public List<EquipmentCategory> getAllCategories() {
        return getAllCategoriesWithCounts();
    }

    /**
     * Get category by ID
     */
    public EquipmentCategory getCategoryById(int id) {
        String sql = "SELECT ec.*, COALESCE(COUNT(ei.id), 0) as itemCount " +
                    "FROM equipment_categories ec " +
                    "LEFT JOIN equipment_items ei ON ec.id = ei.categoryId " +
                    "WHERE ec.id = ? " +
                    "GROUP BY ec.id";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment category: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update equipment category
     */
    public boolean updateCategory(EquipmentCategory category) {
        String sql = "UPDATE equipment_categories SET name = ?, category = ?, location = ?, notes = ? WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getCategory());
            stmt.setString(3, category.getLocation());
            stmt.setString(4, category.getNotes());
            stmt.setInt(5, category.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating equipment category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete equipment category
     */
    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM equipment_categories WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting equipment category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if category name exists
     */
    public boolean categoryNameExists(String name) {
        String sql = "SELECT COUNT(*) FROM equipment_categories WHERE name = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking category name: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}