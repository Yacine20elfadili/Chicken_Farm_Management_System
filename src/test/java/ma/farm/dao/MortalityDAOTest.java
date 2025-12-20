package ma.farm.dao;

import ma.farm.model.Mortality;
import ma.farm.dao.MortalityDAO.MortalityStatistics;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MortalityDAOTest {

    private static MortalityDAO mortalityDAO;
    private static int mortalityId;
    private static final int HOUSE_ID = 1;

    @BeforeAll
    static void setup() {
        mortalityDAO = new MortalityDAO();
    }

    @Test
    @Order(1)
    void testRecordMortality() {
        Mortality mortality = new Mortality();
        mortality.setHouseId(HOUSE_ID);
        mortality.setDeathDate(LocalDate.now());
        mortality.setCount(5);
        mortality.setCause("Test Disease");
        mortality.setSymptoms("Weakness");
        mortality.setIsOutbreak(true);
        mortality.setRecordedBy("JUnit");
        mortality.setNotes("Test record");

        mortalityId = mortalityDAO.recordMortality(mortality);

        assertTrue(mortalityId > 0);
    }

    @Test
    @Order(2)
    void testGetTodayDeaths() {
        List<Mortality> records = mortalityDAO.getTodayDeaths();
        assertNotNull(records);
        assertFalse(records.isEmpty());
    }

    @Test
    @Order(3)
    void testGetMortalityByDate() {
        List<Mortality> records =
                mortalityDAO.getMortalityByDate(LocalDate.now());

        assertFalse(records.isEmpty());
    }

    @Test
    @Order(4)
    void testGetTotalDeathsThisWeek() {
        int total = mortalityDAO.getTotalDeathsThisWeek();
        assertTrue(total >= 0);
    }

    @Test
    @Order(5)
    void testGetTotalDeathsThisMonth() {
        int total = mortalityDAO.getTotalDeathsThisMonth();
        assertTrue(total >= 0);
    }

    @Test
    @Order(6)
    void testGetTotalDeathsInHouse() {
        int total = mortalityDAO.getTotalDeathsInHouse(HOUSE_ID);
        assertTrue(total >= 0);
    }

    @Test
    @Order(7)
    void testGetMortalityByHouse() {
        List<Mortality> records =
                mortalityDAO.getMortalityByHouse(HOUSE_ID);

        assertNotNull(records);
        assertFalse(records.isEmpty());
    }

    @Test
    @Order(8)
    void testGetMortalityByHouseWithDateRange() {
        List<Mortality> records =
                mortalityDAO.getMortalityByHouse(
                        HOUSE_ID,
                        LocalDate.now().minusDays(7),
                        LocalDate.now()
                );

        assertNotNull(records);
    }

    @Test
    @Order(9)
    void testGetMortalityByDateRange() {
        List<Mortality> records =
                mortalityDAO.getMortalityByDateRange(
                        LocalDate.now().minusDays(7),
                        LocalDate.now()
                );

        assertNotNull(records);
    }

    @Test
    @Order(10)
    void testGetMortalityByCause() {
        List<Mortality> records =
                mortalityDAO.getMortalityByCause("Test Disease");

        assertNotNull(records);
        assertFalse(records.isEmpty());
    }

    @Test
    @Order(11)
    void testGetOutbreakRecords() {
        List<Mortality> records =
                mortalityDAO.getOutbreakRecords();

        assertNotNull(records);
    }

    @Test
    @Order(12)
    void testUpdateMortality() {
        List<Mortality> records =
                mortalityDAO.getMortalityByHouse(HOUSE_ID);

        Mortality mortality = records.get(0);
        mortality.setCount(8);
        mortality.setNotes("Updated by JUnit");

        boolean updated = mortalityDAO.updateMortality(mortality);
        assertTrue(updated);
    }

    @Test
    @Order(13)
    void testGetMortalityStatistics() {
        MortalityStatistics stats =
                mortalityDAO.getMortalityStatistics();

        assertNotNull(stats);
        assertTrue(stats.getTotalRecords() >= 0);
        assertTrue(stats.getTotalDeaths() >= 0);
    }

    @Test
    @Order(14)
    void testDeleteMortality() {
        boolean deleted =
                mortalityDAO.deleteMortality(mortalityId);

        assertTrue(deleted);
    }
}
