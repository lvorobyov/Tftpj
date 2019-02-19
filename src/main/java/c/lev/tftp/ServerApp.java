package c.lev.tftp;

import sun.misc.Signal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class ServerApp {

    private static final int PORT = 8969;
    private static final int TIMEOUT = 500;
    private static volatile boolean active = true;

    public static void main(String[] args) {
        System.out.println("Server starting...");
        Signal.handle(new Signal("INT"), signal -> active = false);
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT);
            DatagramPacket p = new DatagramPacket(new byte[1], 1);
            String name = InetAddress.getLocalHost().getHostName();
            do {
                try {
                    socket.receive(p);
                    socket.send(new DatagramPacket(name.getBytes(),name.length(),p.getSocketAddress()));
                } catch (SocketTimeoutException ignored) { }
            } while (active);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Bye!");
    }
}
