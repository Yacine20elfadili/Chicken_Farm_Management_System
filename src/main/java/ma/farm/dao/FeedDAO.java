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
 */
public class FeedDAO {

    private final DatabaseConnection dbConnection;

    public FeedDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    private Feed mapResultSetToFeed(ResultSet rs) throws SQLException {
        Feed feed = new Feed();
        feed.setId(rs.getInt("id"));
        feed.setName(rs.getString("name"));
        feed.setType(rs.getString("type"));
        feed.setQuantityKg(rs.getDouble("quantityKg"));
        feed.setPricePerKg(rs.getDouble("pricePerKg"));
        feed.setSupplier(rs.getString("supplier"));
        
        String lastRestockDateStr = rs.getString("lastRestockDate");
        if (lastRestockDateStr != null) {
            feed.setLastRestockDate(LocalDate.parse(lastRestockDateStr));
        }
        
        String expiryDateStr = rs.getString("expiryDate");
        if (expiryDateStr != null) {
            feed.setExpiryDate(LocalDate.parse(expiryDateStr));
        }
        
        feed.setMinStockLevel(rs.getDouble("minStockLevel"));
        
        return feed;
    }

    /**
     * Add a new feed to the database
     */
    public boolean addFeed(Feed feed) {
        String sql = "INSERT INTO feed (name, type, quantityKg, pricePerKg, supplier, lastRestockDate, expiryDate, minStockLevel) " +
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
            System.out.println("Feed insert: " + rowsAffected + " rows affected");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding feed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all feed from the database
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
     */
    public List<Feed> getLowStockFeed() {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed WHERE quantityKg < minStockLevel ORDER BY name";
        
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
     */
    public List<Feed> getExpiringFeed() {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed WHERE expiryDate IS NOT NULL " +
                     "AND expiryDate <= date('now', '+30 days') " +
                     "AND expiryDate >= date('now') " +
                     "ORDER BY expiryDate";
        
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
     */
    public List<Feed> getExpiredFeed() {
        List<Feed> feedList = new ArrayList<>();
        String sql = "SELECT * FROM feed WHERE expiryDate IS NOT NULL " +
                     "AND expiryDate < date('now') " +
                     "ORDER BY expiryDate DESC";
        
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
     */
    public boolean updateFeed(Feed feed) {
        String sql = "UPDATE feed SET name = ?, type = ?, quantityKg = ?, pricePerKg = ?, supplier = ?, " +
                     "lastRestockDate = ?, expiryDate = ?, minStockLevel = ? WHERE id = ?";

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
     */
    public boolean updateQuantity(int feedId, double newQuantity) {
        String sql = "UPDATE feed SET quantityKg = ? WHERE id = ?";
        
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
     */
    public double getTotalFeedValue() {
        String sql = "SELECT SUM(quantityKg * pricePerKg) as total FROM feed";
        
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
     */
    public double getTotalFeedQuantity() {
        String sql = "SELECT SUM(quantityKg) as total FROM feed";
        
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
