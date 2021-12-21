import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

                Socket s = new Socket(p.getDestination(), 8080);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(new DataInputStream(s.getInputStream()));

                Packet.send(out, p);

                if (p.getType() == 5) {
                    Packet rp = Packet.receive(in);

                    if (rp.getType() == 6) {
                        at.addAddress(rp.getSource());

                        System.out.println("Adicionou nodo " + rp.getSource() + " à tabela de rotas\n");
                    }
                }

                in.close();
                out.close();
                s.close();
            } catch (IOException | InterruptedException e) {
                System.out.println("Falha na conexão\n");
            }
        }
    }
}
