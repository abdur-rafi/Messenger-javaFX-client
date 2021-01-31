package sample.CreateAccount;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadCreateAccount {
    private static LoadCreateAccount instance = new LoadCreateAccount();

    public static LoadCreateAccount getInstance() {
        return instance;
    }

    private LoadCreateAccount(){
        ;
    }

    public void load(Stage stage){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("CreateAccount.fxml"));
            stage.setScene(new Scene(root));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void loadMessageBox(Stage stage, double width, double height){
        try {

            Parent root = FXMLLoader.load(getClass().getResource("MessageBox.fxml"));
            Scene scene;
            stage.setScene(scene = new Scene(root, width, height));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
