package com.emsi.subtracker.controllers;

import com.emsi.subtracker.models.Abonnement;
import com.emsi.subtracker.models.User;
import com.emsi.subtracker.services.SubscriptionService;
import com.emsi.subtracker.services.UserService;
import com.emsi.subtracker.utils.UserSession;
import com.emsi.subtracker.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    protected void goToDashboard() {
        try {
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            SceneManager.switchScene(currentStage, "dashboard.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void goToAnalytics() {
        try {
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            SceneManager.switchScene(currentStage, "analytics.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField newPasswordField;

    @FXML
    private ToggleButton themeToggle;

    @FXML
    private ToggleGroup currencyGroup;
    @FXML
    private RadioButton currencyDH;
    @FXML
    private RadioButton currencyEUR;
    @FXML
    private RadioButton currencyUSD;

    private final UserService userService = new UserService();
    private final SubscriptionService subscriptionService = new SubscriptionService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadUserData();
        loadCurrencyPreference();
        initThemeToggle();

        // Apply Theme
        javafx.application.Platform.runLater(() -> {
            if (usernameField.getScene() != null) {
                // Initial application of theme
                com.emsi.subtracker.utils.ThemeManager.applyTheme(usernameField.getScene());
            }
        });
    }

    private void initThemeToggle() {
        if (themeToggle != null) {
            themeToggle.setSelected(com.emsi.subtracker.utils.ThemeManager.isDarkTheme());
            themeToggle.setText(com.emsi.subtracker.utils.ThemeManager.isDarkTheme() ? "Mode Sombre" : "Mode Clair");

            themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                boolean isDark = newVal;
                com.emsi.subtracker.utils.ThemeManager.setDarkTheme(isDark);
                themeToggle.setText(isDark ? "Mode Sombre" : "Mode Clair");
            });
        }
    }

    private void loadUserData() {
        User user = UserSession.getInstance().getUser();
        if (user != null) {
            if (usernameField != null)
                usernameField.setText(user.getUsername());
            if (emailField != null)
                emailField.setText(user.getEmail());
        }
    }

    private void loadCurrencyPreference() {
        String current = UserSession.getInstance().getCurrency();
        if ("EUR".equals(current))
            currencyEUR.setSelected(true);
        else if ("USD".equals(current))
            currencyUSD.setSelected(true);
        else
            currencyDH.setSelected(true);
    }

    @FXML
    private void onConfirmCurrency() {
        String selected = "DH";
        if (currencyEUR.isSelected())
            selected = "EUR";
        else if (currencyUSD.isSelected())
            selected = "USD";

        updateCurrency(selected);
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Devise mise à jour en " + selected);
    }

    private void updateCurrency(String currency) {
        UserSession.getInstance().setCurrency(currency);
    }

    @FXML
    private void onSaveAccount() {
        User user = UserSession.getInstance().getUser();
        if (user != null) {
            user.setUsername(usernameField.getText());
            user.setEmail(emailField.getText());
            try {
                userService.updateUser(user);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Informations mises à jour !");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
            }
        }
    }

    @FXML
    private void onChangePassword() {
        User user = UserSession.getInstance().getUser();
        String newPass = newPasswordField.getText();
        if (user != null && newPass != null && !newPass.isEmpty()) {
            userService.changePassword(user, newPass);
            newPasswordField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe modifié !");
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer un mot de passe.");
        }
    }

    @FXML
    private void onLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Vous allez être déconnecté.");
        alert.setContentText("Voulez-vous continuer ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserSession.getInstance().cleanUserSession();
            closeWindow();

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(fxmlLoader.load());
                scene.getStylesheets().add(getClass().getResource("/styles_v2.css").toExternalForm());
                com.emsi.subtracker.utils.ThemeManager.applyTheme(scene);

                stage.setTitle("SubTracker - Login");
                stage.setScene(scene);
                stage.show();

                javafx.stage.Window.getWindows().stream()
                        .filter(w -> w instanceof Stage && w != stage)
                        .forEach(w -> ((Stage) w).close());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zone Danger");
        alert.setHeaderText("Supprimer le compte ?");
        alert.setContentText("Cette action est irréversible. Êtes-vous sûr ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            User user = UserSession.getInstance().getUser();
            if (user != null) {
                subscriptionService.removeAll();
                userService.deleteUser(user);

                UserSession.getInstance().cleanUserSession();
                closeWindow();
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                    Stage stage = new Stage();
                    Scene scene = new Scene(fxmlLoader.load());
                    scene.getStylesheets().add(getClass().getResource("/styles_v2.css").toExternalForm());
                    com.emsi.subtracker.utils.ThemeManager.applyTheme(scene);

                    stage.setScene(scene);
                    stage.show();

                    javafx.stage.Window.getWindows().stream()
                            .filter(w -> w instanceof Stage && w != stage)
                            .forEach(w -> ((Stage) w).close());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void onExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les Abonnements");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(usernameField.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Nom,Prix,Frequence,Categorie,DateDebut");
                List<Abonnement> subs = subscriptionService.getAll();
                for (Abonnement sub : subs) {
                    writer.printf("%s,%.2f,%s,%s,%s%n",
                            sub.getNom(), sub.getPrix(), sub.getFrequence(), sub.getCategorie(), sub.getDateDebut());
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Exportation réussie !");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onImportData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Importer CSV");
        alert.setHeaderText("Importer depuis data_abonnements.csv ?");
        alert.setContentText("Cela ajoutera les données du fichier au compte actuel.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // CSV import feature removed - application uses SQL Server exclusively
                showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité désactivée",
                        "L'application utilise maintenant uniquement SQL Server.\nVeuillez importer vos données directement en base de données.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'importation : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void closeWindow() {
        Stage stage = null;
        if (usernameField != null && usernameField.getScene() != null) {
            stage = (Stage) usernameField.getScene().getWindow();
        }

        if (stage != null) {
            stage.close();
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
