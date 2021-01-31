package sample.LogIn;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoadLogIn {
    private static LoadLogIn instance = new LoadLogIn();

    public static LoadLogIn getInstance() {
        return instance;
    }

    private LoadLogIn(){
        ;
    }

    public void load(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("LogIn.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("LogIn");
    }
}
