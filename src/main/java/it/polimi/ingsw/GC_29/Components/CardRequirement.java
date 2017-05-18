package it.polimi.ingsw.GC_29.Components;

import it.polimi.ingsw.GC_29.Player.Player;

/**
 * Created by Lorenzotara on 18/05/17.
 */
public class CardRequirement extends Requirement {
    private CardColor cardColor;
    private int number;

    public CardRequirement(CardColor cardColor, int number) {
        this.cardColor = cardColor;
        this.number = number;
    }

    @Override
    public void check(Player player) {
        super.check(player);
    }
}