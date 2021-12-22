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


    public ServerReceiverTCP(ServerSocket ss, Bootstrapper bs, AddressingTable at) {
        this.ss = ss;
        this.bs = bs;
        this.at = at;
    }

    public void run() {
        while(true) {
            try {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.receive(in);

                if(p.getType() == 1) {
                    String data = Ott.streams + " " + bs.get(p.getSource());
                    if (!Ott.isON) {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 2, data.getBytes(StandardCharsets.UTF_8)));
                    } else {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 3, data.getBytes(StandardCharsets.UTF_8)));
                        Ott.changed = true;
                    }
                } else if (p.getType() == 6) {
                        at.addAddress(p.getSource());
                } else if(p.getType() == 11) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                    at.setStatus(p.getSource(), true, streamID);
                } else if(p.getType() == 12) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                    at.setStatus(p.getSource(), false, streamID);
                } else if(p.getType() == 14) {
                    Ott.changed = true;
                }

                out.close();
                in.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
