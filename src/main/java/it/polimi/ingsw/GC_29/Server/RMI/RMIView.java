package it.polimi.ingsw.GC_29.Server.RMI;

import it.polimi.ingsw.GC_29.Client.ClientRMI.ClientRMIView;
import it.polimi.ingsw.GC_29.Client.ClientRMI.ClientViewRemote;
import it.polimi.ingsw.GC_29.Controllers.Change.Change;
import it.polimi.ingsw.GC_29.Controllers.Change.EndMove;
import it.polimi.ingsw.GC_29.Controllers.Input.*;
import it.polimi.ingsw.GC_29.Model.*;
import it.polimi.ingsw.GC_29.Model.PlayerColor;
import it.polimi.ingsw.GC_29.Controllers.Input.LeaderAction;
import it.polimi.ingsw.GC_29.Server.View;
import it.polimi.ingsw.GC_29.Query.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Christian on 07/06/2017.
 */

/**
 *
 * RMIView class implements the RMIViewRemote and so implements all the method necessary to
 * the remote invocation. The class extends the View class in order to be Observable for the
 * Input objects (the controller observes the view) and observer of Change (the view observes the model)
 */

public class RMIView extends View implements RMIViewRemote {


    private ClientViewRemote clientView;

    private GetValidActions validActionQuery;

    private Model model;

    private PlayerColor playerColor;

    private String username;

    private static final Logger LOGGER  = Logger.getLogger(ClientRMIView.class.getName());



    public RMIView(Model model, PlayerColor playerColor, String username){

        this.model = model;

        this.playerColor = playerColor;

        this.username = username;

        this.validActionQuery = new GetValidActions();
    }


    @Override
    public void registerClient(ClientViewRemote clientStub) throws RemoteException {
        System.out.println("CLIENT REGISTERED");
        clientView = clientStub;
    }

    @Override
    public void update(Change o)  {
        try {

            clientView.updateClient(o);


        } catch (RemoteException e) {
            model.getPlayer(playerColor).unregisterObserver(this);
            model.unregisterObserver(this);
            logoutInterface.clientDisconnected(username);
            System.out.println("AVVERTO SERVER CLIENT DISCONNESSO RMI");
            notifyObserver(new Disconnection(playerColor));
            System.out.println("NOTIFY DISCONNECTION INVIATA");

            LOGGER.log(Level.INFO, "RMI VIEW DISCONNESSA", e);
        }
    }

    /**
     * notify the controller about a SkipAction Input, the player skipped the current action (he can have different bonus
     * action to perform and maybe he just want to skip one action)
     * @throws RemoteException
     */
    @Override
    public void skipAction() throws RemoteException{

        notifyObserver(new SkipAction());

    }

    /**
     * notify controller with UsePawnChosen Input --> the input is used to comunicate the pawn chosen in order
     * to create the valid action list for that specific pawn
     * @param familyPawnType
     * @throws RemoteException
     */
    @Override
    public void usePawnChosen(FamilyPawnType familyPawnType) throws RemoteException{

        notifyObserver(new UsePawnChosen(familyPawnType));


    }


    @Override
    public void endTurn() throws RemoteException{

        notifyObserver(new EndTurn());

    }


    @Override
    public void pray(boolean b, PlayerColor playerColor) throws RemoteException{

        notifyObserver(new Pray(b, playerColor));

    }

    /**
     * it is used at the beginning of the game in order to initialize the playerColor of the Player
     * @param playerColor
     * @throws RemoteException
     */
    @Override
    public void initialize(PlayerColor playerColor) throws RemoteException {

        notifyObserver(new Initialize(playerColor));

    }

    @Override
    public List<String> getDevelopmentCard(CardColor cardColor) throws RemoteException {

        List<String> returnList = new ArrayList<>();

        List<DevelopmentCard> playerCards = Arrays.asList(model.getCurrentPlayer().getPersonalBoard().getLane(cardColor).getCards());

        for (DevelopmentCard playerCard : playerCards) {

            if(playerCard!= null){
                returnList.add(playerCard.toString());
            }

        }

        return returnList;
    }

    @Override
    public List<String> getTowerCards(CardColor towerCardColor) throws RemoteException{

        List<String> returnList = new ArrayList<>();

        List<Floor> floors = Arrays.asList(model.getGameBoard().getTower(towerCardColor).getFloors());

        for (Floor floor : floors) {

            returnList.add( "Floor " + floors.indexOf(floor) + "\n" + floor.getDevelopmentCard().toString());
        }

        return returnList;
    }

    @Override
    public void throwDices() throws RemoteException {

        notifyObserver(new ThrowDices());

    }

    /**
     * return a map with key the amount of workers to pay in a workAction in order to activate
     * all the cards in the ArrayList related with the key
     * @return
     * @throws RemoteException
     */
    @Override
    public Map<Integer, ArrayList<String>> getCardsForWorkers() throws RemoteException {

        return new GetCardsForWorkers().perform(model);

    }

    /**
     * return a map with key the index of the alternative cost for a card
     * @return
     */
    @Override
    public Map<Integer, String> getPossibleCosts() {
        Map<Integer, Cost> possibleCosts = ((TowerAction) model.getCurrentPlayer().getCurrentAction()).getPossibleCardCosts();
        HashMap<Integer, String> possibleCostsToString = new HashMap<>();

        for(Map.Entry<Integer,Cost> entry : possibleCosts.entrySet()){

            String cost = entry.getValue().toString();
            possibleCostsToString.put(entry.getKey(), cost);
        }

        return possibleCostsToString;

    }

    @Override
    public Map<Integer,String> getBonusTileList() throws RemoteException {
        return new GetBonusTile().perform(model);
    }

    @Override
    public void bonusTileChosen(int bonusTileChosen) throws RemoteException {

        notifyObserver(new BonusTileChosen(bonusTileChosen));

    }

    @Override
    public Map<FamilyPawn, Boolean> getFamilyPawns() throws RemoteException {
        return new GetFamilyPawnAvailability().perform(model);
    }

    /**
     * notify controller with a JoinGame input --> this input is created when a player
     * is suspended or disconnected and come back in the game.
     * @param playerColor
     * @throws RemoteException
     */
    @Override
    public void joinGame(PlayerColor playerColor) throws RemoteException {

        notifyObserver(new JoinGame(playerColor));

    }

    @Override
    public void leaderAction(boolean b, int index, PlayerColor playerColor) throws RemoteException{

        notifyObserver(new LeaderAction(b, index, playerColor));

    }

    @Override
    public Map<Integer, Boolean> getLeaderCardsMap(PlayerColor playerColor) throws RemoteException {
        return new GetAvailableLeaderCards(playerColor).perform(model);
    }

    @Override
    public List<String> getLeaderCards(PlayerColor playerColor) throws RemoteException {
        return new LeaderCardsQuery(playerColor).perform(model);
    }

    @Override
    public void useLeaderCardGui(PlayerColor playerColor) throws RemoteException {

        notifyObserver(new UseLeaderCardsGUI(playerColor));

    }

    /**
     * notify controller that the player ha s received the message of the end game and that the
     * client is terminated
     * @throws RemoteException
     */
    @Override
    public void endGame() throws RemoteException {
        logoutInterface.getClientMatch().remove(username);

        notifyObserver(new Closed());

    }

    @Override
    public Map<String, HashMap<Integer, String>> getPayToObtainCardsGUI() throws RemoteException {
        return new GetPayToObtainCards().perform(model);
    }

    @Override
    public String seeGameBoard() throws RemoteException {
        return new GameBoardQuery().perform(model);
    }

    @Override
    public String seePersonalBoard(PlayerColor playerColor) throws RemoteException {
        return new PersonalBoardQuery(playerColor).perform(model);
    }

    @Override
    public String getExcommunicationTileUrl() throws RemoteException {
        return new GetExcommunication().perform(model);
    }

    @Override
    public void activateCards(int workersChosen) throws RemoteException {

        notifyObserver(new ActivateCards(workersChosen));
    }


    /**
     * this method returns to the client a map with key the description of the payToObtain card of the player
     * for the workAction, the field related with the key is a map with key the index of the alternative
     * pay to obtain effect in the specific card (there are cards with two alternatives of effect that the player
     * has to choose.
     * @return
     * @throws RemoteException
     */
    @Override
    public Map<String, HashMap<Integer, String>> getPayToObtainCards() throws RemoteException {

        WorkAction workAction = (WorkAction) model.getCurrentPlayer().getCurrentAction();

        Map<String, HashMap<Integer, String>> payToObtainCardMap = new HashMap<>();

        for(DevelopmentCard card : workAction.getPayToObtainCardsMap().values()){

            payToObtainCardMap.put(card.toString(), new HashMap<>());

            for(Effect effect : card.getPermanentEffect()){

                if(effect instanceof PayToObtainEffect){

                    PayToObtainEffect effect1 = (PayToObtainEffect) effect;

                    if(effect1.checkSufficientGoods(model.getCurrentPlayer())){

                        int effectIndex = card.getPermanentEffect().indexOf(effect);

                        payToObtainCardMap.get(card.toString()).put(effectIndex, effect.toString());

                    }

                }


            }
        }

        return payToObtainCardMap;
    }

    /**
     * notify the controller with the PayToObtainCardsChosen input --> the map has key the toString of the card activated
     * and field the index of the pay to Obtain effect chosen.
     * @param activatedCardMap
     * @throws RemoteException
     */
    @Override
    public void payToObtainCardChosen(Map<String, Integer> activatedCardMap) throws RemoteException {

        notifyObserver(new PayToObtainCardsChosen(activatedCardMap));

    }

    @Override
    public List<Integer> getCouncilPrivileges() throws RemoteException {

        List<Integer> parchmentAmountList = new ArrayList<>();

        for (CouncilPrivilegeEffect councilPrivilegeEffect : model.getCurrentPlayer().getCouncilPrivilegeEffectList()) {

            parchmentAmountList.add(councilPrivilegeEffect.getNumberOfCouncilPrivileges());
        }

        model.getCurrentPlayer().getCouncilPrivilegeEffectList().clear();

        return parchmentAmountList;

    }

    @Override
    public void privilegesChosen(List<Integer> councilPrivilegeEffectChosenList) throws RemoteException {

        notifyObserver(new PrivilegeChosen(councilPrivilegeEffectChosenList));

    }

    @Override
    public GoodSet getPlayerGoodset() throws RemoteException {

      return model.getCurrentPlayer().getActualGoodSet();
    }

    @Override
    public List<FamilyPawn> getPlayerPawns() throws RemoteException {

        return model.getCurrentPlayer().getFamilyPawns();
    }

    @Override
    public void chooseCost(int costChosen) {

        notifyObserver(new PayCard(costChosen));

    }


    @Override
    public Map<Integer, String> getValidActionList() throws RemoteException {

        return validActionQuery.perform(model);
    }

    @Override
    public void doAction(int index) throws RemoteException {

        notifyObserver(new ExecuteAction(index));

    }

    @Override
    public Map<FamilyPawnType, Boolean> getFamilyPawnAvailability() throws RemoteException{
        return model.getCurrentPlayer().getFamilyPawnAvailability();
    }


    @Override
    public void update() {

    }
}
