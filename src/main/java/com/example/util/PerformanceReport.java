package com.example.util;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.stat.Statistics;

public class PerformanceReport {

    private final EntityManager em;

    public PerformanceReport(EntityManager em) {
        this.em = em;
    }

    public void printReport() {
        Session session = em.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();

        System.out.println("=== RAPPORT DE PERFORMANCE ===");
        System.out.println("Requêtes exécutées : " + stats.getQueryExecutionCount());
        System.out.println("Cache de second niveau hits : " + stats.getSecondLevelCacheHitCount());
        System.out.println("Cache de second niveau misses : " + stats.getSecondLevelCacheMissCount());
        System.out.println("Cache de second niveau mises à jour : " + stats.getSecondLevelCachePutCount());
        System.out.println("Transactions : " + stats.getTransactionCount());
        // ATTENTION : l'API Statistics de Hibernate 5.4 n'expose pas
        // getQueryExecutionTotalTime() ; la métrique disponible est la durée
        // maximale d'exécution d'une requête.
        System.out.println("Durée maximale d'une requête (ms) : " + stats.getQueryExecutionMaxTime());
    }
}
