package it.polimi.ingsw.GC_29.Components;

/**
 * Created by Lorenzotara on 17/05/17.
 */
public class GameBoard {
    private Track victoryPointsTrack;
    private Track venturesPointsTrack;
    private Track turnOrderTrack;
    private FaithPointsTrack faithPointsTrack;
    private Tower[] towerList;
    private ExcommunicationLane excommunicationLane;
    private CouncilPalaceActionSpace councilPalace;
    private Workspace harvestArea;
    private Workspace productionArea;
    private Market market;
    private Dice[] diceLane;

    public Track getVictoryPointsTrack() {
        return victoryPointsTrack;
    }

    public Track getVenturesPointsTrack() {
        return venturesPointsTrack;
    }

    public Track getTurnOrderTrack() {
        return turnOrderTrack;
    }

    public FaithPointsTrack getFaithPointsTrack() {
        return faithPointsTrack;
    }

    public Tower[] getTowerList() {
        return towerList;
    }

    public ExcommunicationLane getExcommunicationLane() {
        return excommunicationLane;
    }

    public CouncilPalaceActionSpace getCouncilPalace() {
        return councilPalace;
    }

    public Workspace getHarvestArea() {
        return harvestArea;
    }

    public Workspace getProductionArea() {
        return productionArea;
    }

    public Market getMarket() {
        return market;
    }

    public void clearAll() {
    }
}
