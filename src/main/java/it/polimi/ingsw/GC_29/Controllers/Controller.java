package it.polimi.ingsw.GC_29.Controllers;

import com.google.gson.GsonBuilder;
import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import it.polimi.ingsw.GC_29.Client.ClientRMI.ClientRMIView;
import it.polimi.ingsw.GC_29.Controllers.Change.*;
import it.polimi.ingsw.GC_29.Controllers.Input.EndTurn;
import it.polimi.ingsw.GC_29.Controllers.Input.Input;
import it.polimi.ingsw.GC_29.Model.*;
import it.polimi.ingsw.GC_29.Model.Player;
import it.polimi.ingsw.GC_29.Model.PlayerColor;
import it.polimi.ingsw.GC_29.Server.Observer;
import it.polimi.ingsw.GC_29.Server.ObserverException;
import it.polimi.ingsw.GC_29.Server.ServerNewGame;
import it.polimi.ingsw.GC_29.Server.SuspendPlayer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Christian on 07/06/2017.
 */

/**
 * the controller class is an Observer of Input Objects, it observes the views of server side and when the views call
 * a notifyObserver(Input input) the update(Input input) of the controller is called and it performs the "perform" method
 * of the Input Object
 */
public class Controller implements Observer<Input>  {

    private final Model model;

    private long throwDicesTime;
    private long chooseBonusTilesTime;
    private long prayTime;
    private long turnTime;

    private Integer playersPraying;
    private ActionChecker actionChecker;
    private int currentBonusTileIndexPlayer;
    private Timer timer;

    private List<Player> playerSuspended;

    private List<Player> playerReconnected;
    private Map<Player, Integer> playerBonusTileIndexMap;
    private int closedClients;
    private ServerNewGame currentMatch;
    private final int minNumberOfPlayers = 2;
    private List<Player> playerDisconnected;

    private static final Logger LOGGER  = Logger.getLogger(ClientRMIView.class.getName());

    private final String timerFilePath = "timerMove/timerMove";


    public Controller(Model model){
        this.model = model;
        playersPraying = 0;
        actionChecker = new ActionChecker(model);
        playerReconnected = new ArrayList<>();
        playerBonusTileIndexMap = new HashMap<>();
        closedClients = model.getTurnOrder().size();
        playerDisconnected = new ArrayList<>();

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(timerFilePath);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }

        long timer = 180000;

        if(fileReader!=null){

            timer = new GsonBuilder().create().fromJson(fileReader, Long.class);

            try {
                fileReader.close();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, e.getMessage(), e);
            }

        }

        throwDicesTime = timer;
        chooseBonusTilesTime = timer;
        prayTime = timer;
        turnTime = timer;

        createActions();
    }

    /**
     * update method called by the server view after it has received an input
     * @param input
     */
    public void update(Input input) {
        System.out.println("I AM THE CONTROLLER UPDATING THE MODEL");

        try {
            Observer.super.update(input);
        } catch (ObserverException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }

        try {
            input.perform(model, this);
        } catch (RemoteException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    @Override
    public void update() {
        // Auto-generated method stub

    }

    /**
     * When every player has done 4 turns, this method is called by the input EndTurn.
     * At first it checks if it is an even round: in this case it calls
     * excommunicatePlayers that excommunicates the players that do not have enough
     * faith points and then, if there is any, it asks to every player that has
     * enough points if he wants to pray or not, setting their state to PRAY.
     * If it is an odd round, it calls setNewRound()
     */
    public void handleEndRound() {

        System.out.println("handle end round del controller");

        int round = model.getCurrentRound();
        if (round%2 == 0) {
            model.setGameState(GameState.CHURCHRELATION);
            List<Player> safePlayers = excommunicatePlayers();
            playersPraying = safePlayers.size();

            if(!safePlayers.isEmpty()){

                for (Player safePlayer : safePlayers) {
                    safePlayer.setPlayerState(PlayerState.PRAY);
                    startTimer(safePlayer);
                }
            }
            else{
                setNewRound();
            }

        }
        else setNewRound();
    }


    /**
     * This method handles the setting of a new round, calling
     * the methods that make the pawns available again and the
     * once per round leader cards available again. If the Era is the
     * third one, endGame() is called.
     *
     * After having set the players that have to skip the first turn in case of the specific
     * malus and having cleared the gameboard, it sets the first player to the state THROWDICES.
     * Finally it sets the turn to 1.
     *
     */
    public void setNewRound() {

        setFamilyPawnsAndLeaderValues();
        setNewTurnOrder();

        if (model.getCurrentRound()%2 == 0) {
            Era oldEra = model.getCurrentEra();

            switch (oldEra) {
                case FIRST:
                   model.setCurrentEra(Era.SECOND);
                   break;
                case SECOND:
                    model.setCurrentEra(Era.THIRD);
                    break;
                case THIRD:
                    System.out.println("CONTROLLER CHIAMA END GAME");
                    endGame();
                    return;

            }
        }


        model.setCurrentRound(model.getCurrentRound()+1);


        setSkippingTurnPlayers();

        model.getGameBoard().clearRound();

        model.clearPawns();

        setCardsOnTowers();

        boolean suspendedPlayers = true;

        while (suspendedPlayers) {

            for (Player player : model.getTurnOrder()) {

                System.out.println("INDICE PLAYER " + model.getTurnOrder().indexOf(player) + " nome " + player.getPlayerID());

                if (player.getPlayerState() != PlayerState.SUSPENDED) {

                    suspendedPlayers = false;

                    player.setPlayerState(PlayerState.THROWDICES);

                    System.out.println("IN SET NEW ROUND: playerColor = " + player.getPlayerID() + " colore: " + player.getPlayerColor() + "Set to throw dices\n");

                    startTimer(player);

                    break;
                }

                else {

                    System.out.println("SECONDO IL CONTROLLER QUESTO PLAYER E' SOSPESO " + player.getPlayerID());
                }
            }
        }

        model.setCurrentTurn(1);

    }


    /**
     * This method sets the new cards on the towers at the beginning of a new round
     * and calls the update of the gui.
     */
    public void setCardsOnTowers(){

        DevelopmentCard[] greenDeck = new DevelopmentCard[4];
        DevelopmentCard[] blueDeck = new DevelopmentCard[4];
        DevelopmentCard[] yellowDeck = new DevelopmentCard[4];
        DevelopmentCard[] purpleDeck = new DevelopmentCard[4];

        for (int i = 0; i < 4; i++) {
            greenDeck[i] = model.getOrderedDecks().get(CardColor.GREEN).pop();
            blueDeck[i] = model.getOrderedDecks().get(CardColor.BLUE).pop();
            yellowDeck[i] = model.getOrderedDecks().get(CardColor.YELLOW).pop();
            purpleDeck[i] = model.getOrderedDecks().get(CardColor.PURPLE).pop();
        }

        model.getGameBoard().setTurn(greenDeck, blueDeck, yellowDeck, purpleDeck);
        model.updateTowerGUI(CardColor.GREEN);
        model.updateTowerGUI(CardColor.BLUE);
        model.updateTowerGUI(CardColor.YELLOW);
        model.updateTowerGUI(CardColor.PURPLE);

    }

    /**
     * Reset the availability of the once per round leader cards of the player
     * to true.
     * @param player
     */
    private void setLeaderValues(Player player) {

        List<LeaderCard> playerLeaderCards = player.getLeaderCards();

        if(!playerLeaderCards.isEmpty()){

            for (LeaderCard playerLeaderCard : playerLeaderCards) {
                if (!(playerLeaderCard.isPermanent() || playerLeaderCard.isDiscarded())) {
                    playerLeaderCard.setActivated(false);
                }
            }
        }

    }

    public void chooseCurrentPlayer(Integer index) {

        Player firstPlayer = model.getTurnOrder().get(index);

        model.setCurrentPlayer(firstPlayer);

        if (Filter.applySpecial(firstPlayer, SpecialBonusAndMalus.SKIPFIRSTTURN)
                && model.getCurrentTurn() == 1) {

            firstPlayer.setPlayerState(PlayerState.ENDTURN);

            new EndTurn().perform(model, this);
            return;
        }


        else
        {
            actionChecker.resetActionList();

            actionChecker.setCurrentPlayer();

            //model.notifyEndMove();

            firstPlayer.setPlayerState(PlayerState.DOACTION);

            startTimer(firstPlayer);

        }
    }

    /**
     * setSkippingTurnPlayers finds all the players with the malus "Skip Turn" and
     * saves them in SkippedTurnPlayers
     */
    private void setSkippingTurnPlayers() {
        List<Player> players = model.getTurnOrder();

        for (Player player : players) {
            if (Filter.applySpecial(player, SpecialBonusAndMalus.SKIPFIRSTTURN)) {
                model.getSkippedTurnPlayers().add(player);
            }
        }
    }

    /**
     * excommunicatePlayers finds the players who don't have sufficient FaithPoints and calls executeTiles on them.
     * @return a List of players that have sufficient faithPoints
     *
     */
    private List<Player> excommunicatePlayers() {

        ArrayList<Player> safePlayers = new ArrayList<>();

        int threshold = setThreshold();

        int faithPointsNeeded = model.getGameBoard().getFaithPointsTrack().getVictoryPointsPerSlot()[threshold];

        List<Player> players = model.getTurnOrder();

            for (Player player : players) {

                if (player.getActualGoodSet().getGoodAmount(GoodType.FAITHPOINTS) < faithPointsNeeded) {
                    executeTiles(player);
                }

                else {
                    safePlayers.add(player);
                }
            }

        return safePlayers;

    }


    /**
     * it's the method that assigns the excommunication Tiles to the player excommunicated
     * @param player
     * @throws RemoteException
     */
    public void executeTiles(Player player) {

        Era currentEra = model.getCurrentEra();
        ExcommunicationTile tileToExecute = model.getGameBoard().getExcommunicationLane().getExcommunicationTile(currentEra);
        tileToExecute.execute(player);
        player.notifyObserver(new Excommunicated(model.getCurrentEra()));
        player.setPlayerState(PlayerState.WAITING);
    }


    /**
     * setThreshold finds the right threshold for the FaithPointsTrack of the currentEra.
     * @return an integer - the threshold
     */
    private int setThreshold() {

        int threshold=0;
        Era currentEra = model.getCurrentEra();
        switch (currentEra) {
            case FIRST:
                threshold = 3;
                break;
            case SECOND:
                threshold = 4;
                break;
            case THIRD:
                threshold = 5;
                break;
        }

    return threshold;

    }

    public synchronized Integer getPlayersPraying() {
        return playersPraying;
    }

    public synchronized void praying() {
        if (playersPraying > 0) {
            this.playersPraying--;
        }
    }

    /**
     * endGame calculates the victoryPoints of all the players and chooses the winner
     */
    public void endGame() {

        List<Player> players = model.getTurnOrder();
        Player winner = null;
        int winningPoints = 0;

        pointsFromMilitaryPoints();

        for (Player player : players) {

            //PURPLE CARDS

            pointsFromPurpleCards(player);

            //BLUE CARDS

            pointsFromBlueCards(player);

            //GREEN CARDS

            pointsFromGreenCards(player);

            //EXCOMMUNICATION TILES

            for (ExcommunicationTile excommunicationTile : player.getExcommunicationTiles()) {
                if (excommunicationTile.getEra() == Era.THIRD && excommunicationTile.getEffect() != null) {
                    excommunicationTile.getEffect().execute(player);
                }
            }

            transformResourcesInPoints(player);


            int playerPoints = player.getActualGoodSet().getGoodAmount(GoodType.VICTORYPOINTS);

            if (playerPoints > winningPoints) {
                winningPoints = playerPoints;
                winner = player;
            }
        }

        model.setEndGame(winner);

        model.getGameBoard().clearAll();

    }

    private void transformResourcesInPoints(Player player) {
        int totalResource = 0;

        GoodSet playerGoodSet = player.getActualGoodSet();

        for (GoodType goodType : GoodType.values()) {
            if (goodType != GoodType.VICTORYPOINTS && goodType != GoodType.MILITARYPOINTS && goodType != GoodType.FAITHPOINTS) {

                totalResource += playerGoodSet.getGoodAmount(goodType);
            }
        }

        player.updateGoodSet(new GoodSet(0,0,0,0,totalResource/5,0,0));


    }

    private void pointsFromMilitaryPoints() {

        ArrayList<Player> players = new ArrayList<>();

        players.addAll(model.getTurnOrder());

        Collections.sort(players, new Comparator<Player>() { // descending order
            @Override
            public int compare(Player player2, Player player1) {
                return ((Integer)player1.getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS)).compareTo((Integer)player2.getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS));
            }
        });


        int firstPlayerMilitaryPoints = players.get(0).getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS);

        int militaryIndex;

        players.get(0).updateGoodSet(new GoodSet(0,0,0,0,5,0,0));

        for (militaryIndex = 0; militaryIndex < players.size() - 1; militaryIndex++) {

            Player player = players.get(militaryIndex+1);

            if (player.getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS) != firstPlayerMilitaryPoints) {
                break;
            }
            else player.updateGoodSet(new GoodSet(0,0,0,0,5,0,0));
        }

        if (militaryIndex != 0) return;

        int secondPlayerMilitaryPoints = players.get(1).getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS);

        players.get(1).updateGoodSet(new GoodSet(0,0,0,0,2,0,0));

        for (militaryIndex = 1; militaryIndex < players.size() - 1; militaryIndex++) {

            Player player = players.get(militaryIndex+1);

            if (player.getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS) != secondPlayerMilitaryPoints) {
                break;
            }

            else player.updateGoodSet(new GoodSet(0,0,0,0,2,0,0));
        }

    }

    private void pointsFromPurpleCards(Player player) {

        if (!Filter.applySpecial(player, SpecialBonusAndMalus.NOVICTORYFROMPURPLE) && player.getCardsOwned().get(CardColor.PURPLE) != 0) {
            List<DevelopmentCard> cards =  Arrays.asList(player.getPersonalBoard().getLane(CardColor.PURPLE).getCards());

            for (DevelopmentCard card : cards) {
                if(card == null){
                    break;
                }
                for (Effect effect : card.getPermanentEffect()) {
                    effect.execute(player);
                }
            }
        }
    }


    /**
     * This method calculates how many points the player receives from the number of his blue cards
     * @param player
     */
    private void pointsFromBlueCards(Player player) {

        int numberOfBlueCards = player.getCardsOwned().get(CardColor.BLUE);

        if (!Filter.applySpecial(player, SpecialBonusAndMalus.NOVICTORYFROMBLUE) && numberOfBlueCards != 0) {

            switch (numberOfBlueCards) {

                case 1:
                    player.updateGoodSet(new GoodSet(0,0,0,0,1,0,0));
                    break;
                case 2:
                    player.updateGoodSet(new GoodSet(0,0,0,0,3,0,0));
                    break;
                case 3:
                    player.updateGoodSet(new GoodSet(0,0,0,0,6,0,0));
                    break;
                case 4:
                    player.updateGoodSet(new GoodSet(0,0,0,0,10,0,0));
                    break;
                case 5:
                    player.updateGoodSet(new GoodSet(0,0,0,0,15,0,0));
                    break;
                case 6:
                    player.updateGoodSet(new GoodSet(0,0,0,0,21,0,0));
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * This method calculates how many points the player receives from the number of his green cards
     * @param player
     */
    private void pointsFromGreenCards(Player player) {

        int numberOfGreenCards = player.getCardsOwned().get(CardColor.GREEN);

        if (!Filter.applySpecial(player, SpecialBonusAndMalus.NOVICTORYFROMGREEN) && numberOfGreenCards != 0) {

            player.updateGoodSet(new GoodSet(0,0,0,0,player.getPersonalBoard().getTerritoryLane().getSlot(numberOfGreenCards-1).getVictoryPointsGiven(),0,0));
        }
    }




    /**
     * this method set all the availabilities of the family pawns to true and give them the right action value
     * @throws Exception
     */
    public void setFamilyPawnsAndLeaderValues() {

        for (Player player : model.getTurnOrder()) {

            if (Filter.applySpecial(player, SpecialBonusAndMalus.CHANGEVALUEOFEVERYPAWN)) {
                player.getFamilyPawnAvailability().put(FamilyPawnType.BLACK, true);
                player.getFamilyPawnAvailability().put(FamilyPawnType.ORANGE, true);
                player.getFamilyPawnAvailability().put(FamilyPawnType.WHITE, true);
                player.getFamilyPawnAvailability().put(FamilyPawnType.NEUTRAL, true);

                player.setFamilyPawnValue(FamilyPawnType.BLACK, 5);
                player.setFamilyPawnValue(FamilyPawnType.ORANGE, 5);
                player.setFamilyPawnValue(FamilyPawnType.WHITE, 5);
                player.setFamilyPawnValue(FamilyPawnType.NEUTRAL, 0);
            }



            else {

                Dice tempDice;

                for (FamilyPawnType familyPawnType : FamilyPawnType.values()){

                    if (familyPawnType != FamilyPawnType.BONUS
                            && familyPawnType != FamilyPawnType.ANY
                            && familyPawnType != FamilyPawnType.COLORED)  {
                        player.getFamilyPawnAvailability().put(familyPawnType, true);

                        if (familyPawnType != FamilyPawnType.NEUTRAL) {
                            tempDice = model.getGameBoard().getDice(familyPawnType);
                            player.setFamilyPawnValue(familyPawnType, tempDice.getFace());
                        }

                        else player.setFamilyPawnValue(familyPawnType, 0); // neutral case

                    }
                }
            }

            setLeaderValues(player);
        }
    }


    /**
     * newTurnOrder is the turnOrderTrack from the councilPalace. This track contains the first players, but it is
     * not sure that contains them all. In case some players didn't go to the palace, they will follow the same order,
     * relatively, that they had in the previous turn. For this reason setNewTurnOrder copies all the player of the
     * oldTurnOrder following the order of the color in newTurnOrder in a temporary arrayList. While doing this process,
     * the method saves all the indices of the oldTurnOrder that point to the players that have already been copied.
     * After this first step, all the players of the oldTurnOrder are added to the temporary arrayList, skipping the
     * ones who have already been copied. Then the TurnOrder in the Model is set.
     */
    private void setNewTurnOrder() {

        PlayerColor[] newTurnOrder = model.getGameBoard().getCouncilPalace().getTurnOrder();
        List<Player> oldTurnOrder = model.getTurnOrder();
        ArrayList<Player> temporaryTurnOrder = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        for (PlayerColor playerColor : newTurnOrder) {

            for (Player player : oldTurnOrder) {

                if (player.getPlayerColor() == playerColor) {
                    temporaryTurnOrder.add(player);
                    indices.add(oldTurnOrder.indexOf(player));
                }
            }
        }

        for (int i = 0; i < oldTurnOrder.size(); i++) {
            if (!indices.contains(i)) temporaryTurnOrder.add(oldTurnOrder.get(i));
        }

        model.setTurnOrder(temporaryTurnOrder);

    }


    public ActionChecker getActionChecker() {
        return actionChecker;
    }

    private void createActions() {

        ArrayList<Action> actionList = new ArrayList<>();

        final int NUMBER_OF_FLOORS = 4;

        for(ZoneType zoneType : ZoneType.values()){

            if(zoneType == ZoneType.GREENTOWER || zoneType == ZoneType.YELLOWTOWER || zoneType == ZoneType.BLUETOWER || zoneType == ZoneType.PURPLETOWER){

                for (int i = 0; i < NUMBER_OF_FLOORS; i++){
                    actionList.add(new TowerAction(zoneType, model, i));
                }

            } else if(zoneType == ZoneType.MARKET) {

                for (ShopName shopName : ShopName.values()) {
                    MarketAction marketAction = new MarketAction(shopName, model);

                    if ((shopName == ShopName.MILITARYANDCOINSSHOP || shopName == ShopName.PRIVILEGESHOP) && model.getTurnOrder().size() < 4) {
                        marketAction.setEnable(false);
                    }

                    actionList.add(marketAction);
                }

            } else if (zoneType == ZoneType.COUNCILPALACE) {
                actionList.add(new CouncilPalaceAction(model));

            } else if (zoneType == ZoneType.HARVEST || zoneType == ZoneType.PRODUCTION) {

                for (FieldType fieldType : FieldType.values()) {
                    WorkAction workAction = new WorkAction(zoneType, model, fieldType);
                    if (fieldType == FieldType.SECOND && model.getTurnOrder().size() < 3) {
                        workAction.setEnable(false);
                    }

                    actionList.add(workAction);
                }
            }

        }

        actionChecker.setActionList(actionList);
    }


    public void handleEndAction(){

        if(!model.getCurrentPlayer().getCouncilPrivilegeEffectList().isEmpty()){

            model.getCurrentPlayer().setPlayerState(PlayerState.CHOOSE_COUNCIL_PRIVILEGE);
            return;
        }

        if (!model.getCurrentPlayer().getCurrentBonusActionList().isEmpty()) {

            ActionEffect currentBonusAction = model.getCurrentPlayer().getCurrentBonusActionList().removeFirst();

            // temporary bonusMalusOn cost setted in the player
            if (currentBonusAction.getBonusAndMalusOnCost() != null) {

               model.getCurrentPlayer().getCurrentBonusActionBonusMalusOnCostList().add(currentBonusAction.getBonusAndMalusOnCost());

            }

            actionChecker.resetActionListExceptPlayer();

            FamilyPawn familyPawn = new FamilyPawn(model.getCurrentPlayer().getPlayerColor(), FamilyPawnType.BONUS, currentBonusAction.getActionValue());

            actionChecker.setValidActionForFamilyPawn(familyPawn, currentBonusAction.getType());

            model.getCurrentPlayer().setPlayerState(PlayerState.BONUSACTION);

            return;

        }

        if(model.getCurrentPlayer().getLastState() != null){

            model.getCurrentPlayer().setPlayerState(model.getCurrentPlayer().getLastState());

            model.getCurrentPlayer().setLastState(null);

        }

        else {

            model.getCurrentPlayer().setPlayerState(PlayerState.ENDTURN);


        }
    }

    /**
     * If the workaction has pay to obtain cards to handle, the player state is set to
     * ACTIVATE_PAY_TO_OBTAIN_CARDS. If not the workaction is executed and then handleEndAction()
     * is called.
     * @param workAction
     * @param currentPlayer
     * @param workers
     */
    public void handlePayToObtainCards(WorkAction workAction, Player currentPlayer, int workers) {

        if (workAction.handlePayToObtainCards(workers)) {

            currentPlayer.setPlayerState(PlayerState.ACTIVATE_PAY_TO_OBTAIN_CARDS);

        }

        else {
            try {
                workAction.execute();
            } catch (Exception e) {
                LOGGER.log(Level.INFO, e.getMessage(), e);
            }
            workAction.getCardsForWorkers().clear();
            handleEndAction();
        }
    }


    public void setCurrentBonusTileIndexPlayer(int currentBonusTileIndexPlayer) {
        this.currentBonusTileIndexPlayer = currentBonusTileIndexPlayer;
    }

    public int getCurrentBonusTileIndexPlayer() {
        return currentBonusTileIndexPlayer;
    }

    public void startTimer(Player player) {

        PlayerState playerState = player.getPlayerState();

        long time = 0;

        switch (playerState) {

            case THROWDICES:

                time = throwDicesTime;
                break;

            case CHOOSE_BONUS_TILE:

                time = chooseBonusTilesTime;
                break;

            case PRAY:

                time = prayTime;
                break;

            case DOACTION:

                time = turnTime;
                break;

        }

        timer = new Timer();
        TimerTask suspendPlayer = new SuspendPlayer(this, model, player);

        timer.schedule(suspendPlayer, time);

    }

    public void stopTimer() {

        this.timer.cancel();

    }

    public Timer getTimer() {
        return timer;
    }

    public List<Player> getPlayerReconnected() {
        return playerReconnected;
    }


    public void handleReconnectedPlayers(){

        if(!(playerReconnected.isEmpty())){

            System.out.println("SONO NEL CICLO DI HANDLE RECONNECTED, LA LISTA NON E' VUOTA");

            List<String> usernamePLayerReconnectedList = new ArrayList<>();

            for (Player player : playerReconnected) {

                usernamePLayerReconnectedList.add(player.getPlayerID());

                try {

                    player.notifyObserver(new GoodSetChange(player.getActualGoodSet()));

                    player.setLeaderCards(player.getLeaderCards());

                    for (CardColor cardColor : CardColor.values()) {
                        if (cardColor != CardColor.ANY) {

                            for (DevelopmentCard developmentCard : player.getPersonalBoard().getLane(cardColor).getCards()) {
                                if(developmentCard == null){
                                    break;
                                }
                                else{
                                    player.notifyObserver(new PersonalCardChange(developmentCard.getSpecial(), cardColor));
                                }

                            }
                        }
                    }


                    if(playerBonusTileIndexMap.get(player) != null){

                        player.notifyObserver(new BonusTileChangeGui(playerBonusTileIndexMap.get(player)));


                    }

                    player.setPlayerState(PlayerState.WAITING);

                    playerDisconnected.remove(player);


                } catch (Exception e) {
                    LOGGER.log(Level.INFO, e.getMessage(), e);
                }
            }


            for (CardColor cardColor : CardColor.values()) {
                if (cardColor != CardColor.ANY) {
                    model.updateTowerGUI(cardColor);
                }
            }

            ExcommunicationLane excommunicationLane = model.getGameBoard().getExcommunicationLane();

            model.notifyObserver(new ExcommunicationChange(excommunicationLane.getExcommunicationTile(Era.FIRST).getUrl(), excommunicationLane.getExcommunicationTile(Era.SECOND).getUrl(), excommunicationLane.getExcommunicationTile(Era.THIRD).getUrl()));

            for (Player player : model.getTurnOrder()) {
                model.updateDisconnectedTrackGUI(player.getPlayerColor(), GoodType.VICTORYPOINTS, player.getActualGoodSet().getGoodAmount(GoodType.VICTORYPOINTS));
                model.updateDisconnectedTrackGUI(player.getPlayerColor(), GoodType.MILITARYPOINTS, player.getActualGoodSet().getGoodAmount(GoodType.MILITARYPOINTS));
                model.updateDisconnectedTrackGUI(player.getPlayerColor(), GoodType.FAITHPOINTS, player.getActualGoodSet().getGoodAmount(GoodType.FAITHPOINTS));
            }

            model.notifyPlayerReconnected(usernamePLayerReconnectedList);

            playerReconnected.clear();

        }


    }

    public Map<Player, Integer> getPlayerBonusTileIndexMap() {
        return playerBonusTileIndexMap;
    }

    public synchronized void clientClosed() {

        closedClients--;

        if(closedClients == 0){

            System.out.println("I AM THE CONTROLLER, I AM CLOSING THE GAME");

            for (Player player : model.getTurnOrder()) {
                currentMatch.getLogoutInterface().getClientMatch().remove(player.getPlayerID());
                currentMatch.getLogoutInterface().clientDisconnected(player.getPlayerID());
            }

            currentMatch.setIsRunning(false);
        }
        else {

            System.out.println("HO RICEVUTO CHIUSURA DA UN CLIENT");
        }
    }

    public void setCurrentMatch(ServerNewGame currentMatch) {
        this.currentMatch = currentMatch;
    }

    synchronized public boolean minNumberOfPlayerReached() {

        int numberOfPlayerOnline = 0;

        for (Player player : model.getTurnOrder()) {
            if(player.getPlayerState() != PlayerState.SUSPENDED){
                numberOfPlayerOnline++;
            }
        }

        System.out.println("\n\n" + numberOfPlayerOnline + "\n\n");

        return numberOfPlayerOnline < minNumberOfPlayers;

    }


    public List<Player> getPlayerDisconnected() {
        return playerDisconnected;
    }


    public void handleDisconnectedPlayers() {

        if(!playerDisconnected.isEmpty()){

            System.out.println("\n SONO IN HANDLE DISCONNECTED PLAYERS \n");

            List<String> playerNamesDisconnected = new ArrayList<>();

            for (Player player : playerDisconnected) {

                System.out.println("QUESTO E' IL NOME DEL PLAYER DISCONNECTED " + player.getPlayerID());

                playerNamesDisconnected.add(player.getPlayerID());
            }

            model.notifyPlayerDisconnected(playerNamesDisconnected);

            playerDisconnected.clear();

        }

    }
}
