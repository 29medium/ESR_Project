import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class ServerReceiverUDP implements Runnable{
    private DatagramSocket ds;
    private PacketQueue queue;

    public ServerReceiverUDP(DatagramSocket ds, PacketQueue queue) {
        this.ds = ds;
        this.queue = queue;
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

                if(p.getType()==5) {
                    String message = new String(p.getData(), StandardCharsets.UTF_8);
                    System.out.println(message);
                    message = "Response " + message;

                    Packet newp = new Packet(p.getDestination(), p.getSource(), 6, message.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException ignored) {}
    }
}
