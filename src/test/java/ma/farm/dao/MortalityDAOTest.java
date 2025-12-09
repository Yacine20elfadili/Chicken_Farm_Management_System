package ma.farm.dao;

import ma.farm.model.Mortality;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MortalityDAOTest {

    private MortalityDAO mortalityDAO;
    private DatabaseConnection db;

    @BeforeAll
    static void initDatabase() {
        DatabaseConnection.getInstance(); // charge farm.db
    }

    @BeforeEach
    void setup() {
        db = DatabaseConnection.getInstance();
        mortalityDAO = new MortalityDAO();

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mortality (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    houseId INTEGER NOT NULL,
                    deathDate TEXT NOT NULL,
                    count INTEGER NOT NULL,
                    cause TEXT,
                    symptoms TEXT,
                    isOutbreak INTEGER,
                    recordedBy TEXT,
                    notes TEXT,
                    recorded_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
            """);

        } catch (SQLException e) {
            fail("Erreur creation table : " + e.getMessage());
        }
    }

    private Mortality createSample() {
        Mortality m = new Mortality();
        m.setHouseId(1);
        m.setDeathDate(LocalDate.now());
        m.setCount(5);
        m.setCause("Disease");
        m.setSymptoms("Weakness");
        m.setIsOutbreak(true);
        m.setRecordedBy("Tester");
        m.setNotes("JUnit test");
        return m;
    }

    @Test
    @Order(1)
    void testInsertMortality() {
        Mortality m = createSample();
        int id = mortalityDAO.recordMortality(m);

        assertTrue(id > 0, "L'insertion doit retourner un ID > 0");

        deleteById(id);
    }

    @Test
    @Order(2)
    void testGetByDate() {
        Mortality m = createSample();
        int id = mortalityDAO.recordMortality(m);

        List<Mortality> list = mortalityDAO.getMortalityByDate(LocalDate.now());
        assertFalse(list.isEmpty());

        deleteById(id);
    }

    @Test
    @Order(3)
    void testUpdateMortality() {
        Mortality m = createSample();
        int id = mortalityDAO.recordMortality(m);

        m.setId(id);
        m.setCount(20);
        m.setCause("Heat");
        m.setSymptoms("Panting");

        assertTrue(mortalityDAO.updateMortality(m));

        Mortality m2 = mortalityDAO.getMortalityByHouse(1)
                .stream()
                .filter(x -> x.getId() == id)
                .findFirst()
                .orElse(null);

        assertNotNull(m2);
        assertEquals(20, m2.getCount());
        assertEquals("Heat", m2.getCause());

        deleteById(id);
    }

    @Test
    @Order(4)
    void testDeleteMortality() {
        int id = mortalityDAO.recordMortality(createSample());

        assertTrue(mortalityDAO.deleteMortality(id));

        List<Mortality> list = mortalityDAO.getAllMortalityRecords();
        assertTrue(list.stream().noneMatch(m -> m.getId() == id));
    }

    @Test
    @Order(5)
    void testStatistics() {
        int id1 = mortalityDAO.recordMortality(createSample());
        int id2 = mortalityDAO.recordMortality(createSample());

        MortalityDAO.MortalityStatistics stats = mortalityDAO.getMortalityStatistics();

        assertTrue(stats.getTotalRecords() >= 2);
        assertTrue(stats.getTotalDeaths() >= 10);

        deleteById(id1);
        deleteById(id2);
    }

    /** Helper : delete safely 1 record */
    private void deleteById(int id) {
        try {
            mortalityDAO.deleteMortality(id);
        } catch (Exception ignored) {}
    }
}
