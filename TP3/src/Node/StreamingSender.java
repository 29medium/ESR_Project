package Node;
import java.net.DatagramSocket;

public class StreamingSender implements Runnable{
    private DatagramSocket ds;
    private PacketQueue queue;

    public StreamingSender(DatagramSocket ds, PacketQueue queue) {
        this.ds = ds;
        this.queue = queue;
    }

    public void run() {
        
    }
}
