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
            System.out.println("Entrei na thread");
            bs.full();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Ott.isON = true;

        System.out.println("Cheguei aqui");

        for(int i=1; i<=Ott.streams; i++) {
            Thread serverStream = new Thread(new ServerSenderUDP(i, movies.get(i), at));
            serverStream.start();
        }

        try {
            Set<String> neighbours = at.getNeighbours();
            for (String n : neighbours) {
                Socket s = new Socket(n, 8080);

                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = new Packet(ip, n, 5, "1".getBytes(StandardCharsets.UTF_8));
                Packet.send(out, p);

                in.close();
                out.close();
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                Thread.sleep(20000);

                /*
                if(Ott.changed) {
                    System.out.println("Cheguei aqui 2");
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

                        in.close();
                        out.close();
                        s.close();
                    }
                }
                
                 */
            } catch (InterruptedException /*| IOException*/ e) {
                e.printStackTrace();
            }
        }

    }
}
