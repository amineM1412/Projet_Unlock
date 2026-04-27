package com.unlock.model;

import java.io.Serializable;

/**
 * Représente les différents types/couleurs de cartes dans Unlock!
 */
public enum CardType implements Serializable {
    NEUTRE("Neutre"),     // Gris (Lieu, Indice, etc.)
    BLEU("Bleu"),         // Objet à combiner (Bleu)
    ROUGE("Rouge"),       // Objet à combiner (Rouge)
    MACHINE("Machine"),   // Vert (Mini-jeu interactif)
    CODE("Code"),         // Jaune (Code à entrer)
    PENALITE("Pénalité"), // Pénalité de temps/oxygène
    RESULTAT("Résultat"); // Victoire ou échec

    private final String label;

    CardType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
