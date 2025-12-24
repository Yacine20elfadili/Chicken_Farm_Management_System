package ma.farm.dao;

import ma.farm.model.FinancialTransaction;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancialDAOTest {

    private static FinancialDAO financialDAO;

    @BeforeAll
    static void setup() {
        financialDAO = new FinancialDAO();
    }

    private FinancialTransaction createTestTransaction(String type, String category, double amount) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setTransactionDate(LocalDate.now());
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setPaymentMethod("Cash");
        transaction.setDescription("Test " + type + " - " + category);
        transaction.setRelatedEntityType("Chickens");
        transaction.setRelatedEntityId(1);
        transaction.setReceiptImage(null);
        return transaction;
    }

    @Test
    @Order(1)
    void testAddIncomeTransaction() {
        FinancialTransaction income = createTestTransaction("Income", "Egg Sales", 5000.0);
        boolean created = financialDAO.addTransaction(income);
        assertTrue(created, "Income transaction should be created");
    }

    @Test
    @Order(2)
    void testAddExpenseTransaction() {
        FinancialTransaction expense = createTestTransaction("Expense", "Feed Purchase", 3000.0);
        boolean created = financialDAO.addTransaction(expense);
        assertTrue(created, "Expense transaction should be created");
    }

    @Test
    @Order(3)
    void testGetAllTransactions() {
        List<FinancialTransaction> transactions = financialDAO.getAllTransactions();
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());

        // Transactions should be ordered by date DESC (most recent first)
        if (transactions.size() > 1) {
            LocalDate first = transactions.get(0).getTransactionDate();
            LocalDate second = transactions.get(1).getTransactionDate();
            assertTrue(first.isAfter(second) || first.isEqual(second),
                    "Transactions should be ordered by date descending");
        }
    }

    @Test
    @Order(4)
    void testGetTransactionsByDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        List<FinancialTransaction> recentTransactions = financialDAO.getTransactionsByDateRange(weekAgo, today);
        assertNotNull(recentTransactions);

        // All transactions should be within the date range
        for (FinancialTransaction tx : recentTransactions) {
            LocalDate txDate = tx.getTransactionDate();
            assertTrue(
                    (txDate.isEqual(weekAgo) || txDate.isAfter(weekAgo)) &&
                            (txDate.isEqual(today) || txDate.isBefore(today)),
                    "Transaction date should be within range");
        }
    }

    @Test
    @Order(5)
    void testGetTransactionsByDateRangeOrdering() {
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusDays(30);

        List<FinancialTransaction> transactions = financialDAO.getTransactionsByDateRange(monthAgo, today);

        // Should be ordered by date DESC
        for (int i = 0; i < transactions.size() - 1; i++) {
            LocalDate current = transactions.get(i).getTransactionDate();
            LocalDate next = transactions.get(i + 1).getTransactionDate();
            assertTrue(current.isAfter(next) || current.isEqual(next),
                    "Transactions should be in descending date order");
        }
    }

    @Test
    @Order(6)
    void testAddMultipleTransactionTypes() {
        // Add various types
        financialDAO.addTransaction(createTestTransaction("Income", "Chicken Sales", 8000.0));
        financialDAO.addTransaction(createTestTransaction("Expense", "Medication Purchase", 500.0));
        financialDAO.addTransaction(createTestTransaction("Income", "Egg Sales", 1200.0));
        financialDAO.addTransaction(createTestTransaction("Expense", "Salaries", 4000.0));

        List<FinancialTransaction> all = financialDAO.getAllTransactions();

        // Verify we have income and expense transactions
        boolean hasIncome = all.stream().anyMatch(t -> "Income".equals(t.getType()));
        boolean hasExpense = all.stream().anyMatch(t -> "Expense".equals(t.getType()));

        assertTrue(hasIncome, "Should have income transactions");
        assertTrue(hasExpense, "Should have expense transactions");
    }

    @Test
    @Order(7)
    void testTransactionDataIntegrity() {
        FinancialTransaction testTx = createTestTransaction("Income", "Equipment Sale", 2500.0);
        testTx.setPaymentMethod("Bank Transfer");
        testTx.setDescription("Sold old equipment");
        testTx.setReceiptImage("/path/to/receipt.jpg");

        financialDAO.addTransaction(testTx);

        List<FinancialTransaction> allTx = financialDAO.getAllTransactions();
        FinancialTransaction found = allTx.stream()
                .filter(t -> "Equipment Sale".equals(t.getCategory()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "Transaction should be found");
        assertEquals("Bank Transfer", found.getPaymentMethod());
        assertEquals("Sold old equipment", found.getDescription());
        assertEquals(2500.0, found.getAmount(), 0.01);
        assertEquals("/path/to/receipt.jpg", found.getReceiptImage());
    }

    @Test
    @Order(8)
    void testEmptyDateRange() {
        LocalDate futureStart = LocalDate.now().plusDays(10);
        LocalDate futureEnd = LocalDate.now().plusDays(20);

        List<FinancialTransaction> futureTx = financialDAO.getTransactionsByDateRange(futureStart, futureEnd);
        assertNotNull(futureTx);
        assertTrue(futureTx.isEmpty(), "Future date range should return empty list");
    }
}
