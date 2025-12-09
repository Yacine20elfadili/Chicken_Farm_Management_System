package ma.farm.dao;

import ma.farm.model.Mortality;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MortalityDAOTest {

    private static MortalityDAO mortalityDAO;
    private static int insertedId; // ID du record ajouté pour les tests

    @BeforeAll
    static void init() {
        mortalityDAO = new MortalityDAO();
    }

    private Mortality sampleMortality() {
        Mortality m = new Mortality();
        m.setHouseId(88); // Maison spéciale pour les tests
        m.setDeathDate(LocalDate.of(2025, 1, 10));
        m.setCount(7);
        m.setCause("Respiratory Infection");
        m.setSymptoms("Coughing, lethargy");
        m.setIsOutbreak(true);
        m.setRecordedBy("TestUser");
        m.setNotes("Sample mortality test");
        return m;
    }

    @Test
    @Order(1)
    void testRecordMortality() {
        Mortality m = sampleMortality();

        insertedId = mortalityDAO.recordMortality(m);

        Assertions.assertTrue(insertedId > 0, "L'ID retourné doit être > 0");
    }

    @Test
    @Order(2)
    void testGetMortalityByIdDate() {
        List<Mortality> list = mortalityDAO.getMortalityByDate(LocalDate.of(2025, 1, 10));

        Assertions.assertFalse(list.isEmpty());

        Mortality m = list.stream()
                .filter(x -> x.getId() == insertedId)
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(m);
        Assertions.assertEquals(7, m.getCount());
    }

    @Test
    @Order(3)
    void testUpdateMortality() {
        List<Mortality> list = mortalityDAO.getMortalityByDate(LocalDate.of(2025, 1, 10));
        Mortality m = list.stream()
                .filter(x -> x.getId() == insertedId)
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(m);

        m.setCount(20); // Mise à jour
        boolean updated = mortalityDAO.updateMortality(m);

        Assertions.assertTrue(updated);

        List<Mortality> updatedList = mortalityDAO.getMortalityByDate(LocalDate.of(2025, 1, 10));
        Mortality updatedMortality = updatedList.stream()
                .filter(x -> x.getId() == insertedId)
                .findFirst()
                .orElse(null);

        Assertions.assertEquals(20, updatedMortality.getCount());
    }

    @Test
    @Order(4)
    void testGetMortalityByHouse() {
        List<Mortality> list = mortalityDAO.getMortalityByHouse(88);
        Assertions.assertTrue(list.size() >= 1);
    }

    @Test
    @Order(5)
    void testGetMortalityByDateRange() {
        List<Mortality> list = mortalityDAO.getMortalityByDateRange(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        Assertions.assertTrue(list.size() >= 1);
    }

    @Test
    @Order(6)
    void testGetMortalityByCause() {
        List<Mortality> list = mortalityDAO.getMortalityByCause("Respiratory Infection");
        Assertions.assertTrue(list.size() >= 1);
    }

    @Test
    @Order(7)
    void testGetOutbreakRecords() {
        List<Mortality> list = mortalityDAO.getOutbreakRecords();
        Assertions.assertTrue(list.size() >= 1);
    }

    @Test
    @Order(8)
    void testGetStatistics() {
        MortalityDAO.MortalityStatistics stats = mortalityDAO.getMortalityStatistics();

        Assertions.assertTrue(stats.getTotalRecords() >= 1);
        Assertions.assertTrue(stats.getTotalDeaths() >= 1);
    }

    @Test
    @Order(9)
    void testDeleteMortality() {
        boolean deleted = mortalityDAO.deleteMortality(insertedId);
        Assertions.assertTrue(deleted);

        List<Mortality> list = mortalityDAO.getMortalityByDate(LocalDate.of(2025, 1, 10));

        boolean stillExists = list.stream().anyMatch(m -> m.getId() == insertedId);

        Assertions.assertFalse(stillExists, "Le record doit être supprimé");
    }
}
