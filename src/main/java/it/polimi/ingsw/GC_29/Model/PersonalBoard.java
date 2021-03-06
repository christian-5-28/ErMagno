package it.polimi.ingsw.GC_29.Model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by icedoom on 18/05/17.
 */
public class PersonalBoard {

    private BonusTile bonusTile;
    private Lane buildingLane;
    private Lane venturesLane;
    private Lane familyLane;
    private TerritoryLane territoryLane;
    private EnumMap<CardColor, Lane> laneMap;

    public PersonalBoard(BonusTile chosenTile, int laneDimension){
        bonusTile = chosenTile;
        buildingLane = new Lane(laneDimension);
        venturesLane = new Lane(laneDimension);
        familyLane = new Lane(laneDimension);
        territoryLane = new TerritoryLane(laneDimension);
        laneMap = new EnumMap<CardColor, Lane>(CardColor.class);
        laneMap.put(CardColor.YELLOW, buildingLane);
        laneMap.put(CardColor.PURPLE, venturesLane);
        laneMap.put(CardColor.GREEN, territoryLane);
        laneMap.put(CardColor.BLUE, familyLane);
    }

    public PersonalBoard(int laneDimension) {

        buildingLane = new Lane(laneDimension);
        venturesLane = new Lane(laneDimension);
        familyLane = new Lane(laneDimension);
        territoryLane = new TerritoryLane(laneDimension);
        laneMap = new EnumMap<CardColor, Lane>(CardColor.class);
        laneMap.put(CardColor.YELLOW, buildingLane);
        laneMap.put(CardColor.PURPLE, venturesLane);
        laneMap.put(CardColor.GREEN, territoryLane);
        laneMap.put(CardColor.BLUE, familyLane);
    }

    public BonusTile getBonusTile() {
        return bonusTile;
    }

    public Lane getBuildingLane() {
        return buildingLane;
    }

    public Lane getVenturesLane() {
        return venturesLane;
    }

    public Lane getFamilyLane() {
        return familyLane;
    }

    public TerritoryLane getTerritoryLane() {
        return territoryLane;
    }

    public Lane getLane(CardColor cardColor) {
        return laneMap.get(cardColor);
    }

    public void setBonusTile(BonusTile bonusTile) {
        this.bonusTile = bonusTile;
    }

    public String toTable() {

        StringBuilder bld = new StringBuilder();

        bld.append("\n\n\n");
        bld.append(bonusTile.toTable());

        for (Map.Entry<CardColor, Lane> entry : laneMap.entrySet()) {

            bld.append(entry.getKey());
            bld.append(" LANE :\n");
            bld.append(entry.getValue().toTable());
        }


        return bld.toString();
    }


}
