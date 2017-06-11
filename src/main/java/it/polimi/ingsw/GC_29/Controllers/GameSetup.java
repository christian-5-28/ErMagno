package it.polimi.ingsw.GC_29.Controllers;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.GC_29.Components.*;
import it.polimi.ingsw.GC_29.EffectBonusAndActions.*;
import it.polimi.ingsw.GC_29.Player.Player;
import it.polimi.ingsw.GC_29.ProveGSon.EnumMapInstanceCreator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Christian on 28/05/2017.
 */
public class GameSetup {

    private final int CARDSPERDECK = 24;
    private final int NUMBEROFCARDS = 96;

    private int numberOfPlayers;

    private GameStatus gameStatus;

    private GameBoard gameBoard;

    private EnumMap<CardColor, ArrayDeque<DevelopmentCard>> orderedDecks;

    private EnumMap<Era, ArrayList<ExcommunicationTile>> excommunicationTileMap;

    private ArrayList<Player> players;

    // TODO: possibile refactor: rendo classe singleton e rendo init statico passandogli l'arraylist dei players
    // TODO: nel setup settare era, turno e round come first, 1, 1

    public GameSetup(ArrayList<Player> players) {

        this.numberOfPlayers = players.size();
        //this.gameBoard = new GameBoard(numberOfPlayers);
        this.gameStatus = GameStatus.getInstance();
        this.players = players;

        this.orderedDecks = new EnumMap<>(CardColor.class);
        this.excommunicationTileMap = new EnumMap<>(Era.class);
    }


    /**
     * from the main the init method will be called, it will setup all the gameStatus, at the end the main method
     * will call the GameManager (manager for the setting of the currentPlayer, the management of the begin round, the end round
     * and the end era (relationship with the church is managed there)
     */
    public void init() throws Exception {

        this.gameBoard = loadGameBoardFromFile(numberOfPlayers);

        for(CardColor color : CardColor.values()){
            this.orderedDecks.put(color, getDeckFromFile(color));
        }

        setExcommunicationTiles();

        Collections.shuffle(players);

        setGoodsForPlayers();

        setGameStatus();

        createActions();

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * this method get the specific deck from file, it shuffles the card of the deck compared to the Era and return a deck
     * with the structure of a stack, ( we have the card of the thirdEra, then the secondEra and at the end the firstEra) --> useful to pick
     * a card with the pop() method
     */
    private ArrayDeque<DevelopmentCard> getDeckFromFile(CardColor color) throws FileNotFoundException {

        ArrayDeque<DevelopmentCard> orderedDeck = new ArrayDeque<>();

        ArrayList<DevelopmentCard> deck;

        FileReader cardFileReader;

        switch (color) {
            case GREEN:

                cardFileReader = new FileReader("cards/greenCards");
                break;

            case YELLOW:

                cardFileReader = new FileReader("cards/yellowCards");
                break;

            case BLUE:

                cardFileReader = new FileReader("cards/blueCards");
                break;

            case PURPLE:

                cardFileReader = new FileReader("cards/purpleCards");
                break;

            default:
                cardFileReader = null;
        }

        deck = new ObjectDeserializer().getCardDeck(cardFileReader);

        EnumMap<Era, ArrayList<DevelopmentCard>> eraCardMap = new EnumMap<>(Era.class);
        eraCardMap.put(Era.FIRST, new ArrayList<>());
        eraCardMap.put(Era.SECOND, new ArrayList<>());
        eraCardMap.put(Era.THIRD, new ArrayList<>());

        for(DevelopmentCard card : deck){

            eraCardMap.get(card.getEra()).add(card);

        }

        for(Era era : Era.values()){

            Collections.shuffle(eraCardMap.get(era)); // carte mischiate rispetto all'era

            for(DevelopmentCard card : eraCardMap.get(era)){

                orderedDeck.add(card);
            }
        }

        return orderedDeck;

    }


    private GameBoard loadGameBoardFromFile(int numberOfPlayers) throws IOException {

        return new ObjectDeserializer().getGameBoard(numberOfPlayers);
    }


    private ArrayList<ExcommunicationTile> getExcommunicationTilesFromFile(Era era) {

        // TODO: carica con Gson il file delle excommunicationTile rispetto all'era

        return null;

    }

    private void setExcommunicationTiles(){

        /*for(Era era : Era.values()){

            this.excommunicationTileMap.put(era, getExcommunicationTilesFromFile(era));
        }

        ExcommunicationTile tilefirsEra = getRandomTile(Era.FIRST);
        ExcommunicationTile tileSecondEra = getRandomTile(Era.SECOND);
        ExcommunicationTile tileThirdEra = getRandomTile(Era.THIRD);*/

        ExcommunicationTile tileFirstEra = new ExcommunicationTile(Era.FIRST, "prova1",null, new BonusAndMalusOnGoods(new GoodSet()), null, "");
        ExcommunicationTile tileSecondEra = new ExcommunicationTile(Era.SECOND, "prova2",null, new BonusAndMalusOnGoods(new GoodSet()), null, "");
        ExcommunicationTile tileThirdEra = new ExcommunicationTile(Era.THIRD, "prova3",null, new BonusAndMalusOnGoods(new GoodSet()), null, "");

        gameBoard.getExcommunicationLane().setExcommunicationLane(tileFirstEra, tileSecondEra, tileThirdEra);
    }

    private ExcommunicationTile getRandomTile(Era era) {

        Random randomGenerator = new Random();

        int size = excommunicationTileMap.get(era).size();

        int randomIndex = randomGenerator.nextInt(size);

        return excommunicationTileMap.get(era).get(randomIndex);
    }

    private void setGoodsForPlayers() throws Exception {

        FileReader fileReader = new FileReader("goodsForPlayerSetup");

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(new TypeToken<EnumMap<GoodType, Integer>>() {
                }.getType(),
                new EnumMapInstanceCreator<GoodType, Integer>(GoodType.class)).create();

        Type listType = new TypeToken<ArrayList<GoodSet>>(){}.getType();

        ArrayList<GoodSet> goodsReceivedList =  gsonBuilder.create().fromJson(fileReader, listType);// TODO: parametrizzare i punti da assegnare, da file ricevo ArrayList di coins, dobbiamo fare un file di IMPOSTAZIONI iniziali

        for(int i = 0; i < players.size(); i++){

            players.get(i).updateGoodSet(goodsReceivedList.get(i));

        }

    }

    private void setGameStatus() {

        gameStatus.setGameBoard(gameBoard);

        gameStatus.setOrderedDecks(orderedDecks);

        gameStatus.setTurnOrder(players);

        gameStatus.setCurrentPlayer(players.get(0));

        gameStatus.setCurrentEra(Era.FIRST);

        gameStatus.setCurrentRound(1);

        gameStatus.setCurrentTurn(1);

    }

    public void createActions() {

        ArrayList<Action> actionList = new ArrayList<>();

        final int NUMBER_OF_FLOORS = 4;

        for(ZoneType zoneType : ZoneType.values()){

            if(zoneType == ZoneType.GREENTOWER || zoneType == ZoneType.YELLOWTOWER || zoneType == ZoneType.BLUETOWER || zoneType == ZoneType.PURPLETOWER){

                for (int i = 0; i < NUMBER_OF_FLOORS; i++){
                    actionList.add(new TowerAction(zoneType, i));
                }

            } else if(zoneType == ZoneType.MARKET) {

                for (ShopName shopName : ShopName.values()) {
                    MarketAction marketAction = new MarketAction(shopName);

                    if ((shopName == ShopName.MILITARYANDCOINSSHOP || shopName == ShopName.PRIVILEGESHOP) && numberOfPlayers < 4) {
                        marketAction.setEnable(false);
                    }

                    actionList.add(marketAction);
                }

            } else if (zoneType == ZoneType.COUNCILPALACE) {
                actionList.add(new CouncilPalaceAction());

            } else if (zoneType == ZoneType.HARVEST) {

                //TODO: impl

            } else if (zoneType == ZoneType.PRODUCTION) {

                //TODO: impl

            }

        }

        ActionChecker.getInstance().setActionList(actionList);
    }


}
