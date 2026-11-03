package com.emsi.subtracker.models;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Représente un abonnement utilisateur.
 * Gère la sérialisation vers/depuis le format CSV.
 */
public class Abonnement {
    private int id;
    private String nom;
    private double prix;
    private LocalDate dateDebut;
    private String frequence; // "Mensuel" ou "Annuel"
    private String categorie;
    private int userId;

    private Integer assignedToMemberId; // ID du membre famille (null = compte principal)

    // Champs Branding (Nouveau)
    private String logoUrl;
    private String colorHex;

    // Constructeur complet avec assignedToMemberId
    public Abonnement(int id, String nom, double prix, LocalDate dateDebut, String frequence, String categorie,
            int userId, Integer assignedToMemberId) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.frequence = frequence;
        this.categorie = categorie;
        this.userId = userId;

        this.assignedToMemberId = assignedToMemberId;
        // Init default branding
        this.colorHex = "#2D3436";
    }

    // Constructeur sans assignedToMemberId (par défaut null)
    public Abonnement(int id, String nom, double prix, LocalDate dateDebut, String frequence, String categorie,
            int userId) {
        this(id, nom, prix, dateDebut, frequence, categorie, userId, null);
    }

    // Constructeur sans userId (compatibilité temporaire ou anciens tests)
    public Abonnement(int id, String nom, double prix, LocalDate dateDebut, String frequence, String categorie) {
        this(id, nom, prix, dateDebut, frequence, categorie, 0, null);
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getFrequence() {
        return frequence;
    }

    public void setFrequence(String frequence) {
        this.frequence = frequence;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getAssignedToMemberId() {
        return assignedToMemberId;
    }

    public void setAssignedToMemberId(Integer assignedToMemberId) {
        this.assignedToMemberId = assignedToMemberId;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    @Override
    public String toString() {
        return "Abonnement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", dateDebut=" + dateDebut +
                ", frequence='" + frequence + '\'' +
                ", categorie='" + categorie + '\'' +
                '}';
    }

    /**
     * Convertit l'objet Abonnement en une ligne CSV.
     * Format: id;nom;prix;dateDebut;frequence;categorie
     */
    public static String toCsvString(Abonnement abonnement) {
        if (abonnement == null)
            return "";
        return String.join(";",
                String.valueOf(abonnement.getId()),
                abonnement.getNom(),
                String.valueOf(abonnement.getPrix()),
                abonnement.getDateDebut().toString(),
                abonnement.getFrequence(),
                abonnement.getCategorie());
    }

    /**
     * Crée un objet Abonnement à partir d'une ligne CSV.
     * Attend le format: id;nom;prix;dateDebut;frequence;categorie
     */
    public static Abonnement fromCsvString(String line) {
        if (line == null || line.trim().isEmpty())
            return null;

        String[] parts = line.split(";");
        if (parts.length < 6) {
            // Ligne mal formée, on pourrait logger une erreur
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0]);
            String nom = parts[1];
            double prix = Double.parseDouble(parts[2]);
            LocalDate dateDebut = LocalDate.parse(parts[3]);
            String frequence = parts[4];
            String categorie = parts[5];

            return new Abonnement(id, nom, prix, dateDebut, frequence, categorie);
        } catch (NumberFormatException | DateTimeParseException e) {
            System.err.println("Erreur de parsing CSV sur la ligne : " + line + " - " + e.getMessage());
            return null;
        }
    }
}
