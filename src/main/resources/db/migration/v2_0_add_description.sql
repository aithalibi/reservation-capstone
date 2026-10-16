-- Migration version 2.0 : ajout de la colonne "description" à la table "salles"
ALTER TABLE salles ADD COLUMN IF NOT EXISTS description VARCHAR(255);

-- Ajout d'un index sur la colonne "email" de la table "utilisateurs"
CREATE UNIQUE INDEX IF NOT EXISTS idx_utilisateur_email ON utilisateurs(email);
