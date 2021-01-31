package sample;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.CreateAccount.LoadCreateAccount;
import sample.MainView.LoadMainView;
import sample.TransferPackage.Transmit;
import sample.TransferPackage.transferPackage;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Application {
    private static String fileName = "Database.bin";

    @Override
    public void start(Stage primaryStage) {
        try{
            Files.createDirectories(Paths.get("files"));
        } catch (IOException e){
            e.printStackTrace();
        }
        Image image = new Image(getClass().getResource("/Images/messengerIcon96.png").toExternalForm());
        primaryStage.setOnCloseRequest(event -> {
            if (Binders.instance().getPerson() != null) {
                transferPackage packet = new transferPackage("exit", null,
                        null, null, Binders.instance().getPerson().getMyAccount().getDatabaseIndex(), -1, Binders.instance().getPerson().getUnseenCount(),
                        Binders.instance().getPerson().getNewCount());
                System.out.println("sending");
                Transmit.transmitPackage(packet);
            }
        });
        primaryStage.getIcons().add(image);
        primaryStage.setTitle("Messenger");
        LoadMainView.getInstance().loadStage(primaryStage);
        primaryStage.show();
        Stage newStage = new Stage();
        newStage.setTitle("Create Account");
        newStage.initModality(Modality.APPLICATION_MODAL);
        LoadCreateAccount.getInstance().load(newStage);
        newStage.showAndWait();

    }

    public static void main(String[] args) {
        launch(args);
    }
    public static void receiveFile(Socket socket, String fileName,ObjectInputStream ois) {
        try {
            if(ois == null){
                ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            }
            int count = 0;
            System.out.println("reading file name");
            String name = ois.readUTF();
            if(fileName != null) name = fileName;
            System.out.println("File name: " + name);
            Long size = ois.readLong();
            System.out.println("file size: " + size);
            byte[] buffer = new byte[100000];
            FileOutputStream fos = new FileOutputStream("files/" + name + Constants.format);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            System.out.println("stuck here");

            while (size > 0 && (count = ois.read(buffer,0,(int)Math.min(buffer.length,size))) > 0) {
                bos.write(buffer, 0, count);
                size -= count;
            }
            System.out.println("free");

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendFile(File file,Socket socket,ObjectOutputStream oos){
        byte[] buffer = new byte[100000];
        try {
            if(oos == null){
                oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            oos.writeUTF(file.getName());
            oos.flush();
            oos.writeLong(file.length());
            oos.flush();
            double sz = file.length();
            double s = 0L;
            int count;
            while ((count = bis.read(buffer)) > 0) {
                oos.write(buffer, 0, count);
            }
            System.out.println("finished");
            oos.flush();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Image writeImage(File file,String out){
        Image image = null;
        try {
            System.out.println(file.getName());
            image =  new Image(file.toURI().toString(), 300, 300, false, false);
            if(image == null) System.out.println("crap");
            ByteArrayOutputStream bstram = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png",bstram);
            FileOutputStream fos = new FileOutputStream(out);
            System.out.println("Written Image");
            fos.write(bstram.toByteArray());

        } catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }
}
