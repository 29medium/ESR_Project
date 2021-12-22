import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class OttReceiverTCP implements Runnable {
    private ServerSocket ss;
    private AddressingTable at;
    private PacketQueue queue;
    private String ip;

    public OttReceiverTCP(ServerSocket ss, AddressingTable at, PacketQueue queue, String ip) {
        this.ss = ss;
        this.at = at;
        this.queue = queue;
        this.ip = ip;
    }

    public void run() {
        while(true) {
            try {
                Socket s = ss.accept();
                Thread receiverHandler = new Thread(new OttReceiverHandler(new DataInputStream(new BufferedInputStream(s.getInputStream())), at, queue, ip));
                receiverHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
