import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class OttSenderTCP implements Runnable {
    private String ip;
    private String bootstrapperIP;
    private AddressingTable at;
    private PacketQueue queue;

    public OttSenderTCP(String ip, String bootstrapperIP, AddressingTable at, PacketQueue queue) {
        this.ip = ip;
        this.bootstrapperIP = bootstrapperIP;
        this.at = at;
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Packet p = queue.remove();
                Packet.send(at.getDataOutputStream(p.getDestination()), p);
            } catch (ConnectException ignored) {
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
