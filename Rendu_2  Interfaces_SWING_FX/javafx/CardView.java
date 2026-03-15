
/**
 * Vue graphique d'une Carte
 */
public class CardView {

    private Card card;

    public CardView(Card card) {
        this.card = card;

        // Style de base de la carte (100x150 pixels)
        this.setPrefSize(120, 180);
        this.setPadding(new Insets(10));
        this.setSpacing(5);

        // Application de la couleur d'en-tête selon le type (Rouge/Bleu/Neutre/Machine)
        String colorHex = "#9E9E9E"; // Gris neutre
        switch (card.getType()) {
            case ROUGE:
                colorHex = "#F44336";
                break;
            case BLEU:
                colorHex = "#2196F3";
                break;
            case MACHINE:
                colorHex = "#4CAF50";
                break;
            case RESULTAT:
                colorHex = "#FFC107";
                break;
            default:
                break;
        }

    public Card getCard() {
        return card;
    }
}
}

 

