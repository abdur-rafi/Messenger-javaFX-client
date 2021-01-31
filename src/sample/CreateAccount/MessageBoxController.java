package sample.CreateAccount;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sample.Binders;

public class MessageBoxController {

    @FXML
    private Label label;

    public void initialize(){
        label.textProperty().bind(Binders.instance().userId);
    }

    @FXML
    private void close(ActionEvent event){
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.close();

    }

}
