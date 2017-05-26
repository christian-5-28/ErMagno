package it.polimi.ingsw.GC_29.EffectBonusAndActions;

import it.polimi.ingsw.GC_29.Components.*;
import it.polimi.ingsw.GC_29.Player.PlayerStatus;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static it.polimi.ingsw.GC_29.Player.PlayerColor.*;

/**
 * Created by Lorenzotara on 22/05/17.
 */
public class TowerActionTest {
    @Test
    public void testExecute() throws Exception {

    }

    @Test
    public void testIsPossible() throws Exception {
        FamilyPawn familyPawnBlueOrange = new FamilyPawn(BLUE, FamilyPawnType.ORANGE, 3);
        FamilyPawn familyPawnRedBlack = new FamilyPawn(GREEN, FamilyPawnType.BLACK, 3);
        ActionType actionType = ActionType.BLUETOWER;
        int workersSelected = 1;
        boolean realAction = true;
        PlayerStatus playerStatus = new PlayerStatus(new ArrayList<BonusAndMalusOnAction>(), new ArrayList<BonusAndMalusOnGoods>(), null, new GoodSet(1,2,3,4,5,6,7), new HashMap<CardColor, Integer>(), true, true,true,true);
        Tower tower = new Tower(CardColor.BLUE);
        int floorIndex = 2;
        tower.getFloors()[floorIndex].setDevelopmentCard(new DevelopmentCard("a", Era.FIRST, new CardCost(false, true, new Cost(new GoodSet(4,0,0,0,0,0,0), new GoodSet()), new Cost(new GoodSet(), new GoodSet())), CardColor.BLUE, new ArrayList<Effect>(), new ArrayList<Effect>(), false, 0));
        TowerAction towerAction = new TowerAction(familyPawnBlueOrange, actionType, workersSelected, realAction, playerStatus, tower, floorIndex);

        tower.getFloor(floorIndex-1).getActionSpace().addPawn(familyPawnRedBlack);

        System.out.println(towerAction.isPossible());
    }

}