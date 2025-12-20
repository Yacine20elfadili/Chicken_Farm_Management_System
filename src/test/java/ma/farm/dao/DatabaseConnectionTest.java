package ma.farm.dao;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseConnectionTest {

    private DatabaseConnection dbConnection;

    @BeforeAll
    public void setup() {
        dbConnection = DatabaseConnection.getInstance();
        assertNotNull(dbConnection, "L'instance de DatabaseConnection ne doit pas être nulle");
    }

    @Test
    public void testSingleton() {
        DatabaseConnection anotherInstance = DatabaseConnection.getInstance();
        assertSame(dbConnection, anotherInstance, "getInstance doit toujours retourner le même objet");
    }

    @Test
    public void testGetConnection() throws SQLException {
        Connection conn = dbConnection.getConnection();
        assertNotNull(conn, "La connexion ne doit pas être nulle");
        assertFalse(conn.isClosed(), "La connexion doit être ouverte");
    }

    @Test
    public void testDatabasePath() {
        String path = DatabaseConnection.getDatabasePath();
        assertNotNull(path, "Le chemin de la base ne doit pas être nul");
        assertTrue(path.endsWith("farm.db"), "Le chemin doit se terminer par farm.db");
    }

    @Test
    public void testCloseConnection() throws SQLException {
        Connection conn = dbConnection.getConnection();
        assertFalse(conn.isClosed(), "La connexion doit être ouverte avant fermeture");

        dbConnection.closeConnection();
        assertTrue(conn.isClosed(), "La connexion doit être fermée après closeConnection");
    }
}
