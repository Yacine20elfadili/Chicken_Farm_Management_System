package ma.farm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.User;

public class UserDAO {

    private final DatabaseConnection dbConnection;

    public UserDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // Create user
    public boolean createUser(User user) {
        String sql =
            "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());

            int rows = stmt.executeUpdate();
            if (rows == 0) return false;

            // SQLite: récupérer ID
            try (
                Statement idStmt = conn.createStatement();
                ResultSet rs = idStmt.executeQuery(
                    "SELECT last_insert_rowid() AS id"
                )
            ) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    // Get user by ID
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getTimestamp("CreationDate").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }

    // Get user by email
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getTimestamp("CreationDate").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println(
                "Error getting user by email: " + e.getMessage()
            );
        }
        return null;
    }

    // Get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (
            Statement stmt = dbConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                users.add(
                    new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getTimestamp("CreationDate").toLocalDateTime()
                    )
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }

    // Validate login (plain text)
    public boolean validate(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error validating user: " + e.getMessage());
        }
        return false;
    }

    // Update
    public boolean updateUser(User user) {
        String sql =
            "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setInt(4, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    // Delete
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    // Count
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (
            Statement stmt = dbConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Error getting user count: " + e.getMessage());
        }
        return 0;
    }

    public User authenticate(String email, String password) {
        if (validate(email, password)) return getUserByEmail(email);

        throw new SecurityException("Invalid email or password");
    }
}
