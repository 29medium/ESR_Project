package Client;

import Packet.PacketQueue;
import Packet.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length!=2)
            return;

        String nodeIp = args[0];
        String serverIp = args[1];
        String ip = InetAddress.getLocalHost().getHostAddress();
        DatagramSocket ds = new DatagramSocket(8888);

        PacketQueue queue = new PacketQueue();

        // Teste
        queue.add(new Packet(ip, serverIp, 5, "Hello".getBytes(StandardCharsets.UTF_8)));

        Thread sender = new Thread(new ClientSenderUDP(ds, queue, nodeIp));
        Thread receiver = new Thread(new ClientReceiverUDP(ds, queue));

        sender.start();
        receiver.start();
    }
}
