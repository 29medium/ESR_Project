import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

public class StreamingReceiver implements Runnable{
    private DatagramSocket ds;
    private PacketQueue queue;
    private Set<String> neighbours;
    private AddressingTable at;

    public StreamingReceiver(DatagramSocket ds, PacketQueue queue, Set<String> neighbours, AddressingTable at) {
        this.ds = ds;
        this.queue = queue;
        this.neighbours = Set.copyOf(neighbours);
        this.at = at;
    }

    public void run() {
        try {
            while(true) {
                byte[] arr = new byte[4096];
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

                byte[] content = new byte[dp.getLength()];
                System.arraycopy(dp.getData(), 0, content, 0, dp.getLength());
                Packet p = new Packet(content);

                String nextIP = at.getNextIP(p.getDestination());

                byte[] packet = p.toBytes();
                DatagramPacket newdp = new DatagramPacket(packet, packet.length, InetAddress.getByName(nextIP), 8888);
                queue.add(newdp);
            }
        } catch (IOException ignored) {}
    }
}
