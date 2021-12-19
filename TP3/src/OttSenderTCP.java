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
        try {
            Packet p = new Packet(ip, bootstrapperIP, 1, " ".getBytes(StandardCharsets.UTF_8));
            Socket s = new Socket(p.getDestination(), 8080);

            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            Packet.send(out, p);
            Packet rp = Packet.receive(in);

            in.close();
            out.close();
            s.close();

            if(rp.getType()==1) {
                String n = new String(rp.getData(), StandardCharsets.UTF_8);
                String[] args = n.split(" ");
                Set<String> neighbours = new TreeSet<>(List.of(args[1].split(",")));
                at.addNeighbours(neighbours);
                at.addStream(Integer.parseInt(args[0]));
            }

            while(true) {
                p = queue.remove();

                s = new Socket(p.getDestination(), 8080);
                out = new DataOutputStream(s.getOutputStream());
                in = new DataInputStream(new DataInputStream(s.getInputStream()));

                Packet.send(out, p);

                if(p.getType() == 2) {
                    rp = Packet.receive(in);

                    if(rp.getType() == 3)
                        at.addAddress(p.getSource());
                }

                in.close();
                out.close();
                s.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
