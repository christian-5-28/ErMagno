package it.polimi.ingsw.GC_29.Model;


import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lorenzotara on 17/05/17.
 */
public class DevelopmentCard {

    private String special;
    private Era era;
    private CardCost cardCost;
    private CardColor color;
    private ArrayList<Effect> immediateEffect;
    private ArrayList<Effect> permanentEffect;
    private boolean withActionValue;
    private int actionValue;


    public DevelopmentCard(
            String special,
            Era era,
            CardCost cardCost,
            CardColor color,
            ArrayList<Effect> immediateEffect,
            ArrayList<Effect> permanentEffect,
            boolean withActionValue,
            int actionValue) {

        this.special = special;
        this.era = era;
        this.cardCost = cardCost;
        this.color = color;
        this.immediateEffect = immediateEffect;
        this.permanentEffect = permanentEffect;
        this.withActionValue = withActionValue;
        this.actionValue = actionValue;
    }

    public String getSpecial() {
        return special;
    }

    public Era getEra() {
        return era;
    }

    public CardCost getCardCost() { // immutable object
        return new CardCost(cardCost);
    }

    public CardColor getColor() {
        return color;
    }

    public List<Effect> getImmediateEffect() {
        return immediateEffect;
    }

    public List<Effect> getPermanentEffect() {
        return permanentEffect;
    }

    public boolean isWithActionValue() {
        return withActionValue;
    }

    public int getActionValue() {
        return actionValue;
    }

    @Override
    public String toString() {

        String returnString = "DevelopmentCard{ \n"
                + "special = '" + special + "'\n"
                + "era = " + era + "\n"
                + "color = " + color + "\n"
                + "cardCost = " + cardCost + "\n"
                + "immediateEffect = " + immediateEffect + "\n"
                + "permanentEffect = " + permanentEffect + "\n";

        if (withActionValue) {

        returnString =  returnString
                        + "actionValue = " + actionValue + "\n"
                        + '}';
        }

        return returnString;

    }

    public String toTable() {
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.addRule();
        asciiTable.addRow("Name: " + special, "Era: " + era);
        asciiTable.addRule();
        asciiTable.addRow( "Color: " + color, "Action Value: " + actionValue);
        asciiTable.addRule();


        StringBuilder bldImm = new StringBuilder();

        bldImm.append("Immediate Effects: \n");
        for (Effect effect : immediateEffect) {
            bldImm.append(effect.toString());
            bldImm.append("\n");
        }

        String stringImm = bldImm.toString();


        StringBuilder bldPerm = new StringBuilder();
        bldPerm.append("Permanent Effects: \n");

        for (Effect effect : permanentEffect) {
            bldPerm.append(effect.toString());
            bldPerm.append("\n");
        }

        String stringPer = bldPerm.toString();


        asciiTable.addRow(stringImm, stringPer);
        asciiTable.addRule();
        asciiTable.addRow("Main Cost: " + cardCost.getMainCost().getCost(), "Necessary Resources: " + cardCost.getMainCost().getNecessaryResources());
        asciiTable.addRow("Alternative Cost: " + cardCost.getAlternativeCost().getCost(), "Necessary Resources: " + cardCost.getAlternativeCost().getNecessaryResources());
        asciiTable.addRule();
        asciiTable.setTextAlignment(TextAlignment.CENTER);
        return asciiTable.render();
    }

    public void setCardCost(CardCost cardCost) {
        this.cardCost = cardCost;
    }
}
