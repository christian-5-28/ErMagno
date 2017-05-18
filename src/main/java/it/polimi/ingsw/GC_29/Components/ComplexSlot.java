package it.polimi.ingsw.GC_29.Components;

/**
 * Created by AlbertoPennino on 18/05/2017.
 */
public class ComplexSlot {
    private final int victoryPointsGiven;
    private final int militaryPointsNeeded;

    public ComplexSlot(int victoryPointsGiven, int militaryPointsNeeded) {
        this.victoryPointsGiven = victoryPointsGiven;
        this.militaryPointsNeeded = militaryPointsNeeded;
    }

    public int getVictoryPointsGiven() {
        return victoryPointsGiven;
    }

    public int getMilitaryPointsNeeded() {
        return militaryPointsNeeded;
    }

}
