package it.polimi.ingsw.GC_29.Model;

import de.vandermeer.asciitable.AsciiTable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Lorenzotara on 17/05/17.
 */
public class Workspace implements Cleanable {
    private EnumMap<FieldType,ActionSpace> fields;
    private ZoneType type;

    public Workspace (ZoneType type,int numberOfPlayers) {

        if (type != ZoneType.HARVEST && type != ZoneType.PRODUCTION){
            throw new IllegalArgumentException("Illegal area type: " + type);
        }

        this.type = type;

        this.fields = new EnumMap<>(FieldType.class);

        fields.put(FieldType.FIRST,new ActionSpace(null,1, new PawnSlot(1,true),true,false));

        fields.put(FieldType.SECOND,new ActionSpace(new BonusEffect(new BonusAndMalusOnAction(type,-3),null,null),1, new PawnSlot(numberOfPlayers,true),false,false));

        /*
        if(numberOfPlayers>=3) {
            fields.put(FieldType.SECOND,new ActionSpace(new BonusEffect(new BonusAndMalusOnAction(type,-3),null,null),1, new PawnSlot(numberOfPlayers,true),false,false));
        }

        else{
            fields.put(FieldType.SECOND,null);
        }*/
    }

    @Override
    public void clean() {

        for (ActionSpace actionSpace : fields.values()) {
            actionSpace.clean();
        }

    }

    public Map<FieldType, ActionSpace> getFields() {
        return fields;
    }

    public ZoneType getType() {
        return type;
    }

    public ActionSpace getActionSpace(FieldType fieldType){
        return fields.get(fieldType);
    }

    @Override
    public String toString() {
        return "Workspace{" +
                "fields=" + fields +
                ", type=" + type +
                '}';
    }

    public String toTable() {
        AsciiTable workSpaceTable = new AsciiTable();

        for (Map.Entry<FieldType, ActionSpace> fieldsEntry : fields.entrySet()) {
            workSpaceTable.addRule();
            workSpaceTable.addRow(fieldsEntry.getKey(), fieldsEntry.getValue().toString());
        }

        workSpaceTable.addRule();

        return "\n\n\n" + type + "\n\n" + workSpaceTable.render();
    }
}
