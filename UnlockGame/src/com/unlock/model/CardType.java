package com.unlock.model;

/**
 * Représente les différents types/couleurs de cartes dans Unlock!
 */
public enum CardType {
    NEUTRE("Neutre"), // Gris (Lieu, Indice, etc.)
    BLEU("Bleu"),     // Objet à combiner
    ROUGE("Rouge"),   // Objet à combiner
    MACHINE("Machine"), // Vert (Nécessite une interaction/code)
    CODE("Code"),       // Jaune (Code à entrer)
    PENALITE("Pénalité"),
    RESULTAT("Résultat"); // Victoire ou échec

    private final String label;

    CardType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
