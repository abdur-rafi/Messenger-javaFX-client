package sample.LogIn;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import sample.Binders;
import sample.Constants;
import sample.CreateAccount.LoadCreateAccount;
import sample.DataPackage.Account;
import sample.DataPackage.Person;
import sample.Main;
import sample.MainView.LoadMainView;
import sample.Server.serverThread;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;

import java.io.*;
import java.net.Socket;

public class LogInController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField passwordField, portField;
    @FXML
    private Button logInButton;
    @FXML
    private Label errorMessage;
    @FXML
    private ProgressBar progressbar;

    private Task<Person> task;

    private String error = "Invalid user name or password";

    public void initialize() {

    }

    @FXML
    private void returnScene(ActionEvent event) throws Exception {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        LoadCreateAccount.getInstance().load(stage);
        stage.setTitle("Create Account");
    }

    @FXML
    private void logIn(ActionEvent event) {
        String name = nameField.getText().trim();
        String pass = passwordField.getText();
        int key = 0;
        try {
            key = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage.setText("key must be an integer");
            return;
        }
        transferPackage transmitPackage = new transferPackage("logIn", new Account(name, pass, key),
                null, null, -1, -1, null, null);
        Socket socket = null;
        Person person = null;
        try {
            socket = new Socket(Constants.getInstance().host, Constants.getInstance().tcpPort);
            Socket socket1 = socket;
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            oos.writeObject(transmitPackage);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            person = (Person) ois.readObject();
//            DataInputStream dis = new DataInputStream(socket.getInputStream());
            if (person == null) {
                System.out.println("none user");
                errorMessage.setText("No user found");
                return;
            }
            int c = ois.readInt();
            System.out.println("Number of files " + c);
            while (--c >= 0) Main.receiveFile(socket,null,ois);
            System.out.println(person.getMyAccount().fileId);
            File file = new File("files/"+person.getMyAccount().fileId+Constants.format);
            System.out.println("file name : " + file.getName());
            Image image = new Image(file.toURI().toString());//,80,80,false,true);
            System.out.println(image);
            person.getMyAccount().setImage(image);
            for(var grps:person.getGroups()){
                for(var acc:grps.getParticipants()){
                    if(acc.getDatabaseIndex() != person.getMyAccount().getDatabaseIndex()){
                        file = new File("files/"+acc.fileId+Constants.format);
                        System.out.println("file name : " + file.getName());
                        image = new Image(file.toURI().toString());//,80,80,false,true);
                        acc.setImage(image);
                    }
                }
            }
            person.getMyAccount().socket = socket;
            person.getMyAccount().isActive = true;
            System.out.println(person.getMyAccount().getPort());
            transferPackage packet = new transferPackage("addSocket", null, null, null, person.getMyAccount().getDatabaseIndex()
                    , -1, null, null);
            person.getMyAccount().datagramSocket = Transmit.transmitPackage(packet);
            new serverThread(person.getMyAccount().datagramSocket).start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Binders.instance().setPerson(person);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        LoadMainView.getInstance().loadScene();
        System.out.println("loaded");
        stage = new Stage();
        LoadCreateAccount.getInstance().loadMessageBox(stage, 350, 150);
        stage.setTitle("Note");
        stage.showAndWait();

    }

    @FXML
    private void clearLabel(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            errorMessage.setText("");
    }

}
