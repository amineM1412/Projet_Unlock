package com.unlock.core;

import java.io.Serializable;

/**
 * Interface pour les mini-jeux interactifs (cartes vertes/MACHINE).
 * Chaque mini-jeu doit implémenter cette interface pour être
 * compatible avec le système de sauvegarde Serializable.
 */
public interface MiniGame extends Serializable {

    /**
     * Retourne l'identifiant unique du mini-jeu
     */
    String getId();

    /**
     * Retourne le nom affiché du mini-jeu
     */
    String getName();

    /**
     * Retourne les instructions pour le joueur
     */
    String getInstructions();

    /**
     * Retourne true si le mini-jeu a été complété avec succès
     */
    boolean isCompleted();

    /**
     * Réinitialise le mini-jeu
     */
    void reset();
}
