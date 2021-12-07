import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.TreeSet;

public class Ott {

    public static void main(String[] args) throws IOException {
        if(args.length != 2)
            return;

        String ip = InetAddress.getLocalHost().getHostAddress();
        String name = args[0];
        String bootstrapperIP = args[1];

        DatagramSocket ds = new DatagramSocket(8888);
        ServerSocket ss = new ServerSocket(8080);

        PacketQueue queueUDP = new PacketQueue();
        PacketQueue queueTCP = new PacketQueue();

        Set<String> neighbours = new TreeSet<>();
        AddressingTable at = new AddressingTable();

        // Teste
        at.addAddress("10.0.2.10", "10.0.2.10");
        at.addAddress("10.0.1.20", "10.0.1.20");

        Thread senderUDP = new Thread(new OttSenderUDP(ds, queueUDP, at));
        Thread receiverUDP = new Thread(new OttReceiverUDP(ds, queueUDP, neighbours, at));
        Thread senderTCP = new Thread(new OttSenderTCP(name, ip, bootstrapperIP, queueTCP));
        Thread receiverTCP = new Thread(new OttReceiverTCP(ss, queueTCP, neighbours));

        senderUDP.start();
        receiverUDP.start();
        senderTCP.start();
        receiverTCP.start();
    }
}
