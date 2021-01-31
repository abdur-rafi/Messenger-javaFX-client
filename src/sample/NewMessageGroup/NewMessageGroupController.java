package sample.NewMessageGroup;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.Binders;
import sample.Constants;
import sample.DataPackage.Account;
import sample.DataPackage.Person;
import sample.Main;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;

import java.io.File;

public class NewMessageGroupController {

    @FXML
    private TextField nameField;
    @FXML
    private Button selectImage,create,cancel;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ImageView groupImageView;

    private Image groupImage = Constants.getInstance().defaultImage;

    File file = null;

    public void initialize(){
        selectImage.setOnAction((event -> {
            FileChooser fileChooser = new FileChooser();
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            file =  fileChooser.showOpenDialog(stage);
            if(file != null) {
                groupImageView.setImage(new Image(file.toURI().toString(),
                        groupImageView.getFitWidth(), groupImageView.getFitHeight(), false, false));
                groupImage = new Image(file.toURI().toString(), 65, 65, false, false);
            }
        }));
        create.setOnAction(event -> {
            String name = nameField.getText().trim();
            transferPackage transferPackage = new transferPackage("newGroup",new Account(name,null,groupImage),
                    null,null,Binders.instance().getPerson().getMyAccount().getDatabaseIndex(),-1,null,null);
            Transmit.transmitPackage(transferPackage);
            if(file == null){
                file = new File("src/Images/defaultAccountImage.png");
            }
            Main.writeImage(file,"temp");
            file = new File("temp");
            Main.sendFile(file,Binders.instance().getPerson().getMyAccount().socket,null);
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.close();
        });
        cancel.setOnAction(event -> {
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.close();
        });

    }
}
