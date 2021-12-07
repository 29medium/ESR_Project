import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerSenderTCP implements Runnable{
    private PacketQueue queue;

    public ServerSenderTCP(PacketQueue queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            while(true) {
                Packet p = queue.remove();

                Socket s = new Socket(p.getDestination(), 8080);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                out.write(p.toBytes());
                out.flush();

                out.close();
                s.close();
            }
        } catch (InterruptedException | IOException ignored) { }
    }
}
