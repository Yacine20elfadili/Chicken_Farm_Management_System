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
        feed.setQuantityKg(rs.getDouble("quantityKg"));
        feed.setPricePerKg(rs.getDouble("pricePerKg"));
        feed.setSupplier(rs.getString("supplier"));

        String lastRestockStr = rs.getString("lastRestockDate");
        if (lastRestockStr != null && !lastRestockStr.isEmpty()) {
            feed.setLastRestockDate(LocalDate.parse(lastRestockStr));
        }

        String expiryStr = rs.getString("expiryDate");
        if (expiryStr != null && !expiryStr.isEmpty()) {
            feed.setExpiryDate(LocalDate.parse(expiryStr));
        }

        feed.setMinStockLevel(rs.getDouble("minStockLevel"));

        return feed;
    }

    /**
     * Retrieves all feed inventory from the database
     *
     * @return List of all Feed objects, or empty list if none found
     */
    public List<Feed> getAllFeed() {
        List<Feed> feedList = new ArrayList<>();
        String query = "SELECT * FROM feed ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
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
     * Retrieves feed by ID
     *
     * @param id the feed ID
     * @return Feed object if found, null otherwise
     */
    public Feed getFeedById(int id) {
        String query = "SELECT * FROM feed WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFeed(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving feed by ID: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves feed inventory filtered by type
     *
     * @param type the feed type to filter by (e.g., "Day-old", "Layer", "Meat Growth")
     * @return List of Feed objects matching the type, or empty list if none found
     */
    public List<Feed> getFeedByType(String type) {
        List<Feed> feedList = new ArrayList<>();
        String query = "SELECT * FROM feed WHERE type = ? ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, type);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    feedList.add(mapResultSetToFeed(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving feed by type: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return feedList;
    }

    /**
     * Retrieves all feed items that are below their minimum stock level
     *
     * @return List of Feed objects with low stock, or empty list if none found
     */
    public List<Feed> getLowStockFeed() {
        List<Feed> feedList = new ArrayList<>();
        String query =
            "SELECT * FROM feed WHERE quantityKg < minStockLevel ORDER BY (minStockLevel - quantityKg) DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                feedList.add(mapResultSetToFeed(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving low stock feed: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return feedList;
    }

    /**
     * Gets the count of feed items that are below minimum stock level
     *
     * @return count of low stock feed items
     */
    public int getLowStockCount() {
        String query =
            "SELECT COUNT(*) as count FROM feed WHERE quantityKg < minStockLevel";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println(
                "Error counting low stock feed: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Adds a new feed item to the database
     *
     * @param feed the Feed object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean addFeed(Feed feed) {
        String query =
            "INSERT INTO feed (name, type, quantityKg, pricePerKg, supplier, lastRestockDate, expiryDate, minStockLevel) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, feed.getName());
            stmt.setString(2, feed.getType());
            stmt.setDouble(3, feed.getQuantityKg());
            stmt.setDouble(4, feed.getPricePerKg());
            stmt.setString(5, feed.getSupplier());
            stmt.setString(
                6,
                feed.getLastRestockDate() != null
                    ? feed.getLastRestockDate().toString()
                    : null
            );
            stmt.setString(
                7,
                feed.getExpiryDate() != null
                    ? feed.getExpiryDate().toString()
                    : null
            );
            stmt.setDouble(8, feed.getMinStockLevel());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // SQLite: use last_insert_rowid() to get the generated ID
                try (
                    Statement idStmt = dbConnection
                        .getConnection()
                        .createStatement();
                    ResultSet rs = idStmt.executeQuery(
                        "SELECT last_insert_rowid()"
                    )
                ) {
                    if (rs.next()) {
                        feed.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding feed: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates the quantity of a specific feed item
     *
     * @param id the feed ID
     * @param quantity the new quantity in kg
     * @return true if update was successful, false otherwise
     */
    public boolean updateQuantity(int id, double quantity) {
        String query =
            "UPDATE feed SET quantityKg = ?, lastRestockDate = ? WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setDouble(1, quantity);
            stmt.setString(2, LocalDate.now().toString());
            stmt.setInt(3, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println(
                "Error updating feed quantity: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing feed item
     *
     * @param feed the Feed object with updated values
     * @return true if update was successful, false otherwise
     */
    public boolean updateFeed(Feed feed) {
        String query =
            "UPDATE feed SET name = ?, type = ?, quantityKg = ?, pricePerKg = ?, " +
            "supplier = ?, lastRestockDate = ?, expiryDate = ?, minStockLevel = ? WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, feed.getName());
            stmt.setString(2, feed.getType());
            stmt.setDouble(3, feed.getQuantityKg());
            stmt.setDouble(4, feed.getPricePerKg());
            stmt.setString(5, feed.getSupplier());
            stmt.setString(
                6,
                feed.getLastRestockDate() != null
                    ? feed.getLastRestockDate().toString()
                    : null
            );
            stmt.setString(
                7,
                feed.getExpiryDate() != null
                    ? feed.getExpiryDate().toString()
                    : null
            );
            stmt.setDouble(8, feed.getMinStockLevel());
            stmt.setInt(9, feed.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating feed: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a feed item from the database
     *
     * @param id the feed ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteFeed(int id) {
        String query = "DELETE FROM feed WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting feed: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets the total count of feed items
     *
     * @return total number of feed items in the database
     */
    public int getTotalFeedCount() {
        String query = "SELECT COUNT(*) as count FROM feed";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting feed: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Gets the total value of all feed inventory
     *
     * @return total value of feed inventory
     */
    public double getTotalFeedValue() {
        String query =
            "SELECT SUM(quantityKg * pricePerKg) as totalValue FROM feed";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getDouble("totalValue");
            }
        } catch (SQLException e) {
            System.err.println(
                "Error calculating total feed value: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Gets feed items that are expired or expiring within a specified number of days
     *
     * @param days number of days to check for expiry
     * @return List of Feed objects that are expired or expiring soon
     */
    public List<Feed> getExpiringFeed(int days) {
        List<Feed> feedList = new ArrayList<>();
        String query =
            "SELECT * FROM feed WHERE expiryDate IS NOT NULL AND expiryDate <= date('now', '+' || ? || ' days') ORDER BY expiryDate";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, days);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    feedList.add(mapResultSetToFeed(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving expiring feed: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return feedList;
    }

    /**
     * Main method to test all CRUD operations for FeedDAO
     */
    public static void main(String[] args) {
        System.out.println("=== FeedDAO CRUD Operations Test ===\n");

        FeedDAO feedDAO = new FeedDAO();

        // ==================== CREATE ====================
        System.out.println("--- CREATE: Adding new feed ---");
        Feed newFeed = new Feed();
        newFeed.setName("Test Organic Feed");
        newFeed.setType("Layer");
        newFeed.setQuantityKg(250.0);
        newFeed.setPricePerKg(18.50);
        newFeed.setSupplier("Test Supplier Inc.");
        newFeed.setLastRestockDate(LocalDate.now());
        newFeed.setExpiryDate(LocalDate.now().plusMonths(6));
        newFeed.setMinStockLevel(100.0);

        boolean created = feedDAO.addFeed(newFeed);
        if (created) {
            System.out.println(
                "✓ Feed created successfully with ID: " + newFeed.getId()
            );
            System.out.println("  " + newFeed);
        } else {
            System.out.println("✗ Failed to create feed");
        }

        // ==================== READ ====================
        System.out.println("\n--- READ: Get all feed ---");
        List<Feed> allFeed = feedDAO.getAllFeed();
        System.out.println("Total feed items: " + allFeed.size());
        for (Feed feed : allFeed) {
            System.out.println(
                "  - " +
                    feed.getName() +
                    " (" +
                    feed.getType() +
                    "): " +
                    feed.getQuantityKg() +
                    " kg"
            );
        }

        System.out.println("\n--- READ: Get feed by ID ---");
        if (newFeed.getId() > 0) {
            Feed fetchedFeed = feedDAO.getFeedById(newFeed.getId());
            if (fetchedFeed != null) {
                System.out.println("✓ Found feed: " + fetchedFeed.getName());
            } else {
                System.out.println("✗ Feed not found");
            }
        }

        System.out.println("\n--- READ: Get feed by type (Layer) ---");
        List<Feed> layerFeed = feedDAO.getFeedByType("Layer");
        System.out.println("Layer feed items: " + layerFeed.size());
        for (Feed feed : layerFeed) {
            System.out.println(
                "  - " + feed.getName() + ": " + feed.getQuantityKg() + " kg"
            );
        }

        System.out.println("\n--- READ: Get low stock feed ---");
        List<Feed> lowStockFeed = feedDAO.getLowStockFeed();
        System.out.println("Low stock feed items: " + lowStockFeed.size());
        for (Feed feed : lowStockFeed) {
            System.out.println(
                "  - " +
                    feed.getName() +
                    ": " +
                    feed.getQuantityKg() +
                    " kg (min: " +
                    feed.getMinStockLevel() +
                    " kg)"
            );
        }

        System.out.println("\n--- READ: Get low stock count ---");
        int lowStockCount = feedDAO.getLowStockCount();
        System.out.println("Low stock count: " + lowStockCount);

        System.out.println("\n--- READ: Get total feed value ---");
        double totalValue = feedDAO.getTotalFeedValue();
        System.out.println(
            "Total feed inventory value: $" + String.format("%.2f", totalValue)
        );

        System.out.println(
            "\n--- READ: Get expiring feed (within 90 days) ---"
        );
        List<Feed> expiringFeed = feedDAO.getExpiringFeed(90);
        System.out.println("Expiring feed items: " + expiringFeed.size());
        for (Feed feed : expiringFeed) {
            System.out.println(
                "  - " + feed.getName() + " expires: " + feed.getExpiryDate()
            );
        }

        // ==================== UPDATE ====================
        System.out.println("\n--- UPDATE: Update quantity ---");
        if (newFeed.getId() > 0) {
            boolean quantityUpdated = feedDAO.updateQuantity(
                newFeed.getId(),
                300.0
            );
            if (quantityUpdated) {
                Feed updatedFeed = feedDAO.getFeedById(newFeed.getId());
                System.out.println(
                    "✓ Quantity updated to: " +
                        updatedFeed.getQuantityKg() +
                        " kg"
                );
            } else {
                System.out.println("✗ Failed to update quantity");
            }
        }

        System.out.println("\n--- UPDATE: Update full feed record ---");
        if (newFeed.getId() > 0) {
            newFeed.setName("Updated Organic Feed");
            newFeed.setPricePerKg(19.99);
            newFeed.setQuantityKg(350.0);
            boolean updated = feedDAO.updateFeed(newFeed);
            if (updated) {
                Feed updatedFeed = feedDAO.getFeedById(newFeed.getId());
                System.out.println(
                    "✓ Feed updated: " +
                        updatedFeed.getName() +
                        " - $" +
                        updatedFeed.getPricePerKg() +
                        "/kg"
                );
            } else {
                System.out.println("✗ Failed to update feed");
            }
        }

        // ==================== DELETE ====================
        System.out.println("\n--- DELETE: Remove test feed ---");
        if (newFeed.getId() > 0) {
            boolean deleted = feedDAO.deleteFeed(newFeed.getId());
            if (deleted) {
                System.out.println("✓ Feed deleted successfully");
                Feed deletedFeed = feedDAO.getFeedById(newFeed.getId());
                if (deletedFeed == null) {
                    System.out.println(
                        "✓ Confirmed: Feed no longer exists in database"
                    );
                }
            } else {
                System.out.println("✗ Failed to delete feed");
            }
        }

        // ==================== BUSINESS LOGIC TESTS ====================
        System.out.println("\n--- BUSINESS LOGIC: Test Feed model methods ---");
        Feed testFeed = new Feed();
        testFeed.setQuantityKg(50.0);
        testFeed.setMinStockLevel(100.0);
        testFeed.setPricePerKg(15.0);
        testFeed.setExpiryDate(LocalDate.now().plusDays(5));

        System.out.println(
            "isLowStock() [50kg < 100kg min]: " + testFeed.isLowStock()
        );
        System.out.println(
            "isExpiredOrNearExpiry() [expires in 5 days]: " +
                testFeed.isExpiredOrNearExpiry()
        );
        System.out.println(
            "getTotalValue() [50kg * $15]: $" + testFeed.getTotalValue()
        );

        System.out.println("\n=== FeedDAO Test Complete ===");
    }
}
