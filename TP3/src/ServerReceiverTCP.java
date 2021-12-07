import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerReceiverTCP implements Runnable{
    private ServerSocket ss;
    private Bootstrapper bs;

    public ServerReceiverTCP(ServerSocket ss, Bootstrapper bs) {
        this.ss = ss;
        this.bs = bs;
    }

    public void run() {
        try {
            while(true) {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = new Packet(in.readAllBytes());

                if(p.getType() == 1) {
                    String neighbours = bs.get(p.getSource());

                    System.out.println(neighbours);

                    Packet newp = new Packet(p.getDestination(), p.getSource(), 2, neighbours.getBytes(StandardCharsets.UTF_8));

                    out.write(newp.toBytes());
                    out.flush();
                }

                out.close();
                in.close();
                s.close();
            }
        } catch (IOException ignored) {}
    }
}
