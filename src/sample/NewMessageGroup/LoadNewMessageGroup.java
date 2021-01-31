package sample.NewMessageGroup;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadNewMessageGroup {
    private static LoadNewMessageGroup instance = new LoadNewMessageGroup();
    public static LoadNewMessageGroup getInstance(){
        return instance;
    }
    public void load(Stage stage){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("NewMessageGroup.fxml"));
            stage.setScene(new Scene(root));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
