import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class OttSenderTCP implements Runnable {
    private final PacketQueue queue;

    public OttSenderTCP(PacketQueue queue) {
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Packet p = queue.remove();

                Socket s = new Socket(p.getDestination(), 8080);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                Packet.send(out, p);

                in.close();
                out.close();
                s.close();
            } catch (ConnectException ignored) {
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
