package com.example.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MigrationExecutor {

    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(MigrationExecutor.class.getName());

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public MigrationExecutor(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void executeMigration(String scriptPath) throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             InputStream is = getClass().getClassLoader().getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql.toString());
            }

            LOGGER.info("Migration appliquée avec succès : " + scriptPath);
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la migration : " + e.getMessage());
            throw e;
        }
    }
}
