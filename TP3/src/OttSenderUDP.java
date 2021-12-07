import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class OttSenderUDP implements Runnable{
    private DatagramSocket ds;
    private PacketQueue queue;
    private AddressingTable at;

    public OttSenderUDP(DatagramSocket ds, PacketQueue queue, AddressingTable at) {
        this.ds = ds;
        this.queue = queue;
        this.at = at;
    }

    public void run() {
        try {
            while (true) {
                Packet p = queue.remove();
                String nextIP = "";//at.getNextIP(p.getDestination());
                byte[] packet = p.toBytes();
                DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(nextIP), 8888);

                ds.send(dp);
            }
        } catch (InterruptedException | IOException ignored) {}
    }
}
