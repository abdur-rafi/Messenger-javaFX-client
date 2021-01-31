package sample;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import sample.DataPackage.Account;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.Message;
import sample.DataPackage.Person;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Binders {
    private static Binders instance = new Binders();
    private Person person;
    public ObservableList<GroupMessage> accountList;
    public ObservableList<Message> messageList ;
    public ObservableList<Account> peopleList ;
    public ObjectProperty<Image> imageViewProperty ;
    public ObjectProperty<Image> topImageProperty;
    public ObjectProperty<Image> ContactImageProperty;
    public BooleanProperty refresh;
    public BooleanProperty refreshAccount;
    public BooleanProperty refreshMessage;
    public BooleanProperty selectNew;
//    public BooleanProperty changeUser = new SimpleBooleanProperty(true);
    public StringProperty userId;
    public StringProperty userName ;
    public StringProperty notificationMessage ;
    public ExecutorService executorService ;
    public ProgressBar progressBar;
    public int activeMessageDatabaseIndex;
    public int activeMessageIndex;
    public int addToGroup = -1;
    public ArrayList<Boolean> flag;

    public static Binders instance() {
        return instance;
    }

    private Binders() {

    }

    public Person getPerson() {
        return person;
    }

    public synchronized void setPerson(Person person) {
        this.person = person;
        accountList = FXCollections.observableArrayList(message -> new Observable[]{message.newCount});
        messageList =  FXCollections.observableArrayList();
        peopleList = FXCollections.observableArrayList();
        imageViewProperty = new SimpleObjectProperty<>();
        topImageProperty = new SimpleObjectProperty<>();
        ContactImageProperty = new SimpleObjectProperty<>();
        refresh = new SimpleBooleanProperty(true);
        refreshAccount = new SimpleBooleanProperty(true);
        refreshMessage = new SimpleBooleanProperty(true);
        selectNew = new SimpleBooleanProperty(true);
        userId = new SimpleStringProperty();
        userName = new SimpleStringProperty();
        notificationMessage = new SimpleStringProperty("");
        executorService = Executors.newFixedThreadPool(1);
        flag = new ArrayList<>();
        setGroupImages();
        if (person.getGroups().size() != 0) {
            System.out.println("changing message list from setPerson");
        }
        for (int i = 0; i < person.getGroups().size(); ++i) {
            person.getGroups().get(i).newCount = new SimpleIntegerProperty(person.getNewCount().get(i));
            flag.add(false);
            System.out.println(person.getGroups().get(i).newCount.getValue());
        }
        imageViewProperty.setValue(person.getMyAccount().getImage());
        userId.setValue(Integer.toString(person.getMyAccount().getPort()));
        userName.setValue(person.getMyAccount().getName());
        if (accountList == null) System.out.println("snull");
        else if (person.getGroups() == null) System.out.println("gnull");
        accountList.setAll(person.getGroups());

    }

    public synchronized void addGroupMessage(GroupMessage groupMessage, int id) {
        person.getParticipationIndex().add(id);
        person.getUnseenCount().add(0);
        person.getNewCount().add(++person.messageCount);
        person.getSent().add(0);
        flag.add(true);
        groupMessage.newCount = new SimpleIntegerProperty(person.messageCount);
        for(var acc:groupMessage.getParticipants()){
            File file = new File("files/"+acc.fileId+Constants.format);
            Image image = new Image(file.toURI().toString());//,80,80,false,true);
            acc.setImage(image);
        }

        person.addGroupMessage(groupMessage);
        accountList.add(groupMessage);
        setIndividualGroupImage(person.getGroups().size() - 1);
        selectNew.setValue(!selectNew.getValue());
    }

    public synchronized void updateImage(Image image, int toBeChanged) {
        for (int i = 0; i < person.getGroups().size(); ++i) {
            var groups = person.getGroups().get(i);
            for (int j = 0; j < groups.getParticipants().size(); ++j) {
                Account acc = groups.getParticipants().get(j);
                if (acc.getPort() == toBeChanged) {
                    File file = new File("files/"+acc.fileId+Constants.format);
                    System.out.println("file name : " + file.getName());
                    image = new Image(file.toURI().toString());
                    acc.setImage(image);
                    if (i == activeMessageIndex && !person.getGroups().get(i).isAddAble()) {
                        topImageProperty.setValue(image);
                        ContactImageProperty.setValue(image);
                    }
//                    accountList.get(i).getParticipants().get(j).setImage(image);
                    if (!groups.isAddAble()) groups.setGroupImage(image);
                }
            }
        }
        refresh.setValue(!refresh.getValue());
    }

    public synchronized void addMessageOrAccount(Message message, int databaseIndex, Account account, String state) {
        int i = 0;
        for (var groups : person.getGroups()) {
            if (groups.getDatabaseIndex() == databaseIndex) {
                if (message != null) {
                    groups.getGroupMessage().add(message);
                    if (state.equals("imageMessage")) {
                        notificationMessage.set("");
                    }
//                    groups.newMessage.setValue(!groups.newMessage.getValue());
                } else {
                    groups.getParticipants().add(account);
                    File file = new File("files/"+account.fileId+Constants.format);
                    System.out.println("file name : " + file.getName());
                    Image image =new Image(file.toURI().toString());
                    account.setImage(image);
                }
                break;
            }
            ++i;
        }
        if (message != null) {
            person.getNewCount().set(i, ++person.messageCount);
            person.getGroups().get(i).newCount.setValue(person.messageCount);
        }
        if (activeMessageDatabaseIndex == databaseIndex && message != null) {
            if (person.getGroups().get(activeMessageIndex).getGroupMessage().size() != messageList.size())
                messageList.add(message);
        } else if (message != null) {
            int n = person.getUnseenCount().get(i);
            person.getUnseenCount().set(i, n + 1);
        }
        refreshAccount.setValue(!refreshAccount.getValue());
    }

    public void setGroupImages() {
        for (var groups : person.getGroups()) {
            if (!groups.isAddAble()) {
                for (var acc : groups.getParticipants()) {
                    if (acc.getPort() != Binders.instance().getPerson().getMyAccount().getPort()) {
                        groups.setGroupImage(acc.getImage());
                        groups.setGroupName(acc.getName());
                        groups.fileIndex = acc.fileId;
                        break;
                    }
                }
            }
            else{
                File file = new File("files/"+groups.fileIndex+Constants.format);
                System.out.println("file name : " + file.getName());
                Image image =new Image(file.toURI().toString());
                groups.setGroupImage(image);
            }
        }
    }

    public void setIndividualGroupImage(int i) {
        GroupMessage group = person.getGroups().get(i);
        if (group.isAddAble()){
            File file = new File("files/"+group.fileIndex+Constants.format);
            System.out.println("file name : " + file.getName());
            Image image =new Image(file.toURI().toString());
            group.setGroupImage(image);
            return;
        }
        for (var acc : group.getParticipants()) {
            if (acc.getPort() != Binders.instance().getPerson().getMyAccount().getPort()) {
                group.setGroupImage(acc.getImage());
                group.setGroupName(acc.getName());
                break;
            }
            else{
                acc.setImage(person.getMyAccount().getImage());
            }
        }
    }
}
