import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class ServerFload implements Runnable {
    private Bootstrapper bs;
    private AddressingTable at;
    private String ip;
    private Map<Integer, String> movies;
    private PacketQueue queue;

    public ServerFload(Bootstrapper bs, AddressingTable at, String ip, Map<Integer, String> movies, PacketQueue queue) {
        this.bs = bs;
        this.at = at;
        this.ip = ip;
        this.movies = movies;
        this.queue = queue;
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

        Set<String> neighbours = at.getNeighbours();
        for (String n : neighbours) {
            System.out.println("Enviei fload");
            queue.add(new Packet(ip, n, 5, "1".getBytes(StandardCharsets.UTF_8)));
        }


        while(true) {
            try {
                Thread.sleep(20000);

                if(Ott.changed) {
                    System.out.println("Cheguei aqui 2");
                    Ott.changed = false;

                    neighbours = at.getNeighbours();
                    for (String n : neighbours)
                        queue.add( new Packet(ip, n, 13, null));

                    for (String n : neighbours)
                        queue.add(new Packet(ip, n, 5, "1".getBytes(StandardCharsets.UTF_8)));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
