package it.polimi.ingsw.GC_29.Controllers;

import it.polimi.ingsw.GC_29.Components.Era;
import it.polimi.ingsw.GC_29.Components.GoodSet;
import it.polimi.ingsw.GC_29.Components.GoodType;
import it.polimi.ingsw.GC_29.Components.SpecialBonusAndMalus;
import it.polimi.ingsw.GC_29.EffectBonusAndActions.Filter;
import it.polimi.ingsw.GC_29.Player.Player;
import it.polimi.ingsw.GC_29.Player.PlayerColor;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Lorenzotara on 10/06/17.
 */
public class Pray extends Input {

    private boolean answer;
    private PlayerColor playerColor;

    public Pray(boolean b, PlayerColor playerColor) {

        this.answer = b;
        this.playerColor = playerColor;
    }

    @Override
    public void perform(GameStatus model, Controller controller) throws Exception {

        controller.stopTimer();

        Player player = model.getPlayer(playerColor);

        model.setCurrentPlayer(player);

        if (answer) {

            System.out.println("PERFORMING PRAY ANSWER YES\n");

            int playerFaithPoints = player.getActualGoodSet().getGoodAmount(GoodType.FAITHPOINTS);

            GoodSet goodSet = new GoodSet(0,0,0,0,model.getGameBoard().getFaithPointsTrack().getVictoryPointsPerSlot()[playerFaithPoints],0,0);

            System.out.println("Victory points from church: " + model.getGameBoard().getFaithPointsTrack().getVictoryPointsPerSlot()[playerFaithPoints] + "\n\n");

            if (Filter.applySpecial(player, SpecialBonusAndMalus.FIVEVICTORYPOINTSIFPRAY)) {

                goodSet.addGoodSet(new GoodSet(0,0,0,0,5,0,0));
            }

            Filter.apply(player, goodSet);

            System.out.println("player faithPoints from goodset: " + player.getActualGoodSet().getGoodAmount(GoodType.FAITHPOINTS) +"\n");

            goodSet.addGoodSet(new GoodSet(0,0,0,0, 0, 0, - playerFaithPoints));

            System.out.println("playerFaithPoints variable: " + playerFaithPoints + "\n");

            player.updateGoodSet(goodSet);
        }

        else controller.executeTiles(player);

        controller.praying();

        player.setPlayerState(PlayerState.WAITING);

        System.out.println("PLAYER SETTATO A WAITING\n");


        if (controller.getPlayersPraying() == 0) {

            System.out.println("NESSUNO DEVE PIù PREGARE\n");

            model.setGameState(GameState.RUNNING);

            if (model.getCurrentEra() != Era.THIRD) {

                controller.setNewRound();
            }

            else controller.endGame();
        }

    }


}
