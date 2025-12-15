package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.Personnel;

import java.util.List;

/**
 * PersonnelController - Controls the Personnel view
 * Shows: Grid of personnel cards with basic info
 */
public class PersonnelController {

    // FXML Components
    @FXML
    private GridPane personnelGrid;

    @FXML
    private Label totalWorkersLabel;

    @FXML
    private Label totalTrackersLabel;

    // Statistics labels for new departments
    @FXML
    private Label totalPersonnelLabel;

    @FXML
    private Label totalVeterinaryLabel;

    @FXML
    private Label totalInventoryLabel;

    @FXML
    private Label totalSupervisorsLabel;

    @FXML
    private Label totalFarmhandsLabel;

    // Filter state
    private String currentFilter = "All"; // Default filter

    // DAO
    private PersonnelDAO personnelDAO;

    // Track selected personnel for edit/delete operations
    private Personnel selectedPersonnel;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAO
        personnelDAO = new PersonnelDAO();

        // Configure grid to prevent overlapping
        if (personnelGrid != null) {
            personnelGrid.setHgap(25);
            personnelGrid.setVgap(25);
        }

        // Load all operations personnel by default
        loadPersonnelData();

        // Update all statistics
        updateAllStatistics();
    }

    /**
     * Load and display personnel based on current filter
     */
    private void loadPersonnelData() {
        try {
            // Get personnel based on current filter
            List<Personnel> personnelList;

            switch (currentFilter) {
                case "Veterinary":
                    personnelList = personnelDAO.getAllVeterinary();
                    break;
                case "Inventory":
                    personnelList = personnelDAO.getAllInventoryTrackers();
                    break;
                case "Supervisors":
                    personnelList = personnelDAO.getAllSupervisors();
                    break;
                case "Farmhands":
                    personnelList = personnelDAO.getAllFarmhands();
                    break;
                case "All":
                default:
                    personnelList = personnelDAO.getOperationsPersonnel();
                    break;
            }

            // Clear grid
            if (personnelGrid != null) {
                personnelGrid.getChildren().clear();
            }

            // Create card for each personnel
            int row = 0;
            int col = 0;
            int maxColumns = 3; // 3 columns grid layout

            for (Personnel personnel : personnelList) {
                // Create card
                VBox card = createPersonnelCard(personnel);

                // Add click handler for selection
                card.setOnMouseClicked(event -> {
                    selectedPersonnel = personnel;
                    highlightSelectedCard(card);
                });

                // Set constraints to prevent overlap
                GridPane.setHgrow(card, Priority.ALWAYS);
                GridPane.setVgrow(card, Priority.NEVER);
                GridPane.setFillWidth(card, true);

                // Add to grid
                if (personnelGrid != null) {
                    personnelGrid.add(card, col, row);
                }

                // Move to next position
                col++;
                if (col >= maxColumns) {
                    col = 0;
                    row++;
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading personnel data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Highlight the selected card
     */
    private void highlightSelectedCard(VBox card) {
        // Remove highlight from all cards
        if (personnelGrid != null) {
            personnelGrid.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    node.setStyle(node.getStyle().replace("-fx-border-color: #007bff;", "-fx-border-color: #dee2e6;"));
                }
            });
        }

        // Highlight selected card
        card.setStyle(card.getStyle().replace("-fx-border-color: #dee2e6;", "-fx-border-color: #007bff;"));
    }

    /**
     * Create a personnel card
     * @param personnel Personnel to display
     * @return VBox card containing personnel info
     */
    private VBox createPersonnelCard(Personnel personnel) {
        // Create VBox card container
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));

        // Set card styling (border, padding, background)
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
        );

        // Set fixed size to prevent overlap
        card.setMinWidth(250);
        card.setMaxWidth(350);
        card.setPrefWidth(280);
        card.setMinHeight(180);
        card.setPrefHeight(200);
        card.setCursor(Cursor.HAND);

        // Add name label (bold, larger font)
        Label nameLabel = new Label(personnel.getFullName() != null ? personnel.getFullName() : "Unknown");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setStyle("-fx-text-fill: #212529;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        // Add job title label (with badge) - MOVED UP
        Label jobTitleLabel = new Label(personnel.getJobTitle() != null ? personnel.getJobTitle() : "Unknown");
        jobTitleLabel.setPadding(new Insets(5, 10, 5, 10));
        jobTitleLabel.setMaxWidth(Double.MAX_VALUE);

        // Apply job title badge color
        applyJobTitleBadge(jobTitleLabel, personnel.getJobTitle());

        // Add age label
        Label ageLabel = new Label("👤 Age: " + personnel.getAge() + " ans");
        ageLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");
        ageLabel.setWrapText(true);
        ageLabel.setMaxWidth(Double.MAX_VALUE);

        // Add phone label
        Label phoneLabel = new Label("📞 " + (personnel.getPhone() != null ? personnel.getPhone() : "N/A"));
        phoneLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");
        phoneLabel.setWrapText(true);
        phoneLabel.setMaxWidth(Double.MAX_VALUE);

        // Add email label
        Label emailLabel = new Label("✉️ " + (personnel.getEmail() != null ? personnel.getEmail() : "N/A"));
        emailLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
        emailLabel.setWrapText(true);
        emailLabel.setMaxWidth(Double.MAX_VALUE);

        // Add all labels to card
        card.getChildren().addAll(nameLabel, jobTitleLabel, ageLabel, phoneLabel, emailLabel);

        // Return card
        return card;
    }

    /**
     * Update personnel statistics
     */
    private void updateStatistics() {
        try {
            // Get all personnel
            List<Personnel> allPersonnel = personnelDAO.getAllPersonnel();

            // Count total workers (job title = "Worker")
            long workersCount = allPersonnel.stream()
                    .filter(p -> "Worker".equalsIgnoreCase(p.getJobTitle()))
                    .count();

            // Count total trackers (job title = "Tracker")
            long trackersCount = allPersonnel.stream()
                    .filter(p -> "Tracker".equalsIgnoreCase(p.getJobTitle()))
                    .count();

            // Update totalWorkersLabel
            if (totalWorkersLabel != null) {
                totalWorkersLabel.setText(String.valueOf(workersCount));
            }

            // Update totalTrackersLabel
            if (totalTrackersLabel != null) {
                totalTrackersLabel.setText(String.valueOf(trackersCount));
            }
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add personnel button click
     * Opens dialog to add new worker/tracker
     */
    @FXML
    public void handleAddPersonnel() {
        // TODO: Open add personnel dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Personnel");
        alert.setHeaderText("Add Personnel Feature");
        alert.setContentText("This feature will open a dialog to add new worker/tracker.\n\n" +
                "Required fields:\n" +
                "- Full name\n" +
                "- Age\n" +
                "- Phone\n" +
                "- Email\n" +
                "- Job title (Worker or Tracker)\n\n" +
                "Dialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get personnel details
        // - Validate inputs (email format, age > 0, etc.)
        // - Create Personnel record
        // - Save to database using personnelDAO.createPersonnel()
        // - Refresh personnel grid
    }

    /**
     * Handle edit personnel button click
     * Opens dialog to edit selected personnel
     */
    @FXML
    public void handleEditPersonnel() {
        // Get selected personnel
        if (selectedPersonnel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Personnel Selected");
            alert.setContentText("Please select a personnel card to edit.");
            alert.showAndWait();
            return;
        }

        // TODO: Open edit personnel dialog with current data
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Personnel");
        alert.setHeaderText("Edit Personnel Feature");
        alert.setContentText("This feature will open a dialog to edit the selected personnel.\n\n" +
                "Current personnel: " + selectedPersonnel.getFullName() + "\n\n" +
                "Dialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get updated personnel details
        // - Validate inputs
        // - Update Personnel record
        // - Save to database using personnelDAO.updatePersonnel()
        // - Refresh personnel grid
    }

    /**
     * Handle delete personnel button click
     * Deletes selected personnel
     */
    @FXML
    public void handleDeletePersonnel() {
        // Get selected personnel
        if (selectedPersonnel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Personnel Selected");
            alert.setContentText("Please select a personnel card to delete.");
            alert.showAndWait();
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Personnel");
        confirmAlert.setContentText("Are you sure you want to delete this personnel?\n\n" +
                "Name: " + selectedPersonnel.getFullName() + "\n" +
                "Job Title: " + selectedPersonnel.getJobTitle());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete from database
                    boolean success = personnelDAO.deletePersonnel(selectedPersonnel.getId());

                    if (success) {
                        // Clear selection
                        selectedPersonnel = null;

                        // Refresh personnel grid
                        refreshData();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Personnel Deleted");
                        alert.setContentText("The personnel has been successfully deleted.");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to Delete Personnel");
                        alert.setContentText("Could not delete the personnel. Please try again.");
                        alert.showAndWait();
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting personnel: " + e.getMessage());
                    e.printStackTrace();

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Database Error");
                    alert.setContentText("An error occurred while deleting the personnel: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    /**
     * Handle view personnel details button click
     * Shows detailed information about selected personnel
     */
    @FXML
    public void handleViewDetails() {
        // Get selected personnel
        if (selectedPersonnel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Personnel Selected");
            alert.setContentText("Please select a personnel card to view details.");
            alert.showAndWait();
            return;
        }

        // Show details dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Personnel Details");
        alert.setHeaderText("Details for " + selectedPersonnel.getFullName());

        StringBuilder details = new StringBuilder();
        details.append("Full Name: ").append(selectedPersonnel.getFullName()).append("\n");
        details.append("Age: ").append(selectedPersonnel.getAge()).append(" years\n");
        details.append("Phone: ").append(selectedPersonnel.getPhone() != null ? selectedPersonnel.getPhone() : "N/A").append("\n");
        details.append("Email: ").append(selectedPersonnel.getEmail() != null ? selectedPersonnel.getEmail() : "N/A").append("\n");
        details.append("Job Title: ").append(selectedPersonnel.getJobTitle() != null ? selectedPersonnel.getJobTitle() : "N/A").append("\n");

        if (selectedPersonnel.getHireDate() != null) {
            details.append("Hire Date: ").append(selectedPersonnel.getHireDate()).append("\n");
            details.append("Years of Service: ").append(selectedPersonnel.getYearsOfService()).append(" years\n");
        }

        if (selectedPersonnel.getSalary() > 0) {
            details.append("Salary: ").append(String.format("%.2f MAD", selectedPersonnel.getSalary())).append("\n");
        }

        if (selectedPersonnel.getShift() != null) {
            details.append("Shift: ").append(selectedPersonnel.getShift()).append("\n");
        }

        if (selectedPersonnel.getAddress() != null) {
            details.append("Address: ").append(selectedPersonnel.getAddress()).append("\n");
        }

        if (selectedPersonnel.getEmergencyContact() != null) {
            details.append("Emergency Contact: ").append(selectedPersonnel.getEmergencyContact()).append("\n");
        }

        details.append("Status: ").append(selectedPersonnel.isActive() ? "Active" : "Inactive");

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    /**
     * Handle filter by job title
     * @param jobTitle Job title to filter by (All, Worker, Tracker)
     */
    @FXML
    public void handleFilterByJobTitle(String jobTitle) {
        try {
            List<Personnel> filteredPersonnel;

            if ("All".equalsIgnoreCase(jobTitle)) {
                // Get all personnel
                filteredPersonnel = personnelDAO.getAllPersonnel();
            } else {
                // Get personnel by job title
                filteredPersonnel = personnelDAO.getPersonnelByJobTitle(jobTitle);
            }

            // Clear grid
            if (personnelGrid != null) {
                personnelGrid.getChildren().clear();
            }

            // Recreate grid with filtered personnel
            int row = 0;
            int col = 0;
            int maxColumns = 3;

            for (Personnel personnel : filteredPersonnel) {
                VBox card = createPersonnelCard(personnel);

                card.setOnMouseClicked(event -> {
                    selectedPersonnel = personnel;
                    highlightSelectedCard(card);
                });

                // Set constraints to prevent overlap
                GridPane.setHgrow(card, Priority.ALWAYS);
                GridPane.setVgrow(card, Priority.NEVER);
                GridPane.setFillWidth(card, true);

                if (personnelGrid != null) {
                    personnelGrid.add(card, col, row);
                }

                col++;
                if (col >= maxColumns) {
                    col = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error filtering personnel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apply job title badge styling
     * @param label Label to style
     * @param jobTitle Job title (Worker or Tracker)
     */
    private void applyJobTitleBadge(Label label, String jobTitle) {
        if (label == null || jobTitle == null) {
            return;
        }

        // Remove previous style classes
        label.getStyleClass().removeAll("job-tracker", "job-worker");

        // Apply color based on job title
        if ("Tracker".equalsIgnoreCase(jobTitle)) {
            // Blue background for Tracker
            label.setStyle(
                    "-fx-background-color: #007bff; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-alignment: center;"
            );
        } else if ("Worker".equalsIgnoreCase(jobTitle)) {
            // Gray background for Worker
            label.setStyle(
                    "-fx-background-color: #6c757d; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-alignment: center;"
            );
        } else {
            // Default styling
            label.setStyle(
                    "-fx-background-color: #6c757d; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-font-size: 13px; " +
                            "-fx-alignment: center;"
            );
        }
    }

    /**
     * Update all statistics labels
     */
    private void updateAllStatistics() {
        try {
            // Total operations personnel
            int totalPersonnel = personnelDAO.getOperationsPersonnel().size();
            if (totalPersonnelLabel != null) {
                totalPersonnelLabel.setText(String.valueOf(totalPersonnel));
            }

            // Still update old labels for backward compatibility
            if (totalWorkersLabel != null) {
                totalWorkersLabel.setText(String.valueOf(totalPersonnel));
            }

            // Veterinary staff
            int veterinaryCount = personnelDAO.countByJobTitle("veterinary");
            if (totalVeterinaryLabel != null) {
                totalVeterinaryLabel.setText(String.valueOf(veterinaryCount));
            }

            // Inventory trackers
            int inventoryCount = personnelDAO.countByJobTitle("inventory_tracker");
            if (totalInventoryLabel != null) {
                totalInventoryLabel.setText(String.valueOf(inventoryCount));
            }

            // Supervisors
            int supervisorsCount = personnelDAO.countByJobTitle("supervisor");
            if (totalSupervisorsLabel != null) {
                totalSupervisorsLabel.setText(String.valueOf(supervisorsCount));
            }

            // Farmhands
            int farmhandsCount = personnelDAO.countByJobTitle("farmhand");
            if (totalFarmhandsLabel != null) {
                totalFarmhandsLabel.setText(String.valueOf(farmhandsCount));
            }

            // Still update old tracker label
            if (totalTrackersLabel != null) {
                totalTrackersLabel.setText(String.valueOf(supervisorsCount));
            }

        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle filter button clicks
     * @param filterType the filter to apply (All, Veterinary, Inventory, Supervisors, Farmhands)
     */
    @FXML
    public void handleFilterChange(String filterType) {
        currentFilter = filterType;
        loadPersonnelData();
        System.out.println("Filter changed to: " + filterType);
    }

    /**
     * Refresh all data and statistics
     */
    @FXML
    public void refreshData() {
        loadPersonnelData();
        updateAllStatistics();
        selectedPersonnel = null;
    }
}
