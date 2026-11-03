package com.emsi.subtracker.controllers;

import com.emsi.subtracker.models.Abonnement;
import com.emsi.subtracker.models.FamilyMember;
import com.emsi.subtracker.models.User;
import com.emsi.subtracker.services.FamilyService;
import com.emsi.subtracker.services.SubscriptionService;
import com.emsi.subtracker.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du formulaire d'ajout d'abonnement.
 */
public class AddSubscriptionController implements Initializable {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrix;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private ComboBox<String> cmbFrequence;
    @FXML
    private ComboBox<String> cmbCategorie;
    @FXML
    private javafx.scene.layout.VBox assignToContainer;
    @FXML
    private ComboBox<String> assignToComboBox;
    @FXML
    private Label assignToLabel;

    private final SubscriptionService service = new SubscriptionService();
    private final FamilyService familyService = new FamilyService();
    private List<FamilyMember> familyMembers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des ComboBox
        cmbFrequence.getItems().addAll("Mensuel", "Annuel");
        cmbFrequence.getSelectionModel().selectFirst();

        cmbCategorie.getItems().addAll("Divertissement", "Travail", "Sport", "Musique", "Autre");
        cmbCategorie.getSelectionModel().select("Autre");

        dateDebut.setValue(LocalDate.now());

        UserSession session = UserSession.getInstance();
        User user = session.getUser();

        System.out.println("AddSubscription Init - Session User: " + user);
        if (user != null) {
            System.out.println("  -> Is Family Account? " + user.isFamilyAccount());
            System.out.println("  -> Account Type: " + user.getAccountType());
        }

        if (user != null && user.isFamilyAccount()) {
            familyMembers = familyService.getFamilyMembers(user.getId());

            assignToComboBox.getItems().clear();
            assignToComboBox.getItems().add("Moi (Compte principal)");
            for (FamilyMember member : familyMembers) {
                assignToComboBox.getItems().add(member.getName());
            }

            assignToComboBox.getSelectionModel().selectFirst();

            // Show container
            if (assignToContainer != null) {
                assignToContainer.setVisible(true);
                assignToContainer.setManaged(true);
            }
        } else {
            if (assignToContainer != null) {
                assignToContainer.setVisible(false);
                assignToContainer.setManaged(false);
            }
        }

        javafx.application.Platform.runLater(() -> {
            if (txtNom.getScene() != null) {
                com.emsi.subtracker.utils.ThemeManager.applyTheme(txtNom.getScene());
            }
        });
    }

    private Abonnement currentAbonnement;

    @FXML
    protected void onBtnEnregistrerClick() {
        if (validerChamps()) {
            try {
                String nom = txtNom.getText();
                double prix = Double.parseDouble(txtPrix.getText());
                LocalDate date = dateDebut.getValue();
                String frequence = cmbFrequence.getValue();
                String categorie = cmbCategorie.getValue();

                if (currentAbonnement != null) {
                    currentAbonnement.setNom(nom);
                    currentAbonnement.setPrix(prix);
                    currentAbonnement.setDateDebut(date);
                    currentAbonnement.setFrequence(frequence);
                    currentAbonnement.setCategorie(categorie);

                    Integer assignedMemberId = getAssignedMemberId();
                    currentAbonnement.setAssignedToMemberId(assignedMemberId);

                    com.emsi.subtracker.services.BrandingService brandingObj = com.emsi.subtracker.services.BrandingService
                            .getInstance();
                    currentAbonnement.setLogoUrl(brandingObj.getLogoUrl(nom));
                    currentAbonnement.setColorHex(brandingObj.getDominantColor(nom));

                    service.update(currentAbonnement);
                } else {
                    UserSession session = UserSession.getInstance();
                    int userId = session.getUserId();
                    Integer assignedMemberId = getAssignedMemberId();

                    int id = (int) (System.currentTimeMillis() % 100000);
                    Abonnement nouvelAbonnement = new Abonnement(
                            id, nom, prix, date, frequence, categorie, userId, assignedMemberId);

                    com.emsi.subtracker.services.BrandingService brandingObj = com.emsi.subtracker.services.BrandingService
                            .getInstance();
                    nouvelAbonnement.setLogoUrl(brandingObj.getLogoUrl(nom));
                    nouvelAbonnement.setColorHex(brandingObj.getDominantColor(nom));

                    service.add(nouvelAbonnement);
                }

                retourAuDashboard();

            } catch (NumberFormatException e) {
                afficherErreur("Le prix doit être un nombre valide.");
            }
        }
    }

    public void setAbonnement(Abonnement abonnement) {
        this.currentAbonnement = abonnement;
        if (abonnement != null) {
            txtNom.setText(abonnement.getNom());
            txtPrix.setText(String.valueOf(abonnement.getPrix()));
            dateDebut.setValue(abonnement.getDateDebut());
            cmbFrequence.setValue(abonnement.getFrequence());
            cmbCategorie.setValue(abonnement.getCategorie());
        }
    }

    @FXML
    protected void onBtnAnnulerClick() {
        retourAuDashboard();
    }

    private void retourAuDashboard() {
        Stage currentStage = (Stage) txtNom.getScene().getWindow();
        currentStage.close();
    }

    private boolean validerChamps() {
        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            afficherErreur("Le nom est obligatoire.");
            return false;
        }
        if (txtPrix.getText() == null || txtPrix.getText().trim().isEmpty()) {
            afficherErreur("Le prix est obligatoire.");
            return false;
        }
        if (dateDebut.getValue() == null) {
            afficherErreur("La date est obligatoire.");
            return false;
        }
        return true;
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.show();
    }


    private Integer getAssignedMemberId() {
        UserSession session = UserSession.getInstance();

        if (session.isFamilyMember()) {
            return session.getFamilyMember().getId();
        } else if (session.getUser() != null && session.getUser().isFamilyAccount()) {
            if (assignToComboBox.isVisible()) {
                int selectedIndex = assignToComboBox.getSelectionModel().getSelectedIndex();
                if (selectedIndex > 0) {
                    return familyMembers.get(selectedIndex - 1).getId();
                }
            }
        }

        return null;
    }
}
