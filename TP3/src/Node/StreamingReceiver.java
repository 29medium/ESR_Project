package Node;

import java.net.DatagramSocket;
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

    }
}
