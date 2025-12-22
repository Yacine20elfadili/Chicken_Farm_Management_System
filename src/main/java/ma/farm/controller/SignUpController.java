package ma.farm.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import ma.farm.dao.UserDAO;
import ma.farm.model.User;
import ma.farm.util.ValidationUtil;
import java.io.IOException;

/**
 * Controller for the sign-up page.
 * Handles Moroccan business registration with all required legal fields.
 */
public class SignUpController {

	// Company Information Fields
	@FXML
	private TextField companyNameField;
	@FXML
	private ComboBox<String> legalFormCombo;
	@FXML
	private TextField capitalSocialField;

	// Legal Identifiers (Moroccan)
	@FXML
	private TextField iceField;
	@FXML
	private TextField rcField;
	@FXML
	private TextField fiscalIdField;
	@FXML
	private TextField patenteField;
	@FXML
	private TextField cnssField;
	@FXML
	private TextField onssaField;

	// Address
	@FXML
	private TextField addressField;
	@FXML
	private TextField cityField;
	@FXML
	private TextField postalCodeField;

	// Banking
	@FXML
	private TextField bankRIBField;
	@FXML
	private TextField bankNameField;

	// Contact
	@FXML
	private TextField phoneNumberField;
	@FXML
	private TextField emailField;
	@FXML
	private TextField websiteField;

	// Account Credentials
	@FXML
	private PasswordField passwordField;
	@FXML
	private PasswordField confirmPasswordField;

	// UI Elements
	@FXML
	private Button signUpButton;
	@FXML
	private Button backToLoginButton;
	@FXML
	private Label errorLabel;
	@FXML
	private Label successLabel;

	// DAO
	private UserDAO userDAO;

	/**
	 * Initializes the controller. Sets up DAOs and UI listeners.
	 */
	@FXML
	public void initialize() {
		userDAO = new UserDAO();

		// Hide labels initially
		errorLabel.setVisible(false);
		successLabel.setVisible(false);

		// Setup legal form combo box
		legalFormCombo.setItems(FXCollections.observableArrayList(ValidationUtil.getLegalForms()));
		legalFormCombo.setValue("SARL"); // Default value

		// Set sample data for development/testing
		setSampleData();

		// Add enter key listener to confirmPasswordField to trigger sign up
		confirmPasswordField.setOnAction(this::handleSignUp);

		// Setup text formatters/listeners for restricted input fields
		setupInputRestrictions();
	}

	/**
	 * Sets sample Moroccan business data for development/testing
	 */
	private void setSampleData() {
		companyNameField.setText("Ferme Avicole Al Amal SARL");
		capitalSocialField.setText("100000");
		iceField.setText("002532678000045");
		rcField.setText("RC 12345 Casablanca");
		fiscalIdField.setText("12345678");
		patenteField.setText("1234567");
		cnssField.setText("1234567");
		onssaField.setText("ONSSA-AV-2024-0123");
		addressField.setText("123 Avenue Mohammed V, Quartier Industriel");
		cityField.setText("Casablanca");
		postalCodeField.setText("20000");
		bankRIBField.setText("230780000012345678901234");
		bankNameField.setText("Attijariwafa Bank");
		phoneNumberField.setText("+212 522-123456");
		emailField.setText("admin@farm.ma");
		websiteField.setText("");
		passwordField.setText("admin123");
		confirmPasswordField.setText("admin123");
	}

	/**
	 * Setup input restrictions for numeric and formatted fields
	 */
	private void setupInputRestrictions() {
		// ICE: exactly 15 digits
		iceField.textProperty().addListener((obs, oldText, newText) -> {
			if (!newText.matches("\\d*")) {
				iceField.setText(newText.replaceAll("[^\\d]", ""));
			}
			if (iceField.getText().length() > 15) {
				iceField.setText(iceField.getText().substring(0, 15));
			}
		});

		// RIB: exactly 24 digits
		bankRIBField.textProperty().addListener((obs, oldText, newText) -> {
			if (!newText.matches("\\d*")) {
				bankRIBField.setText(newText.replaceAll("[^\\d]", ""));
			}
			if (bankRIBField.getText().length() > 24) {
				bankRIBField.setText(bankRIBField.getText().substring(0, 24));
			}
		});

		// Postal code: exactly 5 digits
		postalCodeField.textProperty().addListener((obs, oldText, newText) -> {
			if (!newText.matches("\\d*")) {
				postalCodeField.setText(newText.replaceAll("[^\\d]", ""));
			}
			if (postalCodeField.getText().length() > 5) {
				postalCodeField.setText(postalCodeField.getText().substring(0, 5));
			}
		});

		// Fiscal ID: 7-8 digits
		fiscalIdField.textProperty().addListener((obs, oldText, newText) -> {
			if (!newText.matches("\\d*")) {
				fiscalIdField.setText(newText.replaceAll("[^\\d]", ""));
			}
			if (fiscalIdField.getText().length() > 8) {
				fiscalIdField.setText(fiscalIdField.getText().substring(0, 8));
			}
		});

		// CNSS: 7-9 digits
		cnssField.textProperty().addListener((obs, oldText, newText) -> {
			if (!newText.matches("\\d*")) {
				cnssField.setText(newText.replaceAll("[^\\d]", ""));
			}
			if (cnssField.getText().length() > 9) {
				cnssField.setText(cnssField.getText().substring(0, 9));
			}
		});

		// Capital social: numeric only
		capitalSocialField.textProperty().addListener((obs, oldText, newText) -> {
			if (!newText.matches("\\d*")) {
				capitalSocialField.setText(newText.replaceAll("[^\\d]", ""));
			}
		});

		// Patente: numeric only
		patenteField.textProperty().addListener((obs, oldText, newText) -> {
			if (!newText.matches("\\d*")) {
				patenteField.setText(newText.replaceAll("[^\\d]", ""));
			}
		});
	}

	/**
	 * Handles sign up button click
	 */
	@FXML
	private void handleSignUp(ActionEvent event) {
		errorLabel.setVisible(false);
		successLabel.setVisible(false);

		// Collect and sanitize all inputs
		String companyName = ValidationUtil.sanitize(companyNameField.getText());
		String legalForm = legalFormCombo.getValue();
		String capitalStr = ValidationUtil.sanitize(capitalSocialField.getText());
		String ice = ValidationUtil.sanitize(iceField.getText());
		String rc = ValidationUtil.sanitize(rcField.getText());
		String fiscalId = ValidationUtil.sanitize(fiscalIdField.getText());
		String patenteStr = ValidationUtil.sanitize(patenteField.getText());
		String cnss = ValidationUtil.sanitize(cnssField.getText());
		String onssa = ValidationUtil.sanitize(onssaField.getText());
		String address = ValidationUtil.sanitize(addressField.getText());
		String city = ValidationUtil.sanitize(cityField.getText());
		String postalCode = ValidationUtil.sanitize(postalCodeField.getText());
		String bankRIB = ValidationUtil.sanitize(bankRIBField.getText());
		String bankName = ValidationUtil.sanitize(bankNameField.getText());
		String phoneNumber = ValidationUtil.sanitize(phoneNumberField.getText());
		String email = ValidationUtil.sanitize(emailField.getText());
		String website = ValidationUtil.sanitize(websiteField.getText());
		String password = passwordField.getText();
		String confirmPassword = confirmPasswordField.getText();

		// Validate all inputs
		if (!validateAllInputs(companyName, legalForm, capitalStr, ice, rc, fiscalId, patenteStr,
				cnss, onssa, address, city, postalCode, bankRIB, bankName,
				phoneNumber, email, website, password, confirmPassword)) {
			return; // Validation failed, error already shown
		}

		// Parse integer fields
		int capitalSocial = Integer.parseInt(capitalStr);
		int patente = Integer.parseInt(patenteStr);

		// Check uniqueness
		if (!checkUniqueness(email, ice, bankRIB)) {
			return;
		}

		// Create User object
		User user = new User(
				email, password, companyName, legalForm, capitalSocial,
				ice, rc, fiscalId, patente, cnss.isEmpty() ? null : cnss, onssa,
				address, city, postalCode, bankRIB, bankName, phoneNumber,
				website.isEmpty() ? null : website);

		// Attempt to create user
		try {
			boolean success = userDAO.createUser(user);

			if (success) {
				showSuccess("Inscription réussie ! Redirection vers la connexion...");

				// Wait 2 seconds then redirect to login
				new Thread(() -> {
					try {
						Thread.sleep(2000);
						javafx.application.Platform.runLater(this::navigateToLogin);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}).start();

			} else {
				showError("Erreur lors de la création du compte. Veuillez réessayer.");
			}

		} catch (Exception e) {
			System.err.println("Sign up error: " + e.getMessage());
			e.printStackTrace();
			showError("Erreur lors de l'inscription. Veuillez réessayer.");
		}
	}

	/**
	 * Validates all form fields
	 */
	private boolean validateAllInputs(String companyName, String legalForm, String capitalStr,
			String ice, String rc, String fiscalId, String patenteStr,
			String cnss, String onssa, String address, String city,
			String postalCode, String bankRIB, String bankName,
			String phoneNumber, String email, String website,
			String password, String confirmPassword) {
		// Company Information
		if (!ValidationUtil.isValidCompanyName(companyName)) {
			showError("Le nom de société doit contenir entre 3 et 200 caractères.");
			companyNameField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidLegalForm(legalForm)) {
			showError("Veuillez sélectionner une forme juridique.");
			legalFormCombo.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidCapitalSocialStr(capitalStr)) {
			showError("Le capital social doit être un nombre positif.");
			capitalSocialField.requestFocus();
			return false;
		}

		int capital = Integer.parseInt(capitalStr);
		if (!ValidationUtil.isValidCapitalSocial(capital, legalForm)) {
			if ("SA".equals(legalForm)) {
				showError("Le capital social minimum pour SA est 300,000 MAD.");
			} else if ("SARL".equals(legalForm)) {
				showError("Le capital social minimum pour SARL est 10,000 MAD.");
			}
			capitalSocialField.requestFocus();
			return false;
		}

		// Legal Identifiers
		if (!ValidationUtil.isValidICE(ice)) {
			showError("⚠️ L'ICE doit contenir exactement 15 chiffres. (Obligatoire par la loi marocaine)");
			iceField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidRC(rc)) {
			showError("Le Registre de Commerce doit être renseigné.");
			rcField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidIF(fiscalId)) {
			showError("L'Identifiant Fiscal (IF) doit contenir 7 à 8 chiffres.");
			fiscalIdField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidPatente(patenteStr)) {
			showError("Le numéro de patente doit être un nombre positif.");
			patenteField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidCNSS(cnss)) {
			showError("Le numéro CNSS doit contenir 7 à 9 chiffres (ou vide si pas d'employés).");
			cnssField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidONSSA(onssa)) {
			showError("⚠️ L'autorisation ONSSA est obligatoire pour les fermes avicoles.");
			onssaField.requestFocus();
			return false;
		}

		// Address
		if (!ValidationUtil.isValidAddress(address)) {
			showError("L'adresse doit contenir au moins 10 caractères.");
			addressField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidCity(city)) {
			showError("Veuillez entrer le nom de la ville.");
			cityField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidPostalCode(postalCode)) {
			showError("Le code postal doit contenir exactement 5 chiffres.");
			postalCodeField.requestFocus();
			return false;
		}

		// Banking
		if (!ValidationUtil.isValidRIB(bankRIB)) {
			showError("⚠️ Le RIB bancaire doit contenir exactement 24 chiffres. (Critique pour les paiements)");
			bankRIBField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidBankName(bankName)) {
			showError("Veuillez entrer le nom de la banque.");
			bankNameField.requestFocus();
			return false;
		}

		// Contact
		if (!ValidationUtil.isValidPhoneNumber(phoneNumber)) {
			showError("Format de téléphone invalide. Utilisez +212 XXX-XXXXXX ou 0XXXXXXXXX.");
			phoneNumberField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidEmail(email)) {
			showError("Format d'email invalide.");
			emailField.requestFocus();
			return false;
		}

		if (!ValidationUtil.isValidWebsite(website)) {
			showError("Format d'URL invalide pour le site web.");
			websiteField.requestFocus();
			return false;
		}

		// Password
		if (!ValidationUtil.isValidPassword(password)) {
			showError("Le mot de passe doit contenir au moins 6 caractères.");
			passwordField.requestFocus();
			return false;
		}

		if (!password.equals(confirmPassword)) {
			showError("Les mots de passe ne correspondent pas.");
			confirmPasswordField.requestFocus();
			return false;
		}

		return true;
	}

	/**
	 * Check uniqueness of email, ICE, and RIB
	 */
	private boolean checkUniqueness(String email, String ice, String bankRIB) {
		if (userDAO.isEmailExists(email)) {
			showError("Cette adresse email existe déjà.");
			emailField.requestFocus();
			return false;
		}

		if (userDAO.isICEExists(ice)) {
			showError("Ce numéro ICE existe déjà dans le système.");
			iceField.requestFocus();
			return false;
		}

		if (userDAO.isRIBExists(bankRIB)) {
			showError("Ce RIB bancaire existe déjà dans le système.");
			bankRIBField.requestFocus();
			return false;
		}

		return true;
	}

	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setVisible(true);
		successLabel.setVisible(false);
	}

	private void showSuccess(String message) {
		successLabel.setText(message);
		successLabel.setVisible(true);
		errorLabel.setVisible(false);
	}

	@FXML
	private void handleBackToLogin(ActionEvent event) {
		navigateToLogin();
	}

	private void navigateToLogin() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
			Parent loginRoot = loader.load();

			Stage stage = (Stage) signUpButton.getScene().getWindow();
			Scene loginScene = new Scene(loginRoot);
			stage.setScene(loginScene);
			stage.setTitle("Chicken Farm Management - Login");

			System.out.println("Navigated to login page successfully!");

		} catch (IOException e) {
			System.err.println("Error loading login page: " + e.getMessage());
			e.printStackTrace();
			showError("Erreur lors du chargement de la page de connexion.");
		}
	}
}
