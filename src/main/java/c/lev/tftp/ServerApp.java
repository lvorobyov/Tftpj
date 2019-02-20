package c.lev.tftp;

import lombok.val;
import sun.misc.Signal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {

    private static final int PORT = 8969;
    private static final int TIMEOUT = 500;
    private static final int BUFFER_SIZE = 512;
    private static volatile boolean active = true;
    private static Thread receiver = null;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("Server starting...");
        Signal.handle(new Signal("INT"), signal -> active = false);
        startReceiver();
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
            receiver.join();
            executor.shutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Bye!");
    }

    private static void startReceiver() {
        receiver = new Thread(() -> {
            try {
                Selector selector = Selector.open();
                ServerSocketChannel channel = ServerSocketChannel.open();
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_ACCEPT);
                do {
                    int nn = selector.select(TIMEOUT);
                    if (nn == 0)
                        continue;

                    for (val key: selector.selectedKeys()) {
                        if ((key.readyOps() & SelectionKey.OP_ACCEPT) != 0) {
                            SocketChannel client = channel.accept();
                            startConnection(client.socket());
                        }
                    }

                } while (active);
                channel.close();
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }

    private static void startConnection(Socket client) {
        executor.execute(new WorkerRunnable(client));
    }

    static class WorkerRunnable implements Runnable {
        private Socket client;

        WorkerRunnable(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                val file = new File("downloaded_" + System.currentTimeMillis() + ".mkv");
                val output = new FileOutputStream(file);
                val input = client.getInputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = input.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                input.close();
                output.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
