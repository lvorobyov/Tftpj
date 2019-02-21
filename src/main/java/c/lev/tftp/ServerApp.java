package c.lev.tftp;

import lombok.val;
import lombok.var;
import org.apache.tika.Tika;
import org.clapper.util.misc.MIMETypeUtil;
import sun.misc.Signal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerApp {

    private static final int PORT = 8969;
    private static final int TIMEOUT = 500;
    private static final int BUFFER_SIZE = 512;
    private static volatile boolean active = true;
    private static Thread receiver = null;
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static Logger logger = Logger.getLogger("Tftp");

    private static Map<String,String> mimeTypes = new HashMap<String, String>() {{
        put("video/3gpp","3gp");
        put("video/3gpp2","3gp");
        put("audio/mpeg","mp3");
        put("video/mp4","mp4");
        put("video/ogg","ogg");
        put("video/webm","webm");
        put("audio/webm","webm");
        put("video/x-ms-wmv","wmv");
        put("video/x-flv","flv");
        put("audio/x-flac", "flac");
        put("audio/aac","aac");
        put("audio/ogg","ogg");
        put("audio/vorbis","ogg");
        put("audio/x-ms-wma","wma");
        put("audio/vnd.wave","wav");
        put("audio/vnd.rn-realaudio","rm");
        put("image/vnd.microsoft.icon","ico");
        put("image/webp","webp");
        put("application/x-tika-ooxml","docx");
        put("application/x-tika-msoffice","doc");
    }};

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
                channel.bind(new InetSocketAddress(PORT));
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_ACCEPT);
                ServerSocket socket = channel.socket();
                logger.info("Start listening");
                do {
                    int nn = selector.select(TIMEOUT);
                    if (nn == 0)
                        continue;

                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (val key: keys) {
                        if ((key.readyOps() & SelectionKey.OP_ACCEPT) != 0) {
                            startConnection(socket.accept());
                        }
                    }

                    keys.clear();
                } while (active);
                channel.close();
                selector.close();
                logger.info("Stop listening");
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
                logger.info("Downloading...");
                val file = new File("downloaded_" + System.currentTimeMillis());
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
                logger.info("Downloaded");
                Tika tika = new Tika();
                val mime = tika.detect(file);
                logger.info("Mime-Type: " + mime);
                var ext = (mimeTypes.containsKey(mime))?
                        mimeTypes.get(mime) : MIMETypeUtil.fileExtensionForMIMEType(mime);
                Files.move(file.toPath(), Paths.get(file.toPath().toString() + '.' + ext));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
