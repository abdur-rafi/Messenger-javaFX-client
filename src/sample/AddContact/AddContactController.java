package sample.AddContact;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import sample.Binders;
import sample.DataPackage.Account;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;
import sample.checkReceived;

import java.net.DatagramSocket;

public class AddContactController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField keyField;
    @FXML
    private Label errorMessage;

    @FXML
    private void addAccount(ActionEvent event) {
        int key = 0;
        try {
            key = Integer.parseInt(keyField.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage.setText("key must be an integer");
            return;
        }
        String message;
        int index;
        int index2 = Binders.instance().getPerson().getMyAccount().getDatabaseIndex();
        if (Binders.instance().addToGroup != -1) {
            message = "newMember";
            index = Binders.instance().activeMessageDatabaseIndex;
        } else {
            message = "addContact";
            index = Binders.instance().getPerson().getMyAccount().getDatabaseIndex();
        }
        System.out.println(message);
        System.out.println(index);
        checkReceived.instance.contactStatus = 0;
        checkReceived.instance.memberStatus = 0;
        transferPackage transmitPackage = new transferPackage(message,
                new Account(nameField.getText().trim(), null, key), null, null,
                index, index2, null, null);
        DatagramSocket datagramSocket = Transmit.transmitPackage(transmitPackage);
//        cancel(event);
        if(message.equals("addContact")) {
            System.out.println(checkReceived.instance.memberStatus);
            System.out.println(checkReceived.instance.contactStatus);
            while (checkReceived.instance.contactStatus == 0) {
//                System.out.println(checkReceived.instance.contactStatus);
                try{
                    Thread.sleep(40);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
            if(checkReceived.instance.contactStatus == 1){
                checkReceived.instance.contactStatus = 0;
                Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
            else{
                checkReceived.instance.contactStatus = 0;
                errorMessage.setText("No such contact found");
            }
        }
        else{
            while (checkReceived.instance.memberStatus == 0) {
//                System.out.println(checkReceived.instance.memberStatus);
                try{
                    Thread.sleep(40);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(checkReceived.instance.memberStatus == 1){
                errorMessage.setText("Member Added");
//                Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
//                stage.close();
                checkReceived.instance.memberStatus = 0;
            }
            else{
                errorMessage.setText("No such contact found");
                checkReceived.instance.memberStatus = 0;
            }
        }

    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void clearLabel(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            errorMessage.setText("");
    }
}


//        Account account = ReceiveContact.receiveContact(datagramSocket);
//        if(account != null) {
//            System.out.println(account.getName());
//            System.out.println(account.getPort());
//            CurrentPerson.getInstance().addFriend(account);
//            CurrentPerson.friends.add(account);
//            for (var obj : CurrentPerson.getInstance().getFriends()) {
//                System.out.println(obj.getName());
//            }
//            cancel(event);
//        }
//        else{
//            errorMessage.setText("Account not found");
//        }