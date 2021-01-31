package sample.Server;

import javafx.application.Platform;
import javafx.concurrent.Task;
import sample.Binders;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import sample.DataPackage.Account;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.Message;
import sample.Main;
import sample.TransferPackage.*;
import sample.checkReceived;

public class serverThread extends Thread {
    DatagramSocket datagramSocket;

    public serverThread(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = datagramSocket;
            while (true) {
                byte[] buffer = new byte[100000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("waiting to receive");
                socket.receive(packet);
                FromServer fromServer1 = null;
                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(buffer))) {
                    fromServer1 = (FromServer) inputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                FromServer fromServer = fromServer1;
                new Thread(() -> {
                    if (fromServer.getState().equals("addContact") || fromServer.getState().equals("newGroup")) {
                        final FromServer fromServer2 = fromServer;
                        checkReceived.instance.contactStatus = -1;
                        if(fromServer.getToBeModified() == -1){
                            return;
                        }
                        checkReceived.instance.contactStatus = 1;
                        Binders.instance().executorService.execute(new Thread(()->{
                            GroupMessage grp = null;
                            Socket socket1 = Binders.instance().getPerson().getMyAccount().socket;
                            try {
                                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket1.getInputStream()));
                                grp = (GroupMessage) ois.readObject();
                                int c = ois.readInt();
                                while(--c>=0 ) Main.receiveFile(socket1,null,ois);
                            } catch (IOException | ClassNotFoundException e){
                                e.printStackTrace();
                            }
                            GroupMessage grp1 = grp;
                            Platform.runLater(() -> {
                                Binders.instance().addGroupMessage(grp1, fromServer2.getToBeModified());
                                System.out.println("Inside addContact received packet length : " + packet.getLength());
                            });
                        }));
                    } else if (fromServer.getState().equals("updateImage")) {
                        Binders.instance().executorService.execute(() -> {
                            Socket socket1 = Binders.instance().getPerson().getMyAccount().socket;
                            Main.receiveFile(socket1, null,null);
                            Platform.runLater(() -> Binders.instance().updateImage(fromServer.getImage(), fromServer.getToBeModified()));
                        });
                    } else if (fromServer.getState().equals("message") || fromServer.getState().equals("newMember")) {
                        Message message = null;
                        Account account = null;
                        if(fromServer.getState().equals("newMember")){
                            if(fromServer.getToBeModified() == -1){
                                checkReceived.instance.memberStatus = 2;
                            }
                            Main.receiveFile(Binders.instance().getPerson().getMyAccount().socket,null,null);
                        }
                        if (fromServer.getState().equals("message")) {
                            message = fromServer.getMessage().get(0);
                        }
                        else {
                            account = fromServer.getGroupMessage().getParticipants().get(0);
                            checkReceived.instance.memberStatus = 1;
                        }
                        int index = fromServer.getToBeModified();
                        System.out.println("message or account");
                        Message message1 = message;
                        Account account1 = account;
                        Platform.runLater(() -> Binders.instance().addMessageOrAccount(message1, index, account1,fromServer.getState()));
                    }
                    else if(fromServer.getState().equals("imageMessage")){
                        Binders.instance().executorService.execute(()->{
                            Platform.runLater(()->{
                                Binders.instance().notificationMessage.setValue("receiving image.... ");
                            });
                            Socket socket1 = Binders.instance().getPerson().getMyAccount().socket;
                            FromServer fromServer2 = null;
                            try {

                                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket1.getInputStream()));
                                fromServer2 = (FromServer) ois.readObject();
                                Message message = null;
                                Account account = null;
                                if (fromServer2.getState().equals("imageMessage")) {
                                    message = fromServer2.getMessage().get(0);
                                } else {
                                    account = fromServer2.getGroupMessage().getParticipants().get(0);
                                    checkReceived.instance.memberStatus = 1;
                                }
                                int index = fromServer2.getToBeModified();
                                Message message1 = message;
                                Account account1 = account;
                                Platform.runLater(() -> Binders.instance().addMessageOrAccount(message1, index, account1, fromServer.getState()));
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    else if(fromServer.getState().equals("notFound")){
                        checkReceived.instance.contactStatus = -1;
                        checkReceived.instance.memberStatus = -1;
                    }
                    else if(fromServer.getState().equals("active")){
                        int port = fromServer.getToBeModified();
                        boolean active = fromServer.getAnother() == 1;
                        for(var grps : Binders.instance().getPerson().getGroups()){
                            for(var acc:grps.getParticipants()){
                                if(acc.getPort() == port){
                                    acc.isActive = active;
                                    Binders.instance().refresh.setValue(!Binders.instance().refresh.getValue());
                                }
                            }
                        }
                    }
                }).start();
                if (fromServer.getState().equals("exit")) {
                    Binders.instance().executorService.shutdown();
                    System.out.println(fromServer.getState());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}