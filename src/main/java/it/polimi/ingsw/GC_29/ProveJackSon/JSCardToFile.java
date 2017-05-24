package it.polimi.ingsw.GC_29.ProveJackSon;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.polimi.ingsw.GC_29.Components.*;
import it.polimi.ingsw.GC_29.EffectBonusAndActions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Lorenzotara on 22/05/17.
 */
public class JSCardToFile {

    public static void main(String[] args) throws IOException {


        // Ospitare i mendicanti

        ArrayList<Effect> immediateEffectsOIM = new ArrayList<Effect>();
        immediateEffectsOIM.add(new ObtainEffect(new GoodSet(1,1,1,1,1,1,1)));
        immediateEffectsOIM.add(new ObtainOnConditionEffect(new GoodSet(1,1,1,1,1,1,1), new GoodSet(1,1,1,1,1,1,1)));
        //immediateEffectsOIM.add(new CouncilPrivilegeEffect(3));
        //immediateEffectsOIM.add(new BonusEffect(new BonusAndMalusOnAction(ActionType.PURPLETOWER, 3, new GoodSet(1,1,1,1,1,1,1))));
        //immediateEffectsOIM.add(new ActionEffect(ActionType.PURPLETOWER, 3, new Discount(new GoodSet(), new GoodSet(), true)));


        ArrayList<Effect> permanentEffectsOIM = new ArrayList<Effect>();
        //permanentEffectsOIM.add(new CouncilPrivilegeEffect(3));



        DevelopmentCard ospitareIMendicanti = new DevelopmentCard(
                "Ospitare i Mendicanti",
                Era.FIRSTERA,
                new CardCost(false, true, new GoodSet(4,0,0,0,0,0,0), new GoodSet(), false, new GoodSet()),
                CardColor.PURPLE,
                immediateEffectsOIM,
                permanentEffectsOIM,
                false,
                0);


        // JACKSON

        ObjectMapper mapper = new ObjectMapper();
        FileWriter fileWriter = new FileWriter("C:\\Users\\Christian\\Desktop\\prova");

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(fileWriter, ospitareIMendicanti);

        fileWriter.close();

        String ciad;
        Effect ciao;
         ciao = new ObtainOnConditionEffect(new GoodSet(1,1,1,1,1,1,1), new GoodSet(1,1,1,1,1,1,1));
        ciad = new ObjectMapper().writeValueAsString(ciao);
        System.out.println(ciad);

    }
}