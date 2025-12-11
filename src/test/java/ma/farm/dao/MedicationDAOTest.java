package ma.farm.dao;

import ma.farm.model.Medication;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MedicationDAOTest {

    private static MedicationDAO medicationDAO;
    private static int insertedId;

    @BeforeAll
    static void setup() {
        medicationDAO = new MedicationDAO();
    }

    @Test
    @Order(1)
    void testAddMedication() {
        Medication med = new Medication();
        med.setName("JUnit Test Med");
        med.setType("Supplement");
        med.setQuantity(50);
        med.setUnit("ml");
        med.setPricePerUnit(10.0);
        med.setSupplier("Test Supplier");
        med.setPurchaseDate(LocalDate.now());
        med.setExpiryDate(LocalDate.now().plusMonths(6));
        med.setMinStockLevel(10);
        med.setUsage("Test usage");

        boolean created = medicationDAO.addMedication(med);

        assertTrue(created, "The medication should be added successfully");
        assertTrue(med.getId() > 0, "The returned ID must be > 0");

        insertedId = med.getId();
    }

    @Test
    @Order(2)
    void testGetMedicationById() {
        Medication med = medicationDAO.getMedicationById(insertedId);

        assertNotNull(med, "Medication must exist");
        assertEquals("JUnit Test Med", med.getName());
    }

    @Test
    @Order(3)
    void testUpdateQuantity() {
        boolean updated = medicationDAO.updateQuantity(insertedId, 150);

        assertTrue(updated, "Quantity must be updated");

        Medication med = medicationDAO.getMedicationById(insertedId);
        assertEquals(150, med.getQuantity());
    }

    @Test
    @Order(4)
    void testUpdateMedication() {
        Medication med = medicationDAO.getMedicationById(insertedId);

        med.setName("Updated JUnit Med");
        med.setPricePerUnit(19.99);

        boolean updated = medicationDAO.updateMedication(med);

        assertTrue(updated, "Medication should be updated");

        Medication updatedMed = medicationDAO.getMedicationById(insertedId);

        assertEquals("Updated JUnit Med", updatedMed.getName());
        assertEquals(19.99, updatedMed.getPricePerUnit());
    }

    @Test
    @Order(5)
    void testGetAllMedications() {
        List<Medication> meds = medicationDAO.getAllMedications();

        assertNotNull(meds);
        assertTrue(meds.size() > 0, "There must be at least 1 medication in database");
    }

    @Test
    @Order(6)
    void testSearchByName() {
        List<Medication> results = medicationDAO.searchByName("JUnit");

        assertNotNull(results);
        assertTrue(results.size() > 0, "Search should return at least one match");
    }

    @Test
    @Order(7)
    void testLowStockCount() {
        int count = medicationDAO.getLowStockCount();

        assertTrue(count >= 0, "Low stock count must be >= 0");
    }

    @Test
    @Order(8)
    void testGetExpiredMedications() {
        List<Medication> expired = medicationDAO.getExpiredMedications();

        assertNotNull(expired);
    }

    @Test
    @Order(9)
    void testGetTotalMedicationValue() {
        double value = medicationDAO.getTotalMedicationValue();

        assertTrue(value >= 0, "Total value must be >= 0");
    }

    @Test
    @Order(10)
    void testDeleteMedication() {
        boolean deleted = medicationDAO.deleteMedication(insertedId);

        assertTrue(deleted, "Medication must be deleted");

        Medication med = medicationDAO.getMedicationById(insertedId);
        assertNull(med, "Medication must no longer exist");
    }
}
