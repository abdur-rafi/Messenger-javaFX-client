package sample.TransferPackage;

import sample.DataPackage.Person;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceivePerson {
    public static Person receivePerson(DatagramSocket datagramSocket) throws Exception{
        Person person = null;
        byte[] buffer2 = new byte[1000000];
        DatagramPacket packet = new DatagramPacket(buffer2, buffer2.length);
        datagramSocket.receive(packet);
        System.out.println("received packet length: " + packet.getLength());
        try(ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(buffer2))){
            person = (Person) inputStream.readObject();
        } catch (IOException e){
            System.out.println("IOException");
        }
        catch (ClassNotFoundException e){
            System.out.println("Not Found");
        }
        if(person != null) {
            System.out.println(person.getMyAccount().getName());
            System.out.println(person.getMyAccount().getPort());
        }
        return person;
    }
}
