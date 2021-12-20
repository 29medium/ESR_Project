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

            if(rp.getType()==2 || rp.getType() == 3) {
                String data = new String(rp.getData(), StandardCharsets.UTF_8);
                String[] args = data.split(" ");
                Set<String> neighbours = new TreeSet<>(List.of(args[1].split(",")));
                at = new AddressingTable(Integer.parseInt(args[0]));
                at.addNeighbours(neighbours);
                System.out.println("Recebeu vizinhos: " + args[0] + "\n");

                if(rp.getType() == 3) {
                    for(String n : neighbours) {
                        queue.add(new Packet(ip, n, 4, null));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Falha na conexão\n");
        }


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
                        at.addAddress(p.getSource());

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
