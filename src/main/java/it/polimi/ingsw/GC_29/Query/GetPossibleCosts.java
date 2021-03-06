package it.polimi.ingsw.GC_29.Query;

import it.polimi.ingsw.GC_29.Model.Cost;
import it.polimi.ingsw.GC_29.Model.Model;
import it.polimi.ingsw.GC_29.Model.TowerAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzotara on 18/06/17.
 */
public class GetPossibleCosts extends Query<Map<Integer, String>> {

    @Override
    public Map<Integer, String> perform(Model model) {

        Map<Integer, Cost> possibleCosts = ((TowerAction)model.getCurrentPlayer().getCurrentAction()).getPossibleCardCosts();
        HashMap<Integer, String> possibleCostsToString = new HashMap<>();

        for (Map.Entry<Integer, Cost> possibleCostEntry : possibleCosts.entrySet()) {

            possibleCostsToString.put(possibleCostEntry.getKey(), possibleCostEntry.getValue().toString());

        }

        return possibleCostsToString;

    }
}
