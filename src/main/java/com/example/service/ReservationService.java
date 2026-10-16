package com.example.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.example.model.Reservation;

/**
 * Service métier : transactions RESOURCE_LOCAL, requêtes JPQL / Criteria API
 * et gestion des conflits de verrouillage optimiste (@Version).
 */
public class ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());

    private final EntityManager em;

    public ReservationService(EntityManager em) {
        this.em = em;
    }

    // ------------------------------------------------------------------
    // Transactions (RESOURCE_LOCAL)
    // ------------------------------------------------------------------

    public void createReservation(Reservation reservation) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            em.persist(reservation);
            tx.commit();
            LOGGER.info("Création de la réservation réussie pour utilisateur : "
                    + reservation.getUtilisateur().getEmail());
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Erreur lors de la création de la réservation : " + e.getMessage());
            throw e;
        }
    }

    public Reservation modifierReservation(Reservation reservation) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Reservation managed = em.merge(reservation);
            tx.commit();
            return managed;
        } catch (OptimisticLockException ole) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Conflit détecté, veuillez réessayer : " + ole.getMessage());
            throw ole;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public void supprimerReservation(Long id) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Reservation reservation = em.find(Reservation.class, id);
            if (reservation != null) {
                em.remove(reservation);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    // ------------------------------------------------------------------
    // Étape 9 : requêtes JPQL et Criteria API
    // ------------------------------------------------------------------

    public List<Reservation> rechercherParSalleEtDate(String nomSalle, LocalDateTime date) {
        String jpql = "SELECT r FROM Reservation r WHERE r.salle.nom = :nomSalle AND r.dateDebut > :date";
        return em.createQuery(jpql, Reservation.class)
                .setParameter("nomSalle", nomSalle)
                .setParameter("date", date)
                // hibernate.cache.use_query_cache=true : la requête doit en plus être
                // marquée cacheable pour que son résultat soit mis en cache.
                .setHint("org.hibernate.cacheable", Boolean.TRUE)
                .getResultList();
    }

    public List<Reservation> rechercherAvecCriteria(String nomSalle, LocalDateTime date) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Reservation> cq = cb.createQuery(Reservation.class);
        Root<Reservation> root = cq.from(Reservation.class);
        Predicate predicate = cb.and(
                cb.equal(root.get("salle").get("nom"), nomSalle),
                cb.greaterThan(root.get("dateDebut"), date));

        cq.select(root).where(predicate);
        return em.createQuery(cq)
                .setHint("org.hibernate.cacheable", Boolean.TRUE)
                .getResultList();
    }

    public long compterReservations() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class);
        return query.getSingleResult();
    }
}
