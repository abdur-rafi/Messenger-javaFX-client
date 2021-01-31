package sample.MainView;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadMainView {
    private static LoadMainView instance = new LoadMainView();
    private Stage stage = null;
    public static LoadMainView getInstance() {
        return instance;
    }

    public void loadStage(Stage stage){
        this.stage = stage;
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("BlankWindow.fxml"))));
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void loadScene(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("MainView.fxml"));
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private LoadMainView(){
        ;
    }
    public void loadAdminPanel(Stage stage){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("AdminWindow.fxml"));
            stage.setScene(new Scene(root));
        } catch (IOException e){}
    }
    public void loadUserInfo(Stage stage){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("AccountInfo.fxml"));
            stage.setScene(new Scene(root));
        } catch (IOException e){}
    }
}
