package ma.farm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Company;

public class CompanyDAO {

	private final DatabaseConnection dbConnection;

	public CompanyDAO() {
		this.dbConnection = DatabaseConnection.getInstance();
	}

	// Create company
	public boolean createCompany(Company company) {
		String sql = "INSERT INTO companies (companyName, patent, fiscalId, declarationNumber, businessField, workAddress, bankRIB, phoneNumber) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (
			Connection conn = dbConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql)
		) {
			stmt.setString(1, company.getCompanyName());
			stmt.setInt(2, company.getPatent());
			stmt.setInt(3, company.getFiscalId());
			stmt.setInt(4, company.getDeclarationNumber());
			stmt.setString(5, company.getBusinessField());
			stmt.setString(6, company.getWorkAddress());
			stmt.setString(7, company.getBankRIB());
			stmt.setString(8, company.getPhoneNumber());

			int rows = stmt.executeUpdate();
			if (rows == 0) return false;

			// SQLite: retrieve generated ID
			try (
				Statement idStmt = conn.createStatement();
				ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() AS id")
			) {
				if (rs.next()) {
					company.setId(rs.getInt("id"));
				}
			}
			return true;
		} catch (SQLException e) {
			System.err.println("Error creating company: " + e.getMessage());
			return false;
		}
	}

	// Get company by ID
	public Company getCompanyById(int id) {
		String sql = "SELECT * FROM companies WHERE id = ?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return mapResultSetToCompany(rs);
			}
		} catch (SQLException e) {
			System.err.println("Error getting company by ID: " + e.getMessage());
		}
		return null;
	}

	// Get company by patent
	public Company getCompanyByPatent(int patent) {
		String sql = "SELECT * FROM companies WHERE patent = ?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setInt(1, patent);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return mapResultSetToCompany(rs);
			}
		} catch (SQLException e) {
			System.err.println("Error getting company by patent: " + e.getMessage());
		}
		return null;
	}

	// Get company by fiscalId
	public Company getCompanyByFiscalId(int fiscalId) {
		String sql = "SELECT * FROM companies WHERE fiscalId = ?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setInt(1, fiscalId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return mapResultSetToCompany(rs);
			}
		} catch (SQLException e) {
			System.err.println("Error getting company by fiscalId: " + e.getMessage());
		}
		return null;
	}

	// Check if patent exists
	public boolean isPatentExists(int patent) {
		String sql = "SELECT COUNT(*) FROM companies WHERE patent = ?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setInt(1, patent);
			ResultSet rs = stmt.executeQuery();
			return rs.next() && rs.getInt(1) > 0;
		} catch (SQLException e) {
			System.err.println("Error checking patent existence: " + e.getMessage());
		}
		return false;
	}

	// Check if fiscalId exists
	public boolean isFiscalIdExists(int fiscalId) {
		String sql = "SELECT COUNT(*) FROM companies WHERE fiscalId = ?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setInt(1, fiscalId);
			ResultSet rs = stmt.executeQuery();
			return rs.next() && rs.getInt(1) > 0;
		} catch (SQLException e) {
			System.err.println("Error checking fiscalId existence: " + e.getMessage());
		}
		return false;
	}

	// Check if RIB exists
	public boolean isRIBExists(String rib) {
		String sql = "SELECT COUNT(*) FROM companies WHERE bankRIB = ?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setString(1, rib);
			ResultSet rs = stmt.executeQuery();
			return rs.next() && rs.getInt(1) > 0;
		} catch (SQLException e) {
			System.err.println("Error checking RIB existence: " + e.getMessage());
		}
		return false;
	}

	// Get all companies
	public List<Company> getAllCompanies() {
		List<Company> companies = new ArrayList<>();
		String sql = "SELECT * FROM companies";
		try (
			Statement stmt = dbConnection.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(sql)
		) {
			while (rs.next()) {
				companies.add(mapResultSetToCompany(rs));
			}
		} catch (SQLException e) {
			System.err.println("Error getting all companies: " + e.getMessage());
		}
		return companies;
	}

	// Update company
	public boolean updateCompany(Company company) {
		String sql = "UPDATE companies SET companyName=?, patent=?, fiscalId=?, declarationNumber=?, businessField=?, workAddress=?, bankRIB=?, phoneNumber=? WHERE id=?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setString(1, company.getCompanyName());
			stmt.setInt(2, company.getPatent());
			stmt.setInt(3, company.getFiscalId());
			stmt.setInt(4, company.getDeclarationNumber());
			stmt.setString(5, company.getBusinessField());
			stmt.setString(6, company.getWorkAddress());
			stmt.setString(7, company.getBankRIB());
			stmt.setString(8, company.getPhoneNumber());
			stmt.setInt(9, company.getId());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Error updating company: " + e.getMessage());
		}
		return false;
	}

	// Delete company
	public boolean deleteCompany(int id) {
		String sql = "DELETE FROM companies WHERE id = ?";
		try (
			PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)
		) {
			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Error deleting company: " + e.getMessage());
		}
		return false;
	}

	// Helper method to map ResultSet to Company object
	private Company mapResultSetToCompany(ResultSet rs) throws SQLException {
		return new Company(
			rs.getInt("id"),
			rs.getString("companyName"),
			rs.getInt("patent"),
			rs.getInt("fiscalId"),
			rs.getInt("declarationNumber"),
			rs.getString("businessField"),
			rs.getString("workAddress"),
			rs.getString("bankRIB"),
			rs.getString("phoneNumber"),
			rs.getTimestamp("createdAt").toLocalDateTime()
		);
	}
}
