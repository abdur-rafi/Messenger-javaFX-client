package sample.MainView;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.Binders;
import sample.Constants;
import sample.DataPackage.Account;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.Main;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;

import java.io.File;
import java.net.Socket;


public class AccountInfoController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button changeImage;
    @FXML
    private Label nameLabel, portLabel;
    @FXML
    private ImageView accountImage;

    public void initialize() {
        accountImage.setImage(Binders.instance().imageViewProperty.getValue());
        int j = Binders.instance().getPerson().getMyAccount().fileId;
        File file1 = new File("files/" + j + Constants.format);
        System.out.println(file1.getName());
        Image image1 = new Image(file1.toURI().toString() );//,120,120,false,true);
        accountImage.setImage(image1);
        changeImage.setOnAction((event -> {
            Stage stage = (Stage) borderPane.getScene().getWindow();
            File selectedFile = null;
            FileChooser fileChooser = new FileChooser();
            selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile == null) return;
            Image image = new Image(selectedFile.toURI().toString() );//, 65, 65, false, false);
            transferPackage transferPackage = new transferPackage("updateImage", null, null, null,
                    Binders.instance().getPerson().getMyAccount().getDatabaseIndex(), 0, null, null);
            Transmit.transmitPackage(transferPackage);
            Socket socket = Binders.instance().getPerson().getMyAccount().socket;
            int i = Binders.instance().getPerson().getMyAccount().fileId;
            String file = "files/" + i + Constants.format;
            Main.writeImage(selectedFile, file);
            Image image2 = new Image(file1.toURI().toString());
            accountImage.setImage(image2);
            Main.sendFile(new File(file), socket,null);
            Binders.instance().getPerson().getMyAccount().setImage(image);
            Binders.instance().imageViewProperty.setValue(image);
            Binders.instance().refresh.setValue(!Binders.instance().refresh.getValue());
            for(int itr=0;itr<Binders.instance().getPerson().getGroups().size();++itr){
                int participation_index = Binders.instance().getPerson().getParticipationIndex().get(itr);
                GroupMessage grp = Binders.instance().getPerson().getGroups().get(itr);
                if(grp.isAddAble()){
                    grp.getParticipants().get(participation_index).setImage(image2);
                }
            }

        }));
        nameLabel.textProperty().bind(Binders.instance().userName);
        portLabel.textProperty().bind(Binders.instance().userId);
    }
}
