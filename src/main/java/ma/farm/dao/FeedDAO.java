package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Feed;

/**
 * FeedDAO - Data Access Object for Feed inventory management
 *
 * Provides database operations for the Feed model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Filtering by type and stock levels
 * - Low stock alerts
 *
 * All database operations use prepared statements to prevent SQL injection.
 *
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class FeedDAO {

    private final DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public FeedDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Maps a ResultSet row to a Feed object
     *
     * @param rs the ResultSet positioned at the current row
     * @return a Feed object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Feed mapResultSetToFeed(ResultSet rs) throws SQLException {
        Feed feed = new Feed();
        feed.setId(rs.getInt("id"));
        feed.setName(rs.getString("name"));
        feed.setType(rs.getString("type"));
        feed.setQuantityKg(rs.getDouble("quantity_kg"));
        feed.setPricePerKg(rs.getDouble("price_per_kg"));
        feed.setSupplier(rs.getString("supplier"));
        
        String lastRestockDateStr = rs.getString("last_restock_date");
        if (lastRestockDateStr != null) {
            feed.setLastRestockDate(LocalDate.parse(lastRestockDateStr));
        }
        
        String expiryDateStr = rs.getString("expiry_date");
        if (expiryDateStr != null) {
            feed.setExpiryDate(LocalDate.parse(expiryDateStr));
        }
        
        feed.setMinStockLevel(rs.getDouble("min_stock_level"));
        
        return feed;
    }

    /**
     * Add a new feed to the database
     *
     * @param feed the Feed object to add
     * @return true if successful, false otherwise
     */
    public boolean addFeed(Feed feed) {
        String sql = "INSERT INTO feed (name, type, quantity_kg, price_per_kg, supplier, last_restock_date, expiry_date, min_stock_level) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, feed.getName());
            stmt.setString(2, feed.getType());
            stmt.setDouble(3, feed.getQuantityKg());
            stmt.setDouble(4, feed.getPricePerKg());
            stmt.setString(5, feed.getSupplier());
            stmt.setString(6, feed.getLastRestockDate() != null ? feed.getLastRestockDate().toString() : null);
            stmt.setString(7, feed.getExpiryDate() != null ? feed.getExpiryDate().toString() : null);
            stmt.setDouble(8, feed.getMinStockLevel());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding feed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all feed from the database
     *
     * @return a list of all Feed objects
     */
    public List<Feed> getAllFeed() {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                feedList.add(mapResultSetToFeed(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all feed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedList;
    }

    /**
     * Get feed by ID
     *
     * @param id the feed ID
     * @return the Feed object, or null if not found
     */
    public Feed getFeedById(int id) {
        String sql = "SELECT * FROM feed WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFeed(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feed by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get feed by type
     *
     * @param type the feed type
     * @return a list of Feed objects matching the type
     */
    public List<Feed> getFeedByType(String type) {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed WHERE type = ? ORDER BY name";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    feedList.add(mapResultSetToFeed(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feed by type: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedList;
    }

    /**
     * Get all feed with low stock
     *
     * @return a list of Feed objects with quantity below minimum level
     */
    public List<Feed> getLowStockFeed() {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed WHERE quantity_kg < min_stock_level ORDER BY name";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                feedList.add(mapResultSetToFeed(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving low stock feed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedList;
    }

    /**
     * Get all expiring feed (within 30 days)
     *
     * @return a list of Feed objects expiring soon
     */
    public List<Feed> getExpiringFeed() {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed WHERE expiry_date IS NOT NULL " +
                     "AND expiry_date <= date('now', '+30 days') " +
                     "AND expiry_date >= date('now') " +
                     "ORDER BY expiry_date";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                feedList.add(mapResultSetToFeed(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving expiring feed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedList;
    }

    /**
     * Get expired feed
     *
     * @return a list of expired Feed objects
     */
    public List<Feed> getExpiredFeed() {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed WHERE expiry_date IS NOT NULL " +
                     "AND expiry_date < date('now') " +
                     "ORDER BY expiry_date DESC";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                feedList.add(mapResultSetToFeed(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving expired feed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedList;
    }

    /**
     * Update feed information
     *
     * @param feed the Feed object with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateFeed(Feed feed) {
        String sql = "UPDATE feed SET name = ?, type = ?, quantity_kg = ?, price_per_kg = ?, " +
                     "supplier = ?, last_restock_date = ?, expiry_date = ?, min_stock_level = ? " +
                     "WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, feed.getName());
            stmt.setString(2, feed.getType());
            stmt.setDouble(3, feed.getQuantityKg());
            stmt.setDouble(4, feed.getPricePerKg());
            stmt.setString(5, feed.getSupplier());
            stmt.setString(6, feed.getLastRestockDate() != null ? feed.getLastRestockDate().toString() : null);
            stmt.setString(7, feed.getExpiryDate() != null ? feed.getExpiryDate().toString() : null);
            stmt.setDouble(8, feed.getMinStockLevel());
            stmt.setInt(9, feed.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating feed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update feed quantity (for usage/consumption)
     *
     * @param feedId the feed ID
     * @param newQuantity the new quantity in kg
     * @return true if successful, false otherwise
     */
    public boolean updateQuantity(int feedId, double newQuantity) {
        String sql = "UPDATE feed SET quantity_kg = ? WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, newQuantity);
            stmt.setInt(2, feedId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating feed quantity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete feed from the database
     *
     * @param feedId the feed ID
     * @return true if successful, false otherwise
     */
    public boolean deleteFeed(int feedId) {
        String sql = "DELETE FROM feed WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, feedId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting feed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get total value of all feed inventory
     *
     * @return the total value in currency
     */
    public double getTotalFeedValue() {
        String sql = "SELECT SUM(quantity_kg * price_per_kg) as total FROM feed";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total feed value: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Get total quantity of all feed
     *
     * @return the total quantity in kg
     */
    public double getTotalFeedQuantity() {
        String sql = "SELECT SUM(quantity_kg) as total FROM feed";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total feed quantity: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
}
