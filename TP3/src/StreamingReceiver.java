import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

public class StreamingReceiver implements Runnable{
    private DatagramSocket ds;
    private PacketQueue queue;
    private Set<String> neighbours;

    public StreamingReceiver(DatagramSocket ds, PacketQueue queue, Set<String> neighbours) {
        this.ds = ds;
        this.queue = queue;
        this.neighbours = Set.copyOf(neighbours);
    }

    public void run() {
        try {
            while(true) {
                byte[] arr = new byte[4096];
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

                System.out.println(new String(arr, StandardCharsets.UTF_8));

                //tratar informação
                //DatagramPacket newdp = new DatagramPacket(arr, arr.length);

                //queue.add(newdp);
            }
        } catch (IOException ignored) {}
    }
}
