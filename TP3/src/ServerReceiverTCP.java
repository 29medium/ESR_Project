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
    private AddressingTable at;
    private int nstreams;

    public ServerReceiverTCP(ServerSocket ss, Bootstrapper bs, AddressingTable at, int nstreams) {
        this.ss = ss;
        this.bs = bs;
        this.at = at;
        this.nstreams = nstreams;
    }

    public void run() {
        try {
            while(true) {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.receive(in);

                if(p.getType() == 1) {
                    String data = nstreams + " " + bs.get(p.getSource());
                    Packet.send(out, new Packet(p.getDestination(), p.getSource(), 1, data.getBytes(StandardCharsets.UTF_8)));
                } else if(p.getType() == 6) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                    at.setStatus(p.getSource(), true, streamID);
                } else if(p.getType() == 7) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                    at.setStatus(p.getSource(), false, streamID);
                }

                out.close();
                in.close();
                s.close();
            }
        } catch (IOException ignored) {}
    }
}
