package com.example.test;

import java.time.LocalDateTime;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;

public class ReservationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    public static void init() {
        emf = Persistence.createEntityManagerFactory("ReservationPU");
    }

    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    public void tearDown() {
        if (em != null) {
            em.close();
        }
    }

    @AfterAll
    public static void close() {
        if (emf != null) {
            emf.close();
        }
    }

    @Test
    public void testCreateReservation() {
        em.getTransaction().begin();

        Utilisateur user = new Utilisateur();
        user.setNom("Test User");
        user.setEmail("test.user@example.com");
        em.persist(user);

        // La base en mémoire est vide au départ : on crée la salle référencée.
        Salle salle = new Salle();
        salle.setNom("Salle Test");
        salle.setCapacite(15);
        em.persist(salle);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(user);
        reservation.setSalle(salle);
        // withNano(0) : H2 tronque les nanosecondes, on garde une précision à la seconde
        reservation.setDateDebut(LocalDateTime.now().plusDays(2).withNano(0));
        reservation.setDateFin(LocalDateTime.now().plusDays(2).withNano(0).plusHours(1));
        em.persist(reservation);

        em.getTransaction().commit();

        Assertions.assertNotNull(reservation.getId());
        Assertions.assertTrue(reservation.getId() > 0);
    }

    @Test
    public void testModifierReservation() {
        em.getTransaction().begin();

        Utilisateur user = new Utilisateur();
        user.setNom("Test User 2");
        user.setEmail("test.user2@example.com");
        em.persist(user);

        Salle salle = new Salle();
        salle.setNom("Salle Test 2");
        salle.setCapacite(25);
        em.persist(salle);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(user);
        reservation.setSalle(salle);
        reservation.setDateDebut(LocalDateTime.now().plusDays(5).withNano(0));
        reservation.setDateFin(LocalDateTime.now().plusDays(5).withNano(0).plusHours(1));
        em.persist(reservation);

        em.getTransaction().commit();

        em.getTransaction().begin();
        Reservation chargee = em.find(Reservation.class, reservation.getId());
        chargee.setDateFin(chargee.getDateFin().plusHours(1));
        em.getTransaction().commit();

        em.clear();
        Reservation relue = em.find(Reservation.class, reservation.getId());
        Assertions.assertEquals(1, relue.getVersion());
        Assertions.assertEquals(2, java.time.Duration.between(relue.getDateDebut(), relue.getDateFin()).toHours());
    }

    @Test
    public void testSupprimerReservation() {
        em.getTransaction().begin();

        Utilisateur user = new Utilisateur();
        user.setNom("Test User 3");
        user.setEmail("test.user3@example.com");
        em.persist(user);

        Salle salle = new Salle();
        salle.setNom("Salle Test 3");
        salle.setCapacite(30);
        em.persist(salle);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(user);
        reservation.setSalle(salle);
        reservation.setDateDebut(LocalDateTime.now().plusDays(7).withNano(0));
        reservation.setDateFin(LocalDateTime.now().plusDays(7).withNano(0).plusHours(1));
        em.persist(reservation);

        em.getTransaction().commit();

        Long id = reservation.getId();

        em.getTransaction().begin();
        Reservation chargee = em.find(Reservation.class, id);
        em.remove(chargee);
        em.getTransaction().commit();

        em.clear();
        Assertions.assertNull(em.find(Reservation.class, id));
    }
}
