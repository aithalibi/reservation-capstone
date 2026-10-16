package com.example.model;

import java.time.LocalDateTime;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "reservations")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Version
    private int version;

    public Reservation() {
    }

    public Reservation(Salle salle, Utilisateur utilisateur, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.salle = salle;
        this.utilisateur = utilisateur;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", salle=" + (salle != null ? salle.getNom() : null)
                + ", utilisateur=" + (utilisateur != null ? utilisateur.getEmail() : null)
                + ", dateDebut=" + dateDebut + ", dateFin=" + dateFin + "}";
    }
}
