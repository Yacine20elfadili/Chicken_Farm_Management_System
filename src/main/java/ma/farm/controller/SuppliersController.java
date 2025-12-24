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
import ma.farm.controller.dialogs.SupplierDialogController;
import ma.farm.dao.SupplierDAO;
import ma.farm.model.Supplier;

import ma.farm.util.PDFGenerator;
import ma.farm.model.User;
import ma.farm.dao.UserDAO;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SuppliersController {

    @FXML
    private Button addSupplierBtn;
    @FXML
    private Button exportBtn;

    @FXML
    private TableView<Supplier> suppliersTable;
    @FXML
    private TableColumn<Supplier, Integer> colId;
    @FXML
    private TableColumn<Supplier, String> colName;
    @FXML
    private TableColumn<Supplier, String> colCategory;
    @FXML
    private TableColumn<Supplier, String> colContact;
    @FXML
    private TableColumn<Supplier, String> colPhone;
    @FXML
    private TableColumn<Supplier, String> colEmail;
    @FXML
    private TableColumn<Supplier, String> colIce;
    @FXML
    private TableColumn<Supplier, String> colStatus;
    @FXML
    private TableColumn<Supplier, Void> colActions;

    private SupplierDAO supplierDAO;
    private ObservableList<Supplier> supplierList;

    @FXML
    public void initialize() {
        supplierDAO = new SupplierDAO();
        supplierList = FXCollections.observableArrayList();

        setupTable();
        loadSuppliers();

        addSupplierBtn.setOnAction(e -> openSupplierDialog(null));
        exportBtn.setOnAction(e -> handleExportPDF());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colIce.setCellValueFactory(new PropertyValueFactory<>("ice"));

        colStatus.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().isActive() ? "Actif" : "Inactif"));

        // Custom cell factory for Status color
        colStatus.setCellFactory(column -> new TableCell<Supplier, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Actif")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-style: italic;");
                    }
                }
            }
        });

        // Actions Column
        // Actions Column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteRestoreBtn = new Button();
            private final Button exportItemBtn = new Button("📄"); // PDF Icon
            private final HBox pane = new HBox(5, editBtn, exportItemBtn, deleteRestoreBtn);

            {
                editBtn.getStyleClass().add("btn-warning-small"); // Assuming CSS class exists or use inline
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px;");
                editBtn.setOnAction(event -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    openSupplierDialog(supplier);
                });

                exportItemBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
                exportItemBtn.setTooltip(new Tooltip("Exporter Fiche Fournisseur"));
                exportItemBtn.setOnAction(event -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    handleExportIndividualSupplier(supplier);
                });

                deleteRestoreBtn.setOnAction(event -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    handleDeleteRestore(supplier);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Supplier s = getTableView().getItems().get(getIndex());

                    // Edit button valid only if active
                    editBtn.setVisible(s.isActive());
                    editBtn.setManaged(s.isActive()); // Remove space if hidden

                    if (s.isActive()) {
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

        suppliersTable.setItems(supplierList);
    }

    private void loadSuppliers() {
        supplierList.clear();
        supplierList.addAll(supplierDAO.getAllSuppliers());
    }

    private void openSupplierDialog(Supplier supplier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/SupplierDialog.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(supplier == null ? "Nouveau Fournisseur" : "Modifier Fournisseur");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addSupplierBtn.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            SupplierDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            if (supplier != null) {
                controller.setSupplier(supplier);
            }

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadSuppliers(); // Refresh table
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
        }
    }

    private void handleDeleteRestore(Supplier supplier) {
        if (supplier.isActive()) {
            // Soft Delete confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le fournisseur " + supplier.getName() + " ?");
            alert.setContentText("Le fournisseur sera marqué comme inactif. L'historique sera conservé.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (supplierDAO.deleteSupplier(supplier.getId())) {
                    loadSuppliers();
                } else {
                    showAlert("Erreur", "La suppression a échoué.");
                }
            }
        } else {
            // Restore
            if (supplierDAO.restoreSupplier(supplier.getId())) {
                showAlert("Succès", "Fournisseur restauré avec succès.");
                loadSuppliers();
            } else {
                showAlert("Erreur", "La restauration a échoué.");
            }
        }
    }

    private void handleExportPDF() {
        try {
            // Use common documents directory
            PDFGenerator.ensureDirectoryExists();
            String filename = "Liste_Fournisseurs_" + System.currentTimeMillis() + ".pdf";
            String fullPath = PDFGenerator.DOCUMENTS_DIR + filename;

            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Liste des Fournisseurs").setFontSize(18).setBold());
            document.add(new Paragraph("Généré le: " + java.time.LocalDate.now()));

            float[] columnWidths = { 1, 3, 2, 3, 2, 2 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("ID");
            table.addHeaderCell("Nom");
            table.addHeaderCell("Catégorie");
            table.addHeaderCell("Contact");
            table.addHeaderCell("Téléphone");
            table.addHeaderCell("Statut");

            for (Supplier s : supplierList) {
                table.addCell(String.valueOf(s.getId()));
                table.addCell(s.getName());
                table.addCell(s.getCategory());
                table.addCell(s.getContactPerson() != null ? s.getContactPerson() : "");
                table.addCell(s.getPhone() != null ? s.getPhone() : "");
                table.addCell(String.valueOf(s.isActive())); // Debug or translated
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

    private void handleExportIndividualSupplier(Supplier s) {
        if (s == null)
            return;
        try {
            String filename = "Fiche_Fournisseur_" + s.getName().replaceAll("\\s+", "_") + ".pdf";
            Document doc = PDFGenerator.createDocument(filename);

            PDFGenerator.addDocumentHeader(doc, "FICHE FOURNISSEUR", "SUP-" + s.getId(), java.time.LocalDate.now());

            // Check if we can get User info (Farm info)
            // Ideally we should inject UserDAO or have a session
            // For now, we will just list Supplier Info detailed

            doc.add(new Paragraph("Informations Générales").setFontSize(14).setBold().setMarginTop(10));

            Table infoTable = new Table(2).useAllAvailableWidth();
            infoTable.addCell("Nom / Société:");
            infoTable.addCell(s.getName());

            infoTable.addCell("Catégorie:");
            infoTable.addCell(s.getCategory());

            infoTable.addCell("Contact Principal:");
            infoTable.addCell(s.getContactPerson() != null ? s.getContactPerson() : "-");

            infoTable.addCell("Email:");
            infoTable.addCell(s.getEmail() != null ? s.getEmail() : "-");

            infoTable.addCell("Téléphone:");
            infoTable.addCell(s.getPhone() != null ? s.getPhone() : "-");

            infoTable.addCell("Adresse:");
            infoTable.addCell(s.getAddress() != null ? s.getAddress() : "-");

            infoTable.addCell("Site Web:");
            infoTable.addCell(s.getWebsite() != null ? s.getWebsite() : "-");

            doc.add(infoTable);

            // Legal & Financial
            doc.add(new Paragraph("Informations Légales & Financières").setFontSize(14).setBold().setMarginTop(20));
            Table finTable = new Table(2).useAllAvailableWidth();

            finTable.addCell("ICE:");
            finTable.addCell(s.getIce() != null ? s.getIce() : "-");

            finTable.addCell("RC:");
            finTable.addCell(s.getRc() != null ? s.getRc() : "-");

            finTable.addCell("Banque:");
            finTable.addCell(s.getBankName() != null ? s.getBankName() : "-");

            finTable.addCell("RIB:");
            finTable.addCell(s.getRib() != null ? s.getRib() : "-");

            finTable.addCell("Conditions de paiement:");
            finTable.addCell(s.getPaymentTerms() != null ? s.getPaymentTerms() : "-");

            doc.add(finTable);

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
