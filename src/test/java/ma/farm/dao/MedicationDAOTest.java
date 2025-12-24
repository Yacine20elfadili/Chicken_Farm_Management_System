package ma.farm.dao;

import ma.farm.model.Medication;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MedicationDAOTest {

    private static MedicationDAO medicationDAO;
    private static int medicationId;

    @BeforeAll
    static void setup() {
        medicationDAO = new MedicationDAO();
    }

    @Test
    @Order(1)
    void testAddMedication() {
        Medication medication = new Medication();
        medication.setName("Antibiotic Test");
        medication.setType("Antibiotic");
        medication.setQuantity(100);
        medication.setUnit("ml");
        medication.setPricePerUnit(2.5);
        medication.setSupplier("Vet Supplier");
        medication.setPurchaseDate(LocalDate.now().minusDays(5));
        medication.setExpiryDate(LocalDate.now().plusDays(60));
        medication.setMinStockLevel(20);
        medication.setUsage("Treatment");

        boolean created = medicationDAO.addMedication(medication);
        assertTrue(created);
    }

    @Test
    @Order(2)
    void testGetAllMedications() {
        List<Medication> medications = medicationDAO.getAllMedications();
        assertNotNull(medications);
        assertFalse(medications.isEmpty());

        // Find our test medication by name instead of assuming it's the last one
        Medication testMed = medications.stream()
                .filter(m -> "Antibiotic Test".equals(m.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(testMed, "Test medication should exist");
        medicationId = testMed.getId();
        assertTrue(medicationId > 0);
    }

    @Test
    @Order(3)
    void testGetMedicationById() {
        Medication medication = medicationDAO.getMedicationById(medicationId);
        assertNotNull(medication);
        assertEquals("Antibiotic Test", medication.getName());
    }

    @Test
    @Order(4)
    void testGetMedicationByType() {
        List<Medication> medications = medicationDAO.getMedicationByType("Antibiotic");
        assertFalse(medications.isEmpty());
    }

    @Test
    @Order(5)
    void testGetLowStockMedications() {
        List<Medication> medications = medicationDAO.getLowStockMedications();
        assertNotNull(medications); // peut être vide
    }

    @Test
    @Order(6)
    void testGetExpiringMedications() {
        List<Medication> medications = medicationDAO.getExpiringMedications();
        assertNotNull(medications);
    }

    @Test
    @Order(7)
    void testGetExpiredMedications() {
        List<Medication> medications = medicationDAO.getExpiredMedications();
        assertNotNull(medications);
    }

    @Test
    @Order(8)
    void testUpdateMedication() {
        Medication medication = medicationDAO.getMedicationById(medicationId);
        assertNotNull(medication);

        medication.setQuantity(80);
        medication.setPricePerUnit(3.0);
        medication.setSupplier("Updated Supplier");

        boolean updated = medicationDAO.updateMedication(medication);
        assertTrue(updated);

        Medication updatedMedication = medicationDAO.getMedicationById(medicationId);
        assertEquals(80, updatedMedication.getQuantity());
    }

    @Test
    @Order(9)
    void testUpdateQuantity() {
        boolean updated = medicationDAO.updateQuantity(medicationId, 50);
        assertTrue(updated);

        Medication medication = medicationDAO.getMedicationById(medicationId);
        assertEquals(50, medication.getQuantity());
    }

    @Test
    @Order(10)
    void testGetTotalMedicationValue() {
        double value = medicationDAO.getTotalMedicationValue();
        assertTrue(value >= 0);
    }

    @Test
    @Order(11)
    void testGetLowStockMedicationCount() {
        int count = medicationDAO.getLowStockMedicationCount();
        assertTrue(count >= 0);
    }

    @Test
    @Order(12)
    void testDeleteMedication() {
        boolean deleted = medicationDAO.deleteMedication(medicationId);
        assertTrue(deleted);

        Medication medication = medicationDAO.getMedicationById(medicationId);
        assertNull(medication);
    }
}
