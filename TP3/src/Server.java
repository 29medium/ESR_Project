import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(8888);

        byte[] arr = new byte[4096];
        DatagramPacket dp = new DatagramPacket(arr, arr.length);
        ds.receive(dp);

        byte[] content = new byte[dp.getLength()];
        System.arraycopy(dp.getData(), 0, content, 0, dp.getLength());
        Packet p = new Packet(content);

        System.out.println(new String(p.getData(), StandardCharsets.UTF_8));
    }
}
