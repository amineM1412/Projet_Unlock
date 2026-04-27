package com.unlock.model;

import java.io.Serializable;

/**
 * Modèle de données pour une Carte Unlock!
 * Chaque carte possède un identifiant, un type (couleur), une description,
 * et des propriétés spécifiques selon son rôle dans le scénario.
 */
public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;                         // Ex: 10, 6, 24, 30...
    private CardType type;                  // NEUTRE, BLEU, ROUGE, MACHINE, CODE...
    private String description;             // Description textuelle affichée

    private boolean isVisible = false;      // Visible sur la table ?
    private String imagePath;               // Chemin vers l'image de la carte

    // --- Nouveaux champs pour le scénario étendu ---
    private String hint;                    // Indice textuel optionnel
    private int[] revealsCardIds;           // IDs des cartes révélées par fouille (double-clic)
    private String requiredCode;            // Code requis (cartes CODE/JAUNE uniquement)
    private int resultCardOnCode;           // Carte révélée si le bon code est entré
    private int[] requiredInventoryItems;   // IDs d'objets nécessaires (énigme finale)
    private boolean inInventory = false;    // L'objet est-il dans l'inventaire du joueur ?
    private int actNumber;                  // Acte auquel appartient la carte (1, 2 ou 3)
    private String miniGameId;             // ID du mini-jeu associé (cartes MACHINE uniquement)

    // ========== CONSTRUCTEURS ==========

    /**
     * Constructeur de base (compatibilité ascendante)
     */
    public Card(int id, CardType type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.isVisible = false;
        this.imagePath = null;
        this.hint = null;
        this.revealsCardIds = null;
        this.requiredCode = null;
        this.resultCardOnCode = -1;
        this.requiredInventoryItems = null;
        this.actNumber = 1;
        this.miniGameId = null;
    }

    /**
     * Constructeur complet pour les cartes du scénario étendu
     */
    public Card(int id, CardType type, String description, int actNumber) {
        this(id, type, description);
        this.actNumber = actNumber;
    }

    // ========== GETTERS ==========

    public int getId() {
        return id;
    }

    public CardType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getHint() {
        return hint;
    }

    public int[] getRevealsCardIds() {
        return revealsCardIds;
    }

    public String getRequiredCode() {
        return requiredCode;
    }

    public int getResultCardOnCode() {
        return resultCardOnCode;
    }

    public int[] getRequiredInventoryItems() {
        return requiredInventoryItems;
    }

    public boolean isInInventory() {
        return inInventory;
    }

    public int getActNumber() {
        return actNumber;
    }

    public String getMiniGameId() {
        return miniGameId;
    }

    // ========== SETTERS ==========

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void setRevealsCardIds(int[] revealsCardIds) {
        this.revealsCardIds = revealsCardIds;
    }

    public void setRequiredCode(String requiredCode) {
        this.requiredCode = requiredCode;
    }

    public void setResultCardOnCode(int resultCardOnCode) {
        this.resultCardOnCode = resultCardOnCode;
    }

    public void setRequiredInventoryItems(int[] requiredInventoryItems) {
        this.requiredInventoryItems = requiredInventoryItems;
    }

    public void setInInventory(boolean inInventory) {
        this.inInventory = inInventory;
    }

    public void setActNumber(int actNumber) {
        this.actNumber = actNumber;
    }

    public void setMiniGameId(String miniGameId) {
        this.miniGameId = miniGameId;
    }

    // ========== UTILITAIRES ==========

    /**
     * Vérifie si cette carte peut être fouillée (double-clic pour révéler d'autres cartes)
     */
    public boolean canBeSearched() {
        return revealsCardIds != null && revealsCardIds.length > 0;
    }

    /**
     * Vérifie si cette carte est un objet récupérable (BLEU ou ROUGE)
     */
    public boolean isCollectable() {
        return type == CardType.BLEU || type == CardType.ROUGE;
    }

    /**
     * Vérifie si cette carte est un mini-jeu
     */
    public boolean isMiniGame() {
        return type == CardType.MACHINE && miniGameId != null;
    }

    /**
     * Vérifie si cette carte nécessite un code
     */
    public boolean requiresCode() {
        return type == CardType.CODE && requiredCode != null;
    }

    @Override
    public String toString() {
        return "[" + id + " " + type.getLabel() + "] " + description;
    }
}
