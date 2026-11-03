package com.emsi.subtracker.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour gérer le branding (Logos et Couleurs) des abonnements.
 * Singleton.
 */
public class BrandingService {

    private static BrandingService instance;
    private final Map<String, String> colorMap;

    private BrandingService() {
        colorMap = new HashMap<>();
        // Vidéo / Streaming
        colorMap.put("netflix", "#E50914");
        colorMap.put("youtube", "#FF0000");
        colorMap.put("prime video", "#00A8E1");
        colorMap.put("disney+", "#113CCF");
        colorMap.put("hbo", "#5D2E86");
        colorMap.put("hulu", "#1CE783");
        colorMap.put("twitch", "#9146FF");
        colorMap.put("apple tv", "#000000");

        // Musique
        colorMap.put("spotify", "#1DB954");
        colorMap.put("deezer", "#EF5466");
        colorMap.put("apple music", "#FA2D48");
        colorMap.put("soundcloud", "#FF5500");
        colorMap.put("tidal", "#000000");

        // Productivité / Travail
        colorMap.put("linkedin", "#0077B5");
        colorMap.put("adobe", "#FF0000");
        colorMap.put("microsoft", "#00A4EF");
        colorMap.put("google", "#4285F4");
        colorMap.put("zoom", "#2D8CFF");
        colorMap.put("slack", "#4A154B");
        colorMap.put("github", "#181717");
        colorMap.put("chatgpt", "#10A37F");
        colorMap.put("notion", "#000000");

        // Sport / Santé
        colorMap.put("fitness", "#FF4500");
        colorMap.put("strava", "#FC4C02");
        colorMap.put("myfitnesspal", "#0066EE");
    }

    public static BrandingService getInstance() {
        if (instance == null) {
            instance = new BrandingService();
        }
        return instance;
    }

    /**
     * Récupère l'URL du logo via l'API Clearbit.
     * 
     * @param serviceName Le nom du service (ex: "Netflix")
     * @return L'URL du logo ou null si non trouvé
     */
    public String getLogoUrl(String serviceName) {
        if (serviceName == null || serviceName.isEmpty())
            return null;

        // Nettoyage et devinette du domaine
        String cleanName = serviceName.toLowerCase().trim().replaceAll("\\s+", "");
        String domain = cleanName;

        if (!domain.contains(".")) {
            domain += ".com";
        }

        return "https://www.google.com/s2/favicons?domain=" + domain + "&sz=128";
    }

    /**
     * Récupère la couleur dominante associée au service.
     * 
     * @param serviceName Le nom du service
     * @return Code HEX de la couleur (ex: "#E50914") ou une couleur par défaut
     */
    public String getDominantColor(String serviceName) {
        if (serviceName == null)
            return "#2D3436"; // Gris foncé par défaut

        String key = serviceName.toLowerCase().trim();

        // Recherche exacte ou contains
        for (Map.Entry<String, String> entry : colorMap.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "#2D3436"; // Couleur par défaut (Gris moderne)
    }
}
