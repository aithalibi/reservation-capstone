package com.example.util;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;

public class DataInitializer {

    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    private final EntityManager em;

    public DataInitializer(EntityManager em) {
        this.em = em;
    }

    public void initData() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // Utilisateurs
            Utilisateur user1 = new Utilisateur();
            user1.setNom("Alice Dupont");
            user1.setEmail("alice.dupont@example.com");
            em.persist(user1);

            Utilisateur user2 = new Utilisateur();
            user2.setNom("Bob Martin");
            user2.setEmail("bob.martin@example.com");
            em.persist(user2);

            Utilisateur user3 = new Utilisateur();
            user3.setNom("Chloe Bernard");
            user3.setEmail("chloe.bernard@example.com");
            em.persist(user3);

            // Salles
            Salle salle1 = new Salle();
            salle1.setNom("Salle A");
            salle1.setCapacite(20);
            salle1.setDescription("Salle de cours standard");
            em.persist(salle1);

            Salle salle2 = new Salle();
            salle2.setNom("Salle B");
            salle2.setCapacite(50);
            salle2.setDescription("Salle de travaux pratiques");
            em.persist(salle2);

            Salle salle3 = new Salle();
            salle3.setNom("Amphi C");
            salle3.setCapacite(200);
            salle3.setDescription("Amphithéâtre principal");
            em.persist(salle3);

            // Réservations (dates dynamiques pour rester valides à l'exécution)
            Reservation res1 = new Reservation();
            res1.setUtilisateur(user1);
            res1.setSalle(salle1);
            res1.setDateDebut(LocalDateTime.now().plusDays(1));
            res1.setDateFin(LocalDateTime.now().plusDays(1).plusHours(2));
            em.persist(res1);

            Reservation res2 = new Reservation();
            res2.setUtilisateur(user2);
            res2.setSalle(salle2);
            res2.setDateDebut(LocalDateTime.now().plusDays(2));
            res2.setDateFin(LocalDateTime.now().plusDays(2).plusHours(3));
            em.persist(res2);

            Reservation res3 = new Reservation();
            res3.setUtilisateur(user3);
            res3.setSalle(salle3);
            res3.setDateDebut(LocalDateTime.now().plusDays(3));
            res3.setDateFin(LocalDateTime.now().plusDays(3).plusHours(4));
            em.persist(res3);

            tx.commit();

            LOGGER.info("Jeu de données initialisé : 3 utilisateurs, 3 salles, 3 réservations.");

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Erreur lors de l'initialisation des données : " + e.getMessage());
            throw e;
        }
    }
}
