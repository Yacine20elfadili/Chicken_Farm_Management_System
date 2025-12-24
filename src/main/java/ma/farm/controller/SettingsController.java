package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.farm.dao.DatabaseConnection;
import ma.farm.dao.UserDAO;
import ma.farm.model.User;
import ma.farm.util.ValidationUtil;

import java.io.IOException;
import java.util.Optional;

public class SettingsController {

    @FXML
    private TextField companyNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField emailField;

    @FXML
    private Button saveButton;
    @FXML
    private Button deleteAccountButton;

    private UserDAO userDAO;
    private User currentUser;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        loadCurrentUser();

        saveButton.setOnAction(event -> handleSave());
        deleteAccountButton.setOnAction(event -> handleDeleteAccount());
    }

    private void loadCurrentUser() {
        // In a real app, we'd have a SessionManager. For now, we'll fetch the first
        // user or mocked login.
        // Assuming single-user desktop app for now, we pick ID 1 or the first user in
        // DB.
        // Or if we had a Session class, we'd use that.
        // Based on LoginController, we don't seem to have a global Session yet publicly
        // visible in my snippets,
        // but typically the logged-in user is passed or stored.
        // I will assume ID 1 for now or get the first user found.

        var users = userDAO.getAllUsers();
        if (!users.isEmpty()) {
            currentUser = users.get(0);
            populateFields();
        } else {
            // No user found?
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun utilisateur trouvé.");
        }
    }

    private void populateFields() {
        if (currentUser != null) {
            companyNameField.setText(currentUser.getCompanyName());
            phoneField.setText(currentUser.getPhone());
            addressField.setText(currentUser.getAddress());
            cityField.setText(currentUser.getCity());
            emailField.setText(currentUser.getEmail());
        }
    }

    private void handleSave() {
        if (currentUser == null)
            return;

        // Basic validation
        if (companyNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom de la société est obligatoire.");
            return;
        }

        currentUser.setCompanyName(companyNameField.getText());
        currentUser.setPhone(phoneField.getText());
        currentUser.setAddress(addressField.getText());
        currentUser.setCity(cityField.getText());
        currentUser.setEmail(emailField.getText()); // Note: Changing email might affect login

        if (userDAO.updateUser(currentUser)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Informations mises à jour avec succès.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour.");
        }
    }

    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le compte");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        alert.setContentText("Cette action est IRRÉVERSIBLE. Toutes les données seront perdues.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Perform Database Reset
            boolean reset = DatabaseConnection.getInstance().resetDatabase();

            if (reset) {
                showAlert(Alert.AlertType.INFORMATION, "Compte Supprimé",
                        "Votre compte et toutes les données ont été supprimés.");
                logout();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression des données.");
            }
        }
    }

    private void logout() {
        try {
            // Load Login View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) deleteAccountButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
