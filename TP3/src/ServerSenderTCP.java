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
    private Boolean isON;
    private Boolean changed;

    public ServerSenderTCP(Bootstrapper bs, AddressingTable at, String ip, Boolean isON, Boolean changed) {
        this.bs = bs;
        this.at = at;
        this.ip = ip;
        this.isON = isON;
        this.changed = changed;
    }

    public void run() {
        try {
            bs.full();

            isON = true;

            while(true) {
                if(changed) {
                    changed = false;

                    Set<String> neighbours = at.getNeighbours();
                    for (String n : neighbours) {
                        Packet p = new Packet(ip, n, 5, "0".getBytes(StandardCharsets.UTF_8));
                        Socket s = new Socket(n, 8080);

                        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());

                        Packet.send(out, p);

                        System.out.println("Enviou novo caminho com 0 hops ao nodo " + n);

                        if (p.getType() == 5) {
                            Packet rp = Packet.receive(in);

                            if (rp.getType() ==6) {
                                at.addAddress(n);

                                System.out.println("Adicionou nodo " + rp.getSource() + " à tabela de rotas");
                            }
                        }

                        in.close();
                        out.close();
                        s.close();
                    }
                }
                Thread.sleep(20000);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
