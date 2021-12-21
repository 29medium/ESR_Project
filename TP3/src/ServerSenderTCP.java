import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class ServerSenderTCP implements Runnable{
    private Bootstrapper bs;
    private AddressingTable at;
    private String ip;
    private Map<Integer, String> movies;

    public ServerSenderTCP(Bootstrapper bs, AddressingTable at, String ip, Map<Integer, String> movies) {
        this.bs = bs;
        this.at = at;
        this.ip = ip;
        this.movies = movies;
    }

    public void run() {
        try {
            bs.full();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Ott.isON = true;

        for(int i=1; i<=Ott.streams; i++) {
            Thread serverStream = new Thread(new ServerSenderUDP(i, movies.get(i), at));
            serverStream.start();
        }

        while(true) {
            try {
                if(Ott.changed) {
                    Ott.changed = false;

                    Set<String> neighbours = at.getNeighbours();
                    for (String n : neighbours) {
                        Socket s = new Socket(n, 8080);

                        DataOutputStream out = new DataOutputStream(s.getOutputStream());
                        Packet.send(out, new Packet(ip, n, 13, null));
                    }

                    for (String n : neighbours) {
                        Socket s = new Socket(n, 8080);

                        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());

                        Packet p = new Packet(ip, n, 5, "1".getBytes(StandardCharsets.UTF_8));
                        Packet.send(out, p);

                        System.out.println("Enviou novo caminho com 1 hops ao nodo " + n + "\n");

                        if (p.getType() == 5) {
                            Packet rp = Packet.receive(in);

                            if (rp.getType() ==6) {
                                at.addAddress(n);

                                System.out.println("Adicionou nodo " + rp.getSource() + " à tabela de rotas\n");
                            }
                        }

                        in.close();
                        out.close();
                        s.close();
                    }
                }
                Thread.sleep(20000);

                System.out.println("Passaram 20 segundos\n");
            } catch (InterruptedException | IOException e) {
                System.out.println("Falha na conexão\n");
            }
        }

    }

    public void fload() {

    }
}
