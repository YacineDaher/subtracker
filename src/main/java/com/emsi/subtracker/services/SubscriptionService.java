package com.emsi.subtracker.services;

import com.emsi.subtracker.models.Abonnement;

import java.util.List;

/**
 * Service Métier - Intermédiaire entre la vue et la couche de données.
 * Contient la logique de calcul et de validation.
 */
public class SubscriptionService {

    private final com.emsi.subtracker.dao.SubscriptionDAO subscriptionDAO;

    public SubscriptionService() {
        this.subscriptionDAO = new com.emsi.subtracker.dao.impl.SubscriptionDAOImpl();
    }

    /**
     * Récupère tous les abonnements.
     */
    /**
     * Récupère tous les abonnements de l'utilisateur connecté.
     */
    public List<Abonnement> getAll() {
        com.emsi.subtracker.utils.UserSession session = com.emsi.subtracker.utils.UserSession.getInstance();

        // 1. If Family Member is logged in
        if (session.isFamilyMember()) {
            return subscriptionDAO.findAllByAssignedMemberId(session.getFamilyMember().getId());
        }

        // 2. If Parent User is logged in
        com.emsi.subtracker.models.User user = session.getUser();
        if (user != null) {
            return subscriptionDAO.findAll(user.getId());
        }

        System.err.println("Aucun utilisateur connecté !");
        return List.of();
    }

    /**
     * Ajoute un abonnement pour l'utilisateur connecté.
     */
    public void add(Abonnement abonnement) {
        com.emsi.subtracker.models.User user = com.emsi.subtracker.utils.UserSession.getInstance().getUser();
        if (user != null) {
            abonnement.setUserId(user.getId());
        }
        subscriptionDAO.save(abonnement);
    }

    public void update(Abonnement abonnement) {
        subscriptionDAO.update(abonnement);
    }

    /**
     * Supprime un abonnement par son ID.
     */
    /**
     * Supprime tous les abonnements de l'utilisateur connecté.
     */
    public void removeAll() {
        com.emsi.subtracker.models.User user = com.emsi.subtracker.utils.UserSession.getInstance().getUser();
        if (user != null) {
            subscriptionDAO.deleteAll(user.getId());
        }
    }

    /**
     * Supprime un abonnement par son ID.
     */
    public void remove(int id) {
        // TODO: Ensure user owns this subscription before deleting
        boolean removed = subscriptionDAO.delete(id);
        if (removed) {
            System.out.println("Abonnement ID " + id + " supprimé.");
        } else {
            System.err.println("Aucun abonnement trouvé avec l'ID " + id);
        }
    }

    /**
     * Calcule le coût mensuel total de tous les abonnements de l'utilisateur.
     * Pour un abonnement annuel, le prix est divisé par 12.
     * Arrondi à 2 chiffres après la virgule.
     */
    public double calculerTotalMensuel() {
        return getAll().stream()
                .mapToDouble(a -> {
                    if ("Annuel".equalsIgnoreCase(a.getFrequence())) {
                        return a.getPrix() / 12.0;
                    }
                    return a.getPrix();
                })
                .sum();
    }

    /**
     * Calcule le coût mensuel total pour une liste donnée d'abonnements.
     * Utile pour afficher des sous-totaux par membre de famille.
     */
    public double calculerTotalMensuel(List<Abonnement> abonnements) {
        return abonnements.stream()
                .mapToDouble(a -> {
                    if ("Annuel".equalsIgnoreCase(a.getFrequence())) {
                        return a.getPrix() / 12.0;
                    }
                    return a.getPrix();
                })
                .sum();
    }
}
