package com.emsi.subtracker.controllers;

import com.emsi.subtracker.models.Abonnement;
import com.emsi.subtracker.services.SubscriptionService;
import com.emsi.subtracker.utils.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Contrôleur de l'écran Analytics.
 */
public class AnalyticsController implements Initializable {

    @FXML
    private PieChart pieChartCategories;

    @FXML
    private BarChart<String, Number> barChartCosts;

    @FXML
    private javafx.scene.control.Label lblTotalAnnuel;

    @FXML
    private javafx.scene.control.Label lblMoyenneMensuelle;

    @FXML
    private javafx.scene.control.Label lblPlusCher;

    @FXML
    private javafx.scene.control.TextField searchField;

    private final SubscriptionService service = new SubscriptionService();
    private List<Abonnement> allSubscriptions;

    public void initialize(URL location, ResourceBundle resources) {

        allSubscriptions = service.getAll();

        updateStats(allSubscriptions);
        loadPieChartData(allSubscriptions);
        loadBarChartData(allSubscriptions);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterCharts(newVal);
            });
        }

        // Apply Dark Theme to Charts
        javafx.application.Platform.runLater(() -> {
            if (pieChartCategories != null && pieChartCategories.getScene() != null) {
                // PieChart - dark legend and labels (Text Only)
                pieChartCategories.lookupAll(".chart-legend-item").forEach(node -> {
                    if (node instanceof javafx.scene.text.Text) {
                        ((javafx.scene.text.Text) node).setFill(javafx.scene.paint.Color.WHITE);
                    }
                    // Fix: Do NOT touch .chart-legend-item-symbol or it loses category colors
                    // If label is a Label control (sometimes used instead of Text)
                    if (node instanceof javafx.scene.control.Label) {
                        ((javafx.scene.control.Label) node).setStyle("-fx-text-fill: white;");
                    }
                });
            }

            if (barChartCosts != null) {
                // BarChart - white axis labels
                barChartCosts.lookupAll(".axis-label").forEach(node -> {
                    if (node instanceof javafx.scene.text.Text) {
                        ((javafx.scene.text.Text) node).setFill(javafx.scene.paint.Color.WHITE);
                    }
                });
                barChartCosts.lookupAll(".axis-tick-label").forEach(node -> {
                    if (node instanceof javafx.scene.text.Text) {
                        ((javafx.scene.text.Text) node).setFill(javafx.scene.paint.Color.WHITE);
                    }
                });
            }

            // ThemeManager if exists
            try {
                if (pieChartCategories.getScene() != null) {
                    com.emsi.subtracker.utils.ThemeManager.applyTheme(pieChartCategories.getScene());
                }
            } catch (Exception e) {
                // ThemeManager might not exist
            }
        });
    }

    private void filterCharts(String query) {
        if (allSubscriptions == null)
            return;

        String lowerQuery = (query != null) ? query.toLowerCase() : "";
        List<Abonnement> filtered = allSubscriptions.stream()
                .filter(sub -> sub.getNom().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        updateStats(filtered);
        loadPieChartData(filtered);
        loadBarChartData(filtered);
    }

    private void updateStats(List<Abonnement> list) {
        if (list == null || list.isEmpty()) {
            if (lblTotalAnnuel != null)
                lblTotalAnnuel.setText("0.00 DH");
            if (lblMoyenneMensuelle != null)
                lblMoyenneMensuelle.setText("0.00 DH");
            if (lblPlusCher != null)
                lblPlusCher.setText("-");
            return;
        }

        // 1. Total Mensuel & Annuel
        double totalMensuel = service.calculerTotalMensuel(list);
        double totalAnnuel = totalMensuel * 12;

        // 2. Moyenne Mensuelle (Total / Nombre d'abonnements)
        double moyenne = totalMensuel / list.size();

        // 3. Plus Cher (Le plus cher en coût mensuel)
        Abonnement plusCher = list.stream()
                .max(java.util.Comparator.comparingDouble(a -> {
                    return "Annuel".equalsIgnoreCase(a.getFrequence()) ? a.getPrix() / 12.0 : a.getPrix();
                }))
                .orElse(null);

        // Update Labels
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        String currencyCode = com.emsi.subtracker.utils.UserSession.getInstance().getCurrency();
        String currencySymbol = getCurrencySymbol(currencyCode);

        // Fallback if needed, but UserSession usually handles it.
        // NOTE: Currency conversion logic is in DashboardController, we might want to
        // duplicate or centralize it.
        // For now, assuming base calculations. If currency conversion is needed, we
        // should use a utility.
        // Let's stick to simple display for now to fix the "0" issue.

        if (lblTotalAnnuel != null)
            lblTotalAnnuel.setText(df.format(totalAnnuel) + " " + currencySymbol);
        if (lblMoyenneMensuelle != null)
            lblMoyenneMensuelle.setText(df.format(moyenne) + " " + currencySymbol);

        if (lblPlusCher != null && plusCher != null) {
            lblPlusCher.setText(plusCher.getNom() + " (" + df.format(plusCher.getPrix()) + " " + currencySymbol + ")");
        } else if (lblPlusCher != null) {
            lblPlusCher.setText("-");
        }
    }

    private String getCurrencySymbol(String currency) {
        if (currency == null)
            return "DH";
        switch (currency) {
            case "EUR":
                return "€";
            case "USD":
                return "$";
            default:
                return "DH";
        }
    }

    private void loadPieChartData(List<Abonnement> list) {
        // Group by category and sum counts (or costs?)
        // Let's visualize Cost Distribution by Category
        Map<String, Double> costByCategory = list.stream()
                .collect(Collectors.groupingBy(
                        Abonnement::getCategorie,
                        Collectors.summingDouble(a -> {
                            // Normalize to monthly cost
                            return "Annuel".equalsIgnoreCase(a.getFrequence()) ? a.getPrix() / 12.0 : a.getPrix();
                        })));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        costByCategory.forEach((cat, cost) -> {
            pieData.add(new PieChart.Data(cat, cost));
        });

        pieChartCategories.setData(pieData);

        // Add Interactivity
        for (PieChart.Data data : pieChartCategories.getData()) {
            javafx.scene.Node node = data.getNode();
            addInteractiveEffects(node, data.getName(), data.getPieValue());
        }
    }

    private void loadBarChartData(List<Abonnement> list) {
        barChartCosts.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Coût Mensuel");

        for (Abonnement sub : list) {
            double monthlyCost = "Annuel".equalsIgnoreCase(sub.getFrequence()) ? sub.getPrix() / 12.0 : sub.getPrix();
            series.getData().add(new XYChart.Data<>(sub.getNom(), monthlyCost));
        }

        barChartCosts.getData().add(series);

        // Add Interactivity (BarChart nodes are created effectively after layout)
        // using runLater to ensure nodes are available
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                javafx.scene.Node node = data.getNode();
                addInteractiveEffects(node, data.getXValue(), data.getYValue().doubleValue());
            }
        });
    }

    private void addInteractiveEffects(javafx.scene.Node node, String name, double value) {
        if (node == null)
            return;

        // Tooltip
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        String currency = com.emsi.subtracker.utils.UserSession.getInstance().getCurrency();
        String symbol = getCurrencySymbol(currency);

        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                name + "\n" + df.format(value) + " " + symbol);
        tooltip.setStyle(
                "-fx-font-size: 14px; -fx-background-color: #252525; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        tooltip.setShowDelay(javafx.util.Duration.millis(50));
        javafx.scene.control.Tooltip.install(node, tooltip);

        // Hover Effect
        node.setOnMouseEntered(e -> {
            node.setScaleX(1.08); // Slight scale up
            node.setScaleY(1.08);
            node.setEffect(new javafx.scene.effect.Glow(0.3)); // Subtle glow
            node.setStyle("-fx-cursor: hand;");
        });

        node.setOnMouseExited(e -> {
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            node.setEffect(null);
            node.setStyle("");
        });
    }

    @FXML
    protected void goToDashboard() {
        try {
            Stage currentStage = (Stage) pieChartCategories.getScene().getWindow();
            SceneManager.switchScene(currentStage, "dashboard.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSettingsClick() {
        try {
            Stage currentStage = (Stage) pieChartCategories.getScene().getWindow();
            SceneManager.switchScene(currentStage, "settings.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
