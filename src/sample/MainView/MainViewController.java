package sample.MainView;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import sample.AddContact.LoadAddContact;
import sample.Binders;
import sample.Constants;
import sample.CreateAccount.LoadCreateAccount;
import sample.DataPackage.Account;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.Message;
import sample.NewMessageGroup.LoadNewMessageGroup;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static java.lang.Integer.max;
import static java.lang.Math.ceil;
import static java.lang.Math.min;

public class MainViewController {
    final KeyCombination ShiftEnter = KeyCombination.keyCombination("Shift+Enter");
    private final Image defaultImage = (new Image(getClass().getResource("/Images/defaultAccountImage.png").toExternalForm()
            , 65, 65, false, false));
    @FXML
    private ListView<GroupMessage> accountListView;
    @FXML
    private ListView<Message> messageListView;
    @FXML
    private ListView<Account> peopleListView;
    @FXML
    private TextArea messageArea;
    @FXML
    private Label contactLabel, topName, notify;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ImageView imageView, contactImage, settings, topImage, infoImage, addContactImage, newMessage, sendImage, sendFile;
    @FXML
    private HBox hBox1, hBox2;
    @FXML
    private VBox vBox1;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextField searchBar;

    private boolean present = true;
    private ContextMenu contextMenuImageView, contextMenuSettings;
    private ContextMenu contextMenu;
    private ObjectProperty<Image> popUpImage = new SimpleObjectProperty<>();
    private ImageView cellImage;
    private BorderPane borderPane1;
    final double MAX_RES = 1080;
    private boolean available = true;
    private int receiveATime = 15;

    private void setBorderPane() {
        borderPane1 = new BorderPane();
        cellImage = new ImageView();
        cellImage.imageProperty().bind(popUpImage);
        borderPane1.getChildren().add(cellImage);
    }

    private void setRefreshAccount() {
        Binders.instance().refreshAccount.addListener(((observable, oldValue, newValue) -> setAccountListViewCellFactory()));
    }

    private void setRefreshMessage() {
        Binders.instance().refreshMessage.addListener(((observable, oldValue, newValue) -> setMessageListViewCellFactory()));
    }


    private void setAccountListViewContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem item = new MenuItem("Add Member");
        item.setOnAction((event -> {
            Binders.instance().addToGroup = Binders.instance().activeMessageDatabaseIndex;
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            LoadAddContact.getInstance().load(stage);
            stage.showAndWait();
            Binders.instance().addToGroup = -1;
        }));
        contextMenu.getItems().add(item);
    }

//    private void changeUserListener() {
//        Binders.instance().changeUser.addListener(((observable, oldValue, newValue) ->
//                accountListView.getSelectionModel().selectFirst()));
//    }

    private void selectNewListener() {
        Binders.instance().selectNew.addListener(((observable, oldValue, newValue) ->
                accountListView.getSelectionModel().selectLast()));
    }

    private void refreshListener() {
        Binders.instance().refresh.addListener(((observable, oldValue, newValue) -> {
            setAccountListViewCellFactory();
            setMessageListViewCellFactory();
        }));
    }

    @FXML
    private void addContact(ActionEvent event) {
        Stage stage = new Stage();
        LoadAddContact.getInstance().load(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        addContactImage.setStyle("-fx-effect:  dropshadow(three-pass-box, rgba(0,0,253,0.8), 10, .1, 0, 0)");
        stage.showAndWait();
        addContactImage.setStyle("-fx-effect:  dropshadow(three-pass-box, rgba(256,256,256,0.8), 10, .1, 0, 0)");
    }

    @FXML
    private void sendMessage(KeyEvent event) {
        String message = messageArea.getText().trim();
        if (ShiftEnter.match(event)) {
            messageArea.appendText("\n");
            event.consume();
        } else if (!message.equals("") && event.getCode() == KeyCode.ENTER) {
            messageArea.clear();
            int id = Binders.instance().activeMessageIndex;
            int senderId = Binders.instance().getPerson().getParticipationIndex().get(id);
            int dbIndex = Binders.instance().getPerson().getGroups().get(id).getDatabaseIndex();
            boolean first = true;
            int size = Binders.instance().getPerson().getGroups().get(id).getGroupMessage().size();
            if (size != 0) {
                first = Binders.instance().getPerson().getGroups().get(id)
                        .getGroupMessage().get(size - 1).getSenderIndex() != senderId;
            }
            Message message1 = new Message(message, senderId, Binders.instance().getPerson().getMyAccount().getPort()
                    , null, null, first, null);
            ArrayList<Message> arr = new ArrayList<>();
            arr.add(message1);
            transferPackage transferPackage = new transferPackage("message", null, arr,
                    null, dbIndex, -1, null, null);
            Transmit.transmitPackage(transferPackage);
        }
    }

    @FXML
    private void adminPanel(ActionEvent event) {
        Stage stage = new Stage();
        LoadMainView.getInstance().loadAdminPanel(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void contextMenuForSettings() {
        contextMenuSettings = new ContextMenu();
        MenuItem item1 = new MenuItem("Add contact");
        MenuItem item2 = new MenuItem("Admin control");
        MenuItem item3 = new MenuItem("Test");
        MenuItem item4 = new MenuItem("Log out");
        contextMenuSettings.getItems().addAll(item1, item2, item3, item4);
        item1.setOnAction(this::addContact);
        item2.setOnAction(this::adminPanel);
        item4.setOnAction(event -> {
            transferPackage packet = new transferPackage("exit", null,
                    null, null, Binders.instance().getPerson().getMyAccount().getDatabaseIndex()
                    , -1, Binders.instance().getPerson().getUnseenCount(),
                    Binders.instance().getPerson().getNewCount());
            Transmit.transmitPackage(packet);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            LoadMainView.getInstance().loadStage(stage);
            Stage stage1 = new Stage();
            LoadCreateAccount.getInstance().load(stage1);
            stage1.showAndWait();
        });
        settings.setOnMouseClicked((event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                contextMenuSettings.show(settings, Side.BOTTOM, 15, 0);
            }
        }));
    }

    private void setImageViews() {
        imageView.imageProperty().bind(Binders.instance().imageViewProperty);
        topImage.imageProperty().bind(Binders.instance().topImageProperty);
        imageView.setOnMouseClicked((event -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            LoadMainView.getInstance().loadUserInfo(stage);
            stage.showAndWait();
        }));
        contactImage.imageProperty().bind(Binders.instance().ContactImageProperty);
        Circle circle = new Circle(imageView.getFitWidth() / 2, imageView.getFitWidth() / 2, 25);
        imageView.setClip(circle);
        Circle contactCircle = new Circle(contactImage.getFitWidth() / 2, contactImage.getFitHeight() / 2, 40);
        contactImage.setClip(contactCircle);
        Circle topCircle = new Circle(topImage.getFitWidth() / 2, topImage.getFitWidth() / 2, 25);
        topImage.setClip(topCircle);
        infoImage.setStyle("-fx-effect:  dropshadow(three-pass-box, rgba(0,0,253,0.8), 10, .1, 0, 0)");
        infoImage.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) return;
            if (present) {
                infoImage.setStyle("-fx-effect:  dropshadow(three-pass-box, rgba(256,256,256,0.8), 10, .1, 0, 0)");
                hBox2.getChildren().remove(vBox1);
                messageArea.setPrefWidth(700);
                setMessageListViewCellFactory();
                present = false;
            } else {
                hBox2.getChildren().add(vBox1);
                messageArea.setPrefWidth(550);
                infoImage.setStyle("-fx-effect:  dropshadow(three-pass-box, rgba(0,0,253,0.8), 10, .1, 0, 0)");
                setMessageListViewCellFactory();
                present = true;
            }
        });
        addContactImage.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) return;
            addContact(null);
        });
        newMessage.setOnMouseClicked(event -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            LoadNewMessageGroup.getInstance().load(stage);
            stage.showAndWait();
        });
        sendImage.setOnMouseClicked((event -> {
            FileChooser fileChooser = new FileChooser();
            File file;
            file = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
            if (file == null) return;
            Image image = new Image(file.toURI().toString());
            Image image1 = image;
            double w = image.getWidth();
            double h = image.getHeight();
            double r = w / h;
            if (w > h) {
                if (w > MAX_RES) {
                    double newH = MAX_RES / r;
                    image1 = new Image(file.toURI().toString(), MAX_RES, newH, true, false);
                } else {
                    if (h > MAX_RES) {
                        double newW = MAX_RES * r;
                        image1 = new Image(file.toURI().toString(), newW, MAX_RES, true, false);
                    }
                }
            }
            transferPackage packet1 = new transferPackage("imageMessage", null, null, null, Binders.instance().getPerson().getMyAccount().getDatabaseIndex(),
                    -1, null, null);
            Transmit.transmitPackage(packet1);
            int id = Binders.instance().activeMessageIndex;
            int senderId = Binders.instance().getPerson().getParticipationIndex().get(id);
            int dbIndex = Binders.instance().getPerson().getGroups().get(id).getDatabaseIndex();
            boolean first = true;
            int size = Binders.instance().getPerson().getGroups().get(id).getGroupMessage().size();
            if (size != 0) {
                first = Binders.instance().getPerson().getGroups().get(id)
                        .getGroupMessage().get(size - 1).getSenderIndex() != senderId;
            }
            Message message1 = new Message("", senderId, Binders.instance().getPerson().getMyAccount().getPort()
                    , null, null, first, image1);
            ArrayList<Message> arr = new ArrayList<>();
            arr.add(message1);
            Binders.instance().executorService.execute(() -> {
                Platform.runLater(() -> Binders.instance().notificationMessage.setValue("Sending message to " +
                        Binders.instance().getPerson().getGroups().get(id).getGroupName() + " ....."));
                transferPackage transferPackage = new transferPackage("imageMessage", null, arr,
                        null, dbIndex, -1, null, null);
                Socket socket = Binders.instance().getPerson().getMyAccount().socket;
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    oos.writeObject(transferPackage);
                    oos.flush();
                    Platform.runLater(() -> Binders.instance().notificationMessage.set(""));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> Binders.instance().notificationMessage.setValue(""));
            });
        }));
        sendFile.setOnMouseClicked((event) -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
            if (file == null) return;
            int id = Binders.instance().activeMessageIndex;
            int senderId = Binders.instance().getPerson().getParticipationIndex().get(id);
            int dbIndex = Binders.instance().getPerson().getGroups().get(id).getDatabaseIndex();
            boolean first = true;
            int size = Binders.instance().getPerson().getGroups().get(id).getGroupMessage().size();
            if (size != 0) {
                first = Binders.instance().getPerson().getGroups().get(id)
                        .getGroupMessage().get(size - 1).getSenderIndex() != senderId;
            }
            boolean first2 = first;
            Binders.instance().executorService.execute(() -> {
                Platform.runLater(() -> {
                    Binders.instance().notificationMessage.setValue("Sending " + file.getName() + " ....");
                    progressBar.setOpacity(1);
                    progressBar.setProgress(0.0);
                });
                Message message1 = new Message("", senderId, Binders.instance().getPerson().getMyAccount().getPort()
                        , null, null, first2, null, file.getName(), -1);
                ArrayList<Message> arr = new ArrayList<>();
                arr.add(message1);
                transferPackage packet = new transferPackage("fileMessage", null, arr, null,
                        dbIndex, Binders.instance().getPerson().getMyAccount().getDatabaseIndex(), null, null);
                Transmit.transmitPackage(packet);
                Socket socket = Binders.instance().getPerson().getMyAccount().socket;
                byte[] buffer = new byte[100000];
                try {
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    oos.writeLong(file.length());
                    oos.flush();
                    double sz = file.length();
                    double s = 0L;
                    int count;
                    while ((count = bis.read(buffer)) > 0) {
                        s += count;
                        double r = s / sz;
                        Platform.runLater(() -> progressBar.setProgress(r));
                        oos.write(buffer, 0, count);
                    }
                    System.out.println("finished");
                    oos.flush();
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                            Binders.instance().notificationMessage.setValue("");
                            progressBar.setOpacity(0.0);
                        }
                );
            });
        });
    }

    void accountListViewChangeSelectedListener() {
        accountListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                synchronized (Binders.instance().messageList) {
                    messageListView.setDisable(false);
                    messageArea.setDisable(false);
                    int id = Binders.instance().getPerson().getGroups().indexOf(newValue);
                    Binders.instance().getPerson().getUnseenCount().set(id, 0);
                    Binders.instance().activeMessageIndex = id;
                    Binders.instance().activeMessageDatabaseIndex = Binders.instance().getPerson().getGroups()
                            .get(id).getDatabaseIndex();
                    Binders.instance().messageList.clear();
                    Binders.instance().peopleList.clear();
                    Binders.instance().peopleList.addAll(newValue.getParticipants());
                    System.out.println("Clearing and changing message list from ChangeListener");
                    Binders.instance().topImageProperty.setValue(Binders.instance().getPerson().getGroups().get(id).getGroupImage());
                    topName.setText(Binders.instance().getPerson().getGroups().get(id).getGroupName());
                    Binders.instance().ContactImageProperty.setValue(Binders.instance().getPerson().getGroups().get(id).getGroupImage());
                    contactLabel.setText(Binders.instance().getPerson().getGroups().get(id).getGroupName());
                    Binders.instance().refreshAccount.setValue(!Binders.instance().refreshAccount.getValue());
                    if (newValue.getGroupMessage().size() == 0 && Binders.instance().getPerson().getSent().get(id) != 0
                            && !Binders.instance().flag.get(id)) {
                        Binders.instance().flag.set(id, true);
                        System.out.println("Message Request For :" + Binders.instance().getPerson().getGroups().get(id)
                                .getGroupName() + " from changeListener");
                        int val = Binders.instance().getPerson().getSent().get(id);
                        Binders.instance().getPerson().getSent().set(id, max(0, val - receiveATime));
                        receiveMessages(true);
                    }
                    Binders.instance().messageList.addAll(newValue.getGroupMessage());
                    if (newValue.getGroupMessage().size() != 0)
                        messageListView.scrollTo(newValue.getGroupMessage().size() - 1);
                }
            }
        }));
    }

    void setMessageListViewChangeListener() {
        messageListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                popUpImage.setValue(newValue.getImage());
            }
        });
    }

    private void setAccountListView() {
        setAccountListViewContextMenu();
        accountListViewChangeSelectedListener();
        setAccountListViewCellFactory();
        SortedList<GroupMessage> sList = new SortedList<>(Binders.instance().accountList, ((o1, o2) -> (-1) * o1.newCount.getValue().compareTo(o2.newCount.getValue())));
        FilteredList<GroupMessage> fList = new FilteredList<>(sList, s -> true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            String text = searchBar.getText();
            if (text == null || text.length() == 0)
                fList.setPredicate(s -> true);
            else {
                fList.setPredicate(groupMessage -> {
                            String lower = groupMessage.getGroupName().toLowerCase();
                            String lower2 = text.toLowerCase();
                            return lower.contains(lower2);
                        }
                );
            }
        });
        accountListView.setItems(fList);
//        accountListView.setItems(Binders.instance().accountList);
        accountListView.setStyle("-fx-selection-bar:#b8e2f2;-fx-accent:#ffffff;-fx-focus-color: #ffffff;" +
                " -fx-selection-background-radius:10;");
        accountListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//        accountListView.getSelectionModel().selectFirst();
    }

    private void setMessageListView() {
        setMessageListViewCellFactory();
        System.out.println("changing message list from setMessageView");
        messageListView.setItems(Binders.instance().messageList);
        messageListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setMessageListViewChangeListener();
//        messageListView.setStyle("-fx-selection-bar:#ffffff;-fx-accent:#ffffff;");
    }

    void setAccountListViewCellFactory() {
        accountListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<GroupMessage> call(ListView<GroupMessage> param) {
                ListCell<GroupMessage> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(GroupMessage item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setPadding(new Insets(5, 5, 5, 5));
                            int i = Binders.instance().getPerson().getGroups().indexOf(item);
                            // ImageView
                            ImageView imageView = new ImageView();
                            imageView.setFitHeight(40);
                            imageView.setFitWidth(40);
                            imageView.setImage(item.getGroupImage());
                            Circle circle = new Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, 20);
                            imageView.setClip(circle);

                            // TextFlow Inside HBox
                            Text text1 = new Text(Integer.toString(Binders.instance().getPerson().getUnseenCount().get(i)));
                            text1.setFont(Font.font("Verdana", 14));
                            TextFlow textFlow = new TextFlow(text1);
                            textFlow.setPadding(new Insets(0, 5, 0, 5));
                            textFlow.setPrefWidth(10 + text1.getBoundsInLocal().getWidth());
                            textFlow.setStyle("-fx-background-color:red;-fx-background-radius:7;");
                            HBox hBox2 = new HBox();
                            hBox2.getChildren().add(textFlow);
                            HBox.setHgrow(hBox2, Priority.ALWAYS);
                            hBox2.setAlignment(Pos.BASELINE_RIGHT);
                            hBox2.setPadding(new Insets(5, 10, 0, 0));

                            // Two Labels Inside VBox
                            String actualText = item.getGroupName();
                            double width = 120;// min(text.getBoundsInLocal().getWidth()+10,200);
                            Label label = new Label();
                            label.setText(actualText);
                            label.setFont(Font.font("Verdana", 12));
                            label.setPadding(new Insets(0, 3, 0, 3));
                            label.setPrefWidth(width);
                            label.setWrapText(true);
                            label.setStyle("-fx-font-weight: bold;");//+"-fx-border-color:#000000;-fx-border-width:1.5;");
                            Label activeLabel = new Label("(Active)");
                            HBox hBox3 = new HBox();
                            boolean active = (!item.isAddAble());
                            if (!item.isAddAble()) {
                                for (var per : item.getParticipants()) {
                                    active &= per.isActive;
                                }
                            }
                            if (active)
                                hBox3.getChildren().addAll(label, activeLabel);
                            else hBox3.getChildren().addAll(label);
                            int sz = item.getGroupMessage().size();
                            String mes = null;
                            if (sz > 0) mes = item.getGroupMessage().get(sz - 1).getMessage();
                            Label label1 = new Label(mes);
                            label1.setPrefWidth(160);
                            label1.setPadding(new Insets(0, 3, 0, 3));
                            label1.setTextFill(Paint.valueOf("#949494"));
                            label1.setPrefHeight(20);
                            VBox vBox = new VBox();
                            vBox.getChildren().addAll(hBox3, label1);

                            HBox hBox = new HBox();
                            hBox.getChildren().add(imageView);
                            hBox.getChildren().add(vBox);
                            if (!text1.getText().equals("0"))
                                hBox.getChildren().add(hBox2);
                            setGraphic(hBox);
                            if (item.isAddAble()) setContextMenu(contextMenu);
                        }
                    }
                };
                return cell;
            }
        });

    }

    void setMessageListViewCellFactory() {
        messageListView.setCellFactory(param -> {
            ListCell<Message> cell = new ListCell<>() {
                TextFlow textFlow;
                ImageView imageView;
                Hyperlink hyperlink;

                protected void updateItem(Message item, boolean empty) {
                    super.updateItem(item, empty);
                    setStyle("-fx-background-color:#ffffff;");
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (getIndex() == 0 && Binders.instance().getPerson().getSent().get(Binders.instance().activeMessageIndex)
                                > 0) {
                            int id = Binders.instance().activeMessageIndex;
                            int n = Binders.instance().getPerson().getSent().get(id);
                            Binders.instance().getPerson().getSent().set(id, max(0, n - receiveATime));
                            System.out.println("Message Request For :" + Binders.instance().getPerson().getGroups().get(id)
                                    .getGroupName() + "from messageListView");
                            receiveMessages(true);
                            return;
                        }
                        if (item.getSenderIndex() == -1) {
                            VBox vBox = new VBox();
                            VBox vBox2 = new VBox();
                            ImageView imageView = new ImageView();
                            imageView.setFitWidth(100);
                            imageView.setFitHeight(100);
                            Circle circle = new Circle(imageView.getFitWidth()/2,imageView.getFitHeight()/2,50);
                            imageView.setClip(circle);
                            GroupMessage grp = Binders.instance().getPerson()
                                    .getGroups().get(Binders.instance().activeMessageIndex);
                            imageView.setImage(grp.getGroupImage());
                            Label bottomText = new Label();
                            bottomText.setText(grp.getGroupName());
                            bottomText.setTextFill(Paint.valueOf("#000000"));
                            bottomText.setFont(Font.font("Verdana",18));
                            bottomText.setPadding(new Insets(5,0,5,0));
                            Label bottomText2 = new Label();
                            bottomText2.setText("You can now chat with " + grp.getGroupName());
                            bottomText2.setFont(Font.font("Verdana",18));
                            vBox2.getChildren().addAll(imageView, bottomText,bottomText2);
                            vBox.getChildren().addAll(vBox2);
                            vBox.setAlignment(Pos.CENTER);
                            vBox2.setAlignment(Pos.CENTER);
                            vBox.setPadding(new Insets(80,0,0,0));
                            setGraphic(vBox);
                            setText(null);
                            return;
                        }
                        if (item.getImage() != null) {
                            HBox hBox = new HBox();
                            imageView = new ImageView();
                            imageView.setImage(item.getImage());
                            double h = item.getImage().getHeight();
                            double w = item.getImage().getWidth();
                            double r = h / w;
                            imageView.setFitHeight(ceil(r * 250));
                            imageView.setFitWidth(250);
                            Rectangle rectangle = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
                            rectangle.setArcWidth(20);
                            rectangle.setArcHeight(20);
                            imageView.setClip(rectangle);
                            imageView.setOnMouseClicked((event -> {
                                borderPane1 = new BorderPane();
                                cellImage = new ImageView();
                                cellImage.setImage(item.getImage());
                                borderPane1.getChildren().addAll(cellImage);
                                Stage stage = new Stage();
                                stage.initModality(Modality.APPLICATION_MODAL);
                                Scene scene = new Scene(borderPane1, item.getImage().getWidth(), item.getImage().getHeight());
                                stage.setScene(scene);
//                                stage.setResizable(false);
                                stage.showAndWait();
                            }));
                            imageView.setOnMouseEntered((event -> {
                                ContextMenu contextMenu = new ContextMenu();
                                MenuItem menuItem = new MenuItem(" " + item.getDate() + " at " + item.getTime().toString().substring(0, 5));
                                menuItem.setStyle("-fx-background-color:#000000");
                                contextMenu.setStyle("-fx-selection-bar:#000000;-fx-accent:#ffffff;-fx-focus-color: #ffffff;" +
                                        " -fx-background-color:#000000; -fx-background-radius:10;");
                                contextMenu.getItems().addAll(menuItem);
                                int dx = item.isFirst() ? 40 : 15;
                                contextMenu.show(imageView, Side.RIGHT, dx, 0);
                                imageView.setOnMouseExited((event1 -> contextMenu.hide()));
                            }));

                            VBox vBox = new VBox();
                            vBox.getChildren().add(imageView);
                            vBox.setStyle("-fx-border-width:2;-fx-border-color:#3886c9;-fx-border-radius:10;");
                            HBox hBox2 = new HBox();
                            hBox2.setPadding(new Insets(15, 0, 0, 0));
                            hBox2.getChildren().add(vBox);
                            ImageView imageView1 = new ImageView();
                            imageView1.setFitWidth(40);
                            imageView1.setFitHeight(40);
                            if (item.getSenderIndex() == Binders.instance().getPerson().getParticipationIndex()
                                    .get(Binders.instance().activeMessageIndex)) {
                                hBox.setAlignment(Pos.TOP_RIGHT);
                                hBox.getChildren().addAll(hBox2, imageView1);
                            } else {
                                hBox.setAlignment(Pos.TOP_LEFT);
                                hBox.getChildren().addAll(imageView1, hBox2);
                                vBox.setStyle("-fx-border-width:2;-fx-border-color:#e6ebe8;-fx-border-radius:10;");
                            }
                            if (item.isFirst()) {
                                int id = item.getSenderIndex();
                                int selected = Binders.instance().activeMessageIndex;
                                Image image = Binders.instance().getPerson().
                                        getGroups().get(selected).getParticipants().get(id).getImage();
                                imageView1.setImage(image);
//                                hBox.setPadding(new Insets(0, 0, 0, 30));
                            } else {
                                imageView1.setFitHeight(10);
//                                hBox.setPadding(new Insets(0, 30, 0, 0));
                            }
                            Circle circle = new Circle(ceil(imageView1.getFitWidth() / 2), ceil(imageView1.getFitHeight() / 2), 20);
                            imageView1.setClip(circle);
                            setGraphic(hBox);
                            return;
                        }
                        if (item.getFileName() != null) {
                            ImageView imageView = new ImageView(Constants.getInstance().downloadImage);
                            imageView.setFitWidth(25);
                            imageView.setFitHeight(25);
                            HBox imageHBox = new HBox(imageView);
                            imageHBox.setPadding(new Insets(3, 0, 0, 0));
                            imageHBox.setAlignment(Pos.BOTTOM_CENTER);
                            ImageView personImage = new ImageView();
                            personImage.setFitHeight(40);
                            personImage.setFitWidth(40);
                            Circle circle = new Circle(20, 20, 20);
                            personImage.setClip(circle);
                            HBox hBox2 = new HBox(personImage);
                            hBox2.setAlignment(Pos.TOP_CENTER);
                            if (item.isFirst()) {
                                int id = item.getSenderIndex();
                                int selected = Binders.instance().activeMessageIndex;
                                Image image = Binders.instance().getPerson().
                                        getGroups().get(selected).getParticipants().get(id).getImage();
                                personImage.setImage(image);
                                hBox2.setPrefHeight(50);
                                imageHBox.setPadding(new Insets(13, 0, 0, 0));
                            } else {
                                personImage.setFitHeight(10);
                            }
                            Text text = new Text(item.getFileName());
                            text.setFont(Font.font("Verdana", 14));
                            hyperlink = new Hyperlink(item.getFileName());
                            hyperlink.setFont(Font.font("Verdana", 14));
                            hyperlink.setPadding(new Insets(5, 10, 5, 10));
                            double width = text.getBoundsInLocal().getWidth();
                            width = min(width + 25, param.getWidth() - 80);
                            hyperlink.setPrefWidth(width);
                            HBox hBox = new HBox();
                            hBox.setAlignment(Pos.BOTTOM_RIGHT);
                            if (item.getSenderIndex() == Binders.instance().getPerson().getParticipationIndex()
                                    .get(Binders.instance().activeMessageIndex)) {
                                hyperlink.setTextFill(Paint.valueOf("#ffffff"));
                                hyperlink.setStyle("-fx-background-color:#3886c9;-fx-background-radius:10");
                                hBox.setPadding(new Insets(0, 0, 0, 30));
                                hBox.getChildren().addAll(imageHBox, hyperlink, hBox2);
                            } else {
                                hBox.setAlignment(Pos.BOTTOM_LEFT);
                                hyperlink.setTextFill(Paint.valueOf("#000000"));
                                hyperlink.setStyle("-fx-background-color:#e6ebe8;-fx-background-radius:10");
                                hBox.setPadding(new Insets(0, 30, 0, 0));
                                hBox.getChildren().addAll(hBox2, hyperlink, imageHBox);
                            }
                            hyperlink.setOnAction((event) -> Binders.instance().executorService.execute(() -> {
                                Platform.runLater(() -> {
                                    Binders.instance().notificationMessage.setValue("downloading " +
                                            item.getFileName() + " ....");
                                    progressBar.setOpacity(1);
                                    progressBar.setProgress(0);
                                });
                                transferPackage packet = new transferPackage("downloadFile", null, null, null,
                                        Binders.instance().getPerson().getMyAccount().getDatabaseIndex(), item.getFileIndex(), null, null);
                                Transmit.transmitPackage(packet);
                                Socket socket = Binders.instance().getPerson().getMyAccount().socket;
                                try {
                                    int count = 0;
                                    InputStream is = socket.getInputStream();
                                    DataInputStream dis = new DataInputStream(is);
                                    Long size = dis.readLong();
                                    double sz = size;
                                    double s = 0;
                                    byte[] buffer = new byte[100000];
                                    FileOutputStream fos = new FileOutputStream("files/" + item.getFileName());
                                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                                    while (size > 0 && (count = is.read(buffer)) > 0) {
                                        bos.write(buffer, 0, count);
                                        s += count;
                                        double r = s / sz;
                                        Platform.runLater(() -> progressBar.setProgress(r));
                                        size -= count;
                                    }
                                    bos.close();
                                    File file = new File("files/" + item.getFileName());
                                    Runtime.getRuntime().exec("explorer.exe /select," + file.getPath());

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Platform.runLater(() -> {
                                    Binders.instance().notificationMessage.setValue("");
                                    progressBar.setOpacity(0.0);
                                });
                            }));

                            setGraphic(hBox);
                            return;
                        }
                        Text text = new Text(item.getMessage());
                        text.setFont(Font.font("Verdana", 14));
                        double width = text.getBoundsInLocal().getWidth();
                        width = min(width + 10, param.getWidth() - 80);
                        HBox hBox = new HBox();
                        hBox.setPrefWidth(width);
//                        hBox.setStyle("-fx-border-color:#000000;-fx-border-width:1.5");
                        textFlow = new TextFlow();
                        textFlow.getChildren().add(text);
                        textFlow.setPadding(new Insets(5, 10, 5, 10));
                        textFlow.setStyle("-fx-background-color : #e6ebe8; -fx-background-radius: 10;");

                        textFlow.setOnMouseEntered((event -> {
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem menuItem = new MenuItem(" " + item.getDate() + " at " + item.getTime().toString().substring(0, 5));
                            menuItem.setStyle("-fx-background-color:#000000");
                            contextMenu.setStyle("-fx-selection-bar:#000000;-fx-accent:#ffffff;-fx-focus-color: #ffffff;" +
                                    " -fx-background-color:#000000; -fx-background-radius:10;");
                            contextMenu.getItems().addAll(menuItem);
                            int dx = item.isFirst() ? 40 : 15;
                            contextMenu.show(textFlow, Side.RIGHT, dx, 0);
                            textFlow.setOnMouseExited((event1 -> contextMenu.hide()));
                        }));

                        hBox.setAlignment(Pos.BASELINE_LEFT);
                        ImageView imageView = new ImageView();
                        imageView.setFitWidth(40);
                        imageView.setFitHeight(40);
                        imageView.setStyle("-fx-border-width:1.5");
                        if (item.isFirst()) {
                            int id = item.getSenderIndex();
                            int selected = Binders.instance().activeMessageIndex;
                            Image image = Binders.instance().getPerson().
                                    getGroups().get(selected).getParticipants().get(id).getImage();
                            imageView.setImage(image);
                        } else {
                            imageView.setFitHeight(10);
                        }
                        Circle circle = new Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, 20);
                        imageView.setClip(circle);
                        ImageView imageView1 = new ImageView(Constants.getInstance().fileImage);
                        if (item.getSenderIndex() == Binders.instance().getPerson().getParticipationIndex()
                                .get(Binders.instance().activeMessageIndex)) {
                            hBox.setAlignment(Pos.BASELINE_RIGHT);
                            textFlow.setStyle("-fx-background-color : #3886c9; -fx-background-radius: 10;");
//                                    "-fx-border-width:1.5;-fx-border-color:#000000");
                            text.setFill(Paint.valueOf("#ffffff"));
                            hBox.setPadding(new Insets(0, 0, 0, 30));
                            hBox.getChildren().add(textFlow);
                            hBox.getChildren().add(imageView);
                        } else {
                            hBox.setPadding(new Insets(0, 30, 0, 0));
                            hBox.getChildren().add(imageView);
                            hBox.getChildren().add(textFlow);
//                            if(item.getFileName() != null) hBox.getChildren().add(imageView1);
                        }
                        setGraphic(hBox);
                    }
                }
            };
            return cell;
        });
    }

    private void setPeopleListViewCellFactory() {
        peopleListView.setCellFactory(param -> {
            ListCell<Account> cell = new ListCell<>() {
                @Override
                protected void updateItem(Account item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }
                    setPadding(new Insets(5, 5, 5, 5));
                    // ImageView
                    ImageView imageView = new ImageView();
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setImage(item.getImage());
                    Circle circle = new Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, 15);
                    imageView.setClip(circle);

                    String actualText = item.getName();
                    double width = 120;// min(text.getBoundsInLocal().getWidth()+10,200);
                    Label label = new Label();
                    label.setText(actualText);
                    label.setFont(Font.font("Verdana", 10));
                    label.setPadding(new Insets(0, 3, 0, 3));
                    label.setPrefWidth(width);
                    label.setWrapText(true);
//                    label.setStyle("-fx-font-weight: bold;");

                    HBox hBox = new HBox();
                    hBox.getChildren().add(imageView);
                    hBox.getChildren().add(label);
//                    hBox.setSpacing(10);


                    setGraphic(hBox);

                }
            };
            return cell;
        });
    }

    public void initialize() {
        Binders.instance().progressBar = progressBar;
        progressBar.setOpacity(0.0);
        messageListView.setDisable(true);
        messageArea.setDisable(true);
        topName.setText("");
        contactLabel.setText("");
        peopleListView.setItems(Binders.instance().peopleList);
        setPeopleListViewCellFactory();
        notify.textProperty().bind(Binders.instance().notificationMessage);
        refreshListener();
        setRefreshAccount();
        setRefreshMessage();
        selectNewListener();
//        changeUserListener();
        setAccountListView();
        setMessageListView();
        contextMenuForSettings();
        setImageViews();
        setBorderPane();
    }

    private void receiveMessages(boolean add) {
        int index = Binders.instance().activeMessageIndex;
        Binders.instance().executorService.execute(() -> {
            Platform.runLater(() -> {
                Binders.instance().notificationMessage.setValue("Loading messages of " +
                        Binders.instance().getPerson().getGroups().get(index).getGroupName() + ".....");
                progressBar.setOpacity(1);
                progressBar.setProgress(0);
            });
            transferPackage packet = new transferPackage("receiveMessage", null, null, null,
                    Binders.instance().getPerson().getMyAccount().getDatabaseIndex(), index,
                    null, null);
            Transmit.transmitPackage(packet);
            try {
                Socket socket = Binders.instance().getPerson().getMyAccount().socket;
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                int n = (Integer) ois.readObject();
                ArrayList<Message> messages = new ArrayList<>();
                for (int i = 0; i < n; ++i) {
                    double c = i + 1;
                    double r = c / n;
                    Platform.runLater(() -> progressBar.setProgress(r));
                    messages.add((Message) ois.readObject());
                }
                ArrayList<Message> messages1
                        = Binders.instance().getPerson().getGroups()
                        .get(index).getGroupMessage();
                int c = messages.size();
                messages.addAll(messages1);
                Binders.instance().getPerson().getGroups()
                        .get(index).setGroupMessage(messages);
                Platform.runLater(() -> {
                    System.out.println(Thread.currentThread());
                    progressBar.setOpacity(0);
                    if (add && index == Binders.instance().activeMessageIndex) {
                        Binders.instance().messageList.clear();
                        Binders.instance().messageList.addAll(messages);
                        messageListView.scrollTo(c);
                    }
                    Binders.instance().notificationMessage.set("");
                });

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

}
