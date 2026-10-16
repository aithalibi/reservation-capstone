package com.example;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.service.ReservationService;
import com.example.util.DataInitializer;
import com.example.util.MigrationExecutor;
import com.example.util.PerformanceReport;

public class MainApp {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    private static final String JDBC_URL = "jdbc:h2:mem:reservationdb;DB_CLOSE_DELAY=-1";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReservationPU");
        EntityManager em = emf.createEntityManager();

        try {
            // 1. Initialisation des données (crée les tables + le schéma de base)
            DataInitializer initializer = new DataInitializer(em);
            initializer.initData();

            // 2. Migration de base de données (JDBC) : ajoute description + index unique
            System.out.println();
            System.out.println("=== MIGRATION DE BASE DE DONNEES ===");
            MigrationExecutor migrationExecutor = new MigrationExecutor(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            migrationExecutor.executeMigration("db/migration/v2_0_add_description.sql");
            System.out.println("Migration v2_0_add_description.sql appliquée.");

            // 3. Scénarios de test fonctionnels
            System.out.println();
            System.out.println("=== SCENARIOS DE TEST ===");
            executerScenarios(em);

            // 4. Requêtes JPQL / Criteria API
            System.out.println();
            System.out.println("=== REQUETES JPQL ET CRITERIA API ===");
            executerRequetes(em);

            // 5. Cache de second niveau : partagé entre plusieurs sessions
            System.out.println();
            System.out.println("=== CACHE DE SECOND NIVEAU ===");
            demonstrationCacheSecondNiveau(em, emf);

            // 6. Rapport de performance
            System.out.println();
            PerformanceReport report = new PerformanceReport(em);
            report.printReport();

        } finally {
            em.close();
            emf.close();
        }
    }

    private static void executerScenarios(EntityManager em) {
        ReservationService service = new ReservationService(em);

        long total = service.compterReservations();
        System.out.println("Réservations présentes en base : " + total);

        // Création
        com.example.model.Utilisateur nouveau = new com.example.model.Utilisateur(
                "David Petit", "david.petit@example.com");
        com.example.model.Salle salle = em.find(com.example.model.Salle.class, 1L);
        Reservation nouvelle = new Reservation(salle, nouveau, 
                LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(10).plusHours(1));

        em.getTransaction().begin();
        em.persist(nouveau);
        em.persist(nouvelle);
        em.getTransaction().commit();
        System.out.println("Création : réservation id=" + nouvelle.getId() + " pour " + nouveau.getEmail()
                + " dans " + salle.getNom());

        // Modification
        em.getTransaction().begin();
        Reservation aModifier = em.find(Reservation.class, nouvelle.getId());
        aModifier.setDateFin(aModifier.getDateFin().plusHours(1));
        em.getTransaction().commit();
        System.out.println("Modification : nouvelle date de fin = " + aModifier.getDateFin());

        // Suppression
        Long idSupprime = nouvelle.getId();
        service.supprimerReservation(idSupprime);
        System.out.println("Suppression : réservation id=" + idSupprime + " supprimée.");
    }

    private static void executerRequetes(EntityManager em) {
        ReservationService service = new ReservationService(em);

        // Paramètre de date FIXE : réutiliser exactement les mêmes paramètres est
        // indispensable pour que le cache de requêtes puisse renvoyer un hit.
        LocalDateTime reference = LocalDateTime.now();

        System.out.println("-- Requête JPQL : réservations de Salle A à venir --");
        List<Reservation> parJpql = service.rechercherParSalleEtDate("Salle A", reference);
        for (Reservation r : parJpql) {
            System.out.println("   " + r);
        }

        em.clear();

        System.out.println("-- Requête JPQL relue à l'identique (cache de requêtes) --");
        for (Reservation r : service.rechercherParSalleEtDate("Salle A", reference)) {
            System.out.println("   " + r);
        }

        em.clear();

        System.out.println("-- Requête Criteria API : réservations de Salle A à venir --");
        List<Reservation> parCriteria = service.rechercherAvecCriteria("Salle A", reference);
        for (Reservation r : parCriteria) {
            System.out.println("   " + r);
        }

        // Lecture répétée : le cache de second niveau évite le second SELECT
        em.clear();
        System.out.println("-- Deuxième lecture des mêmes entités (cache de second niveau) --");
        for (Reservation r : service.rechercherAvecCriteria("Salle A", reference)) {
            System.out.println("   " + r);
        }

        System.out.println("Total réservations en base : " + service.compterReservations());
    }

    /**
     * Le cache de second niveau ne remplace pas le cache de premier niveau
     * (EntityManager) : il est partagé entre plusieurs sessions. On le démontre
     * en relisant la même entité depuis deux EntityManager distincts.
     */
    private static void demonstrationCacheSecondNiveau(EntityManager em, EntityManagerFactory emf) {
        // Session 1 : le contexte de persistance est vidé pour forcer un vrai accès
        em.clear();
        Salle depuisSession1 = em.find(Salle.class, 1L);
        System.out.println("Session 1 : " + depuisSession1);

        // Session 2 : nouvelle session, la même entité est servie par le cache
        // de second niveau (aucun SELECT supplémentaire si elle y est présente).
        EntityManager em2 = emf.createEntityManager();
        try {
            Salle depuisSession2 = em2.find(Salle.class, 1L);
            System.out.println("Session 2 : " + depuisSession2);
            System.out.println("Même entité logique dans les deux sessions : "
                    + depuisSession1.getNom().equals(depuisSession2.getNom()));
        } finally {
            em2.close();
        }
    }
}
