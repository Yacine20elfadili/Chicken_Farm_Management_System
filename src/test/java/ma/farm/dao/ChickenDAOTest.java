package ma.farm.dao;

import ma.farm.model.Chicken;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChickenDAOTest {

    private static ChickenDAO chickenDAO;
    private static int testChickenId;
    private static int testHouseId;

    @BeforeAll
    public static void setup() {
        chickenDAO = new ChickenDAO();

        try (var stmt = DatabaseConnection.getInstance()
                .getConnection()
                .createStatement()) {

            // ✅ NETTOYAGE (IMPORTANT)
            stmt.executeUpdate("DELETE FROM chickens");
            stmt.executeUpdate("DELETE FROM houses");

            // ✅ CRÉATION D’UNE MAISON DE TEST
            stmt.executeUpdate("""
                INSERT INTO houses (name, type, chickenCount, capacity, healthStatus, creationDate)
                VALUES ('TEST-HOUSE', 'Broiler', 0, 1000, 'Good', DATE('now'))
            """);

            var rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                testHouseId = rs.getInt(1);
            }

        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    public void testCreateChickenBatch() {
        Chicken chicken = new Chicken();
        chicken.setHouseId(testHouseId);
        chicken.setBatchNumber("BATCH-TEST-001");
        chicken.setQuantity(50);
        chicken.setArrivalDate(LocalDate.now());
        chicken.setAgeInDays(10);
        chicken.setGender("Female");
        chicken.setHealthStatus("Good");
        chicken.setAverageWeight(1.2);
        chicken.setNextTransferDate(LocalDate.now().plusDays(30));

        assertTrue(chickenDAO.createChickenBatch(chicken));

        List<Chicken> chickens =
                chickenDAO.getChickensByBatch("BATCH-TEST-001");

        assertFalse(chickens.isEmpty());
        testChickenId = chickens.get(0).getId();
    }

    @Test
    @Order(2)
    public void testGetChickenById() {
        Chicken chicken = chickenDAO.getChickenById(testChickenId);
        assertNotNull(chicken);
    }

    @Test
    @Order(3)
    public void testGetChickensByHouse() {
        List<Chicken> chickens =
                chickenDAO.getChickensByHouse(testHouseId);
        assertFalse(chickens.isEmpty());
    }

    @Test
    @Order(4)
    public void testUpdateChickenBatch() {
        Chicken chicken = chickenDAO.getChickenById(testChickenId);
        assertNotNull(chicken);

        chicken.setQuantity(60);
        chicken.setHealthStatus("Fair");

        assertTrue(chickenDAO.updateChickenBatch(chicken));
    }

    @Test
    @Order(5)
    public void testGetTotalChickensInHouse() {
        int total =
                chickenDAO.getTotalChickensInHouse(testHouseId);
        assertTrue(total >= 60);
    }

    @Test
    @Order(6)
    public void testGetChickensByHealthStatus() {
        List<Chicken> chickens =
                chickenDAO.getChickensByHealthStatus("Fair");
        assertFalse(chickens.isEmpty());
    }

    @Test
    @Order(7)
    public void testDeleteChickenBatch() {
        assertTrue(chickenDAO.deleteChickenBatch(testChickenId));
        assertNull(chickenDAO.getChickenById(testChickenId));
    }
}
