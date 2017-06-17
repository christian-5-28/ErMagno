package it.polimi.ingsw.GC_29.EffectBonusAndActions;

import it.polimi.ingsw.GC_29.Components.Era;
import it.polimi.ingsw.GC_29.Components.FamilyPawnType;
import it.polimi.ingsw.GC_29.Components.GameBoard;
import it.polimi.ingsw.GC_29.Components.PersonalBoard;
import it.polimi.ingsw.GC_29.Controllers.ActionChecker;
import it.polimi.ingsw.GC_29.Controllers.Controller;
import it.polimi.ingsw.GC_29.Controllers.GameSetup;
import it.polimi.ingsw.GC_29.Controllers.GameStatus;
import it.polimi.ingsw.GC_29.Player.Player;
import it.polimi.ingsw.GC_29.Player.PlayerColor;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

/**
 * Created by Lorenzotara on 15/06/17.
 */
public class TowerActionTest2 {
    @Test
    public void testIsPossible() throws Exception {

        ArrayList<Player> players = new ArrayList<>();

        Player player1 = new Player("l", PlayerColor.BLUE, new PersonalBoard(6));
        Player player2 = new Player("e", PlayerColor.GREEN, new PersonalBoard(6));
        //Player player3 = new Player("d", PlayerColor.YELLOW, new PersonalBoard(6));

        players.add(player1);
        players.add(player2);

        GameSetup gameSetup = new GameSetup(players);

        gameSetup.init();

        GameStatus model = gameSetup.getGameStatus();


        Player currentPlayer = player1;

        model.setCurrentPlayer(currentPlayer);

        Controller controller = new Controller(model);

        controller.setNewRound();

        ActionChecker actionChecker = controller.getActionChecker();

        actionChecker.setCurrentPlayer();

        actionChecker.checkActionOnPawn(player1.getFamilyPawn(FamilyPawnType.BLACK), actionChecker.getActionList());

        for (Action action : actionChecker.getValidActionList()) {
            if (action.getValid()) System.out.println(action);
        }



    }

}