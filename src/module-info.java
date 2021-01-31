module Client {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires javafx.swing;

    opens sample;
    opens sample.MainView;
    opens sample.CreateAccount;
    opens sample.LogIn;
    opens sample.AddContact;
    opens sample.NewMessageGroup;
}