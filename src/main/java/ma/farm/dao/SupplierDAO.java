package ma.farm.dao;

import ma.farm.model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    private DatabaseConnection dbConnection;

    public SupplierDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, companyName, legalForm, category, subCategories, " +
                "contactPerson, email, phone, address, ice, rc, website, secondaryContactName, secondaryContactPhone, "
                +
                "secondaryContactEmail, bankName, rib, swift, paymentTerms, preferredPaymentMethod, minOrderAmount, " +
                "avgDeliveryTime, notes, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            setSupplierParams(stmt, supplier);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE suppliers SET name=?, companyName=?, legalForm=?, category=?, subCategories=?, " +
                "contactPerson=?, email=?, phone=?, address=?, ice=?, rc=?, website=?, secondaryContactName=?, " +
                "secondaryContactPhone=?, secondaryContactEmail=?, bankName=?, rib=?, swift=?, paymentTerms=?, " +
                "preferredPaymentMethod=?, minOrderAmount=?, avgDeliveryTime=?, notes=?, isActive=? WHERE id=?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            setSupplierParams(stmt, supplier);
            stmt.setInt(25, supplier.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setSupplierParams(PreparedStatement stmt, Supplier supplier) throws SQLException {
        stmt.setString(1, supplier.getName());
        stmt.setString(2, supplier.getCompanyName());
        stmt.setString(3, supplier.getLegalForm());
        stmt.setString(4, supplier.getCategory());
        stmt.setString(5, supplier.getSubCategories());
        stmt.setString(6, supplier.getContactPerson());
        stmt.setString(7, supplier.getEmail());
        stmt.setString(8, supplier.getPhone());
        stmt.setString(9, supplier.getAddress());
        stmt.setString(10, supplier.getIce());
        stmt.setString(11, supplier.getRc());
        stmt.setString(12, supplier.getWebsite());
        stmt.setString(13, supplier.getSecondaryContactName());
        stmt.setString(14, supplier.getSecondaryContactPhone());
        stmt.setString(15, supplier.getSecondaryContactEmail());
        stmt.setString(16, supplier.getBankName());
        stmt.setString(17, supplier.getRib());
        stmt.setString(18, supplier.getSwift());
        stmt.setString(19, supplier.getPaymentTerms());
        stmt.setString(20, supplier.getPreferredPaymentMethod());
        stmt.setDouble(21, supplier.getMinOrderAmount());
        stmt.setInt(22, supplier.getAvgDeliveryTime());
        stmt.setString(23, supplier.getNotes());
        stmt.setBoolean(24, supplier.isActive());
    }

    // Soft Delete
    public boolean deleteSupplier(int id) {
        String sql = "UPDATE suppliers SET isActive=0 WHERE id=?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreSupplier(int id) {
        String sql = "UPDATE suppliers SET isActive=1 WHERE id=?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public List<Supplier> getActiveSuppliersByCategory(String category) {
        List<Supplier> suppliers = new ArrayList<>();
        // category can be 'Feed', 'Medication', etc.
        // We also want to include 'Mixed' suppliers who might supply everything
        String sql = "SELECT * FROM suppliers WHERE isActive=1 AND (category = ? OR category = 'Mixed') ORDER BY name";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new Supplier(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("companyName"),
                rs.getString("legalForm"),
                rs.getString("category"),
                rs.getString("subCategories"),
                rs.getString("contactPerson"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("ice"),
                rs.getString("rc"),
                rs.getString("website"),
                rs.getString("secondaryContactName"),
                rs.getString("secondaryContactPhone"),
                rs.getString("secondaryContactEmail"),
                rs.getString("bankName"),
                rs.getString("rib"),
                rs.getString("swift"),
                rs.getString("paymentTerms"),
                rs.getString("preferredPaymentMethod"),
                rs.getDouble("minOrderAmount"),
                rs.getInt("avgDeliveryTime"),
                rs.getString("notes"),
                rs.getBoolean("isActive"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null);
    }
}
