package sample.AddContact;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadAddContact {
    public static LoadAddContact loadAddContact = new LoadAddContact();

    public static LoadAddContact getInstance() {
        return loadAddContact;
    }

    public void load(Stage stage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("AddContact.fxml"));
        } catch (IOException e) {
            System.out.println("exception");
        }

        stage.setScene(new Scene(root));
    }
}
