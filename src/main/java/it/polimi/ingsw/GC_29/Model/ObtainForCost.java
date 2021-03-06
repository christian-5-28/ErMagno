package it.polimi.ingsw.GC_29.Model;

import java.util.ArrayList;

/**
 * Created by Lorenzotara on 13/06/17.
 */
public class ObtainForCost implements Effect {

    private CardColor cardColor;
    private ArrayList<GoodType> goodTypesToPay;
    private GoodSet goodSetObtained;

    public ObtainForCost(CardColor cardColor, ArrayList<GoodType> goodTypesToPay, GoodSet goodSetObtained) {
        this.cardColor = cardColor;
        this.goodTypesToPay = goodTypesToPay;
        this.goodSetObtained = goodSetObtained;
    }


    /**
     * Calculates the goodset obtained by looking at the costs of the cards owned by the player
     * if those costs happen to involve the goodTypes in goodTypesToPay then an increment is calculated
     * that increment will be multiplied with the goodsetObtained
     * the final result will be the GoodSet that will be given to the player
     */
    @Override
    public void execute(Player status) {

        DevelopmentCard[] cards = status.getPersonalBoard().getLane(cardColor).getCards();

        int increment = 0;

        for (DevelopmentCard card : cards) {

            if (card != null) {
                for (GoodType goodType : goodTypesToPay) {
                    increment += card.getCardCost().getMainCost().getCost().getGoodAmount(goodType);
                    increment += card.getCardCost().getAlternativeCost().getCost().getGoodAmount(goodType);

                }
            }
        }

        for (GoodType goodType : GoodType.values()) {
            goodSetObtained.setGoodAmount(goodType, goodSetObtained.getGoodAmount(goodType)*increment);
        }

        (new ObtainEffect(goodSetObtained)).execute(status);

    }
}
