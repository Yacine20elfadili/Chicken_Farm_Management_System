package ma.farm.dao;

import ma.farm.model.EggProduction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EggProductionDAOTest {

    private static EggProductionDAO eggProductionDAO;
    private static int testProductionId;
    private static int testHouseId;

    @BeforeAll
    static void setup() {
        eggProductionDAO = new EggProductionDAO();

        // ⚠️ IMPORTANT : créer une house de test (clé étrangère)
        String sql = """
            INSERT INTO houses (name, type, chickenCount, capacity, healthStatus, creationDate)
            VALUES ('TEST-HOUSE-EGG', 'Egg Layer', 500, 1000, 'Good', DATE('now'))
        """;

        try (var stmt = DatabaseConnection.getInstance()
                .getConnection()
                .createStatement()) {

            stmt.executeUpdate(sql);

            var rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                testHouseId = rs.getInt(1);
            }

        } catch (Exception e) {
            fail("House creation failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testAddProduction() {
        EggProduction production = new EggProduction(
                0,
                testHouseId,
                LocalDate.now(),
                100,
                5,
                95,
                1,
                "Worker-1",
                "JUnit test"
        );

        boolean created = eggProductionDAO.addProduction(production);
        assertTrue(created);
        assertTrue(production.getId() > 0);

        testProductionId = production.getId();
    }

    @Test
    @Order(2)
    void testGetProductionById() {
        EggProduction production =
                eggProductionDAO.getProductionById(testProductionId);

        assertNotNull(production);
        assertEquals(testHouseId, production.getHouseId());
        assertEquals(95, production.getGoodEggs());
    }

    @Test
    @Order(3)
    void testGetProductionByDate() {
        List<EggProduction> list =
                eggProductionDAO.getProductionByDate(LocalDate.now());

        assertFalse(list.isEmpty());
    }

    @Test
    @Order(4)
    void testGetProductionByHouse() {
        List<EggProduction> list =
                eggProductionDAO.getProductionByHouse(testHouseId);

        assertFalse(list.isEmpty());
    }

    @Test
    @Order(5)
    void testUpdateProduction() {
        EggProduction production =
                eggProductionDAO.getProductionById(testProductionId);

        assertNotNull(production);

        production.setEggsCollected(120);
        production.setCrackedEggs(10);
        production.setGoodEggs(110);
        production.setNotes("Updated by JUnit");
        production.setCollectedBy("Worker-2");

        boolean updated =
                eggProductionDAO.updateProduction(production);

        assertTrue(updated);

        EggProduction updatedProduction =
                eggProductionDAO.getProductionById(testProductionId);

        assertEquals(110, updatedProduction.getGoodEggs());
    }

    @Test
    @Order(6)
    void testGetEggsToday() {
        int eggsToday = eggProductionDAO.getEggsToday();
        assertTrue(eggsToday >= 0);
    }

    @Test
    @Order(7)
    void testGetTotalEggsByDateRange() {
        int total = eggProductionDAO.getTotalEggsByDateRange(
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        assertTrue(total >= 95);
    }

    @Test
    @Order(8)
    void testGetAverageEfficiencyByHouse() {
        double efficiency =
                eggProductionDAO.getAverageEfficiencyByHouse(testHouseId);

        assertTrue(efficiency >= 0.0);
    }

    @Test
    @Order(9)
    void testGetProductionCountByHouse() {
        int count =
                eggProductionDAO.getProductionCountByHouse(testHouseId);

        assertTrue(count > 0);
    }

    @Test
    @Order(10)
    void testGetAllProduction() {
        List<EggProduction> list =
                eggProductionDAO.getAllProduction();

        assertFalse(list.isEmpty());
    }

    @Test
    @Order(11)
    void testDeleteProduction() {
        boolean deleted =
                eggProductionDAO.deleteProduction(testProductionId);

        assertTrue(deleted);

        EggProduction production =
                eggProductionDAO.getProductionById(testProductionId);

        assertNull(production);
    }
}
