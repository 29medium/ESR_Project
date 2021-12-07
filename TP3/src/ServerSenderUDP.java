import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerSenderUDP implements Runnable{
    private DatagramSocket ds;
    private PacketQueue queue;
    private AddressingTable at;

    public ServerSenderUDP(DatagramSocket ds, PacketQueue queue, AddressingTable at) {
        this.ds = ds;
        this.queue = queue;
        this.at = at;
    }

    public void run() {
        //try {
            //while (true) {
                //Packet p = queue.remove();

                //byte[] packet = p.toBytes();
                //DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(at.getNextIP("0.0.0.0")), 8888);

                //ds.send(dp);
            //}
        //} catch (InterruptedException | IOException ignored) {}
    }
}
