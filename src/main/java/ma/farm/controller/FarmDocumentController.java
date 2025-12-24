package ma.farm.controller;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ma.farm.dao.*;
import ma.farm.model.*;
import ma.farm.util.PDFGenerator;

import java.time.LocalDate;
import java.util.List;
import javafx.util.StringConverter;

/**
 * FarmDocumentController - Manages the Farm Documents page.
 * 
 * This page displays pre-defined document templates that cannot be
 * added/deleted.
 * Each document section has:
 * - Metadata: Fixed data from Settings/Suppliers/Customers/Personnel (read-only
 * here)
 * - Editable fields: Document-specific data (items, dates, notes)
 * 
 * PDFs are saved to:
 * C:\Users\elfad\Desktop\Chicken_Farm_Management_System\Documents\
 */
public class FarmDocumentController {

    // === BON DE COMMANDE ===
    @FXML
    private ToggleButton bcInitialBtn;
    @FXML
    private ToggleButton bcFinalBtn;
    @FXML
    private ToggleGroup bcTypeGroup;
    @FXML
    private ComboBox<Supplier> bcSupplierCombo;
    @FXML
    private TableView<OrderItem> bcItemsTable;
    @FXML
    private TableColumn<OrderItem, String> bcColRef;
    @FXML
    private TableColumn<OrderItem, String> bcColDesc;
    @FXML
    private TableColumn<OrderItem, Integer> bcColQty;
    @FXML
    private TableColumn<OrderItem, String> bcColUnit;
    @FXML
    private Button bcAddItemBtn;
    @FXML
    private Button bcRemoveItemBtn;
    @FXML
    private DatePicker bcDeliveryDate;
    @FXML
    private TextArea bcNotes;
    @FXML
    private Button bcGenerateBtn;

    // === DEVIS (To Customers Only) ===
    @FXML
    private ComboBox<Customer> devisPartyCombo;
    @FXML
    private DatePicker devisValidityDate;
    @FXML
    private TableView<InvoiceItem> devisItemsTable;
    @FXML
    private Button devisAddItemBtn;
    @FXML
    private Button devisRemoveItemBtn;
    @FXML
    private Label devisTotalHT;
    @FXML
    private Label devisTVA;
    @FXML
    private Label devisTotalTTC;
    @FXML
    private Button devisGenerateBtn;

    // === BON DE LIVRAISON ===
    @FXML
    private ComboBox<Customer> blPartyCombo;
    @FXML
    private TextField blOrderRef;
    @FXML
    private DatePicker blDate;
    @FXML
    private TableView<OrderItem> blItemsTable;
    @FXML
    private TableColumn<OrderItem, String> blColRef;
    @FXML
    private TableColumn<OrderItem, String> blColDesc;
    @FXML
    private TableColumn<OrderItem, Integer> blColQty;
    @FXML
    private TableColumn<OrderItem, String> blColUnit;
    @FXML
    private Button blAddItemBtn;
    @FXML
    private Button blRemoveItemBtn;
    @FXML
    private Button blGenerateBtn;

    // === FACTURE ===
    @FXML
    private ComboBox<Customer> factureCustomerCombo;
    @FXML
    private Button factureImportBtn;
    @FXML
    private TextField factureDevisRef;
    @FXML
    private TextField factureBLRef;
    @FXML
    private TableView<InvoiceItem> factureItemsTable;
    @FXML
    private Button factureAddItemBtn;
    @FXML
    private Button factureRemoveItemBtn;
    @FXML
    private Label factureTotalHT;
    @FXML
    private Label factureTVA;
    @FXML
    private Label factureTotalTTC;
    @FXML
    private Button factureGenerateBtn;

    // === AVOIR ===
    @FXML
    private ComboBox<Customer> avoirPartyCombo;
    @FXML
    private Button avoirImportBtn;
    @FXML
    private TextField avoirInvoiceRef;
    @FXML
    private TextField avoirReason;
    @FXML
    private TableView<InvoiceItem> avoirItemsTable;
    @FXML
    private TableColumn<InvoiceItem, String> avoirColRef;
    @FXML
    private TableColumn<InvoiceItem, String> avoirColDesc;
    @FXML
    private TableColumn<InvoiceItem, Integer> avoirColQty;
    @FXML
    private TableColumn<InvoiceItem, Double> avoirColPrice;
    @FXML
    private TableColumn<InvoiceItem, Double> avoirColTotal;
    @FXML
    private Button avoirAddItemBtn;
    @FXML
    private Button avoirRemoveItemBtn;
    @FXML
    private Label avoirTotalHT;
    @FXML
    private Label avoirTVA;
    @FXML
    private Label avoirTotalTTC;
    @FXML
    private Button avoirGenerateBtn;

    // === REÇU ===
    @FXML
    private TextField recuCustomerName;
    @FXML
    private TextField recuCustomerPhone;
    @FXML
    private TableView<InvoiceItem> recuItemsTable;
    @FXML
    private TableColumn<InvoiceItem, String> recuColDesc;
    @FXML
    private TableColumn<InvoiceItem, Integer> recuColQty;
    @FXML
    private TableColumn<InvoiceItem, Double> recuColPrice;
    @FXML
    private TableColumn<InvoiceItem, Double> recuColTotal;
    @FXML
    private Button recuAddItemBtn;
    @FXML
    private Button recuRemoveItemBtn;
    @FXML
    private ComboBox<String> recuPaymentMethod;
    @FXML
    private Label recuTotalTTC;
    @FXML
    private Button recuGenerateBtn;

    // === BULLETIN DE PAIE ===
    @FXML
    private ComboBox<Personnel> bulletinEmployeeCombo;
    @FXML
    private ComboBox<String> bulletinMonthCombo;
    @FXML
    private TextField bulletinYear;
    @FXML
    private Button bulletinGenerateBtn;

    // === CONTRAT DE TRAVAIL ===
    @FXML
    private ComboBox<Personnel> contratEmployeeCombo;
    @FXML
    private ComboBox<String> contratTypeCombo;
    @FXML
    private Button contratGenerateBtn;

    // === ATTESTATION ===
    @FXML
    private ComboBox<Personnel> attestationEmployeeCombo;
    @FXML
    private ComboBox<String> attestationReasonCombo;
    @FXML
    private Button attestationGenerateBtn;

    // DAOs
    private UserDAO userDAO;
    private SupplierDAO supplierDAO;
    private CustomerDAO customerDAO;
    private PersonnelDAO personnelDAO;
    private FinancialDAO financialDAO;

    // Company metadata (loaded once)
    private User companyInfo;

    // Data lists for tables
    private ObservableList<OrderItem> bcItems = FXCollections.observableArrayList();
    private ObservableList<InvoiceItem> devisItems = FXCollections.observableArrayList();
    private ObservableList<InvoiceItem> factureItems = FXCollections.observableArrayList();
    private ObservableList<InvoiceItem> recuItems = FXCollections.observableArrayList();
    private ObservableList<OrderItem> blItems = FXCollections.observableArrayList();
    private ObservableList<InvoiceItem> avoirItems = FXCollections.observableArrayList();

    // Document counters (in real app, these would come from DB)
    private int bcCounter = 1;
    private int devisCounter = 1;
    private int factureCounter = 1;
    private int recuCounter = 1;
    private int blCounter = 1;
    private int avoirCounter = 1;

    @FXML
    public void initialize() {
        System.out.println("FarmDocumentController initialized");

        // Initialize DAOs
        userDAO = new UserDAO();
        supplierDAO = new SupplierDAO();
        customerDAO = new CustomerDAO();
        personnelDAO = new PersonnelDAO();
        financialDAO = new FinancialDAO();

        // Load company metadata
        loadCompanyInfo();

        // Setup UI components
        setupBonCommande();
        setupDevis();
        setupBonLivraison();
        setupFacture();
        setupAvoir();
        setupRecu();
        setupHRDocuments();

        // Load Data into ComboBoxes
        loadComboBoxData();

        // Set up accordion handling or other initial UI states
        // if (commercialAccordion != null) {
        // commercialAccordion.setExpandedPane(bonCommandePane);
        // }

        // Ensure Documents directory exists
        PDFGenerator.ensureDirectoryExists();
    }

    private void loadComboBoxData() {
        // Suppliers
        if (bcSupplierCombo != null) {
            bcSupplierCombo.getItems().setAll(supplierDAO.getAllSuppliers());
        }

        // Customers
        List<Customer> customers = customerDAO.getAllCustomers();
        if (devisPartyCombo != null)
            devisPartyCombo.getItems().setAll(customers);
        if (blPartyCombo != null)
            blPartyCombo.getItems().setAll(customers);
        if (factureCustomerCombo != null)
            factureCustomerCombo.getItems().setAll(customers);
        if (avoirPartyCombo != null)
            avoirPartyCombo.getItems().setAll(customers);

        // Personnel
        List<Personnel> personnel = personnelDAO.getAllPersonnel();
        if (bulletinEmployeeCombo != null)
            bulletinEmployeeCombo.getItems().setAll(personnel);
        if (contratEmployeeCombo != null)
            contratEmployeeCombo.getItems().setAll(personnel);
        if (attestationEmployeeCombo != null)
            attestationEmployeeCombo.getItems().setAll(personnel);

        // Convertors are set in setup methods or we can ensure them here
    }

    private void loadCompanyInfo() {
        List<User> users = userDAO.getAllUsers();
        if (!users.isEmpty()) {
            companyInfo = users.get(0);
            System.out.println("Loaded company info: " + companyInfo.getCompanyName());
        } else {
            System.err.println("Warning: No company info found in database");
        }
    }

    // ==================== BON DE COMMANDE ====================

    // ==================== BON DE COMMANDE ====================

    private void setupBonCommande() {
        // Converter
        bcSupplierCombo.setConverter(createSupplierConverter());

        // Setup items table
        bcColRef.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReference()));
        bcColDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        bcColQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        bcColUnit.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnit()));
        bcItemsTable.setItems(bcItems);

        // Default delivery date (7 days from now)
        bcDeliveryDate.setValue(LocalDate.now().plusDays(7));

        // Add item button
        bcAddItemBtn.setOnAction(e -> {
            OrderItem item = promptForOrderItem();
            if (item != null)
                bcItems.add(item);
        });

        // Remove item button
        bcRemoveItemBtn.setOnAction(e -> {
            OrderItem selected = bcItemsTable.getSelectionModel().getSelectedItem();
            if (selected != null)
                bcItems.remove(selected);
        });

        // Generate button
        bcGenerateBtn.setOnAction(e -> generateBonCommande());
    }

    // ==================== DEVIS ====================

    private void setupDevis() {
        // Converter
        devisPartyCombo.setConverter(createCustomerConverter());

        // Validity date (30 days default)
        devisValidityDate.setValue(LocalDate.now().plusDays(30));

        // Setup items table
        devisItemsTable.setItems(devisItems);

        // Bind columns programmatically
        for (TableColumn<InvoiceItem, ?> col : devisItemsTable.getColumns()) {
            String header = col.getText();
            if (header == null)
                continue;

            if (header.startsWith("Réf") || header.equalsIgnoreCase("Ref")) {
                ((TableColumn<InvoiceItem, String>) col)
                        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReference()));
            } else if (header.startsWith("Desc")) {
                ((TableColumn<InvoiceItem, String>) col)
                        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
            } else if (header.startsWith("Qt") || header.startsWith("Quant")) {
                ((TableColumn<InvoiceItem, Integer>) col)
                        .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQuantity()).asObject());
            } else if (header.startsWith("Prix")) {
                ((TableColumn<InvoiceItem, Double>) col)
                        .setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getUnitPrice()).asObject());
            } else if (header.contains("TVA")) {
                ((TableColumn<InvoiceItem, Double>) col)
                        .setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTvaRate()).asObject());
            } else if (header.startsWith("Total")) {
                ((TableColumn<InvoiceItem, Double>) col).setCellValueFactory(
                        d -> new SimpleDoubleProperty(d.getValue().getQuantity() * d.getValue().getUnitPrice())
                                .asObject());
            }
        }

        devisAddItemBtn.setOnAction(e -> {
            InvoiceItem item = promptForInvoiceItem();
            if (item != null) {
                devisItems.add(item);
                updateDevisTotals();
            }
        });

        devisRemoveItemBtn.setOnAction(e -> {
            InvoiceItem selected = devisItemsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                devisItems.remove(selected);
                updateDevisTotals();
            }
        });

        devisGenerateBtn.setOnAction(e -> generateDevis());
    }

    // ==================== BON DE LIVRAISON ====================

    private void setupBonLivraison() {
        // Converter
        blPartyCombo.setConverter(createCustomerConverter());

        // Setup items table
        blColRef.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReference()));
        blColDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        blColQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        blColUnit.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnit()));
        blItemsTable.setItems(blItems);

        // Default date
        blDate.setValue(LocalDate.now());

        // Buttons
        blAddItemBtn.setOnAction(e -> {
            OrderItem item = promptForOrderItem();
            if (item != null)
                blItems.add(item);
        });

        blRemoveItemBtn.setOnAction(e -> {
            OrderItem selected = blItemsTable.getSelectionModel().getSelectedItem();
            if (selected != null)
                blItems.remove(selected);
        });

        blGenerateBtn.setOnAction(e -> generateBonLivraison());
    }

    // ==================== FACTURE ====================

    private void setupFacture() {
        // Converter
        factureCustomerCombo.setConverter(createCustomerConverter());

        if (factureImportBtn != null) {
            factureImportBtn.setOnAction(e -> {
                Customer c = factureCustomerCombo.getValue();
                if (c == null) {
                    showAlert("Attention", "Veuillez d'abord sélectionner un client.");
                    return;
                }
                handleImportTransaction(c);
            });
        }

        factureItemsTable.setItems(factureItems);

        // Bind columns programmatically
        for (TableColumn<InvoiceItem, ?> col : factureItemsTable.getColumns()) {
            String header = col.getText();
            if (header == null)
                continue;

            if (header.startsWith("Réf") || header.equalsIgnoreCase("Ref")) {
                ((TableColumn<InvoiceItem, String>) col)
                        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReference()));
            } else if (header.startsWith("Desc")) {
                ((TableColumn<InvoiceItem, String>) col)
                        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
            } else if (header.startsWith("Qt") || header.startsWith("Quant")) {
                ((TableColumn<InvoiceItem, Integer>) col)
                        .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQuantity()).asObject());
            } else if (header.startsWith("Prix")) {
                ((TableColumn<InvoiceItem, Double>) col)
                        .setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getUnitPrice()).asObject());
            } else if (header.contains("TVA")) {
                ((TableColumn<InvoiceItem, Double>) col)
                        .setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTvaRate()).asObject());
            } else if (header.startsWith("Total")) {
                ((TableColumn<InvoiceItem, Double>) col).setCellValueFactory(
                        d -> new SimpleDoubleProperty(d.getValue().getQuantity() * d.getValue().getUnitPrice())
                                .asObject());
            }
        }

        factureAddItemBtn.setOnAction(e -> {
            InvoiceItem item = promptForInvoiceItem();
            if (item != null) {
                factureItems.add(item);
                updateFactureTotals();
            }
        });

        factureRemoveItemBtn.setOnAction(e -> {
            InvoiceItem selected = factureItemsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                factureItems.remove(selected);
                updateFactureTotals();
            }
        });

        factureGenerateBtn.setOnAction(e -> generateFacture());
    }

    // ... skipped updateFactureTotals ...

    // ==================== AVOIR ====================
    // (Note: Avoir setup is further down, handled separately if needed, but we can
    // do method extraction here if in range)
    // The range is up to 1387. setupAvoir starts around 848 (in old view).
    // I can stick to prompt methods at end of file.

    // ==================== HELPER METHODS ====================

    private OrderItem promptForOrderItem() {
        // Simple Dialog
        Dialog<OrderItem> dialog = new Dialog<>();
        dialog.setTitle("Ajout Article");
        dialog.setHeaderText("Saisir les détails de l'article");

        ButtonType loginButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField ref = new TextField();
        ref.setPromptText("Ex: REF-001");
        TextField desc = new TextField();
        desc.setPromptText("Description produit");
        TextField qty = new TextField("1");
        TextField unit = new TextField("Unité");

        grid.add(new Label("Référence:"), 0, 0);
        grid.add(ref, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(desc, 1, 1);
        grid.add(new Label("Quantité:"), 0, 2);
        grid.add(qty, 1, 2);
        grid.add(new Label("Unité:"), 0, 3);
        grid.add(unit, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                int q = 1;
                try {
                    q = Integer.parseInt(qty.getText());
                } catch (Exception e) {
                }
                return new OrderItem(ref.getText(), desc.getText(), q, unit.getText());
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private InvoiceItem promptForInvoiceItem() {
        Dialog<InvoiceItem> dialog = new Dialog<>();
        dialog.setTitle("Ajout Article");
        dialog.setHeaderText("Saisir les détails de l'article");

        ButtonType loginButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField ref = new TextField();
        ref.setPromptText("Ref");
        TextField desc = new TextField();
        desc.setPromptText("Description");
        TextField qty = new TextField("1");
        TextField price = new TextField("0.00");
        TextField tva = new TextField("20");

        grid.add(new Label("Réf:"), 0, 0);
        grid.add(ref, 1, 0);
        grid.add(new Label("Desc:"), 0, 1);
        grid.add(desc, 1, 1);
        grid.add(new Label("Qté:"), 0, 2);
        grid.add(qty, 1, 2);
        grid.add(new Label("Prix HT:"), 0, 3);
        grid.add(price, 1, 3);
        grid.add(new Label("TVA %:"), 0, 4);
        grid.add(tva, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                int q = 1;
                double p = 0;
                double t = 20;
                try {
                    q = Integer.parseInt(qty.getText());
                } catch (Exception e) {
                }
                try {
                    p = Double.parseDouble(price.getText());
                } catch (Exception e) {
                }
                try {
                    t = Double.parseDouble(tva.getText());
                } catch (Exception e) {
                }
                return new InvoiceItem(ref.getText(), desc.getText(), q, p, t);
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private StringConverter<Supplier> createSupplierConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Supplier s) {
                return s != null ? s.getName() : "";
            }

            @Override
            public Supplier fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<Customer> createCustomerConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Customer c) {
                return c != null ? c.getName() + " (" + (c.getIce() != null ? "Entr." : "Part.") + ")" : "";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        };
    }

    private void generateBonCommande() {
        Supplier supplier = bcSupplierCombo.getValue();
        if (supplier == null) {
            showAlert("Erreur", "Veuillez sélectionner un fournisseur.");
            return;
        }
        if (bcItems.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un article.");
            return;
        }

        try {
            String type = bcInitialBtn.isSelected() ? "INITIAL" : "FINAL";
            String docNumber = PDFGenerator.generateDocNumber("BC", bcCounter++);
            String filename = docNumber + ".pdf";

            Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc, "BON DE COMMANDE " + type, docNumber, LocalDate.now());

            // Issuer (Farm)
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "ÉMETTEUR (Votre Ferme)",
                        companyInfo.getCompanyName(),
                        companyInfo.getIce(),
                        companyInfo.getRc(),
                        companyInfo.getAddress(),
                        companyInfo.getPhone(),
                        companyInfo.getEmail());
            }

            // Supplier
            PDFGenerator.addPartyInfo(doc, "FOURNISSEUR",
                    supplier.getName(),
                    supplier.getIce(),
                    supplier.getRc(),
                    supplier.getAddress(),
                    supplier.getPhone(),
                    supplier.getEmail());

            // Items table
            String[] headers = { "Réf", "Description", "Quantité", "Unité" };
            float[] widths = { 1, 4, 1, 1 };
            Table itemsTable = PDFGenerator.createItemsTable(headers, widths);
            for (OrderItem item : bcItems) {
                PDFGenerator.addTableRow(itemsTable, new String[] {
                        item.getReference(),
                        item.getDescription(),
                        String.valueOf(item.getQuantity()),
                        item.getUnit()
                });
            }
            doc.add(itemsTable);

            // Notes
            if (bcNotes.getText() != null && !bcNotes.getText().isEmpty()) {
                doc.add(new com.itextpdf.layout.element.Paragraph("\nNotes: " + bcNotes.getText())
                        .setFontSize(10));
            }

            // Delivery date
            if (bcDeliveryDate.getValue() != null) {
                doc.add(new com.itextpdf.layout.element.Paragraph(
                        "Date de livraison souhaitée: " + bcDeliveryDate.getValue())
                        .setFontSize(10));
            }

            // Signature
            PDFGenerator.addSignatureSection(doc, "L'Administration", "Le Fournisseur");

            // Footer
            PDFGenerator.addFooter(doc, "Ce document est un bon de commande " + type.toLowerCase()
                    + ". Merci de nous envoyer votre devis.");

            doc.close();

            showAlert("Succès", "Bon de Commande généré !\n\nFichier: " + filename + "\n\nChemin complet: "
                    + PDFGenerator.DOCUMENTS_DIR);

            // Try to open the file folder
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
                // Ignore if we can't open folder
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Échec de génération: " + ex.getMessage());
        }
    }

    // ==================== DEVIS ====================

    private void generateDevis() {
        Customer customer = devisPartyCombo.getValue();
        if (customer == null) {
            showAlert("Erreur", "Veuillez sélectionner un client.");
            return;
        }
        if (devisItems.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un article.");
            return;
        }

        try {
            String docNumber = PDFGenerator.generateDocNumber("DEV", devisCounter++);
            String filename = docNumber + ".pdf";

            Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc, "DEVIS (Valable jusqu'au " +
                    (devisValidityDate.getValue() != null ? devisValidityDate.getValue() : "N/A") + ")",
                    docNumber, LocalDate.now());

            // Issuer (Farm)
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "ÉMETTEUR",
                        companyInfo.getCompanyName(),
                        companyInfo.getIce(),
                        companyInfo.getRc(),
                        companyInfo.getAddress(),
                        companyInfo.getPhone(),
                        companyInfo.getEmail());
            }

            // Client
            PDFGenerator.addPartyInfo(doc, "CLIENT",
                    customer.getName(),
                    customer.getIce(),
                    null,
                    customer.getAddress(),
                    customer.getPhone(),
                    customer.getEmail());

            // Items table
            String[] headers = { "Réf", "Description", "Qté", "Prix HT", "Total HT" };
            float[] widths = { 1, 3, 0.7f, 1, 1 };
            Table itemsTable = PDFGenerator.createItemsTable(headers, widths);

            double totalHT = 0;
            for (InvoiceItem item : devisItems) {
                double lineTotal = item.getQuantity() * item.getUnitPrice();
                totalHT += lineTotal;
                PDFGenerator.addTableRow(itemsTable, new String[] {
                        item.getReference(),
                        item.getDescription(),
                        String.valueOf(item.getQuantity()),
                        String.format("%.2f", item.getUnitPrice()),
                        String.format("%.2f", lineTotal)
                });
            }
            doc.add(itemsTable);

            // Financial summary
            double tvaAmount = totalHT * 0.20;
            double totalTTC = totalHT + tvaAmount;
            PDFGenerator.addFinancialSummary(doc, totalHT, 20.0, totalTTC);

            // Terms
            doc.add(new com.itextpdf.layout.element.Paragraph("\nConditions:")
                    .setFontSize(10).setMarginTop(10));
            doc.add(new com.itextpdf.layout.element.Paragraph(
                    "Ce devis est valable 30 jours. Pour valider, merci de retourner ce document signé avec la mention 'Bon pour accord'.")
                    .setFontSize(9));

            // Signature
            PDFGenerator.addSignatureSection(doc, "L'Émetteur", "Le Client (Bon pour accord)");

            // Footer
            PDFGenerator.addFooter(doc, "Merci de votre confiance.");

            doc.close();

            showAlert("Succès", "Devis généré !\n\nFichier: " + filename + "\n\nChemin: " + PDFGenerator.DOCUMENTS_DIR);

            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Échec de génération: " + ex.getMessage());
        }
    }

    private void updateDevisTotals() {
        double totalHT = devisItems.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice()).sum();
        double tva = totalHT * 0.20;
        double ttc = totalHT + tva;
        devisTotalHT.setText(String.format("Total HT: %.2f DH", totalHT));
        devisTVA.setText(String.format("TVA (20%%): %.2f DH", tva));
        devisTotalTTC.setText(String.format("Total TTC: %.2f DH", ttc));
    }

    // ==================== BON DE LIVRAISON ====================

    private void generateBonLivraison() {
        Customer customer = blPartyCombo.getValue();
        if (customer == null) {
            showAlert("Erreur", "Veuillez sélectionner un client.");
            return;
        }
        if (blItems.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un article.");
            return;
        }

        try {
            String docNumber = PDFGenerator.generateDocNumber("BL", blCounter++);
            String filename = docNumber + ".pdf";
            Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc, "BON DE LIVRAISON " +
                    (blOrderRef.getText().isEmpty() ? "" : "(Réf: " + blOrderRef.getText() + ")"),
                    docNumber, blDate.getValue() != null ? blDate.getValue() : LocalDate.now());

            // Sender (Farm)
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "EXPÉDITEUR",
                        companyInfo.getCompanyName(), companyInfo.getIce(), companyInfo.getRc(),
                        companyInfo.getAddress(), companyInfo.getPhone(), companyInfo.getEmail());
            }

            // Receiver (Customer)
            PDFGenerator.addPartyInfo(doc, "DESTINATAIRE",
                    customer.getName(), customer.getIce(), null,
                    customer.getAddress(), customer.getPhone(), customer.getEmail());

            // Items Table
            String[] headers = { "Réf", "Description", "Quantité", "Unité" };
            float[] widths = { 1, 4, 1, 1 };
            Table itemsTable = PDFGenerator.createItemsTable(headers, widths);
            for (OrderItem item : blItems) {
                PDFGenerator.addTableRow(itemsTable, new String[] {
                        item.getReference(), item.getDescription(),
                        String.valueOf(item.getQuantity()), item.getUnit()
                });
            }
            doc.add(itemsTable);

            // Footer / Signature
            doc.add(new com.itextpdf.layout.element.Paragraph("\nCe bon de livraison ne constitue pas une facture.")
                    .setFontSize(10).setMarginTop(10));

            PDFGenerator.addSignatureSection(doc, "Le Livreur", "Le Client (Reçu le: ______)");

            doc.close();
            showAlert("Succès", "Bon de Livraison généré !\n" + filename);
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Echec BL: " + e.getMessage());
        }
    }

    // ==================== FACTURE ====================

    private void updateFactureTotals() {
        double totalHT = factureItems.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice()).sum();
        double tva = totalHT * 0.20; // Simplified - should use per-item TVA rates
        double ttc = totalHT + tva;
        factureTotalHT.setText(String.format("Total HT: %.2f DH", totalHT));
        factureTVA.setText(String.format("TVA: %.2f DH", tva));
        factureTotalTTC.setText(String.format("Total TTC: %.2f DH", ttc));
    }

    private void generateFacture() {
        Customer customer = factureCustomerCombo.getValue();
        if (customer == null) {
            showAlert("Erreur", "Veuillez sélectionner un client.");
            return;
        }
        if (customer.getIce() == null || customer.getIce().isEmpty()) {
            showAlert("ATTENTION - ICE OBLIGATOIRE",
                    "Le client '" + customer.getName() + "' n'a pas d'ICE enregistré.\n" +
                            "En vertu de la loi marocaine, les factures DOIVENT inclure l'ICE des deux parties.\n" +
                            "Veuillez mettre à jour les informations du client dans la page Clients.");
            return;
        }
        if (factureItems.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un article.");
            return;
        }

        try {
            String docNumber = PDFGenerator.generateDocNumber("FACT", factureCounter++);
            String filename = docNumber + ".pdf";

            Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc, "FACTURE", docNumber, LocalDate.now());

            // Seller (Farm) - Must include ICE
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "VENDEUR",
                        companyInfo.getCompanyName(),
                        companyInfo.getIce(),
                        companyInfo.getRc(),
                        companyInfo.getAddress(),
                        companyInfo.getPhone(),
                        companyInfo.getEmail());
            }

            // Buyer - Must include ICE
            PDFGenerator.addPartyInfo(doc, "ACHETEUR",
                    customer.getName(),
                    customer.getIce(),
                    null, // RC
                    customer.getAddress(),
                    customer.getPhone(),
                    customer.getEmail());

            // Items table
            String[] headers = { "Réf", "Description", "Qté", "Prix HT", "TVA%", "Total HT" };
            float[] widths = { 1, 3, 0.7f, 1, 0.7f, 1 };
            Table itemsTable = PDFGenerator.createItemsTable(headers, widths);

            double totalHT = 0;
            for (InvoiceItem item : factureItems) {
                double lineTotal = item.getQuantity() * item.getUnitPrice();
                totalHT += lineTotal;
                PDFGenerator.addTableRow(itemsTable, new String[] {
                        item.getReference(),
                        item.getDescription(),
                        String.valueOf(item.getQuantity()),
                        String.format("%.2f", item.getUnitPrice()),
                        String.format("%.0f%%", item.getTvaRate()),
                        String.format("%.2f", lineTotal)
                });
            }
            doc.add(itemsTable);

            // Financial summary
            double tvaAmount = totalHT * 0.20;
            double totalTTC = totalHT + tvaAmount;
            PDFGenerator.addFinancialSummary(doc, totalHT, 20.0, totalTTC);

            // Payment terms
            doc.add(new com.itextpdf.layout.element.Paragraph("\nConditions de paiement: Net 30 jours")
                    .setFontSize(10).setMarginTop(10));
            doc.add(new com.itextpdf.layout.element.Paragraph(
                    "En cas de retard, des pénalités de 1.5% par mois seront appliquées.")
                    .setFontSize(8).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));

            // Signature
            PDFGenerator.addSignatureSection(doc, "Le Vendeur", "L'Acheteur");

            // Footer with legal notice
            PDFGenerator.addFooter(doc,
                    "Facture établie le " + LocalDate.now() +
                            " - ICE Vendeur: " + (companyInfo != null ? companyInfo.getIce() : "N/A") +
                            " - ICE Acheteur: " + customer.getIce());

            doc.close();

            // === INTEGRATION WITH FINANCIAL REPORTS ===
            if (financialDAO != null) {
                try {
                    FinancialTransaction tx = new FinancialTransaction(
                            0,
                            LocalDate.now(),
                            "Income",
                            "Vente (Facture: " + docNumber + ")",
                            totalTTC,
                            "Virement/Chèque",
                            "Facture pour " + customer.getName(),
                            "Customer",
                            customer.getId(),
                            null);
                    financialDAO.addTransaction(tx);
                } catch (Exception e) {
                    System.err.println("Failed to record tx: " + e.getMessage());
                }
            }

            showAlert("Facture Générée", "Fichier: " + filename + "\n\nChemin: " + PDFGenerator.DOCUMENTS_DIR);

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Échec de génération: " + ex.getMessage());
        }
    }

    private void handleImportTransaction(Customer c) {
        // Fetch Income transactions for this customer
        List<FinancialTransaction> allTx = financialDAO.getAllTransactions();

        List<FinancialTransaction> customerIncome = allTx.stream()
                .filter(t -> "Income".equalsIgnoreCase(t.getType())
                        && "Customer".equalsIgnoreCase(t.getRelatedEntityType())
                        && t.getRelatedEntityId() == c.getId())
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate())) // Newest first
                .toList();

        if (customerIncome.isEmpty()) {
            showAlert("Info", "Aucune transaction de vente trouvée pour ce client.");
            return;
        }

        // Show Dialog
        ChoiceDialog<FinancialTransaction> dialog = new ChoiceDialog<>(customerIncome.get(0), customerIncome);
        dialog.setTitle("Importer Transaction");
        dialog.setHeaderText("Sélectionnez une vente récente à importer :");
        dialog.setContentText("Transaction :");

        ((ComboBox<FinancialTransaction>) dialog.getDialogPane().lookup(".combo-box"))
                .setConverter(new StringConverter<>() {
                    @Override
                    public String toString(FinancialTransaction t) {
                        return t.getTransactionDate() + " - " + t.getDescription() + " ("
                                + String.format("%.2f DH", t.getAmount()) + ")";
                    }

                    @Override
                    public FinancialTransaction fromString(String s) {
                        return null;
                    }
                });

        dialog.showAndWait().ifPresent(tx -> {
            String ref = "TX-" + tx.getId();
            String desc = tx.getDescription();
            int qty = 1;
            double price = tx.getAmount();

            // Attempt simple parsing
            try {
                String[] parts = desc.split(" ");
                if (parts.length >= 2 && parts[0].equalsIgnoreCase("Vente")) {
                    try {
                        int parsedQty = Integer.parseInt(parts[1]);
                        if (parsedQty > 0) {
                            qty = parsedQty;
                            price = tx.getAmount() / qty;
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
            } catch (Exception e) {
            }

            InvoiceItem item = new InvoiceItem(ref, desc, qty, price, 20.0);
            factureItems.add(item);
            updateFactureTotals();

            showAlert("Succès", "Transaction importée !");
        });
    }

    // ==================== AVOIR ====================

    private void setupAvoir() {
        // Converter
        avoirPartyCombo.setConverter(createCustomerConverter());

        if (avoirPartyCombo != null) {
            avoirPartyCombo.setOnAction(e -> {
                // Logic if needed
            });
        }

        if (avoirImportBtn != null) {
            avoirImportBtn.setOnAction(e -> {
                Customer c = avoirPartyCombo.getValue();
                if (c == null) {
                    showAlert("Attention", "Veuillez d'abord sélectionner un client.");
                    return;
                }
                handleImportTransactionForAvoir(c);
            });
        }

        avoirItemsTable.setItems(avoirItems);
        avoirColRef.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReference()));
        avoirColDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        avoirColQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQuantity()).asObject());
        avoirColPrice.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getUnitPrice()).asObject());
        avoirColTotal.setCellValueFactory(cell -> {
            InvoiceItem item = cell.getValue();
            double total = item.getQuantity() * item.getUnitPrice();
            return new SimpleDoubleProperty(total).asObject();
        });

        if (avoirAddItemBtn != null) {
            avoirAddItemBtn.setOnAction(e -> {
                InvoiceItem item = promptForInvoiceItem();
                if (item != null) {
                    avoirItems.add(item);
                    updateAvoirTotals();
                }
            });
        }

        if (avoirRemoveItemBtn != null) {
            avoirRemoveItemBtn.setOnAction(e -> {
                InvoiceItem selected = avoirItemsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    avoirItems.remove(selected);
                    updateAvoirTotals();
                }
            });
        }

        if (avoirGenerateBtn != null) {
            avoirGenerateBtn.setOnAction(e -> generateAvoir());
        }
    }

    private void handleImportTransactionForAvoir(Customer c) {
        // Similar to handleImportTransaction but could set invoice reference
        // automatically
        List<FinancialTransaction> allTx = financialDAO.getAllTransactions();

        List<FinancialTransaction> customerIncome = allTx.stream()
                .filter(t -> "Income".equalsIgnoreCase(t.getType())
                        && "Customer".equalsIgnoreCase(t.getRelatedEntityType())
                        && t.getRelatedEntityId() == c.getId())
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList();

        if (customerIncome.isEmpty()) {
            showAlert("Info", "Aucune transaction trouvée pour ce client.");
            return;
        }

        ChoiceDialog<FinancialTransaction> dialog = new ChoiceDialog<>(customerIncome.get(0), customerIncome);
        dialog.setTitle("Importer pour Avoir");
        dialog.setHeaderText("Sélectionnez la transaction d'origine :");
        dialog.setContentText("Transaction :");

        ((ComboBox<FinancialTransaction>) dialog.getDialogPane().lookup(".combo-box"))
                .setConverter(new StringConverter<>() {
                    @Override
                    public String toString(FinancialTransaction t) {
                        return t.getTransactionDate() + " - " + t.getDescription() + " ("
                                + String.format("%.2f", t.getAmount()) + ")";
                    }

                    @Override
                    public FinancialTransaction fromString(String s) {
                        return null;
                    }
                });

        dialog.showAndWait().ifPresent(tx -> {
            if (avoirInvoiceRef != null) {
                avoirInvoiceRef.setText("REF-TX-" + tx.getId()); // Auto-fill reference
            }
            if (avoirReason != null) {
                avoirReason.setText("Avoir sur transaction du " + tx.getTransactionDate());
            }

            // Add items reverse
            String ref = "TX-" + tx.getId();
            String desc = tx.getDescription();
            int qty = 1;
            double price = tx.getAmount();

            InvoiceItem item = new InvoiceItem(ref, desc, qty, price, 20.0);
            avoirItems.add(item);
            updateAvoirTotals();

            showAlert("Succès", "Données importées pour l'avoir !");
        });
    }

    private void updateAvoirTotals() {
        double totalHT = avoirItems.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice()).sum();
        double tva = totalHT * 0.20;
        double ttc = totalHT + tva;
        avoirTotalHT.setText(String.format("Total HT: %.2f DH", totalHT));
        avoirTVA.setText(String.format("TVA: %.2f DH", tva));
        avoirTotalTTC.setText(String.format("Total TTC: %.2f DH", ttc));
    }

    private void generateAvoir() {
        Customer customer = avoirPartyCombo.getValue();
        if (customer == null) {
            showAlert("Erreur", "Veuillez sélectionner un client.");
            return;
        }
        if (avoirItems.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un article.");
            return;
        }

        try {
            String docNumber = PDFGenerator.generateDocNumber("AV", avoirCounter++);
            String filename = docNumber + ".pdf";
            Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc, "AVOIR / NOTE DE CRÉDIT", docNumber, LocalDate.now());

            // Issuer
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "ÉMETTEUR",
                        companyInfo.getCompanyName(), companyInfo.getIce(), companyInfo.getRc(),
                        companyInfo.getAddress(), companyInfo.getPhone(), companyInfo.getEmail());
            }

            // Recipient
            PDFGenerator.addPartyInfo(doc, "CLIENT",
                    customer.getName(), customer.getIce(), null,
                    customer.getAddress(), customer.getPhone(), customer.getEmail());

            // Ref Invoice
            if (!avoirInvoiceRef.getText().isEmpty()) {
                doc.add(new com.itextpdf.layout.element.Paragraph("Concerne la facture: " + avoirInvoiceRef.getText())
                        .setFontSize(10).setBold().setMarginBottom(5));
            }
            if (!avoirReason.getText().isEmpty()) {
                doc.add(new com.itextpdf.layout.element.Paragraph("Motif: " + avoirReason.getText())
                        .setFontSize(10).setItalic().setMarginBottom(10));
            }

            // Items table
            String[] headers = { "Réf", "Description", "Qté", "Prix HT", "Total HT" };
            float[] widths = { 1, 3, 0.7f, 1, 1 };
            Table itemsTable = PDFGenerator.createItemsTable(headers, widths);

            double totalHT = 0;
            for (InvoiceItem item : avoirItems) {
                double lineTotal = item.getQuantity() * item.getUnitPrice();
                totalHT += lineTotal;
                PDFGenerator.addTableRow(itemsTable, new String[] {
                        item.getReference(), item.getDescription(),
                        String.valueOf(item.getQuantity()),
                        String.format("%.2f", item.getUnitPrice()),
                        String.format("%.2f", lineTotal)
                });
            }
            doc.add(itemsTable);

            // Financial Summary
            double totalTTC = totalHT * 1.20;
            PDFGenerator.addFinancialSummary(doc, totalHT, 20.0, totalTTC);

            // Footer info
            doc.add(new com.itextpdf.layout.element.Paragraph(
                    "\nCe montant sera crédité sur votre compte ou déduit de votre prochaine facture.")
                    .setFontSize(10).setBold().setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE));

            PDFGenerator.addSignatureSection(doc, "L'Émetteur", "Le Client");

            doc.close();

            // === INTEGRATION WITH FINANCIAL REPORTS ===
            if (financialDAO != null) {
                try {
                    FinancialTransaction tx = new FinancialTransaction(
                            0,
                            LocalDate.now(),
                            "Expense",
                            "Avoir (Remboursement: " + docNumber + ")",
                            totalTTC,
                            "Virement/Chèque",
                            "Avoir pour " + customer.getName() + " (Ref: " + avoirInvoiceRef.getText() + ")",
                            "Customer",
                            customer.getId(),
                            null);
                    financialDAO.addTransaction(tx);
                } catch (Exception e) {
                    System.err.println("Failed to record tx: " + e.getMessage());
                }
            }
            showAlert("Succès", "Avoir généré !\n" + filename);
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Echec Avoir: " + e.getMessage());
        }
    }

    // ==================== REÇU ====================

    private void setupRecu() {
        recuItemsTable.setItems(recuItems);

        // Populate payment methods
        recuPaymentMethod.getItems().setAll("Espèces", "Carte", "Chèque");
        recuPaymentMethod.setValue("Espèces");

        // Columns setup
        recuColDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        recuColQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        recuColPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        recuColTotal.setCellValueFactory(
                data -> new SimpleDoubleProperty(data.getValue().getQuantity() * data.getValue().getUnitPrice())
                        .asObject());

        recuAddItemBtn.setOnAction(e -> {
            InvoiceItem item = promptForInvoiceItem();
            if (item != null) {
                recuItems.add(item);
                updateRecuTotal();
            }
        });

        recuRemoveItemBtn.setOnAction(e -> {
            InvoiceItem selected = recuItemsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                recuItems.remove(selected);
                updateRecuTotal();
            }
        });

        recuGenerateBtn.setOnAction(e -> generateRecu());
    }

    private void generateRecu() {
        if (recuItems.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un article.");
            return;
        }

        try {
            String docNumber = PDFGenerator.generateDocNumber("REC", recuCounter++);
            String filename = docNumber + ".pdf";
            Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc, "REÇU / TICKET CAISSE", docNumber, LocalDate.now());

            // Seller (Farm)
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "VENDEUR",
                        companyInfo.getCompanyName(), companyInfo.getIce(), companyInfo.getRc(),
                        companyInfo.getAddress(), companyInfo.getPhone(), companyInfo.getEmail());
            }

            // Client Info (Simplified)
            String clientInfo = "Client: "
                    + (recuCustomerName.getText().isEmpty() ? "Client de passage" : recuCustomerName.getText()) + "\n" +
                    "Tél: " + recuCustomerPhone.getText() + "\n" +
                    "Paiement: " + recuPaymentMethod.getValue();

            com.itextpdf.layout.element.Paragraph clientPara = new com.itextpdf.layout.element.Paragraph(clientInfo)
                    .setFontSize(10).setMarginBottom(10)
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY, 0.5f).setPadding(5);
            doc.add(clientPara);

            // Items Table
            String[] headers = { "Description", "Qté", "Prix", "Total" };
            float[] widths = { 3, 1, 1, 1 };
            Table itemsTable = PDFGenerator.createItemsTable(headers, widths);

            double totalTTC = 0;
            for (InvoiceItem item : recuItems) {
                double lineTotal = item.getQuantity() * item.getUnitPrice(); // Using UnitPrice as TTC for Receipt
                                                                             // simplicity or assuming tax included
                totalTTC += lineTotal;
                PDFGenerator.addTableRow(itemsTable, new String[] {
                        item.getDescription(),
                        String.valueOf(item.getQuantity()),
                        String.format("%.2f", item.getUnitPrice()),
                        String.format("%.2f", lineTotal)
                });
            }
            doc.add(itemsTable);

            // Total
            com.itextpdf.layout.element.Paragraph totalPara = new com.itextpdf.layout.element.Paragraph(
                    String.format("TOTAL TTC: %.2f DH", totalTTC))
                    .setFontSize(14).setBold().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setMarginTop(10);
            doc.add(totalPara);

            // Footer
            PDFGenerator.addFooter(doc, "Merci de votre visite !");

            doc.close();

            // === INTEGRATION WITH FINANCIAL REPORTS ===
            if (financialDAO != null) {
                try {
                    FinancialTransaction tx = new FinancialTransaction(
                            0,
                            LocalDate.now(),
                            "Income",
                            "Vente Comptoir (Reçu: " + docNumber + ")",
                            totalTTC,
                            recuPaymentMethod.getValue(),
                            "Reçu pour " + clientInfo.replace("\n", ", "),
                            "Customer",
                            0,
                            null);
                    financialDAO.addTransaction(tx);
                } catch (Exception e) {
                    System.err.println("Failed to record tx: " + e.getMessage());
                }
            }
            showAlert("Succès", "Reçu généré !\n" + filename);
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Echec Reçu: " + e.getMessage());
        }
    }

    private void updateRecuTotal() {
        double total = recuItems.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice() * 1.20).sum();
        recuTotalTTC.setText(String.format("Total TTC: %.2f DH", total));
    }

    // ==================== HR DOCUMENTS ====================

    private void setupHRDocuments() {
        // Create a string converter for Personnel in combos
        javafx.util.StringConverter<Personnel> personnelConverter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(Personnel p) {
                if (p == null)
                    return "";
                String jobTitle = p.getJobTitle() != null ? p.getJobTitle().replace("_", " ") : "";
                return p.getFullName() + " (" + jobTitle + ")";
            }

            @Override
            public Personnel fromString(String string) {
                return null;
            }
        };

        // Setup Bulletin de Paie
        if (bulletinMonthCombo != null) {
            bulletinMonthCombo.getItems().setAll(
                    "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre");
            bulletinMonthCombo.setValue("Décembre");
        }
        if (bulletinEmployeeCombo != null) {
            bulletinEmployeeCombo.setConverter(personnelConverter);
            // Items loaded by loadComboBoxData
        }

        // Setup Contrat de Travail
        if (contratTypeCombo != null) {
            contratTypeCombo.getItems().setAll("CDI", "CDD", "Stage");
            contratTypeCombo.setValue("CDI");
        }
        if (contratEmployeeCombo != null) {
            contratEmployeeCombo.setConverter(personnelConverter);
            // Items loaded by loadComboBoxData
        }

        // Setup Attestation de Travail
        if (attestationReasonCombo != null) {
            attestationReasonCombo.getItems().setAll("Démission", "Fin de contrat", "Licenciement");
        }
        if (attestationEmployeeCombo != null) {
            attestationEmployeeCombo.setConverter(personnelConverter);
            // Items loaded by loadComboBoxData
        }

        // Button handlers
        if (bulletinGenerateBtn != null) {
            bulletinGenerateBtn.setOnAction(e -> {
                Personnel emp = bulletinEmployeeCombo.getValue();
                String month = bulletinMonthCombo.getValue();
                String year = bulletinYear.getText();
                if (emp == null) {
                    showAlert("Erreur", "Veuillez sélectionner un employé.");
                    return;
                }
                generateBulletinPaie(emp, month, year);
            });
        }
        if (contratGenerateBtn != null) {
            contratGenerateBtn.setOnAction(e -> {
                Personnel emp = contratEmployeeCombo.getValue();
                String type = contratTypeCombo.getValue();
                if (emp == null) {
                    showAlert("Erreur", "Veuillez sélectionner un employé.");
                    return;
                }
                if (type == null) {
                    showAlert("Erreur", "Veuillez sélectionner un type de contrat.");
                    return;
                }
                generateContract(emp, type);
            });
        }
        if (attestationGenerateBtn != null) {
            attestationGenerateBtn.setOnAction(e -> {
                Personnel emp = attestationEmployeeCombo.getValue();
                String reason = attestationReasonCombo.getValue();
                if (emp == null) {
                    showAlert("Erreur", "Veuillez sélectionner un employé.");
                    return;
                }
                generateAttestation(emp, reason);
            });
        }
    }

    private void generateBulletinPaie(Personnel employee, String month, String year) {
        try {
            String filename = "Bulletin_" + employee.getFullName().replace(" ", "_") + "_" + month + ".pdf";
            Document doc = PDFGenerator.createDocument(filename);

            PDFGenerator.addDocumentHeader(doc, "BULLETIN DE PAIE", "Mois: " + month + " " + year, LocalDate.now());

            // Employer
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "EMPLOYEUR",
                        companyInfo.getCompanyName(), companyInfo.getIce(), companyInfo.getRc(),
                        companyInfo.getAddress(), companyInfo.getPhone(), companyInfo.getEmail());
            }

            // Employee
            PDFGenerator.addPartyInfo(doc, "SALARIÉ",
                    employee.getFullName(), "CNSS: " + (employee.getId() * 123456), "CIN: [CIN]",
                    employee.getJobTitle(), "Entrée: " + employee.getHireDate(), "");

            // Calculations
            double salaureBase = employee.getSalary();
            double cnss = salaureBase * 0.0448;
            double amo = salaureBase * 0.0226;
            double taxable = salaureBase - cnss - amo;
            // Simplified IR (This is a simplified assumption for demo)
            double ir = 0;
            if (taxable > 2500)
                ir = (taxable - 2500) * 0.10;
            if (taxable > 4166)
                ir = (taxable - 4166) * 0.20; // Very rough progressive placeholder

            double totalDeductions = cnss + amo + ir;
            double net = salaureBase - totalDeductions;

            // Details Table
            String[] headers = { "Rubrique", "Base / Taux", "Gains (+)", "Retenues (-)" };
            float[] widths = { 3, 2, 1, 1 };
            Table table = PDFGenerator.createItemsTable(headers, widths);

            PDFGenerator.addTableRow(table,
                    new String[] { "Salaire de Base", "26 jours", String.format("%.2f", salaureBase), "" });
            PDFGenerator.addTableRow(table, new String[] { "CNSS (4.48%)", "4.48%", "", String.format("%.2f", cnss) });
            PDFGenerator.addTableRow(table, new String[] { "AMO (2.26%)", "2.26%", "", String.format("%.2f", amo) });
            PDFGenerator.addTableRow(table,
                    new String[] { "Impôt sur le Revenu (IR)", "Barème", "", String.format("%.2f", ir) });

            doc.add(table);

            PDFGenerator.addFinancialSummary(doc, salaureBase, 0.0, net); // Reusing summary for Net

            doc.add(new com.itextpdf.layout.element.Paragraph("\n\nNet à payer: " + String.format("%.2f DH", net))
                    .setFontSize(16).setBold().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.YELLOW));

            PDFGenerator.addFooter(doc, "Document généré conformément au Code du Travail Marocain.");
            doc.close();
            showAlert("Succès", "Bulletin de Paie généré !\n" + filename);
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Echec Bulletin: " + e.getMessage());
        }
    }

    private void generateAttestation(Personnel employee, String reason) {
        try {
            String filename = "Attestation_" + employee.getFullName().replace(" ", "_") + ".pdf";
            Document doc = PDFGenerator.createDocument(filename);

            PDFGenerator.addDocumentHeader(doc, "ATTESTATION DE TRAVAIL", "", LocalDate.now());

            com.itextpdf.layout.element.Paragraph body = new com.itextpdf.layout.element.Paragraph()
                    .setFontSize(12).setMarginTop(30).setMultipliedLeading(1.5f);

            body.add("Nous soussignés, " + (companyInfo != null ? companyInfo.getCompanyName() : "[Nom Société]") +
                    ", sise à " + (companyInfo != null ? companyInfo.getAddress() : "[Adresse]")
                    + ", attestons par la présente que :\n\n");

            body.add(new com.itextpdf.layout.element.Text("Monsieur/Madame " + employee.getFullName())
                    .setBold().setFontSize(14));

            body.add("\n\nA été employé(e) au sein de notre structure en qualité de " +
                    (employee.getJobTitle() != null ? employee.getJobTitle() : "Employé(e)") + ".\n");

            body.add("Date d'entrée: " + (employee.getHireDate() != null ? employee.getHireDate() : "N/A") + "\n");
            body.add("Date de sortie: " + LocalDate.now() + "\n\n"); // Assuming simplified exit is today for
                                                                     // attestation

            body.add("Cette attestation est délivrée à l'intéressé(e) pour servir et valoir ce que de droit.\n\n");

            if (reason != null && !reason.isEmpty()) {
                body.add("Motif: " + reason + "\n");
            }

            doc.add(body);

            PDFGenerator.addSignatureSection(doc, "L'Employeur", "");

            doc.close();
            showAlert("Succès", "Attestation générée !\n" + filename);
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Echec Attestation: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void generateContract(Personnel employee, String type) {
        try {
            String docNumber = PDFGenerator.generateDocNumber("CNTR", employee.getId());
            String filename = "Contrat_" + employee.getFullName().replace(" ", "_") + "_" + type + ".pdf";

            Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc, "CONTRAT DE TRAVAIL " + type.toUpperCase(), docNumber, LocalDate.now());

            // Employer Info
            if (companyInfo != null) {
                PDFGenerator.addPartyInfo(doc, "L'EMPLOYEUR",
                        companyInfo.getCompanyName(),
                        companyInfo.getIce(),
                        companyInfo.getRc(),
                        companyInfo.getAddress(),
                        companyInfo.getPhone(),
                        companyInfo.getEmail());
            }

            // Employee Info
            // Note: Since Personnel model might lack specific address/CIN fields, we'll use
            // available data and placeholders
            PDFGenerator.addPartyInfo(doc, "L'EMPLOYÉ(E)",
                    employee.getFullName(),
                    "CIN: [À Remplir]",
                    "CNSS: " + (employee.getId() * 123456), // Placeholder generation
                    employee.getAddress() != null ? employee.getAddress() : "[Adresse]",
                    employee.getPhone(),
                    employee.getEmail());

            // Contract Details Section
            com.itextpdf.layout.element.Paragraph detailsHeader = new com.itextpdf.layout.element.Paragraph(
                    "DÉTAILS DU CONTRAT")
                    .setFont(com.itextpdf.kernel.font.PdfFontFactory
                            .createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD))
                    .setFontSize(12)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                    .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(52, 73, 94))
                    .setPadding(5)
                    .setMarginTop(15);
            doc.add(detailsHeader);

            com.itextpdf.layout.element.Table detailsTable = new com.itextpdf.layout.element.Table(2)
                    .useAllAvailableWidth();
            detailsTable.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new com.itextpdf.layout.element.Paragraph("Poste Occupé:")));
            detailsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(
                    employee.getJobTitle() != null ? employee.getJobTitle() : "Employé Agricole")));

            detailsTable.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new com.itextpdf.layout.element.Paragraph("Date de début:")));
            detailsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(
                    employee.getHireDate() != null ? employee.getHireDate().toString() : LocalDate.now().toString())));

            detailsTable.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new com.itextpdf.layout.element.Paragraph("Salaire Mensuel Brut:")));
            detailsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(
                    String.format("%.2f DH", employee.getSalary()))));

            doc.add(detailsTable);

            // Terms
            com.itextpdf.layout.element.Paragraph terms = new com.itextpdf.layout.element.Paragraph()
                    .setFontSize(10)
                    .setMarginTop(10)
                    .add("ARTICLE 1: CONDITIONS DE TRAVAIL\n")
                    .add("L'employé(e) exercera ses fonctions à la ferme située à "
                            + (companyInfo != null ? companyInfo.getAddress() : "...") + ". ")
                    .add("La durée de travail est de 44 heures par semaine.\n\n")
                    .add("ARTICLE 2: PÉRIODE D'ESSAI\n")
                    .add("Ce contrat est soumis à une période d'essai de 3 mois, renouvelable une fois.\n\n")
                    .add("ARTICLE 3: CONGÉS\n")
                    .add("L'employé(e) a droit à un congé annuel payé de 1.5 jours par mois de travail effectif, soit 18 jours par an.");
            doc.add(terms);

            // Signatures
            PDFGenerator.addSignatureSection(doc, "L'Employeur (Signature + Cachet)", "L'Employé(e) (Lu et approuvé)");

            // Footer
            PDFGenerator.addFooter(doc, "Contrat régis par le Code du Travail Marocain.");

            doc.close();

            showAlert("Succès",
                    "Contrat généré !\n\nFichier: " + filename + "\n\nChemin complet: " + PDFGenerator.DOCUMENTS_DIR);

            // Try to open the file folder
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(PDFGenerator.DOCUMENTS_DIR));
            } catch (Exception e) {
                // Ignore if we can't open folder
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Échec de génération du contrat: " + ex.getMessage());
        }
    }

    // ==================== INNER CLASSES FOR TABLE DATA ====================

    public static class OrderItem {
        private String reference;
        private String description;
        private int quantity;
        private String unit;

        public OrderItem(String reference, String description, int quantity, String unit) {
            this.reference = reference;
            this.description = description;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getReference() {
            return reference;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }
    }

    public static class InvoiceItem {
        private String reference;
        private String description;
        private int quantity;
        private double unitPrice;
        private double tvaRate;

        public InvoiceItem(String reference, String description, int quantity, double unitPrice, double tvaRate) {
            this.reference = reference;
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.tvaRate = tvaRate;
        }

        public String getReference() {
            return reference;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTvaRate() {
            return tvaRate;
        }
    }
}
