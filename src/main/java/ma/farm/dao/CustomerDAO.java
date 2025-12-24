package ma.farm.dao;

import ma.farm.model.Customer;
import java.sql.*;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private DatabaseConnection dbConnection;

    public CustomerDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, companyName, type, legalForm, contactPerson, email, phone, address, "
                +
                "ice, rc, website, secondaryContactName, secondaryContactPhone, bankName, rib, " +
                "paymentTerms, usualPurchases, deliverySchedule, outstandingBalance, notes, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getCompanyName());
            stmt.setString(3, customer.getType());
            stmt.setString(4, customer.getLegalForm());
            stmt.setString(5, customer.getContactPerson());
            stmt.setString(6, customer.getEmail());
            stmt.setString(7, customer.getPhone());
            stmt.setString(8, customer.getAddress());
            stmt.setString(9, customer.getIce());
            stmt.setString(10, customer.getRc());
            stmt.setString(11, customer.getWebsite());
            stmt.setString(12, customer.getSecondaryContactName());
            stmt.setString(13, customer.getSecondaryContactPhone());
            stmt.setString(14, customer.getBankName());
            stmt.setString(15, customer.getRib());
            stmt.setString(16, customer.getPaymentTerms());
            stmt.setString(17, customer.getUsualPurchases());
            stmt.setString(18, customer.getDeliverySchedule());
            stmt.setDouble(19, customer.getOutstandingBalance());
            stmt.setString(20, customer.getNotes());
            stmt.setBoolean(21, customer.isActive());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name=?, companyName=?, type=?, legalForm=?, contactPerson=?, email=?, phone=?, "
                +
                "address=?, ice=?, rc=?, website=?, secondaryContactName=?, secondaryContactPhone=?, " +
                "bankName=?, rib=?, paymentTerms=?, usualPurchases=?, deliverySchedule=?, outstandingBalance=?, notes=? "
                +
                "WHERE id=?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getCompanyName());
            stmt.setString(3, customer.getType());
            stmt.setString(4, customer.getLegalForm());
            stmt.setString(5, customer.getContactPerson());
            stmt.setString(6, customer.getEmail());
            stmt.setString(7, customer.getPhone());
            stmt.setString(8, customer.getAddress());
            stmt.setString(9, customer.getIce());
            stmt.setString(10, customer.getRc());
            stmt.setString(11, customer.getWebsite());
            stmt.setString(12, customer.getSecondaryContactName());
            stmt.setString(13, customer.getSecondaryContactPhone());
            stmt.setString(14, customer.getBankName());
            stmt.setString(15, customer.getRib());
            stmt.setString(16, customer.getPaymentTerms());
            stmt.setString(17, customer.getUsualPurchases());
            stmt.setString(18, customer.getDeliverySchedule());
            stmt.setDouble(19, customer.getOutstandingBalance());
            stmt.setString(20, customer.getNotes());
            stmt.setInt(21, customer.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Soft delete - sets isActive to false.
     */
    public boolean deleteCustomer(int id) {
        String sql = "UPDATE customers SET isActive = 0 WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restore a soft-deleted customer.
     */
    public boolean restoreCustomer(int id) {
        String sql = "UPDATE customers SET isActive = 1 WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error restoring customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all customers (including inactive for admin view).
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    /**
     * Get only active customers (for dropdowns, sales, etc.).
     */
    public List<Customer> getActiveCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE isActive = 1 ORDER BY name";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    /**
     * Get customers by type (Company or Individual).
     */
    public List<Customer> getCustomersByType(String type) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE type = ? AND isActive = 1 ORDER BY name";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting customers by type: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    /**
     * Increment visit count and update last visit date (called on each sale).
     */
    public boolean recordVisit(int id, double saleAmount) {
        String sql = "UPDATE customers SET visitCount = visitCount + 1, lastVisitDate = ?, " +
                "totalPurchases = totalPurchases + ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, LocalDate.now().toString());
            stmt.setDouble(2, saleAmount);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording visit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update outstanding balance (add to balance for credit sale, subtract for
     * payment).
     */
    public boolean updateOutstandingBalance(int id, double amount) {
        String sql = "UPDATE customers SET outstandingBalance = outstandingBalance + ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get customer by ID.
     */
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setCompanyName(rs.getString("companyName"));
        c.setType(rs.getString("type"));
        c.setLegalForm(rs.getString("legalForm"));
        c.setContactPerson(rs.getString("contactPerson"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setIce(rs.getString("ice"));
        c.setRc(rs.getString("rc"));
        c.setWebsite(rs.getString("website"));
        c.setSecondaryContactName(rs.getString("secondaryContactName"));
        c.setSecondaryContactPhone(rs.getString("secondaryContactPhone"));
        c.setBankName(rs.getString("bankName"));
        c.setRib(rs.getString("rib"));
        c.setPaymentTerms(rs.getString("paymentTerms"));
        c.setUsualPurchases(rs.getString("usualPurchases"));
        c.setDeliverySchedule(rs.getString("deliverySchedule"));
        c.setOutstandingBalance(rs.getDouble("outstandingBalance"));
        c.setTotalPurchases(rs.getDouble("totalPurchases"));
        c.setVisitCount(rs.getInt("visitCount"));

        String lastVisitStr = rs.getString("lastVisitDate");
        if (lastVisitStr != null && !lastVisitStr.isEmpty()) {
            try {
                c.setLastVisitDate(LocalDate.parse(lastVisitStr));
            } catch (Exception e) {
                // Ignore parse error
            }
        }

        c.setActive(rs.getBoolean("isActive"));
        c.setNotes(rs.getString("notes"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        c.setCreatedAt(createdTs != null ? createdTs.toLocalDateTime() : null);
        c.setUpdatedAt(updatedTs != null ? updatedTs.toLocalDateTime() : null);

        return c;
    }
}
