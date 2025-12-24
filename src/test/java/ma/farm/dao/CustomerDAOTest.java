package ma.farm.dao;

import ma.farm.model.Customer;
import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerDAOTest {

    private static CustomerDAO customerDAO;
    private static int testCustomerId;
    private static String testEmail;

    @BeforeAll
    static void setup() {
        customerDAO = new CustomerDAO();
        testEmail = "test_customer_" + System.currentTimeMillis() + "@test.com";
    }

    private Customer createTestCustomer(String email) {
        Customer customer = new Customer();
        customer.setName("Test Customer " + System.currentTimeMillis());
        customer.setCompanyName("Test Company " + System.currentTimeMillis());
        customer.setLegalForm("SARL");
        customer.setType("Company");
        customer.setContactPerson("John Doe");
        customer.setEmail(email);
        customer.setPhone("0612345678");
        customer.setAddress("123 Test Street, Agadir, Morocco");
        customer.setPaymentTerms("Immediate");
        customer.setOutstandingBalance(0.0);
        customer.setTotalPurchases(0.0);
        customer.setVisitCount(0);
        customer.setActive(true);
        return customer;
    }

    @Test
    @Order(1)
    void testAddCustomer() {
        Customer customer = createTestCustomer(testEmail);
        boolean created = customerDAO.addCustomer(customer);
        assertTrue(created, "Customer should be created successfully");
    }

    @Test
    @Order(2)
    void testGetAllCustomers() {
        List<Customer> customers = customerDAO.getAllCustomers();
        assertNotNull(customers);
        assertFalse(customers.isEmpty());

        // Find our test customer
        Customer found = customers.stream()
                .filter(c -> testEmail.equals(c.getEmail()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "Test customer should exist");
        testCustomerId = found.getId();
        assertTrue(testCustomerId > 0);
    }

    @Test
    @Order(3)
    void testGetCustomerById() {
        Customer customer = customerDAO.getCustomerById(testCustomerId);
        assertNotNull(customer);
        assertEquals(testEmail, customer.getEmail());
        assertEquals("Test Company", customer.getCompanyName().substring(0, 12));
    }

    @Test
    @Order(4)
    void testGetActiveCustomers() {
        List<Customer> activeCustomers = customerDAO.getActiveCustomers();
        assertNotNull(activeCustomers);

        boolean foundActive = activeCustomers.stream()
                .anyMatch(c -> c.getId() == testCustomerId);
        assertTrue(foundActive, "Test customer should be in active customers list");
    }

    @Test
    @Order(5)
    void testGetCustomersByType() {
        List<Customer> companyCustomers = customerDAO.getCustomersByType("Company");
        assertNotNull(companyCustomers);

        boolean foundInType = companyCustomers.stream()
                .anyMatch(c -> c.getId() == testCustomerId);
        assertTrue(foundInType, "Test customer should be in Company type list");
    }

    @Test
    @Order(6)
    void testRecordVisit() {
        boolean recorded = customerDAO.recordVisit(testCustomerId, 500.0);
        assertTrue(recorded, "Visit should be recorded successfully");

        Customer updated = customerDAO.getCustomerById(testCustomerId);
        assertNotNull(updated.getLastVisitDate());
        assertEquals(1, updated.getVisitCount());
        assertEquals(500.0, updated.getTotalPurchases(), 0.01);
    }

    @Test
    @Order(7)
    void testUpdateOutstandingBalance() {
        // Add to balance (credit sale)
        boolean updated = customerDAO.updateOutstandingBalance(testCustomerId, 250.0);
        assertTrue(updated, "Balance should be updated");

        Customer customer = customerDAO.getCustomerById(testCustomerId);
        assertEquals(250.0, customer.getOutstandingBalance(), 0.01);

        // Subtract from balance (payment)
        customerDAO.updateOutstandingBalance(testCustomerId, -100.0);
        customer = customerDAO.getCustomerById(testCustomerId);
        assertEquals(150.0, customer.getOutstandingBalance(), 0.01);
    }

    @Test
    @Order(8)
    void testUpdateCustomer() {
        Customer customer = customerDAO.getCustomerById(testCustomerId);
        assertNotNull(customer);

        customer.setContactPerson("Jane Smith");
        customer.setPhone("0698765432");

        boolean updated = customerDAO.updateCustomer(customer);
        assertTrue(updated, "Customer should be updated");

        Customer updatedCustomer = customerDAO.getCustomerById(testCustomerId);
        assertEquals("Jane Smith", updatedCustomer.getContactPerson());
        assertEquals("0698765432", updatedCustomer.getPhone());
    }

    @Test
    @Order(9)
    void testSoftDeleteCustomer() {
        boolean deleted = customerDAO.deleteCustomer(testCustomerId);
        assertTrue(deleted, "Customer should be soft deleted");

        // Should still exist in getAllCustomers but not in getActiveCustomers
        Customer customer = customerDAO.getCustomerById(testCustomerId);
        assertNotNull(customer);
        assertFalse(customer.isActive());

        List<Customer> activeCustomers = customerDAO.getActiveCustomers();
        boolean inActive = activeCustomers.stream()
                .anyMatch(c -> c.getId() == testCustomerId);
        assertFalse(inActive, "Deleted customer should not appear in active list");
    }

    @Test
    @Order(10)
    void testRestoreCustomer() {
        boolean restored = customerDAO.restoreCustomer(testCustomerId);
        assertTrue(restored, "Customer should be restored");

        Customer customer = customerDAO.getCustomerById(testCustomerId);
        assertNotNull(customer);
        assertTrue(customer.isActive());
    }

    @Test
    @Order(11)
    void testFinalCleanup() {
        // Permanently delete test customer if needed
        // For now just soft delete again
        customerDAO.deleteCustomer(testCustomerId);
        assertTrue(true, "Cleanup completed");
    }
}
