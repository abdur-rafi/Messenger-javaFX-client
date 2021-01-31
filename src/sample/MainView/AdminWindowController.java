package sample.MainView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;

public class AdminWindowController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField passwordField, codeField;

    @FXML
    private void close(ActionEvent event){
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void sendCode(ActionEvent event) throws Exception{
        String name = nameField.getText();
        String password = passwordField.getText();
        String code = codeField.getText();
        transferPackage transmitPackage = new transferPackage("save",null,null,null,0,0,null
        ,null);
        Transmit.transmitPackage(transmitPackage);
        close(event);
    }
}
