package com.emsi.subtracker.controllers;

import com.emsi.subtracker.models.Abonnement;
import com.emsi.subtracker.models.User;
import com.emsi.subtracker.services.SubscriptionService;
import com.emsi.subtracker.utils.UserSession;
import com.emsi.subtracker.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable {

    @FXML
    private Label lblUsername;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblTotalSubscriptions;
    @FXML
    private Label lblTotalCost;
    @FXML
    private javafx.scene.control.ToggleButton themeToggle;

    private final SubscriptionService service = new SubscriptionService();
    private final DecimalFormat df = new DecimalFormat("0.00");

    @FXML
    private javafx.scene.image.ImageView profileImageView;

    private final com.emsi.subtracker.services.UserService userService = new com.emsi.subtracker.services.UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
        setupThemeToggle();
    }

    private void setupThemeToggle() {
        if (themeToggle != null) {
            boolean isDark = ThemeManager.isDarkTheme();
            themeToggle.setSelected(isDark);
            themeToggle.setText(isDark ? "ON" : "OFF");
            themeToggle.setStyle(isDark
                    ? "-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-background-radius: 20;"
                    : "-fx-background-color: #ccc; -fx-text-fill: black; -fx-background-radius: 20;");

            themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                ThemeManager.setDarkTheme(newVal);
                themeToggle.setText(newVal ? "ON" : "OFF");
                themeToggle.setStyle(newVal
                        ? "-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-background-radius: 20;"
                        : "-fx-background-color: #ccc; -fx-text-fill: black; -fx-background-radius: 20;");
            });
        }
    }

    private void loadUserData() {
        UserSession session = UserSession.getInstance();

        if (session.isFamilyMember()) {
            // Case: Family Member
            com.emsi.subtracker.models.FamilyMember member = session.getFamilyMember();
            if (member != null) {
                lblUsername.setText(member.getName());

                // Fetch Parent Email
                User parent = userService.getUserById(member.getUserId());
                String parentEmail = (parent != null) ? parent.getEmail() : "Email introuvable";

                lblEmail.setText("Compte Famille (" + parentEmail + ")");
                lblEmail.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 13; -fx-font-style: italic;");
            }
        } else {
            // Case: Parent User
            User user = session.getUser();
            if (user != null) {
                lblUsername.setText(user.getUsername());
                lblEmail.setText(user.getEmail());
                lblEmail.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 14;");

                // Load Profile Picture
                if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    loadProfileImage(user.getProfilePicture());
                }
            }
        }

        // Fetch stats (Service already handles filtering based on session)
        List<Abonnement> subs = service.getAll();
        lblTotalSubscriptions.setText(String.valueOf(subs.size()));

        double total = service.calculerTotalMensuel();
        lblTotalCost.setText(df.format(total) + " DH");
    }

    @FXML
    private void onEditProfilePicture() {
        UserSession session = UserSession.getInstance();
        if (session.isFamilyMember()) {
            showAlert("Action non autorisée", "Seul le compte principal peut changer la photo de profil.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        java.io.File file = fileChooser.showOpenDialog(lblUsername.getScene().getWindow());
        if (file != null) {
            saveProfilePicture(file);
        }
    }

    private void saveProfilePicture(java.io.File sourceFile) {
        try {
            User user = UserSession.getInstance().getUser();

            // Create target directory
            String userHome = System.getProperty("user.home");
            java.io.File appDir = new java.io.File(userHome, ".subtracker/images");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            // Generate filename: profile_USERID_TIMESTAMP.ext
            String ext = getFileExtension(sourceFile);
            String fileName = "profile_" + user.getId() + "_" + System.currentTimeMillis() + "." + ext;
            java.io.File targetFile = new java.io.File(appDir, fileName);

            // Copy file
            java.nio.file.Files.copy(sourceFile.toPath(), targetFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Update User Object
            String oldPath = user.getProfilePicture();
            user.setProfilePicture(targetFile.getAbsolutePath());

            // Persist to DB
            userService.updateUser(user);

            // Update UI
            loadProfileImage(targetFile.getAbsolutePath());

            // Cleanup old file if exists and different
            if (oldPath != null && !oldPath.equals(targetFile.getAbsolutePath())) {
                try {
                    java.io.File oldFile = new java.io.File(oldPath);
                    if (oldFile.exists() && oldFile.getName().startsWith("profile_")) {
                        oldFile.delete();
                    }
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de sauvegarder l'image : " + e.getMessage());
        }
    }

    private void loadProfileImage(String path) {
        try {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
                profileImageView.setImage(img);

                // Keep the circle clip
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(45, 45, 45);
                profileImageView.setClip(clip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileExtension(java.io.File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "png"; // default
        }
        return name.substring(lastIndexOf + 1);
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblUsername.getScene().getWindow();
        stage.close();
    }
}
