package ma.farm.dao;

import ma.farm.model.FinancialTransaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinancialDAO {
    private DatabaseConnection dbConnection;

    public FinancialDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public boolean addTransaction(FinancialTransaction tx) {
        String sql = "INSERT INTO financial_transactions (transactionDate, type, category, amount, paymentMethod, description, relatedEntityType, relatedEntityId, receiptImage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, tx.getTransactionDate().toString());
            stmt.setString(2, tx.getType());
            stmt.setString(3, tx.getCategory());
            stmt.setDouble(4, tx.getAmount());
            stmt.setString(5, tx.getPaymentMethod());
            stmt.setString(6, tx.getDescription());
            stmt.setString(7, tx.getRelatedEntityType());
            stmt.setInt(8, tx.getRelatedEntityId());
            stmt.setString(9, tx.getReceiptImage());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FinancialTransaction> getAllTransactions() {
        List<FinancialTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM financial_transactions ORDER BY transactionDate DESC";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<FinancialTransaction> getTransactionsByDateRange(LocalDate start, LocalDate end) {
        List<FinancialTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM financial_transactions WHERE transactionDate BETWEEN ? AND ? ORDER BY transactionDate DESC";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, start.toString());
            stmt.setString(2, end.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    private FinancialTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new FinancialTransaction(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("transactionDate")),
                rs.getString("type"),
                rs.getString("category"),
                rs.getDouble("amount"),
                rs.getString("paymentMethod"),
                rs.getString("description"),
                rs.getString("relatedEntityType"),
                rs.getInt("relatedEntityId"),
                rs.getString("receiptImage"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null);
    }
}
