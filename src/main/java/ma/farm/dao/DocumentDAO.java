package ma.farm.dao;

import ma.farm.model.FarmDocument;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    private DatabaseConnection dbConnection;

    public DocumentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public boolean addDocument(FarmDocument doc) {
        String sql = "INSERT INTO documents (type, referenceNumber, generatedDate, relatedEntityType, relatedEntityId, totalAmount, status, pdfContent, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, doc.getType());
            stmt.setString(2, doc.getReferenceNumber());
            stmt.setString(3, doc.getGeneratedDate().toString());
            stmt.setString(4, doc.getRelatedEntityType());
            stmt.setInt(5, doc.getRelatedEntityId());
            stmt.setDouble(6, doc.getTotalAmount());
            stmt.setString(7, doc.getStatus());
            stmt.setString(8, doc.getPdfContent());
            stmt.setString(9, doc.getMetadata());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FarmDocument> getAllDocuments() {
        List<FarmDocument> docs = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY generatedDate DESC";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                docs.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docs;
    }

    private FarmDocument mapResultSetToDocument(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new FarmDocument(
                rs.getInt("id"),
                rs.getString("type"),
                rs.getString("referenceNumber"),
                LocalDate.parse(rs.getString("generatedDate")),
                rs.getString("relatedEntityType"),
                rs.getInt("relatedEntityId"),
                rs.getDouble("totalAmount"),
                rs.getString("status"),
                rs.getString("pdfContent"),
                rs.getString("metadata"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null);
    }
}
