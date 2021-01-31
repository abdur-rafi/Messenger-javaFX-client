package sample.CreateAccount;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import sample.Binders;
import sample.Constants;
import sample.DataPackage.Account;
import sample.DataPackage.Person;
import sample.LogIn.LoadLogIn;
import sample.Main;
import sample.MainView.LoadMainView;
import sample.Server.serverThread;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;

import java.io.*;
import java.net.Socket;

public class CreateAccountController {
    @FXML
    private Hyperlink logInScene;
    @FXML
    private TextField nameField, passwordField, rePasswordField;
    @FXML
    private Label errorMessage;

    @FXML
    private void loadLogInScene(ActionEvent event) throws Exception {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        LoadLogIn.getInstance().load(stage);
    }

    @FXML
    private void newAccount(ActionEvent event) {
        String name = nameField.getText().trim();
        String password = passwordField.getText();
        String rePassword = rePasswordField.getText();

        if (password.length() < 5) {
            errorMessage.setText("Password must be at least 5 characters long");
        } else if (!password.equals(rePassword)) {
            errorMessage.setText("Password and re-Password does not match");
        } else {
            Account account = new Account(name, password, Constants.getInstance().defaultImage);
            transferPackage transmitPackage = new transferPackage("createUser", account, null, null,
                    -1, -1, null, null);
            Socket socket = null;
            boolean found = true;
            Person person = null;
            try {
                socket = new Socket(Constants.getInstance().host, Constants.getInstance().tcpPort);
                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                oos.writeObject(transmitPackage);
                oos.flush();

                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                person = (Person) ois.readObject();

                Image image =  Main.writeImage(new File("src/Images/defaultAccountImage.png"), "files/"+person.getMyAccount().fileId+Constants.format);
                Main.sendFile(new File("files/"+person.getMyAccount().fileId+Constants.format), socket,oos);
                System.out.println("image file sent");
                person.getMyAccount().setImage(image);
                person.getMyAccount().socket = socket;
                System.out.println(person.getMyAccount().getPort());
                transferPackage packet = new transferPackage("addSocket", null, null, null, person.getMyAccount().getDatabaseIndex()
                        , -1, null, null);
                person.getMyAccount().datagramSocket = Transmit.transmitPackage(packet);
                new serverThread(person.getMyAccount().datagramSocket).start();
            } catch (IOException e) {
                found = false;
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                found = false;
                e.printStackTrace();
            }
            if (person == null) {
                errorMessage.setText("No user found");
                return;
            }
            Binders.instance().setPerson(person);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            LoadMainView.getInstance().loadScene();
            stage = new Stage();
            LoadCreateAccount.getInstance().loadMessageBox(stage, 350, 150);
            stage.setTitle("Note");
            stage.showAndWait();


        }
    }

    @FXML
    private void clearLabel(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            errorMessage.setText("");
    }

}