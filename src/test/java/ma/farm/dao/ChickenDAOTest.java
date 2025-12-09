package ma.farm.dao;

import ma.farm.model.Chicken;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChickenDAOTest {

    private static ChickenDAO chickenDAO;
    private static int insertedId; // ID du batch créé pour les tests

    @BeforeAll
    static void init() {
        // Utilise automatiquement base farm.db via DatabaseConnection
        chickenDAO = new ChickenDAO();
    }

    private Chicken sampleChicken() {
        Chicken c = new Chicken();
        c.setHouseId(99); // maison spéciale pour les tests
        c.setBatchNumber("TEST-BATCH-XYZ");
        c.setQuantity(50);
        c.setArrivalDate(LocalDate.of(2025, 1, 1));
        c.setAgeInDays(10);
        c.setGender("Male");
        c.setHealthStatus("Healthy");
        c.setAverageWeight(1.3);
        c.setNextTransferDate(LocalDate.of(2025, 2, 5));
        return c;
    }

    @Test
    @Order(1)
    void testCreateChickenBatch() {
        Chicken c = sampleChicken();
        boolean created = chickenDAO.createChickenBatch(c);

        Assertions.assertTrue(created);

        // Récupérer l’ID du batch en base
        Chicken found = chickenDAO.getAllChickens().stream()
                .filter(ch -> "TEST-BATCH-XYZ".equals(ch.getBatchNumber()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(found);

        insertedId = found.getId();
    }

    @Test
    @Order(2)
    void testGetChickenById() {
        Chicken found = chickenDAO.getChickenById(insertedId);
        Assertions.assertNotNull(found);
        Assertions.assertEquals("TEST-BATCH-XYZ", found.getBatchNumber());
    }

    @Test
    @Order(3)
    void testUpdateChickenBatch() {
        Chicken c = chickenDAO.getChickenById(insertedId);
        c.setQuantity(999);

        boolean updated = chickenDAO.updateChickenBatch(c);
        Assertions.assertTrue(updated);

        Chicken found = chickenDAO.getChickenById(insertedId);
        Assertions.assertEquals(999, found.getQuantity());
    }

    @Test
    @Order(4)
    void testFilterByBatch() {
        List<Chicken> list = chickenDAO.getChickensByBatch("TEST-BATCH-XYZ");
        Assertions.assertTrue(list.size() >= 1);
    }

    @Test
    @Order(5)
    void testFilterByHouse() {
        List<Chicken> list = chickenDAO.getChickensByHouse(99);
        Assertions.assertTrue(list.size() >= 1);
    }

    @Test
    @Order(6)
    void testChickensByHealthStatus() {
        List<Chicken> list = chickenDAO.getChickensByHealthStatus("Healthy");
        Assertions.assertTrue(list.size() >= 1);
    }

    @Test
    @Order(7)
    void testDeleteChickenBatch() {
        boolean deleted = chickenDAO.deleteChickenBatch(insertedId);
        Assertions.assertTrue(deleted);

        Chicken found = chickenDAO.getChickenById(insertedId);
        Assertions.assertNull(found);
    }
}
