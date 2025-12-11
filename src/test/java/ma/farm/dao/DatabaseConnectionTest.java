package ma.farm.dao;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseConnectionTest {

    private static DatabaseConnection db;

    @BeforeAll
    static void init() {
        db = DatabaseConnection.getInstance();
    }

    @Test
    @Order(1)
    void testSingletonInstance() {
        DatabaseConnection db2 = DatabaseConnection.getInstance();
        assertSame(db, db2, "DatabaseConnection doit être singleton");
    }

    @Test
    @Order(2)
    void testDatabaseFileExists() {
        String path = DatabaseConnection.getDatabasePath();
        File dbFile = new File(path);

        assertTrue(dbFile.exists(), "Le fichier SQLite farm.db doit exister");
        assertTrue(dbFile.isFile(), "farm.db doit être un fichier SQLite");
    }

    @Test
    @Order(3)
    void testConnectionIsOpen() throws Exception {
        Connection conn = db.getConnection();
        assertNotNull(conn, "La connexion ne doit pas être null");
        assertFalse(conn.isClosed(), "La connexion ne doit pas être fermée");
    }

    @Test
    @Order(4)
    void testForeignKeysEnabled() throws Exception {
        try (Statement stmt = db.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys;");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "Les clés étrangères doivent être activées");
        }
    }

    @Test
    @Order(5)
    void testSchemaTablesExist() throws Exception {
        try (Statement stmt = db.getConnection().createStatement()) {

            // Vérifie la table users
            ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='users';"
            );

            assertTrue(rs.next(), "La table users doit exister (schema.sql doit être exécuté)");
        }
    }

    @Test
    @Order(6)
    void testSimpleQuery() throws Exception {
        try (Statement stmt = db.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM users");
            assertTrue(rs.next(), "La requête doit retourner un résultat");
            assertTrue(rs.getInt("count") >= 0, "Le nombre d'utilisateurs doit être >= 0");
        }
    }
}
