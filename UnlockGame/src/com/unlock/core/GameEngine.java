package com.unlock.core;

import com.unlock.model.Card;
import com.unlock.model.CardType;
import java.util.HashMap;
import java.util.Map;

/**
 * Moteur principal du jeu.
 * Gère le temps, les pénalités et les combinaisons de cartes.
 */
public class GameEngine {

    // Temps restant en secondes (défaut : 60 min)
    private int timeRemaining = 3600; 
    
    // Le paquet de cartes disponibles (Id -> Carte)
    private Map<Integer, Card> deck;

    public GameEngine() {
        deck = new HashMap<>();
        initScenarioStationSpatiale();
    }

    /**
     * Initialise le scénario minimal dicté dans le cours/documents.
     */
    private void initScenarioStationSpatiale() {
        // La carte 10 est visible par défaut au début du jeu
        Card startCard = new Card(10, CardType.NEUTRE, "Sas de décompression (10)");
        startCard.setVisible(true);
        deck.put(10, startCard);

        Card c6 = new Card(6, CardType.BLEU, "Bras magnétique (6 B)");
        deck.put(6, c6);

        Card c24 = new Card(24, CardType.ROUGE, "Conduit entrebâillé (24 R)");
        deck.put(24, c24);

        deck.put(30, new Card(30, CardType.MACHINE, "Pile et Terminal code (30)"));

        Card c12 = new Card(12, CardType.NEUTRE, "Plafond étoiles (Indice visuel)");
        deck.put(12, c12);

        Card c13 = new Card(13, CardType.NEUTRE, "Tableau de bord (Indice visuel)");
        deck.put(13, c13);
        deck.put(45, new Card(45, CardType.RESULTAT, "Victoire (45) !"));
    }

    /**
     * Tente de combiner deux cartes.
     * Dans Unlock: Bleu + Rouge = Nouvelle Carte (la somme de leurs identifiants).
     */
    public Card combineCards(int idCard1, int idCard2) {
        Card c1 = deck.get(idCard1);
        Card c2 = deck.get(idCard2);

        if (c1 != null && c2 != null) {
            // Vérifie que l'une est Bleue et l'autre est Rouge
            boolean hasBlue = c1.getType() == CardType.BLEU || c2.getType() == CardType.BLEU;
            boolean hasRed = c1.getType() == CardType.ROUGE || c2.getType() == CardType.ROUGE;

            if (hasBlue && hasRed) {
                int sum = c1.getId() + c2.getId();
                Card resultCard = deck.get(sum);
                // Si la somme existe dans le deck, la combinaison est valide
                if (resultCard != null) {
                    resultCard.setVisible(true);
                    return resultCard;
                }
            }
        }
        
        // Si la combinaison est invalide, pénalité de 30sec
        applyPenalty(30); 
        return null;
    }

    /**
     * Tente d'entrer un code sur une machine.
     * Pour le scénario 1: Machine=30, Code=352. Si correct: affiche 45.
     */
    public Card tryCodeOnMachine(int machineId, int code) {
        if (machineId == 30 && code == 352) {
            Card victoryCard = deck.get(45);
            victoryCard.setVisible(true);
            return victoryCard;
        }

        // Code faux: -1 minute
        applyPenalty(60);
        return null;
    }

    /**
     * Applique une pénalité de temps au joueur.
     * @param penaltySeconds nombre de secondes à retirer
     */
    public void applyPenalty(int penaltySeconds) {
        System.out.println("❌ PENALITE : -" + penaltySeconds + " secondes !");
        timeRemaining -= penaltySeconds;
        if (timeRemaining < 0) timeRemaining = 0;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public Map<Integer, Card> getDeck() {
        return deck;
    }
}
