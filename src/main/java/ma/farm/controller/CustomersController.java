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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.controller.dialogs.CustomerDialogController;
import ma.farm.dao.CustomerDAO;
import ma.farm.model.Customer;
import ma.farm.util.PDFGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class CustomersController {

    @FXML
    private Button addCustomerBtn;
    @FXML
    private Button exportBtn;
    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableColumn<Customer, Integer> colId;
    @FXML
    private TableColumn<Customer, String> colName;
    @FXML
    private TableColumn<Customer, String> colType;
    @FXML
    private TableColumn<Customer, String> colContact;
    @FXML
    private TableColumn<Customer, String> colPhone;
    @FXML
    private TableColumn<Customer, String> colIce;
    @FXML
    private TableColumn<Customer, Double> colBalance;
    @FXML
    private TableColumn<Customer, Integer> colVisits;
    @FXML
    private TableColumn<Customer, String> colStatus;
    @FXML
    private TableColumn<Customer, Void> colActions;

    private CustomerDAO customerDAO;
    private ObservableList<Customer> customerList;

    @FXML
    public void initialize() {
        customerDAO = new CustomerDAO();
        customerList = FXCollections.observableArrayList();

        setupFilterCombo();
        setupTable();
        loadCustomers();

        addCustomerBtn.setOnAction(e -> openCustomerDialog(null));
        exportBtn.setOnAction(e -> handleExportPDF());
    }

    private void setupFilterCombo() {
        filterCombo.getItems().addAll("Tous", "Entreprise", "Particulier");
        filterCombo.setValue("Tous");
        filterCombo.setOnAction(e -> filterCustomersByType());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(data -> {
            Customer c = data.getValue();
            String display = c.getName();
            if (c.getCompanyName() != null && !c.getCompanyName().isEmpty()) {
                display = c.getCompanyName() + " (" + c.getName() + ")";
            }
            return new SimpleStringProperty(display);
        });

        colType.setCellValueFactory(data -> {
            String type = data.getValue().getType();
            String displayType = "Company".equals(type) ? "Entreprise" : "Particulier";
            return new SimpleStringProperty(displayType);
        });

        // Type badge styling
        colType.setCellFactory(column -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Entreprise".equals(item)) {
                        setStyle(
                                "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setStyle(
                                "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-font-weight: bold; -fx-alignment: center;");
                    }
                }
            }
        });

        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colIce.setCellValueFactory(new PropertyValueFactory<>("ice"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("outstandingBalance"));
        colVisits.setCellValueFactory(new PropertyValueFactory<>("visitCount"));

        colStatus.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "Actif" : "Inactif"));

        colStatus.setCellFactory(column -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Actif".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-style: italic;");
                    }
                }
            }
        });

        // Actions Column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button exportItemBtn = new Button("📄");
            private final Button deleteRestoreBtn = new Button();
            private final HBox pane = new HBox(5, editBtn, exportItemBtn, deleteRestoreBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px;");
                editBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    openCustomerDialog(customer);
                });

                exportItemBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
                exportItemBtn.setTooltip(new Tooltip("Exporter Fiche Client"));
                exportItemBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleExportIndividualCustomer(customer);
                });

                deleteRestoreBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleDeleteRestore(customer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Customer c = getTableView().getItems().get(getIndex());

                    editBtn.setVisible(c.isActive());
                    editBtn.setManaged(c.isActive());

                    if (c.isActive()) {
                        deleteRestoreBtn.setText("Supprimer");
                        deleteRestoreBtn
                                .setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
                    } else {
                        deleteRestoreBtn.setText("Restaurer");
                        deleteRestoreBtn
                                .setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 10px;");
                    }
                    setGraphic(pane);
                }
            }
        });

        customersTable.setItems(customerList);
    }

    private void loadCustomers() {
        customerList.clear();
        customerList.addAll(customerDAO.getAllCustomers());
    }

    private void filterCustomersByType() {
        String filter = filterCombo.getValue();
        if (filter == null || "Tous".equals(filter)) {
            loadCustomers();
            return;
        }

        String dbType = "Entreprise".equals(filter) ? "Company" : "Individual";
        customerList.clear();
        customerList.addAll(customerDAO.getCustomersByType(dbType));
    }

    private void openCustomerDialog(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/CustomerDialog.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(customer == null ? "Nouveau Client" : "Modifier Client");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addCustomerBtn.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            CustomerDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            if (customer != null) {
                controller.setCustomer(customer);
            }

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadCustomers();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
        }
    }

    private void handleDeleteRestore(Customer customer) {
        if (customer.isActive()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le client " + customer.getName() + " ?");
            alert.setContentText("Le client sera marqué comme inactif. L'historique sera conservé.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (customerDAO.deleteCustomer(customer.getId())) {
                    loadCustomers();
                } else {
                    showAlert("Erreur", "La suppression a échoué.");
                }
            }
        } else {
            if (customerDAO.restoreCustomer(customer.getId())) {
                showAlert("Succès", "Client restauré avec succès.");
                loadCustomers();
            } else {
                showAlert("Erreur", "La restauration a échoué.");
            }
        }
    }

    private void handleExportPDF() {
        try {
            PDFGenerator.ensureDirectoryExists();
            String filename = "Liste_Clients_" + System.currentTimeMillis() + ".pdf";
            String fullPath = PDFGenerator.DOCUMENTS_DIR + filename;

            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Liste des Clients").setFontSize(18).setBold());
            document.add(new Paragraph("Généré le: " + java.time.LocalDate.now()));

            float[] columnWidths = { 1, 3, 2, 2, 2, 2 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("ID");
            table.addHeaderCell("Nom");
            table.addHeaderCell("Type");
            table.addHeaderCell("Téléphone");
            table.addHeaderCell("Solde");
            table.addHeaderCell("Visites");

            for (Customer c : customerList) {
                table.addCell(String.valueOf(c.getId()));
                table.addCell(c.getName());
                table.addCell("Company".equals(c.getType()) ? "Entreprise" : "Particulier");
                table.addCell(c.getPhone() != null ? c.getPhone() : "");
                table.addCell(String.format("%.2f DH", c.getOutstandingBalance()));
                table.addCell(String.valueOf(c.getVisitCount()));
            }

            document.add(table);
            document.close();

            showAlert("Succès", "PDF exporté dans Documents:\n" + filename);
            java.awt.Desktop.getDesktop().open(new File(PDFGenerator.DOCUMENTS_DIR));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "L'export PDF a échoué: " + e.getMessage());
        }
    }

    private void handleExportIndividualCustomer(Customer c) {
        if (c == null)
            return;
        try {
            String filename = "Fiche_Client_" + c.getName().replaceAll("\\s+", "_") + ".pdf";
            Document doc = PDFGenerator.createDocument(filename);

            PDFGenerator.addDocumentHeader(doc, "FICHE CLIENT", "CLI-" + c.getId(), java.time.LocalDate.now());

            doc.add(new Paragraph("Informations Générales").setFontSize(14).setBold().setMarginTop(10));

            Table infoTable = new Table(2).useAllAvailableWidth();
            infoTable.addCell("Nom / Contact:");
            infoTable.addCell(c.getName());

            if (c.isCompany()) {
                infoTable.addCell("Société:");
                infoTable.addCell(c.getCompanyName() != null ? c.getCompanyName() : "-");

                infoTable.addCell("Forme Juridique:");
                infoTable.addCell(c.getLegalForm() != null ? c.getLegalForm() : "-");
            }

            infoTable.addCell("Type:");
            infoTable.addCell(c.isCompany() ? "Entreprise" : "Particulier");

            infoTable.addCell("Email:");
            infoTable.addCell(c.getEmail() != null ? c.getEmail() : "-");

            infoTable.addCell("Téléphone:");
            infoTable.addCell(c.getPhone() != null ? c.getPhone() : "-");

            infoTable.addCell("Adresse:");
            infoTable.addCell(c.getAddress() != null ? c.getAddress() : "-");

            doc.add(infoTable);

            if (c.isCompany()) {
                doc.add(new Paragraph("Informations Légales").setFontSize(14).setBold().setMarginTop(20));
                Table legalTable = new Table(2).useAllAvailableWidth();

                legalTable.addCell("ICE:");
                legalTable.addCell(c.getIce() != null ? c.getIce() : "-");

                legalTable.addCell("RC:");
                legalTable.addCell(c.getRc() != null ? c.getRc() : "-");

                legalTable.addCell("Conditions de paiement:");
                legalTable.addCell(c.getPaymentTerms() != null ? c.getPaymentTerms() : "-");

                doc.add(legalTable);
            }

            doc.add(new Paragraph("Statistiques").setFontSize(14).setBold().setMarginTop(20));
            Table statsTable = new Table(2).useAllAvailableWidth();
            statsTable.addCell("Total Achats:");
            statsTable.addCell(String.format("%.2f DH", c.getTotalPurchases()));
            statsTable.addCell("Nombre de Visites:");
            statsTable.addCell(String.valueOf(c.getVisitCount()));
            statsTable.addCell("Solde Impayé:");
            statsTable.addCell(String.format("%.2f DH", c.getOutstandingBalance()));
            doc.add(statsTable);

            PDFGenerator.addFooter(doc, "Fiche générée automatiquement par Farm Management System");

            doc.close();

            showAlert("Succès", "Fiche exportée: " + filename);
            java.awt.Desktop.getDesktop().open(new File(PDFGenerator.DOCUMENTS_DIR));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer la fiche: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
