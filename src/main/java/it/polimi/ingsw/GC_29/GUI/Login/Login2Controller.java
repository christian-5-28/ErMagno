package it.polimi.ingsw.GC_29.GUI.Login;

import it.polimi.ingsw.GC_29.GUI.ClientSocketView;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.event.ActionEvent;


/**
 * Created by AlbertoPennino on 21/06/2017.
 */
public class Login2Controller {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private RadioButton rmi;
    @FXML private RadioButton socket;
    @FXML private Button submit;
    @FXML private Text error;

    private String connection = null;



    public void sendSubmit(ActionEvent event){
        if(!username.getText().isEmpty()&& !password.getText().isEmpty()&&(rmi.isSelected() || socket.isSelected())){
            if(rmi.isSelected()){
                setConnection("rmi");
            }
            if (socket.isSelected()){
                setConnection("socket");
            }

            Node source = (Node) event.getSource();
            Stage stage  = (Stage) source.getScene().getWindow();
            stage.close();
        }
        else {
            error.setVisible(true);
        }
    }

    public void switchButtons(ActionEvent event){
        if (event.getSource()==rmi){
            socket.setSelected(false);
        }
        else if (event.getSource()==socket){
            rmi.setSelected(false);
        }
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }
}
