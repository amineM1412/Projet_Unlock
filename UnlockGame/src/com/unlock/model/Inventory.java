package com.unlock.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventaire du joueur dans Unlock!
 * Stocke les cartes-objets (BLEU et ROUGE) récoltées.
 */
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Card> items;

    public Inventory() {
        this.items = new ArrayList<>();
    }

    /**
     * Ajoute une carte-objet à l'inventaire
     */
    public void addItem(Card card) {
        if (card != null && !hasItem(card.getId())) {
            card.setInInventory(true);
            items.add(card);
        }
    }

    /**
     * Retire une carte-objet de l'inventaire
     */
    public void removeItem(int cardId) {
        items.removeIf(c -> {
            if (c.getId() == cardId) {
                c.setInInventory(false);
                return true;
            }
            return false;
        });
    }

    /**
     * Vérifie si l'inventaire contient un objet donné
     */
    public boolean hasItem(int cardId) {
        return items.stream().anyMatch(c -> c.getId() == cardId);
    }

    /**
     * Vérifie si l'inventaire contient TOUS les objets spécifiés
     */
    public boolean hasAllItems(int[] cardIds) {
        if (cardIds == null) return false;
        for (int id : cardIds) {
            if (!hasItem(id)) return false;
        }
        return true;
    }

    /**
     * Retourne la liste des objets de l'inventaire
     */
    public List<Card> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Retourne le nombre d'objets dans l'inventaire
     */
    public int size() {
        return items.size();
    }

    /**
     * Vide l'inventaire
     */
    public void clear() {
        for (Card c : items) {
            c.setInInventory(false);
        }
        items.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Inventaire [" + items.size() + " objets] :\n");
        for (Card c : items) {
            sb.append("  - ").append(c.toString()).append("\n");
        }
        return sb.toString();
    }
}
