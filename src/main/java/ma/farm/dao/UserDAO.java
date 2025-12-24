package ma.farm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.User;

/**
 * Data Access Object for User operations.
 * Handles all database CRUD operations for Moroccan business users.
 */
public class UserDAO {

    private final DatabaseConnection dbConnection;

    public UserDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Create a new user with all Moroccan business registration fields.
     */
    public boolean createUser(User user) {
        String sql = """
                INSERT INTO users (email, password, companyName, legalForm, capitalSocial,
                    ice, rc, fiscalId, patente, cnss, onssa,
                    address, city, postalCode, bankRIB, bankName, phoneNumber, website, logo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (
                Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getCompanyName());
            stmt.setString(4, user.getLegalForm());
            stmt.setInt(5, user.getCapitalSocial());
            stmt.setString(6, user.getIce());
            stmt.setString(7, user.getRc());
            stmt.setString(8, user.getFiscalId());
            stmt.setInt(9, user.getPatente());
            stmt.setString(10, user.getCnss());
            stmt.setString(11, user.getOnssa());
            stmt.setString(12, user.getAddress());
            stmt.setString(13, user.getCity());
            stmt.setString(14, user.getPostalCode());
            stmt.setString(15, user.getBankRIB());
            stmt.setString(16, user.getBankName());
            stmt.setString(17, user.getPhone());
            stmt.setString(18, user.getWebsite());
            stmt.setString(19, user.getLogo());

            int rows = stmt.executeUpdate();
            if (rows == 0)
                return false;

            // Retrieve generated ID
            try (
                    Statement idStmt = conn.createStatement();
                    ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() AS id")) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user by ID.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (
                PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get user by email.
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (
                PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all users.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (
                Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Validate login credentials.
     */
    public boolean validate(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (
                PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error validating user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update user with all fields.
     */
    public boolean updateUser(User user) {
        String sql = """
                UPDATE users SET email=?, password=?, companyName=?, legalForm=?, capitalSocial=?,
                    ice=?, rc=?, fiscalId=?, patente=?, cnss=?, onssa=?,
                    address=?, city=?, postalCode=?, bankRIB=?, bankName=?, phoneNumber=?, website=?, logo=?,
                    updatedAt=CURRENT_TIMESTAMP
                WHERE id=?
                """;
        try (
                PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getCompanyName());
            stmt.setString(4, user.getLegalForm());
            stmt.setInt(5, user.getCapitalSocial());
            stmt.setString(6, user.getIce());
            stmt.setString(7, user.getRc());
            stmt.setString(8, user.getFiscalId());
            stmt.setInt(9, user.getPatente());
            stmt.setString(10, user.getCnss());
            stmt.setString(11, user.getOnssa());
            stmt.setString(12, user.getAddress());
            stmt.setString(13, user.getCity());
            stmt.setString(14, user.getPostalCode());
            stmt.setString(15, user.getBankRIB());
            stmt.setString(16, user.getBankName());
            stmt.setString(17, user.getPhone());
            stmt.setString(18, user.getWebsite());
            stmt.setString(19, user.getLogo());
            stmt.setInt(20, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ============== Uniqueness Check Methods ==============

    /**
     * Check if email already exists.
     */
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if ICE already exists.
     */
    public boolean isICEExists(String ice) {
        String sql = "SELECT COUNT(*) FROM users WHERE ice = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, ice);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking ICE existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if RIB already exists.
     */
    public boolean isRIBExists(String rib) {
        String sql = "SELECT COUNT(*) FROM users WHERE bankRIB = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, rib);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking RIB existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete user by ID.
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get user count.
     */
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (
                Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Error getting user count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Authenticate user and return User object if successful.
     */
    public User authenticate(String email, String password) {
        if (validate(email, password)) {
            return getUserByEmail(email);
        }
        throw new SecurityException("Invalid email or password");
    }

    /**
     * Extract User from ResultSet.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        Timestamp creationTs = rs.getTimestamp("creationDate");
        Timestamp updatedTs = rs.getTimestamp("updatedAt");

        String logo = null;
        try {
            logo = rs.getString("logo");
        } catch (SQLException e) {
            // Ignore if column doesn't exist yet
        }

        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("companyName"),
                rs.getString("legalForm"),
                rs.getInt("capitalSocial"),
                rs.getString("ice"),
                rs.getString("rc"),
                rs.getString("fiscalId"),
                rs.getInt("patente"),
                rs.getString("cnss"),
                rs.getString("onssa"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("postalCode"),
                rs.getString("bankRIB"),
                rs.getString("bankName"),
                rs.getString("phoneNumber"), // DB column
                rs.getString("website"),
                logo,
                creationTs != null ? creationTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null);
    }
}
