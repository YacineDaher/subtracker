package com.emsi.subtracker.dao;

import com.emsi.subtracker.models.Abonnement;
import java.util.List;
import java.util.Optional;

public interface SubscriptionDAO {
    Abonnement save(Abonnement abonnement);

    Abonnement update(Abonnement abonnement);

    boolean delete(Integer id);

    void deleteAll(int userId);

    Optional<Abonnement> findById(Integer id);

    List<Abonnement> findAll(int userId);

    // New method for family members
    List<Abonnement> findAllByAssignedMemberId(int memberId);

    List<Abonnement> findAll();
}
