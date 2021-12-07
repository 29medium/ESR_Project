import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

                byte[] arr = new byte[4096];
                int size = in.read(arr, 0, 4096);
                byte[] content = new byte[size];
                System.arraycopy(arr, 0, content, 0, size);

                System.out.println(Arrays.toString(content));

                Packet p = new Packet(content);

                if(p.getType() == 1) {
                    String neighbours = bs.get(p.getSource());

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
