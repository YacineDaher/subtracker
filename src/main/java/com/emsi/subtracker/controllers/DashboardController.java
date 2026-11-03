package com.emsi.subtracker.controllers;

import com.emsi.subtracker.models.Abonnement;
import com.emsi.subtracker.models.User;
import com.emsi.subtracker.services.EmailService;
import com.emsi.subtracker.services.SubscriptionService;
import com.emsi.subtracker.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de l'écran principal (Dashboard) - Version Cartes Visuelles.
 */
public class DashboardController implements Initializable {

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblTotalMensuel;

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private ComboBox<String> sortFilter;

    @FXML
    private ToggleButton themeToggleDashboard;

    private final SubscriptionService service = new SubscriptionService();
    private final EmailService emailService = EmailService.getInstance();
    private final DecimalFormat df = new DecimalFormat("0.00");

    private List<Abonnement> allSubscriptions;

    private final com.emsi.subtracker.services.FamilyService familyService = new com.emsi.subtracker.services.FamilyService();
    private java.util.Map<Integer, String> familyMemberNames = new java.util.HashMap<>();

    @FXML
    private Label lblNombreAbonnements;

    @FXML
    private javafx.scene.image.ImageView userAvatar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("DashboardController initialization started...");

            // ✅ Init Theme Toggle
            if (themeToggleDashboard != null) {
                boolean isDark = com.emsi.subtracker.utils.ThemeManager.isDarkTheme();
                themeToggleDashboard.setSelected(isDark);
                themeToggleDashboard.setText(isDark ? "☀️" : "🌙");
            }

            // 1. Init Category Filter
            if (categoryFilter != null) {
                categoryFilter.getItems().addAll("Toutes", "Divertissement", "Travail", "Sport", "Musique", "Autre");
                categoryFilter.getSelectionModel().selectFirst();
                categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterSubscriptions());
            }

            // 2. Init Sort Filter
            if (sortFilter != null) {
                sortFilter.getItems().addAll("Par défaut", "Prix croissant", "Prix décroissant", "Date de début");
                sortFilter.getSelectionModel().selectFirst();
                sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterSubscriptions());
            }

            // 3. Init Search Listener
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterSubscriptions();
                });
            } else {
                System.err.println("WARNING: searchField is null in DashboardController");
            }

            // 4. Preload Family Members for Name Resolution
            preloadFamilyMembers();

            refreshDashboard();

            // ✅ INTÉGRATION EMAIL: Vérifier et envoyer les alertes de renouvellement J-3
            checkRenewalAlerts();

            System.out.println("DashboardController initialization completed.");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR initializing DashboardController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateActiveWindows() {
        if (lblTotalMensuel != null && lblTotalMensuel.getScene() != null) {
            com.emsi.subtracker.utils.ThemeManager.applyTheme(lblTotalMensuel.getScene());
        }
    }

    private void refreshDashboard() {
        com.emsi.subtracker.models.User currentUser = com.emsi.subtracker.utils.UserSession.getInstance().getUser();

        if (lblWelcome != null) {
            if (currentUser != null) {
                lblWelcome.setText("Bonjour, " + currentUser.getUsername());
            } else if (com.emsi.subtracker.utils.UserSession.getInstance().isFamilyMember()) {
                lblWelcome.setText(
                        "Bonjour, " + com.emsi.subtracker.utils.UserSession.getInstance().getFamilyMember().getName());
            }
        }

        // Fetch Data once
        this.allSubscriptions = service.getAll();

        // 1. Mise à jour du total
        double total = service.calculerTotalMensuel();

        // Currency Conversion
        String currency = com.emsi.subtracker.utils.UserSession.getInstance().getCurrency();
        double displayTotal = convertPrice(total, currency);
        String symbol = getCurrencySymbol(currency);

        lblTotalMensuel.setText(df.format(displayTotal) + " " + symbol);

        // 2. Refresh List
        filterSubscriptions();

        // 3. Sync Active Stats
        if (lblNombreAbonnements != null) {
            lblNombreAbonnements.setText(String.valueOf(allSubscriptions.size()));
        }

        // 4. Update User Avatar
        loadUserAvatar();
    }

    private void loadUserAvatar() {
        com.emsi.subtracker.utils.UIUtils.loadUserAvatar(userAvatar, 22.5);
    }

    private double convertPrice(double priceInDH, String targetCurrency) {
        switch (targetCurrency) {
            case "EUR":
                return priceInDH * 0.091; // Approx rate
            case "USD":
                return priceInDH * 0.099; // Approx rate
            default:
                return priceInDH;
        }
    }

    private String getCurrencySymbol(String currency) {
        switch (currency) {
            case "EUR":
                return "€";
            case "USD":
                return "$";
            default:
                return "DH";
        }
    }

    private void filterSubscriptions() {
        if (allSubscriptions == null)
            return;

        cardsContainer.getChildren().clear();

        String query = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase()
                : "";
        String category = (categoryFilter != null) ? categoryFilter.getValue() : "Toutes";
        String sortMode = (sortFilter != null) ? sortFilter.getValue() : "Par défaut";

        List<Abonnement> filtered = allSubscriptions.stream()
                .filter(sub -> {
                    // Filter by Search
                    boolean matchesSearch = query.isEmpty()
                            || (sub.getNom() != null && sub.getNom().toLowerCase().contains(query));

                    // Filter by Category
                    boolean matchesCategory = "Toutes".equals(category)
                            || (sub.getCategorie() != null && sub.getCategorie().equalsIgnoreCase(category));

                    return matchesSearch && matchesCategory;
                })
                .collect(java.util.stream.Collectors.toList());

        // Sort
        if ("Prix croissant".equals(sortMode)) {
            filtered.sort(java.util.Comparator.comparingDouble(Abonnement::getPrix));
        } else if ("Prix décroissant".equals(sortMode)) {
            filtered.sort(java.util.Comparator.comparingDouble(Abonnement::getPrix).reversed());
        } else if ("Date de début".equals(sortMode)) {
            filtered.sort(java.util.Comparator.comparing(Abonnement::getDateDebut));
        }

        // Display
        for (Abonnement sub : filtered) {
            VBox card = createSubscriptionCard(sub);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createSubscriptionCard(Abonnement sub) {
        VBox card = new VBox(14);
        card.getStyleClass().add("sub-card"); // Apply CSS class for dimensions and base style

        // 🎨 SMART BRANDING COLOR
        String colorHex = sub.getColorHex();
        String bgStyle;

        if (colorHex == null || colorHex.isEmpty()) {
            colorHex = getCategoryGradient(sub.getCategorie()); // Fallback to category gradient
            bgStyle = "-fx-background-color: linear-gradient(to bottom right, " + colorHex + ");";
        } else {
            bgStyle = "-fx-background-color: " + colorHex + ";";
        }

        // Only set background color via inline style, let CSS handle the rest (padding,
        // radius, shadow)
        card.setStyle(bgStyle);

        // HEADER: Nom + Logo
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Logo Container (White Circle Background for better visibility)
        javafx.scene.layout.StackPane logoContainer = new javafx.scene.layout.StackPane();
        logoContainer.setMinSize(40, 40);
        logoContainer.setPrefSize(40, 40);
        logoContainer.setMaxSize(40, 40);

        javafx.scene.shape.Circle bgCircle = new javafx.scene.shape.Circle(20, javafx.scene.paint.Color.WHITE);
        bgCircle.setEffect(new javafx.scene.effect.DropShadow(4, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));

        javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView();
        logoView.setFitWidth(24);
        logoView.setFitHeight(24);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        logoContainer.getChildren().addAll(bgCircle, logoView);

        boolean hasLogo = sub.getLogoUrl() != null && !sub.getLogoUrl().isEmpty();

        // Load Image Async
        if (hasLogo) {
            new Thread(() -> {
                try {
                    String url = sub.getLogoUrl();
                    javafx.scene.image.Image img = new javafx.scene.image.Image(url, true); // background loading

                    // Update UI when loaded
                    img.progressProperty().addListener((obs, oldVal, progress) -> {
                        if (progress.doubleValue() == 1.0 && !img.isError()) {
                            javafx.application.Platform.runLater(() -> logoView.setImage(img));
                        }
                    });
                } catch (Exception ignored) {
                }
            }).start();
        }

        Label lblName = new Label(sub.getNom());
        lblName.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold;");
        lblName.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.HBox.setHgrow(lblName, javafx.scene.layout.Priority.ALWAYS);

        if (hasLogo) {
            header.getChildren().addAll(logoContainer, lblName);
        } else {
            header.getChildren().addAll(lblName);
        }

        // --- NEW: Display Assigned Member if applicable ---
        Integer memberId = sub.getAssignedToMemberId();

        // Hide if self-assigned (User is the Family Member)
        com.emsi.subtracker.utils.UserSession session = com.emsi.subtracker.utils.UserSession.getInstance();
        boolean isSelf = false;
        if (session.isFamilyMember() && memberId != null) {
            if (session.getFamilyMember().getId() == memberId) {
                isSelf = true;
            }
        }

        if (memberId != null && memberId > 0 && !isSelf) {
            String memberName = familyMemberNames.getOrDefault(memberId, "Membre #" + memberId);
            Label lblAssigned = new Label("👤 " + memberName);
            lblAssigned.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12; -fx-padding: 4 0 0 0;");

            // Add below name or in a VBox with name
            VBox nameBox = new VBox(2);
            nameBox.getChildren().addAll(lblName, lblAssigned);
            javafx.scene.layout.HBox.setHgrow(nameBox, javafx.scene.layout.Priority.ALWAYS);

            // Clear previous children and rebuild
            header.getChildren().clear();
            if (hasLogo) {
                header.getChildren().addAll(logoContainer, nameBox);
            } else {
                header.getChildren().addAll(nameBox);
            }
        }
        // --------------------------------------------------

        // PRIX ÉNORME
        javafx.scene.layout.HBox priceRow = new javafx.scene.layout.HBox(10);
        priceRow.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);

        String currency = com.emsi.subtracker.utils.UserSession.getInstance().getCurrency();
        double displayPrice = convertPrice(sub.getPrix(), currency);
        String symbol = getCurrencySymbol(currency);

        Label lblPrice = new Label(df.format(displayPrice));
        lblPrice.getStyleClass().add("sub-card-price");

        Label currencyLabel = new Label(symbol);
        currencyLabel.getStyleClass().add("sub-card-currency");

        Label freqLabel = new Label("/ " + getFrequencyShort(sub.getFrequence()));
        freqLabel.getStyleClass().add("sub-card-freq");

        priceRow.getChildren().addAll(lblPrice, currencyLabel, freqLabel);

        // 📅 RENOUVELLEMENT
        String nextRenewal = calculateNextRenewal(sub);
        Label dateLabel = new Label("📅  Renouvellement: " + nextRenewal);
        dateLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.92); -fx-font-size: 14; -fx-font-weight: 500; -fx-padding: 4 0 0 0;");

        // SPACER
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // ACTIONS FOOTER
        javafx.scene.layout.HBox actionsRow = new javafx.scene.layout.HBox(12);
        actionsRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 11 20; " +
                        "-fx-cursor: hand;");
        editBtn.setOnAction(e -> editSubscription(sub));

        // Hover effect pour edit
        final String editStyle = editBtn.getStyle();
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(editStyle + "-fx-background-color: rgba(255,255,255,0.35);"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(editStyle));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
                "-fx-background-color: rgba(255,70,70,0.4); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 11 16; " +
                        "-fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteSubscription(sub));

        // Hover effect pour delete
        final String deleteStyle = deleteBtn.getStyle();
        deleteBtn.setOnMouseEntered(
                e -> deleteBtn.setStyle(deleteStyle + "-fx-background-color: rgba(255,70,70,0.65);"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(deleteStyle));

        actionsRow.getChildren().addAll(editBtn, deleteBtn);

        // ASSEMBLAGE
        card.getChildren().addAll(header, priceRow, dateLabel, spacer, actionsRow);

        // Hover effect sur la carte entière
        final String cardStyle = card.getStyle();
        card.setOnMouseEntered(e -> {
            card.setStyle(cardStyle
                    + "-fx-scale-x: 1.02; -fx-scale-y: 1.02; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 6);");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(cardStyle);
        });

        return card;
    }

    // 🎨 Obtenir gradient selon catégorie
    private String getCategoryGradient(String category) {
        if (category == null)
            return "#667EEA, #764BA2";
        return switch (category.toLowerCase()) {
            case "divertissement" -> "#FF6B6B, #FF8E53";
            case "travail" -> "#4E54C8, #8F94FB";
            case "sport" -> "#11998E, #38EF7D";
            case "musique" -> "#C471ED, #F64F59";
            case "santé" -> "#FA709A, #FEE140";
            default -> "#667EEA, #764BA2";
        };
    }

    // Raccourcir fréquence
    private String getFrequencyShort(String freq) {
        if (freq == null)
            return "mois";
        return switch (freq.toLowerCase()) {
            case "mensuel" -> "mois";
            case "annuel" -> "an";
            case "hebdomadaire" -> "sem";
            default -> freq;
        };
    }

    // Calculer prochaine date renouvellement
    private String calculateNextRenewal(Abonnement sub) {
        if (sub.getDateDebut() == null)
            return "Non défini";

        java.time.LocalDate next = sub.getDateDebut();
        java.time.LocalDate today = java.time.LocalDate.now();

        while (next.isBefore(today) || next.equals(today)) {
            String freq = sub.getFrequence() != null ? sub.getFrequence().toLowerCase() : "mensuel";
            next = switch (freq) {
                case "mensuel" -> next.plusMonths(1);
                case "annuel" -> next.plusYears(1);
                case "hebdomadaire" -> next.plusWeeks(1);
                default -> next.plusMonths(1);
            };
        }

        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, next);
        if (daysUntil == 0)
            return "Aujourd'hui ⚠️";
        if (daysUntil == 1)
            return "Demain ⚠️";
        if (daysUntil <= 7)
            return "Dans " + daysUntil + " jours";
        if (daysUntil <= 30)
            return "Dans " + (daysUntil / 7) + " semaines";

        return next.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private void editSubscription(Abonnement sub) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/add_subscription.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/styles_v2.css").toExternalForm());
            com.emsi.subtracker.utils.ThemeManager.applyTheme(scene);

            AddSubscriptionController controller = fxmlLoader.getController();
            controller.setAbonnement(sub);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'abonnement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            refreshDashboard();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteSubscription(Abonnement sub) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + sub.getNom() + " ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet abonnement ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            service.remove(sub.getId());
            refreshDashboard();
        }
    }

    @FXML
    protected void onBtnAjouterClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/add_subscription.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Apply CSS to Modal
            scene.getStylesheets().add(getClass().getResource("/styles_v2.css").toExternalForm());
            com.emsi.subtracker.utils.ThemeManager.applyTheme(scene);

            Stage stage = new Stage();
            stage.setTitle("Nouvel Abonnement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh après fermeture
            refreshDashboard();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onProfileClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/user_profile.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/styles_v2.css").toExternalForm());
            com.emsi.subtracker.utils.ThemeManager.applyTheme(scene);

            Stage stage = new Stage();
            stage.setTitle("Mon Profil");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh Avatar after close
            loadUserAvatar();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSettingsClick() {
        try {
            Stage currentStage = (Stage) lblTotalMensuel.getScene().getWindow();
            SceneManager.switchScene(currentStage, "settings.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void goToAnalytics() {
        try {
            Stage currentStage = (Stage) lblTotalMensuel.getScene().getWindow();
            SceneManager.switchScene(currentStage, "analytics.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie les abonnements et envoie des alertes pour ceux qui arrivent à
     * échéance dans 3 jours.
     */
    private void checkRenewalAlerts() {
        if (com.emsi.subtracker.utils.UserSession.getInstance().isFamilyMember()) {
            return; // Pas d'alertes email pour les membres de famille
        }

        User currentUser = com.emsi.subtracker.utils.UserSession.getInstance().getUser();
        if (currentUser != null && allSubscriptions != null) {
            emailService.checkAndSendAlerts(currentUser, allSubscriptions);
            System.out.println("🔔 Vérification des alertes de renouvellement effectuée.");
        }
    }

    @FXML
    private void onThemeToggle() {
        boolean isDark = themeToggleDashboard.isSelected();
        com.emsi.subtracker.utils.ThemeManager.setDarkTheme(isDark);
        themeToggleDashboard.setText(isDark ? "☀️" : "🌙");
    }

    private void preloadFamilyMembers() {
        com.emsi.subtracker.models.User user = com.emsi.subtracker.utils.UserSession.getInstance().getUser();
        if (user != null && user.isFamilyAccount()) {
            List<com.emsi.subtracker.models.FamilyMember> members = familyService.getFamilyMembers(user.getId());
            for (com.emsi.subtracker.models.FamilyMember m : members) {
                familyMemberNames.put(m.getId(), m.getName());
            }
        }
    }
}
