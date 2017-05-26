package it.polimi.ingsw.GC_29.EffectBonusAndActions;

import it.polimi.ingsw.GC_29.Components.*;
import it.polimi.ingsw.GC_29.Player.PlayerStatus;

import java.util.ArrayList;

/**
 * Created by Lorenzotara on 19/05/17.
 */
public class TowerAction extends Action {

    //TODO testing per nuove condizioni di isPossible
    //TODO gestire il costo della carta sia per controlli (con le alternative) sia per l'esecuzione

    private Tower towerChosen;
    private int floorIndex;
    private GoodSet temporaryGoodSet; // accumula il bonus dell'actionSpace
    private GoodSet towerCost; // 3 coins
    private CardCost cardCost;
    private GoodSet discount; // discount che c'è se non è realAction e viene passato dall'ActionEffect già selezionato se c'è l'alternativa
    private GoodSet mainTotalCost;
    private GoodSet alternativeTotalCost;
    private DevelopmentCard cardSelected;

    public TowerAction(
            FamilyPawn pawnSelected,
            ActionType actionSelected,
            int workersSelected,
            boolean realAction,
            PlayerStatus playerStatus,
            Tower towerChosen,
            int floorIndex) {

        super(pawnSelected, actionSelected, workersSelected, realAction, playerStatus);
        this.towerChosen = towerChosen;
        this.floorIndex = floorIndex;
        this.actionSpaceSelected = towerChosen.getFloor(floorIndex).getActionSpace();
        this.temporaryGoodSet = new GoodSet();
        this.cardSelected = towerChosen.getFloor(floorIndex).getDevelopmentCard();
        this.cardCost = towerChosen.getFloor(floorIndex).getDevelopmentCard().getCardCost();
        this.mainTotalCost = new GoodSet();
        this.alternativeTotalCost = new GoodSet();
        this.towerCost = new GoodSet();

    }

    public TowerAction(FamilyPawn pawnSelected,
                       ActionType actionSelected,
                       int workersSelected,
                       boolean realAction,
                       PlayerStatus playerStatus,
                       Tower towerChosen,
                       int floorIndex,
                       GoodSet discount) {

        super(pawnSelected, actionSelected, workersSelected, realAction, playerStatus);
        this.towerChosen = towerChosen;
        this.floorIndex = floorIndex;
        this.actionSpaceSelected = towerChosen.getFloor(floorIndex).getActionSpace();
        this.temporaryGoodSet = new GoodSet();
        this.cardSelected = towerChosen.getFloor(floorIndex).getDevelopmentCard();
        this.cardCost = towerChosen.getFloor(floorIndex).getDevelopmentCard().getCardCost();
        this.discount = discount;
        this.mainTotalCost = new GoodSet();
        this.alternativeTotalCost = new GoodSet();
        this.towerCost = new GoodSet();
    }

    @Override
    public void execute() {

        super.addPawn();
        payCard();
        giveCard();
        activateCardEffects();
        update();
    }

    @Override
    public boolean isPossible() {

        return super.isPossible()
                && !checkFamilyPresence()
                && isTowerAccessPossible()
                && laneAvailable()
                && checkSufficientGoodsForCard();
    }

    /**
     * This method checks if there already is a player's familiar in the tower
     * @return true if there is a player's familiar, false otherwise
     */
    private boolean checkFamilyPresence() { // va a controllare nella torre se c'è un familiare del player

        boolean familiarPresent = false;
        for (Floor floor : towerChosen.getFloors()) {
            ActionSpace actionSpace = floor.getActionSpace();
            if (actionSpace.isOccupied()) {
                familiarPresent = actionSpace.getPawnPlaced().searchFamiliar(this.pawnSelected);
            }
        }
        return familiarPresent;
    }

    /**
     * This method checks if the tower is occupied: in this case it controls if the player
     * has a BM that make him access the tower; if the player has it return true, otherwise it checks if the player has enough coins
     * to enter the tower
     * @return true if you can access the tower, false otherwise
     */
    private boolean isTowerAccessPossible() {

        if (towerChosen.isOccupied()) {

            if (!Filter.applySpecial(playerStatus, towerCost)) {
                int goldCost = towerChosen.getCostIfOccupied();

                if (playerStatus.getActualGoodSet().getGoodAmount(GoodType.COINS) >= goldCost) {
                    //This branch is taken if the player have enough coins to pay the access to the occupied tower
                    towerCost = new GoodSet(0,0,goldCost,0,0,0,0);
                    return true;

                } else {
                    System.out.println("You don't have enough coins to access to the tower!");
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * This method saves the resources of the actionSpace Effect, after being filtered, in order to make other
     * controls in the future. The effect is not activated.
     */
    private void setActionSpaceEffect() {

        ObtainEffect effect = (ObtainEffect) this.actionSpaceSelected.getEffect();
        GoodSet actionSpaceGoodSet = effect.getGoodsObtained();
        Filter.apply(this.playerStatus, actionSpaceGoodSet);
        temporaryGoodSet = actionSpaceGoodSet;

    }


    /**
     * After saving the potential resources that the actionSpace can give to the player,
     * this method filters the cost of the card, add the cost of the tower and subtract the resources received
     * by the actionSpace effect and if it is an action created by an effect with a discount, subtract also the discount.
     * Then it checks if the player has enough resources in his goodSet to pay the selected card
     * @return true if there are enough resources to pay the card, false otherwise
     */
    private boolean checkSufficientGoodsForCard() {

        setActionSpaceEffect();
        Filter.apply(playerStatus, cardCost);

        mainTotalCost.addGoodSet(towerCost);
        mainTotalCost.subGoodSet(temporaryGoodSet);
        alternativeTotalCost.addGoodSet(mainTotalCost);
        mainTotalCost.addGoodSet(cardCost.getMainCost());
        alternativeTotalCost.addGoodSet(cardCost.getAlternativeCost());

        if (!this.realAction) {
            mainTotalCost.subGoodSet(discount);
            alternativeTotalCost.subGoodSet(discount);
        }

        return playerStatus.getActualGoodSet().enoughResources(mainTotalCost)
                || playerStatus.getActualGoodSet().enoughResources(alternativeTotalCost);
    }

    private boolean checkTerritorySlotAvailability() {
        TerritoryLane lane = playerStatus.getPersonalBoard().getTerritoryLane();
        int index = lane.getFirstFreeSlotIndex();
        return playerStatus.getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS) == lane.getSlot(index).getMilitaryPointsNeeded();
    }

    /**
     * laneAvailable checks if the lane where the card will be put has free space.
     * If the tower chosen contains Territory cards, there is another control to
     * check if the player has enough military points to put the card in the first
     * free slot
     * @return true if lane aren't full e the player has enough military points to pay
     * the necessary militaryNecessaryPoints, false otherwise
     */
    private boolean laneAvailable() {

        boolean var1 = true;
        CardColor actualColor = towerChosen.getCardType();
        if (actualColor == CardColor.GREEN) {
            var1 = checkTerritorySlotAvailability();
        }

        return var1
                && playerStatus.getPersonalBoard().getLane(actualColor).isFree();

    }

    private void payCard() {

        playerStatus.getActualGoodSet().subGoodSet(mainTotalCost);
    }


    /**
     * This method removes the Card from the chosen floor and gives this card to
     * the player. From the colour of the card the method chooses where to add
     * the card (TerritoryLane, FamilyLane, BuildingLane, VenturesLane).
     */
    private void giveCard() {

        DevelopmentCard card = towerChosen.getFloor(floorIndex).removeCard();
        switch (card.getColor()) {
            case GREEN:
                playerStatus.getPersonalBoard().getTerritoryLane().addCard(card);
                break;
            case BLUE:
                playerStatus.getPersonalBoard().getFamilyLane().addCard(card);
                break;
            case YELLOW:
                playerStatus.getPersonalBoard().getBuildingLane().addCard(card);
                break;
            case PURPLE:
                playerStatus.getPersonalBoard().getVenturesLane().addCard(card);
                break;
            default:
                System.out.println("Ops! There has been an error!");
        }

    }


    /**
     * This method activates all the immediate effects of the selected card
     */
    private void activateCardEffects() {

        ArrayList<Effect> immediateEffects = this.cardSelected.getImmediateEffect();
        for (Effect immediateEffect : immediateEffects) {
            immediateEffect.execute(playerStatus);
        }
    }


    /**
     * This method update:
     * sets availability false for the pawn used (super)
     * sets the actionSpace as occupied (super)
     * sets permanent effects of the card in playerStatus
     * updates number of card per colour in playerStatus
     * ...
     */
    protected void update() {

        super.update();

        if (cardSelected.getColor() == CardColor.BLUE) {
            for (Effect effect : cardSelected.getPermanentEffect()) {
                playerStatus.getBonusAndMalusOnAction().add((BonusAndMalusOnAction) effect); //cast obbligatorio
            }
        }

        int numberOfCards = playerStatus.getCardsOwned().get(cardSelected.getColor());
        numberOfCards++;
        playerStatus.getCardsOwned().put(cardSelected.getColor(), numberOfCards);


        // altre cose?
    }

}
