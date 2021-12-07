import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Thread.sleep;

public class OttSenderTCP implements Runnable {
    private String ip;
    private String bootstrapperIP;
    private AddressingTable at;

    public OttSenderTCP(String ip, String bootstrapperIP, AddressingTable at) {
        this.ip = ip;
        this.bootstrapperIP = bootstrapperIP;
        this.at = at;
    }

    public void run() {
        try {
            Packet np = new Packet(ip, bootstrapperIP, 1, " ".getBytes(StandardCharsets.UTF_8));
            Socket s = new Socket(np.getDestination(), 8080);

            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            out.write(np.toBytes());
            out.flush();

            Packet rp = new Packet(in.readAllBytes());
            if(rp.getType() == 2) {
                String n = new String(rp.getData(), StandardCharsets.UTF_8);
                at.addAddress(new TreeSet<>(List.of(n.split(","))));
            }

            System.out.println("Aqui");

            out.close();
            s.close();

            // Verificar se está vivo e rotas
        } catch (IOException ignored) { }
    }
}
