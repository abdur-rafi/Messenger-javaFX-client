package sample;

import javafx.scene.image.Image;

public class Constants {
    private static Constants constants = new Constants();
    public static Constants getInstance(){
        return constants;
    }
    public final Image defaultImage = new Image(getClass().getResource("/Images/defaultAccountImage.png").toExternalForm()
            ,65,65,false,false);
    public final Image fileImage = new Image(getClass().getResource("/Images/file-48.png").toExternalForm());
    public final Image downloadImage = new Image(getClass().getResource("/Images/download-48.png").toExternalForm());
    public final String host = "192.168.0.102";
    public final int tcpPort = 4003;
    public final int udpPort = 4000;
    public static String format = ".png";
    private Constants(){
        ;
    }
}
