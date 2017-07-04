package it.polimi.ingsw.GC_29.Client.GUI.Suspended;


import it.polimi.ingsw.GC_29.Client.InputInterfaceGUI;

import it.polimi.ingsw.GC_29.Client.GUI.GameBoard.GameBoardController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.event.ActionEvent;

/**
 * Created by Lorenzotara on 01/07/17.
 */
public class SuspendedController {

    private InputInterfaceGUI sender;
    private GameBoardController gameBoardController;

    @FXML
    private Button joinGame;


    public void setSender(InputInterfaceGUI sender) {
        this.sender = sender;
    }

    /*
    public void joinGame(ActionEvent actionEvent) {
        System.out.println("Sending input");
        sender.sendInput("join game");
    }
    */

    public void joinGame(ActionEvent actionEvent) {
        sender.sendInput("join game");

        System.out.println("BOTTONE SCHIACCIATO JOIN GAME");
    }
}