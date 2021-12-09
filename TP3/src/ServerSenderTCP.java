import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ServerSenderTCP implements Runnable{
    private Bootstrapper bs;
    private AddressingTable at;
    private String ip;

    public ServerSenderTCP(Bootstrapper bs, AddressingTable at, String ip) {
        this.bs = bs;
        this.at = at;
        this.ip = ip;
    }

    public void run() {
        try {
            while(true) {
                bs.full();

                Set<String> neighbours = at.getNeighbours();
                for(String n : neighbours) {
                    Packet p = new Packet(ip, n, 2, "0".getBytes(StandardCharsets.UTF_8));
                    Socket s = new Socket(n, 8080);

                    DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());

                    Packet.send(out, p);

                    if(p.getType()==2) {
                        Packet rp = Packet.receive(in);

                        if(rp.getType() == 3) {
                            at.addAddress(n);
                        }
                    }

                    // falta por pacote a aceitar null
                    in.close();
                    out.close();
                    s.close();
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
