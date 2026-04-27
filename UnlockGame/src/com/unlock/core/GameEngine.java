package com.unlock.core;

import com.unlock.model.Card;
import com.unlock.model.CardType;
import com.unlock.model.GameState;
import com.unlock.model.Inventory;

import java.io.IOException;
import java.io.Serializable;

import java.util.Map;

/**
 * Moteur principal du jeu Unlock! — "Signal Fantôme"
 * Gère le scénario complet (35 cartes, 3 actes), l'oxygène,
 * le timer, l'inventaire, les mini-jeux et la sauvegarde.
 */
public class GameEngine implements Serializable {

    private static final long serialVersionUID = 2L;

    // État du jeu (sérialisable)
    private GameState state;

    // Mini-jeux (stockés séparément pour accès rapide)
    private CablesMiniGame cablesMiniGame;
    private RadioMiniGame radioMiniGame;
    private PipesMiniGame pipesMiniGame;

    // Répertoire de sauvegarde
    private String saveDirectory = ".";

    public GameEngine() {
        state = new GameState();
        cablesMiniGame = new CablesMiniGame();
        radioMiniGame = new RadioMiniGame();
        pipesMiniGame = new PipesMiniGame();
        initScenarioStationSpatiale();
    }

    // ================================================================
    //  INITIALISATION DU SCÉNARIO — 35 CARTES
    // ================================================================

    private void initScenarioStationSpatiale() {
        Map<Integer, Card> deck = state.getDeck();

        // =============== ACTE 1 — Sas de Décompression ===============

        // Carte 10 — Lieu de départ
        Card c10 = new Card(10, CardType.NEUTRE, "Sas de décompression — Lumière rouge clignotante, hublot fêlé.", 1);
        c10.setRevealsCardIds(new int[]{12, 13, 6, 24});
        c10.setVisible(true); // Carte de départ
        deck.put(10, c10);

        // Carte 12 — Indice visuel
        Card c12 = new Card(12, CardType.NEUTRE, "Plafond étoilé — Constellation en flèche. 3 étoiles brillantes.", 1);
        c12.setHint("Les 3 étoiles forment le chiffre 3-5-2...");
        deck.put(12, c12);

        // Carte 13 — Indice visuel
        Card c13 = new Card(13, CardType.NEUTRE, "Tableau de bord — Écran fissuré : '3_2'. Voyant O₂ en rouge.", 1);
        c13.setHint("Le chiffre manquant sur l'écran... 3?2");
        deck.put(13, c13);

        // Carte 6 — Objet Bleu
        Card c6 = new Card(6, CardType.BLEU, "Bras magnétique — Bras articulé avec aimant, rangé dans un casier.", 1);
        deck.put(6, c6);

        // Carte 24 — Objet Rouge
        Card c24 = new Card(24, CardType.ROUGE, "Conduit entrebâillé — Objet coincé au fond du conduit.", 1);
        deck.put(24, c24);

        // Carte 30 — Code (ex-Machine, maintenant CODE jaune)
        Card c30 = new Card(30, CardType.CODE, "Terminal d'accès — Écran vert, clavier numérique. 'CODE SECTEUR B'.", 1);
        c30.setRequiredCode("352");
        c30.setResultCardOnCode(45);
        deck.put(30, c30);

        // Carte 45 — Lieu (transition vers Acte 2)
        Card c45 = new Card(45, CardType.NEUTRE, "Couloir Secteur B — Éclairage d'urgence bleu. Portes numérotées.", 2);
        c45.setRevealsCardIds(new int[]{14, 17, 8, 21});
        deck.put(45, c45);

        // =============== ACTE 2 — Laboratoire & Communications ===============

        // Carte 14 — Lieu
        Card c14 = new Card(14, CardType.NEUTRE, "Laboratoire — Tables de chimie, écrans brisés, capsules en apesanteur.", 2);
        c14.setRevealsCardIds(new int[]{7, 18, 11});
        deck.put(14, c14);

        // Carte 17 — Lieu
        Card c17 = new Card(17, CardType.NEUTRE, "Salle de communication — Antenne intérieure, console radio, oscilloscope.", 2);
        c17.setHint("La console radio semble fonctionnelle... (voir carte 25)");
        deck.put(17, c17);

        // Carte 8 — Objet Bleu
        Card c8 = new Card(8, CardType.BLEU, "Tournevis isolé — Tournevis à embout spécial dans une boîte flottante.", 2);
        deck.put(8, c8);

        // Carte 21 — Objet Rouge
        Card c21 = new Card(21, CardType.ROUGE, "Panneau électrique — Panneau mural, fils exposés, étincelles.", 2);
        deck.put(21, c21);

        // Carte 7 — Objet Bleu (clé pour l'énigme finale)
        Card c7 = new Card(7, CardType.BLEU, "Cristal de données — Cristal luminescent bleu, données cryptées.", 2);
        deck.put(7, c7);

        // Carte 18 — Objet Rouge
        Card c18 = new Card(18, CardType.ROUGE, "Lecteur de cristaux — Appareil avec fente, écran noir.", 2);
        deck.put(18, c18);

        // Carte 11 — Indice
        Card c11 = new Card(11, CardType.NEUTRE, "Journal du Dr. Vasquez — 'Fréquence 4721', 'les miroirs reflètent la vérité'.", 2);
        c11.setHint("Fréquence de secours : 4721 Hz. Code de l'équipage : année de la mission.");
        deck.put(11, c11);

        // Carte 19 — Indice
        Card c19 = new Card(19, CardType.NEUTRE, "Photo d'équipage — Dr. Vasquez, Ing. Chen, Cpt. Nakamura — 1987.", 2);
        c19.setHint("L'année de la mission est inscrite sous la photo : 1987");
        deck.put(19, c19);

        // Carte 36 — Indice
        Card c36 = new Card(36, CardType.NEUTRE, "Schéma de câblage — Ordre : Rouge→1, Jaune→2, Bleu→3, Vert→4.", 2);
        c36.setHint("L'ordre correct des câbles : Rouge, Jaune, Bleu, Vert.");
        deck.put(36, c36);

        // Carte 29 — Mini-jeu Câbles (8B + 21R = 29)
        Card c29 = new Card(29, CardType.MACHINE, "Boîtier d'alimentation — 4 câbles déconnectés (R, J, B, V).", 2);
        c29.setMiniGameId(CablesMiniGame.GAME_ID);
        deck.put(29, c29);

        // Carte 25 — Mini-jeu Radio (7B + 18R = 25)
        Card c25 = new Card(25, CardType.MACHINE, "Transmission radio — Console avec fréquences, oscilloscope, molettes.", 2);
        c25.setMiniGameId(RadioMiniGame.GAME_ID);
        deck.put(25, c25);

        // Carte 38 — Lieu (révélé par mini-jeu câbles)
        Card c38 = new Card(38, CardType.NEUTRE, "Sas de maintenance — Petit sas technique, tuyaux, accès Secteur C.", 2);
        c38.setRevealsCardIds(new int[]{15, 9, 26});
        deck.put(38, c38);

        // Carte 33 — Code (révélé par mini-jeu radio)
        Card c33 = new Card(33, CardType.CODE, "Coffre sécurisé — Coffre métallique, clavier à code. 'Protocole d'urgence'.", 2);
        c33.setRequiredCode("1987");
        c33.setResultCardOnCode(40);
        deck.put(33, c33);

        // Carte 40 — Lieu (contient le kit SOS)
        Card c40 = new Card(40, CardType.NEUTRE, "Kit SOS spatial — Mallette : émetteur SOS démonté + manuel.", 2);
        c40.setRevealsCardIds(new int[]{5, 20});
        deck.put(40, c40);

        // Carte 27 — Pénalité
        Card c27 = new Card(27, CardType.PENALITE, "Court-circuit ! — Étincelles et fumée. Perte de 2 minutes d'O₂.", 2);
        deck.put(27, c27);

        // =============== ACTE 3 — Réacteur & Énigme Finale ===============

        // Carte 15 — Lieu
        Card c15 = new Card(15, CardType.NEUTRE, "Salle du réacteur — Réacteur central, lumières rouges, jauges dangereuses.", 3);
        c15.setRevealsCardIds(new int[]{3, 22, 16});
        deck.put(15, c15);

        // Carte 9 — Objet Bleu
        Card c9 = new Card(9, CardType.BLEU, "Clé magnétique — Carte-clé avec bande dorée, sous un panneau.", 3);
        deck.put(9, c9);

        // Carte 26 — Objet Rouge
        Card c26 = new Card(26, CardType.ROUGE, "Verrou de secours — Serrure magnétique, porte 'ACCÈS RESTREINT'.", 3);
        deck.put(26, c26);

        // Carte 35 — Lieu (9B + 26R = 35)
        Card c35 = new Card(35, CardType.NEUTRE, "Salle des miroirs — Miroirs déformants, inscriptions inversées.", 3);
        c35.setHint("Message dans le miroir : 'Séquence réacteur : 2-8-4-6'");
        c35.setRevealsCardIds(new int[]{19, 36, 44});
        deck.put(35, c35);

        // Carte 3 — Objet Bleu
        Card c3 = new Card(3, CardType.BLEU, "Barre de combustible — Barre radiante dans un conteneur de plomb.", 3);
        deck.put(3, c3);

        // Carte 22 — Objet Rouge
        Card c22 = new Card(22, CardType.ROUGE, "Injecteur de combustible — Mécanisme d'injection, jauge pression.", 3);
        deck.put(22, c22);

        // Carte 16 — Mini-jeu Tuyaux
        Card c16 = new Card(16, CardType.MACHINE, "Système de tuyauterie — Réseau de tuyaux, vannes, coudes. Fluide bloqué.", 3);
        c16.setMiniGameId(PipesMiniGame.GAME_ID);
        deck.put(16, c16);

        // Carte 5 — Objet Bleu (composant SOS)
        Card c5 = new Card(5, CardType.BLEU, "Antenne SOS — Antenne parabolique pliable du kit de secours.", 3);
        deck.put(5, c5);

        // Carte 20 — Objet Rouge (composant SOS)
        Card c20 = new Card(20, CardType.ROUGE, "Module d'alimentation — Batterie de secours pour l'émetteur.", 3);
        deck.put(20, c20);

        // Carte 44 — Indice
        Card c44 = new Card(44, CardType.NEUTRE, "Manuel du réacteur — Page arrachée : 'Séquence : 2-8-4-6'.", 3);
        c44.setHint("Séquence d'amorçage du réacteur : 2846");
        deck.put(44, c44);

        // Carte 42 — Code (révélé par mini-jeu tuyaux)
        Card c42 = new Card(42, CardType.CODE, "Console du réacteur — Console principale, 3 emplacements.", 3);
        c42.setRequiredCode("2846");
        c42.setResultCardOnCode(50);
        deck.put(42, c42);

        // Carte 50 — Énigme finale (MACHINE spéciale)
        Card c50 = new Card(50, CardType.MACHINE, "Station SOS finale — Console avec emplacements : antenne, batterie, cristal.", 3);
        c50.setRequiredInventoryItems(new int[]{5, 20, 7}); // Antenne + Batterie + Cristal
        c50.setMiniGameId("final_sos");
        deck.put(50, c50);

        // Carte 99 — Victoire !
        Card c99 = new Card(99, CardType.RESULTAT, "VICTOIRE ! SOS ENVOYE - Frequence 4721 Hz - SECOURS EN ROUTE !", 3);
        deck.put(99, c99);

        // Carte 77 — Pénalité grave
        Card c77 = new Card(77, CardType.PENALITE, "FUITE D'OXYGENE ! Perte de 5 minutes d'O2 !", 0);
        deck.put(77, c77);
    }

    // ================================================================
    //  FOUILLE (Double-clic sur une carte)
    // ================================================================

    /**
     * Fouille une carte pour révéler les cartes cachées associées.
     * @param cardId identifiant de la carte fouillée
     * @return nombre de nouvelles cartes révélées, ou -1 si pénalité
     */
    public int handleFouille(int cardId) {
        Card card = state.getCard(cardId);
        if (card == null || !card.isVisible()) return 0;

        int[] reveals = card.getRevealsCardIds();
        if (reveals == null || reveals.length == 0) {
            // Rien à trouver → pénalité légère
            applyPenalty(10);
            return -1;
        }

        int newCards = 0;
        for (int rid : reveals) {
            Card hidden = state.getCard(rid);
            if (hidden != null && !hidden.isVisible()) {
                hidden.setVisible(true);
                newCards++;
            }
        }

        if (newCards == 0) {
            // Déjà tout trouvé → pénalité
            applyPenalty(10);
            return -1;
        }

        // Changer d'acte si la carte fouillée introduit un nouvel acte
        if (cardId == 45) state.setCurrentAct(2);
        if (cardId == 38 || cardId == 15) state.setCurrentAct(3);

        return newCards;
    }

    // ================================================================
    //  COMBINAISON DE CARTES (Bleu + Rouge = somme)
    // ================================================================

    /**
     * Tente de combiner deux cartes.
     * Bleu + Rouge = Nouvelle Carte (somme des identifiants).
     */
    public Card combineCards(int idCard1, int idCard2) {
        Card c1 = state.getCard(idCard1);
        Card c2 = state.getCard(idCard2);

        if (c1 != null && c2 != null) {
            boolean hasBlue = c1.getType() == CardType.BLEU || c2.getType() == CardType.BLEU;
            boolean hasRed = c1.getType() == CardType.ROUGE || c2.getType() == CardType.ROUGE;

            if (hasBlue && hasRed) {
                int sum = c1.getId() + c2.getId();
                Card resultCard = state.getCard(sum);

                if (resultCard != null && !resultCard.isVisible()) {
                    resultCard.setVisible(true);
                    return resultCard;
                } else if (resultCard != null && resultCard.isVisible()) {
                    // Carte déjà visible → pénalité
                    applyPenalty(30);
                    showPenaltyCard(27);
                    return null;
                }
            }
        }

        // Combinaison invalide → pénalité de 30 secondes
        applyPenalty(30);
        return null;
    }

    // ================================================================
    //  CODES (Cartes jaunes)
    // ================================================================

    /**
     * Tente d'entrer un code sur une carte CODE.
     * @param machineId identifiant de la carte CODE
     * @param code le code entré par le joueur
     * @return la carte révélée si correct, null sinon
     */
    public Card tryCode(int machineId, String code) {
        Card machine = state.getCard(machineId);
        if (machine == null || !machine.requiresCode()) {
            applyPenalty(60);
            return null;
        }

        if (machine.getRequiredCode().equals(code)) {
            int resultId = machine.getResultCardOnCode();
            Card resultCard = state.getCard(resultId);
            if (resultCard != null) {
                resultCard.setVisible(true);
                // Auto-retirer la carte CODE apres utilisation reussie
                machine.setVisible(false);
                return resultCard;
            }
        }

        // Code incorrect → pénalité de 60 secondes
        applyPenalty(60);
        return null;
    }

    /**
     * Ancienne méthode maintenue pour compatibilité
     */
    public Card tryCodeOnMachine(int machineId, int code) {
        return tryCode(machineId, String.valueOf(code));
    }

    // ================================================================
    //  MINI-JEUX
    // ================================================================

    /**
     * Retourne le mini-jeu associé à un identifiant
     */
    public MiniGame getMiniGame(String miniGameId) {
        if (miniGameId == null) return null;
        switch (miniGameId) {
            case CablesMiniGame.GAME_ID: return cablesMiniGame;
            case RadioMiniGame.GAME_ID: return radioMiniGame;
            case PipesMiniGame.GAME_ID: return pipesMiniGame;
            default: return null;
        }
    }

    /**
     * Appelé quand un mini-jeu est réussi
     */
    public Card onMiniGameSuccess(String miniGameId) {
        state.completeMiniGame(miniGameId);

        switch (miniGameId) {
            case CablesMiniGame.GAME_ID:
                // Câbles réussis → révèle carte 38 (Sas de maintenance)
                Card c38 = state.getCard(38);
                if (c38 != null) { c38.setVisible(true); return c38; }
                break;
            case RadioMiniGame.GAME_ID:
                // Radio réussie → révèle carte 33 (Coffre sécurisé)
                Card c33 = state.getCard(33);
                if (c33 != null) { c33.setVisible(true); return c33; }
                break;
            case PipesMiniGame.GAME_ID:
                // Tuyaux réussis → révèle carte 42 (Console du réacteur)
                Card c42 = state.getCard(42);
                if (c42 != null) { c42.setVisible(true); return c42; }
                break;
        }
        return null;
    }

    /**
     * Appelé quand un mini-jeu échoue (tentative de validation incorrecte)
     */
    public void onMiniGameFailure() {
        applyPenalty(30);
    }

    // ================================================================
    //  ÉNIGME FINALE (SOS)
    // ================================================================

    /**
     * Tente de finaliser l'envoi du SOS.
     * Nécessite les cartes 5 (antenne), 20 (batterie), 7 (cristal) en inventaire.
     * @return true si le SOS est envoyé (victoire)
     */
    public boolean tryFinalSOS() {
        Card c50 = state.getCard(50);
        if (c50 == null || !c50.isVisible()) return false;

        int[] required = c50.getRequiredInventoryItems();
        if (required == null) return false;

        Inventory inv = state.getInventory();
        if (inv.hasAllItems(required)) {
            // Victoire !
            Card c99 = state.getCard(99);
            if (c99 != null) c99.setVisible(true);
            state.setGameWon(true);
            return true;
        }

        return false;
    }

    /**
     * Retourne la liste des composants manquants pour le SOS
     */
    public String getMissingSosComponents() {
        Inventory inv = state.getInventory();
        StringBuilder sb = new StringBuilder();
        if (!inv.hasItem(5)) sb.append("- Antenne SOS (carte 5)\n");
        if (!inv.hasItem(20)) sb.append("- Module d'alimentation (carte 20)\n");
        if (!inv.hasItem(7)) sb.append("- Cristal de données (carte 7)\n");
        return sb.length() > 0 ? sb.toString() : "Tous les composants sont réunis !";
    }

    // ================================================================
    //  INVENTAIRE
    // ================================================================

    /**
     * Ajoute une carte à l'inventaire du joueur
     */
    public boolean addToInventory(int cardId) {
        Card card = state.getCard(cardId);
        if (card != null && card.isVisible() && card.isCollectable()) {
            state.getInventory().addItem(card);
            return true;
        }
        return false;
    }

    /**
     * Retourne l'inventaire
     */
    public Inventory getInventory() {
        return state.getInventory();
    }

    // ================================================================
    //  PÉNALITÉS & OXYGÈNE
    // ================================================================

    /**
     * Applique une pénalité (réduit le temps ET l'oxygène)
     */
    public void applyPenalty(int penaltySeconds) {
        System.out.println("PENALITE : -" + penaltySeconds + " secondes !");
        state.reduceTime(penaltySeconds);
        state.reduceOxygen(penaltySeconds);

        // Vérification Game Over
        if (state.isOxygenDepleted()) {
            state.setGameLost(true);
            System.out.println("OXYGENE EPUISE -- GAME OVER !");
        }
    }

    /**
     * Décrémente de 1 seconde (appelé chaque seconde par le timer)
     */
    public void tick() {
        state.reduceTime(1);
        state.reduceOxygen(1);

        if (state.isOxygenDepleted()) {
            state.setGameLost(true);
        }
    }

    /**
     * Affiche une carte de pénalité (si elle existe dans le deck)
     */
    private void showPenaltyCard(int penaltyCardId) {
        Card penalty = state.getCard(penaltyCardId);
        if (penalty != null) {
            penalty.setVisible(true);
        }
    }

    // ================================================================
    //  SAUVEGARDE / CHARGEMENT
    // ================================================================

    public void saveGame() throws IOException {
        state.saveToFile(saveDirectory);
    }

    public void loadGame() throws IOException, ClassNotFoundException {
        state = GameState.loadFromFile(saveDirectory);
    }

    public boolean hasSaveFile() {
        return GameState.saveExists(saveDirectory);
    }

    public void setSaveDirectory(String dir) {
        this.saveDirectory = dir;
    }

    // ================================================================
    //  ACCESSEURS
    // ================================================================

    public int getTimeRemaining() {
        return state.getTimeRemaining();
    }

    public int getOxygenRemaining() {
        return state.getOxygenRemaining();
    }

    public int getOxygenPercentage() {
        return state.getOxygenPercentage();
    }

    public int getCurrentAct() {
        return state.getCurrentAct();
    }

    public Map<Integer, Card> getDeck() {
        return state.getDeck();
    }

    public GameState getState() {
        return state;
    }

    public boolean isGameWon() {
        return state.isGameWon();
    }

    public boolean isGameLost() {
        return state.isGameLost();
    }

    public boolean isGameOver() {
        return state.isGameOver();
    }

    public CablesMiniGame getCablesMiniGame() { return cablesMiniGame; }
    public RadioMiniGame getRadioMiniGame() { return radioMiniGame; }
    public PipesMiniGame getPipesMiniGame() { return pipesMiniGame; }
}
