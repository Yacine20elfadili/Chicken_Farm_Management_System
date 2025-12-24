package ma.farm.controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ma.farm.dao.CustomerDAO;
import ma.farm.dao.FinancialDAO;
import ma.farm.dao.PersonnelDAO;
import ma.farm.dao.SupplierDAO;
import ma.farm.model.Customer;
import ma.farm.model.FinancialTransaction;
import ma.farm.model.Personnel;
import ma.farm.model.Supplier;
import ma.farm.util.PDFGenerator;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FinancialTrackingController {

    @FXML
    private Button exportAllBtn;

    @FXML
    private ComboBox<String> typeFilterCombo;
    @FXML
    private ComboBox<String> entityFilterCombo;

    @FXML
    private Label totalIncomeLabel;
    @FXML
    private Label totalExpenseLabel;
    @FXML
    private Label netBalanceLabel;
    @FXML
    private Label outstandingLabel;

    @FXML
    private TableView<FinancialTransaction> transactionTable;
    @FXML
    private TableColumn<FinancialTransaction, Integer> colId;
    @FXML
    private TableColumn<FinancialTransaction, LocalDate> colDate;
    @FXML
    private TableColumn<FinancialTransaction, String> colType;
    @FXML
    private TableColumn<FinancialTransaction, String> colCategory;
    @FXML
    private TableColumn<FinancialTransaction, String> colEntity;
    @FXML
    private TableColumn<FinancialTransaction, Double> colAmount;
    @FXML
    private TableColumn<FinancialTransaction, String> colDescription;
    @FXML
    private TableColumn<FinancialTransaction, Void> colActions;

    private FinancialDAO financialDAO;
    private SupplierDAO supplierDAO;
    private CustomerDAO customerDAO;
    private PersonnelDAO personnelDAO;

    private ObservableList<FinancialTransaction> allTransactions;
    private ObservableList<FinancialTransaction> filteredTransactions;

    // Cache for entity names
    private Map<Integer, String> supplierNames;
    private Map<Integer, String> customerNames;
    private Map<Integer, String> personnelNames;

    @FXML
    public void initialize() {
        financialDAO = new FinancialDAO();
        supplierDAO = new SupplierDAO();
        customerDAO = new CustomerDAO();
        personnelDAO = new PersonnelDAO();

        allTransactions = FXCollections.observableArrayList();
        filteredTransactions = FXCollections.observableArrayList();
        supplierNames = new HashMap<>();
        customerNames = new HashMap<>();
        personnelNames = new HashMap<>();

        setupFilters();
        setupTable();
        loadEntityNames();
        loadTransactions();
        updateSummary();

        exportAllBtn.setOnAction(e -> handleExportAll());
    }

    private void setupFilters() {
        // Type filter
        typeFilterCombo.getItems().addAll("Tous", "Revenus", "Dépenses");
        typeFilterCombo.setValue("Tous");
        typeFilterCombo.setOnAction(e -> applyFilters());

        // Entity filter
        entityFilterCombo.getItems().addAll("Tous", "Fournisseurs", "Clients", "Personnel");
        entityFilterCombo.setValue("Tous");
        entityFilterCombo.setOnAction(e -> applyFilters());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));

        // Type with badge styling
        colType.setCellValueFactory(data -> {
            String type = String.valueOf(data.getValue().getType());
            return new SimpleStringProperty("Income".equalsIgnoreCase(type) ? "Revenu" : "Dépense");
        });
        colType.setCellFactory(column -> new TableCell<FinancialTransaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Revenu".equals(item)) {
                        setStyle(
                                "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setStyle(
                                "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold; -fx-alignment: center;");
                    }
                }
            }
        });

        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Entity name resolution
        colEntity.setCellValueFactory(data -> {
            FinancialTransaction t = data.getValue();
            String entityType = t.getRelatedEntityType();
            int entityId = t.getRelatedEntityId();

            if (entityType == null || entityId <= 0) {
                return new SimpleStringProperty("-");
            }

            if ("Supplier".equalsIgnoreCase(entityType)) {
                return new SimpleStringProperty(supplierNames.getOrDefault(entityId, "Fournisseur #" + entityId));
            } else if ("Customer".equalsIgnoreCase(entityType)) {
                return new SimpleStringProperty(customerNames.getOrDefault(entityId, "Client #" + entityId));
            } else if ("Personnel".equalsIgnoreCase(entityType)) {
                return new SimpleStringProperty(personnelNames.getOrDefault(entityId, "Personnel #" + entityId));
            }
            return new SimpleStringProperty("-");
        });

        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(column -> new TableCell<FinancialTransaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DH", item));
                }
            }
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Actions column with export button
        colActions.setCellFactory(column -> new TableCell<FinancialTransaction, Void>() {
            private final Button exportBtn = new Button("📄");

            {
                exportBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;");
                exportBtn.setTooltip(new Tooltip("Exporter cette transaction"));
                exportBtn.setOnAction(e -> {
                    FinancialTransaction tx = getTableView().getItems().get(getIndex());
                    handleExportTransaction(tx);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(exportBtn);
                }
            }
        });

        transactionTable.setItems(filteredTransactions);
    }

    private void loadEntityNames() {
        // Load all supplier names
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier s : suppliers) {
            supplierNames.put(s.getId(), s.getName());
        }

        // Load all customer names
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer c : customers) {
            customerNames.put(c.getId(), c.getName());
        }

        // Load all personnel names
        List<Personnel> personnelList = personnelDAO.getAllPersonnel();
        for (Personnel p : personnelList) {
            personnelNames.put(p.getId(), p.getFullName());
        }
    }

    private void loadTransactions() {
        allTransactions.clear();
        allTransactions.addAll(financialDAO.getAllTransactions());
        applyFilters();
    }

    private void applyFilters() {
        String typeFilter = typeFilterCombo.getValue();
        String entityFilter = entityFilterCombo.getValue();

        filteredTransactions.clear();

        for (FinancialTransaction t : allTransactions) {
            boolean passType = true;
            boolean passEntity = true;

            // Type filter
            if (!"Tous".equals(typeFilter)) {
                String type = String.valueOf(t.getType());
                if ("Revenus".equals(typeFilter) && !"Income".equalsIgnoreCase(type)) {
                    passType = false;
                }
                if ("Dépenses".equals(typeFilter) && !"Expense".equalsIgnoreCase(type)) {
                    passType = false;
                }
            }

            // Entity filter
            if (!"Tous".equals(entityFilter)) {
                String entityType = t.getRelatedEntityType();
                if ("Fournisseurs".equals(entityFilter) && !"Supplier".equalsIgnoreCase(entityType)) {
                    passEntity = false;
                }
                if ("Clients".equals(entityFilter) && !"Customer".equalsIgnoreCase(entityType)) {
                    passEntity = false;
                }
                if ("Personnel".equals(entityFilter) && !"Personnel".equalsIgnoreCase(entityType)) {
                    passEntity = false;
                }
            }

            if (passType && passEntity) {
                filteredTransactions.add(t);
            }
        }

        updateSummary();

    }

    private void updateSummary() {
        double income = 0;
        double expense = 0;

        for (FinancialTransaction t : filteredTransactions) {
            if ("Income".equalsIgnoreCase(String.valueOf(t.getType()))) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }

        double net = income - expense;

        totalIncomeLabel.setText(String.format("%.2f DH", income));
        totalExpenseLabel.setText(String.format("%.2f DH", expense));
        netBalanceLabel.setText(String.format("%.2f DH", net));

        // Calculate outstanding customer balance
        double outstanding = 0;
        for (Customer c : customerDAO.getAllCustomers()) {
            outstanding += c.getOutstandingBalance();
        }
        outstandingLabel.setText(String.format("%.2f DH", outstanding));
    }

    private void handleAddTransaction() {
        // TODO: Open add transaction dialog
        System.out.println("Add transaction clicked");
        showAlert("Information", "Fonctionnalité à implémenter: Ajouter une nouvelle transaction.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleExportAll() {
        try {
            PDFGenerator.ensureDirectoryExists();
            String filename = "Transactions_Financieres_" + System.currentTimeMillis() + ".pdf";
            String fullPath = PDFGenerator.DOCUMENTS_DIR + filename;

            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Rapport Financier").setFontSize(18).setBold());
            document.add(new Paragraph("Généré le: " + LocalDate.now()));
            document.add(new Paragraph("Filtres appliqués - Type: " + typeFilterCombo.getValue() + ", Tiers: "
                    + entityFilterCombo.getValue()).setFontSize(10));
            document.add(new Paragraph("\n"));

            // Table
            float[] columnWidths = {1, 2, 2, 3, 3, 2, 4};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("ID");
            table.addHeaderCell("Date");
            table.addHeaderCell("Type");
            table.addHeaderCell("Catégorie");
            table.addHeaderCell("Tiers");
            table.addHeaderCell("Montant");
            table.addHeaderCell("Description");

            for (FinancialTransaction t : filteredTransactions) {
                table.addCell(String.valueOf(t.getId()));
                table.addCell(t.getTransactionDate() != null ? t.getTransactionDate().toString() : "");
                table.addCell("Income".equalsIgnoreCase(t.getType()) ? "Revenu" : "Dépense");
                table.addCell(t.getCategory());
                table.addCell(resolveEntityName(t));
                table.addCell(String.format("%.2f DH", t.getAmount()));
                table.addCell(t.getDescription());
            }

            document.add(table);

            // Summary
            document.add(new Paragraph("\nRésumé Financier").setFontSize(14).setBold());
            document.add(new Paragraph("Total Revenus: " + totalIncomeLabel.getText()));
            document.add(new Paragraph("Total Dépenses: " + totalExpenseLabel.getText()));
            document.add(new Paragraph("Solde Net: " + netBalanceLabel.getText()));

            document.close();

            openFile(fullPath);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'exportation PDF: " + e.getMessage());
        }
    }

    private void handleExportTransaction(FinancialTransaction tx) {
        try {
            PDFGenerator.ensureDirectoryExists();
            String filename = "Transaction_" + tx.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String fullPath = PDFGenerator.DOCUMENTS_DIR + filename;

            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Détail de la Transaction #" + tx.getId()).setFontSize(18).setBold());
            document.add(new Paragraph("Généré le: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph(
                    "Date: " + (tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : "")));
            document.add(new Paragraph("Type: " + ("Income".equalsIgnoreCase(tx.getType()) ? "Revenu" : "Dépense")));
            document.add(new Paragraph("Catégorie: " + tx.getCategory()));
            document.add(new Paragraph("Tiers: " + resolveEntityName(tx)));
            document.add(new Paragraph("Montant: " + String.format("%.2f DH", tx.getAmount())).setBold());
            document.add(new Paragraph("Méthode de paiement: " + tx.getPaymentMethod()));
            document.add(new Paragraph("Description: " + tx.getDescription()));

            document.close();

            openFile(fullPath);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'exportation PDF: " + e.getMessage());
        }
    }

    private String resolveEntityName(FinancialTransaction t) {
        String entityType = t.getRelatedEntityType();
        int entityId = t.getRelatedEntityId();

        if (entityType == null || entityId <= 0) {
            return "-";
        }

        if ("Supplier".equalsIgnoreCase(entityType)) {
            return supplierNames.getOrDefault(entityId, "Fournisseur #" + entityId);
        } else if ("Customer".equalsIgnoreCase(entityType)) {
            return customerNames.getOrDefault(entityId, "Client #" + entityId);
        } else if ("Personnel".equalsIgnoreCase(entityType)) {
            return personnelNames.getOrDefault(entityId, "Personnel #" + entityId);
        }
        return "-";
    }

    private void openFile(String path) {
        try {
            File file = new File(path);
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Succès", "Fichier généré: " + path);
            }
        } catch (IOException e) {
            showAlert("Succès", "Fichier généré (impossible d'ouvrir auto): " + path);
        }
    }
}