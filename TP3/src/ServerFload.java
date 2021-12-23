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

        Set<String> neighbours = at.getNeighbours();
        for (String n : neighbours)
            queue.add(new Packet(ip, n, 5, "1 null".getBytes(StandardCharsets.UTF_8)));

        for(int i=1; i<=Ott.streams; i++) {
            Thread serverStream = new Thread(new ServerSenderUDP(i, movies.get(i), at, (i-1)*200));
            serverStream.start();
        }


        while(true) {
            try {
                Thread.sleep(20000);

                if(Ott.changed) {
                    Ott.changed = false;

                    neighbours = at.getNeighboursOn();
                    for (String n : neighbours) {
                        queue.add(new Packet(ip, n, 13, null));
                        queue.add(new Packet(ip, n, 5, "1 null".getBytes(StandardCharsets.UTF_8)));
                    }

                    Set<String> neighbours_temp = at.getNeighbourTemp();
                    for (String n : neighbours_temp) {
                        queue.add(new Packet(ip, n, 13, null));
                        queue.add(new Packet(ip, n, 5, "1 null".getBytes(StandardCharsets.UTF_8)));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
