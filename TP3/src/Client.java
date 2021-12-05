import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length!=1)
            return;

        String nodeIp = args[0];
        DatagramSocket ds = new DatagramSocket(8888);

        String message = "Hello";
        Packet p = new Packet("10.0.1.20", "10.0.2.10", message.getBytes(StandardCharsets.UTF_8));
        byte[] content = p.toBytes();
        DatagramPacket dp = new DatagramPacket(content, content.length, InetAddress.getByName(nodeIp), 8888);

        ds.send(dp);
    }
}
