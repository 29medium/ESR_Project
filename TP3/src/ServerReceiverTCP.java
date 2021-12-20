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
    private Boolean isON;
    private Boolean changed;

    public ServerReceiverTCP(ServerSocket ss, Bootstrapper bs, AddressingTable at, int nstreams, Boolean isON, Boolean changed) {
        this.ss = ss;
        this.bs = bs;
        this.at = at;
        this.nstreams = nstreams;
        this.isON = isON;
        this.changed = changed;
    }

    public void run() {
        try {
            while(true) {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.receive(in);

                if(p.getType() == 1) {
                    System.out.println("Ott " + p.getSource() + " pediu vizinhos");
                    String data = nstreams + " " + bs.get(p.getSource());
                    if(!isON) {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 2, data.getBytes(StandardCharsets.UTF_8)));
                    } else {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 3, data.getBytes(StandardCharsets.UTF_8)));
                    }
                } else if(p.getType() == 11) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                    at.setStatus(p.getSource(), true, streamID);
                } else if(p.getType() == 12) {
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
