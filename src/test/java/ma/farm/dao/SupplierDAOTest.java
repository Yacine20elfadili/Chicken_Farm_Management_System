package ma.farm.dao;

import ma.farm.model.Supplier;
import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SupplierDAOTest {

    private static SupplierDAO supplierDAO;
    private static int testSupplierId;
    private static String testEmail;

    @BeforeAll
    static void setup() {
        supplierDAO = new SupplierDAO();
        testEmail = "test_supplier_" + System.currentTimeMillis() + "@test.com";
    }

    private Supplier createTestSupplier(String email, String category) {
        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier " + System.currentTimeMillis());
        supplier.setCompanyName("Test Supply Co.");
        supplier.setLegalForm("SARL");
        supplier.setCategory(category);
        supplier.setSubCategories("Grains, Seeds");
        supplier.setContactPerson("Alice Brown");
        supplier.setEmail(email);
        supplier.setPhone("0623456789");
        supplier.setAddress("456 Supply Ave");
        supplier.setIce("ICE123456789");
        supplier.setRc("RC987654");
        supplier.setWebsite("www.testsupplier.ma");
        supplier.setSecondaryContactName("Bob Green");
        supplier.setSecondaryContactPhone("0634567890");
        supplier.setSecondaryContactEmail("bob@testsupplier.ma");
        supplier.setBankName("Bank Al-Maghrib");
        supplier.setRib("123456789012345678901234");
        supplier.setSwift("BMCEMAMCXXX");
        supplier.setPaymentTerms("NET 30");
        supplier.setPreferredPaymentMethod("Bank Transfer");
        supplier.setMinOrderAmount(1000.0);
        supplier.setAvgDeliveryTime(7);
        supplier.setNotes("Reliable supplier");
        supplier.setActive(true);
        return supplier;
    }

    @Test
    @Order(1)
    void testAddSupplier() {
        Supplier supplier = createTestSupplier(testEmail, "Feed");
        boolean created = supplierDAO.addSupplier(supplier);
        assertTrue(created, "Supplier should be created successfully");
    }

    @Test
    @Order(2)
    void testGetAllSuppliers() {
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        assertNotNull(suppliers);
        assertFalse(suppliers.isEmpty());

        // Find our test supplier
        Supplier found = suppliers.stream()
                .filter(s -> testEmail.equals(s.getEmail()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "Test supplier should exist");
        testSupplierId = found.getId();
        assertTrue(testSupplierId > 0);
    }

    @Test
    @Order(3)
    void testGetActiveSuppliersByCategory() {
        List<Supplier> feedSuppliers = supplierDAO.getActiveSuppliersByCategory("Feed");
        assertNotNull(feedSuppliers);

        boolean foundInCategory = feedSuppliers.stream()
                .anyMatch(s -> s.getId() == testSupplierId);
        assertTrue(foundInCategory, "Test supplier should be in Feed category list");
    }

    @Test
    @Order(4)
    void testGetActiveSuppliersByCategoryIncludesMixed() {
        // Create a Mixed category supplier
        Supplier mixedSupplier = createTestSupplier("mixed_" + System.currentTimeMillis() + "@test.com", "Mixed");
        supplierDAO.addSupplier(mixedSupplier);

        List<Supplier> feedSuppliers = supplierDAO.getActiveSuppliersByCategory("Feed");

        // Should include "Mixed" suppliers
        boolean hasMixedSupplier = feedSuppliers.stream()
                .anyMatch(s -> "Mixed".equals(s.getCategory()));
        assertTrue(hasMixedSupplier, "Should include Mixed category suppliers");
    }

    @Test
    @Order(5)
    void testUpdateSupplier() {
        Supplier supplier = supplierDAO.getAllSuppliers().stream()
                .filter(s -> s.getId() == testSupplierId)
                .findFirst()
                .orElse(null);

        assertNotNull(supplier);

        supplier.setContactPerson("Updated Contact");
        supplier.setPhone("0699999999");
        supplier.setAvgDeliveryTime(5);

        boolean updated = supplierDAO.updateSupplier(supplier);
        assertTrue(updated, "Supplier should be updated");

        Supplier updatedSupplier = supplierDAO.getAllSuppliers().stream()
                .filter(s -> s.getId() == testSupplierId)
                .findFirst()
                .orElse(null);

        assertNotNull(updatedSupplier);
        assertEquals("Updated Contact", updatedSupplier.getContactPerson());
        assertEquals("0699999999", updatedSupplier.getPhone());
        assertEquals(5, updatedSupplier.getAvgDeliveryTime());
    }

    @Test
    @Order(6)
    void testSoftDeleteSupplier() {
        boolean deleted = supplierDAO.deleteSupplier(testSupplierId);
        assertTrue(deleted, "Supplier should be soft deleted");

        // Should still exist in getAllSuppliers but not in getActiveSuppliersByCategory
        List<Supplier> allSuppliers = supplierDAO.getAllSuppliers();
        Supplier deletedSupplier = allSuppliers.stream()
                .filter(s -> s.getId() == testSupplierId)
                .findFirst()
                .orElse(null);

        assertNotNull(deletedSupplier);
        assertFalse(deletedSupplier.isActive());

        List<Supplier> activeSuppliers = supplierDAO.getActiveSuppliersByCategory("Feed");
        boolean inActive = activeSuppliers.stream()
                .anyMatch(s -> s.getId() == testSupplierId);
        assertFalse(inActive, "Deleted supplier should not appear in active category list");
    }

    @Test
    @Order(7)
    void testRestoreSupplier() {
        boolean restored = supplierDAO.restoreSupplier(testSupplierId);
        assertTrue(restored, "Supplier should be restored");

        List<Supplier> allSuppliers = supplierDAO.getAllSuppliers();
        Supplier restoredSupplier = allSuppliers.stream()
                .filter(s -> s.getId() == testSupplierId)
                .findFirst()
                .orElse(null);

        assertNotNull(restoredSupplier);
        assertTrue(restoredSupplier.isActive());
    }

    @Test
    @Order(8)
    void testFinalCleanup() {
        // Soft delete test supplier
        supplierDAO.deleteSupplier(testSupplierId);
        assertTrue(true, "Cleanup completed");
    }
}
