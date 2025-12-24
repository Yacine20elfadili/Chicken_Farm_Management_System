package ma.farm.dao;

import ma.farm.model.FarmDocument;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentDAOTest {

    private static DocumentDAO documentDAO;
    private static int testDocumentId;

    @BeforeAll
    static void setup() {
        documentDAO = new DocumentDAO();
    }

    private FarmDocument createTestDocument(String type, String relatedEntityType) {
        FarmDocument doc = new FarmDocument();
        doc.setType(type);
        doc.setReferenceNumber("REF-" + System.currentTimeMillis());
        doc.setGeneratedDate(LocalDate.now());
        doc.setRelatedEntityType(relatedEntityType);
        doc.setRelatedEntityId(1);
        doc.setTotalAmount(5000.0);
        doc.setStatus("Generated");
        doc.setPdfContent("/documents/test_" + System.currentTimeMillis() + ".pdf");
        doc.setMetadata("{\"pages\": 3, \"size\": \"A4\"}");
        return doc;
    }

    @Test
    @Order(1)
    void testAddInvoiceDocument() {
        FarmDocument invoice = createTestDocument("Invoice", "Customer");
        boolean created = documentDAO.addDocument(invoice);
        assertTrue(created, "Invoice document should be created");
    }

    @Test
    @Order(2)
    void testAddContractDocument() {
        FarmDocument contract = createTestDocument("Contract", "Supplier");
        boolean created = documentDAO.addDocument(contract);
        assertTrue(created, "Contract document should be created");
    }

    @Test
    @Order(3)
    void testAddCertificateDocument() {
        FarmDocument certificate = createTestDocument("Certificate", "Farm");
        boolean created = documentDAO.addDocument(certificate);
        assertTrue(created, "Certificate document should be created");
    }

    @Test
    @Order(4)
    void testGetAllDocuments() {
        List<FarmDocument> documents = documentDAO.getAllDocuments();
        assertNotNull(documents);
        assertFalse(documents.isEmpty());

        // Documents should be ordered by generatedDate DESC
        if (documents.size() > 1) {
            LocalDate first = documents.get(0).getGeneratedDate();
            LocalDate second = documents.get(1).getGeneratedDate();
            assertTrue(first.isAfter(second) || first.isEqual(second),
                    "Documents should be ordered by date descending");
        }

        // Get our test document ID
        testDocumentId = documents.get(0).getId();
        assertTrue(testDocumentId > 0);
    }

    @Test
    @Order(5)
    void testDocumentDataIntegrity() {
        List<FarmDocument> documents = documentDAO.getAllDocuments();

        // Find one of our test documents
        FarmDocument found = documents.stream()
                .filter(d -> d.getReferenceNumber().startsWith("REF-"))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "Test document should exist");
        assertNotNull(found.getType());
        assertNotNull(found.getReferenceNumber());
        assertNotNull(found.getGeneratedDate());
        assertNotNull(found.getRelatedEntityType());
        assertTrue(found.getTotalAmount() > 0);
        assertEquals("Generated", found.getStatus());
        assertNotNull(found.getPdfContent());
        assertTrue(found.getPdfContent().contains(".pdf"));
    }

    @Test
    @Order(6)
    void testMultipleDocumentTypes() {
        List<FarmDocument> documents = documentDAO.getAllDocuments();

        boolean hasInvoice = documents.stream()
                .anyMatch(d -> "Invoice".equals(d.getType()));
        boolean hasContract = documents.stream()
                .anyMatch(d -> "Contract".equals(d.getType()));
        boolean hasCertificate = documents.stream()
                .anyMatch(d -> "Certificate".equals(d.getType()));

        assertTrue(hasInvoice, "Should have Invoice documents");
        assertTrue(hasContract, "Should have Contract documents");
        assertTrue(hasCertificate, "Should have Certificate documents");
    }

    @Test
    @Order(7)
    void testMultipleEntityTypes() {
        List<FarmDocument> documents = documentDAO.getAllDocuments();

        boolean hasCustomer = documents.stream()
                .anyMatch(d -> "Customer".equals(d.getRelatedEntityType()));
        boolean hasSupplier = documents.stream()
                .anyMatch(d -> "Supplier".equals(d.getRelatedEntityType()));
        boolean hasFarm = documents.stream()
                .anyMatch(d -> "Farm".equals(d.getRelatedEntityType()));

        assertTrue(hasCustomer, "Should have Customer-related documents");
        assertTrue(hasSupplier, "Should have Supplier-related documents");
        assertTrue(hasFarm, "Should have Farm-related documents");
    }

    @Test
    @Order(8)
    void testReferenceNumberUniqueness() {
        List<FarmDocument> documents = documentDAO.getAllDocuments();

        // Get all our test reference numbers
        List<String> refNumbers = documents.stream()
                .map(FarmDocument::getReferenceNumber)
                .filter(ref -> ref.startsWith("REF-"))
                .toList();

        // Check uniqueness
        long uniqueCount = refNumbers.stream().distinct().count();
        assertEquals(refNumbers.size(), uniqueCount,
                "All reference numbers should be unique");
    }

    @Test
    @Order(9)
    void testDocumentWithLargeAmount() {
        FarmDocument doc = createTestDocument("Invoice", "Customer");
        doc.setTotalAmount(999999.99);

        boolean created = documentDAO.addDocument(doc);
        assertTrue(created, "Document with large amount should be created");

        List<FarmDocument> documents = documentDAO.getAllDocuments();
        boolean hasLargeAmount = documents.stream()
                .anyMatch(d -> d.getTotalAmount() > 999000);
        assertTrue(hasLargeAmount, "Should have document with large amount");
    }

    @Test
    @Order(10)
    void testDocumentWithMetadata() {
        FarmDocument doc = createTestDocument("Report", "Production");
        doc.setMetadata("{\"pages\": 10, \"size\": \"A3\", \"color\": true}");

        documentDAO.addDocument(doc);

        List<FarmDocument> documents = documentDAO.getAllDocuments();
        FarmDocument found = documents.stream()
                .filter(d -> d.getMetadata() != null && d.getMetadata().contains("\"color\": true"))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "Document with metadata should exist");
        assertTrue(found.getMetadata().contains("pages"));
    }
}
