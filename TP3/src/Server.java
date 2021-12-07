import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws IOException {
        if(args.length != 2)
            return;

        String nodeIp = args[0];

        DatagramSocket ds = new DatagramSocket(8888);
        ServerSocket ss = new ServerSocket(8080);

        PacketQueue queueUDP = new PacketQueue();
        PacketQueue queueTCP = new PacketQueue();

        Bootstrapper bs = new Bootstrapper(args[1]);

        Thread senderUDP = new Thread(new ServerSenderUDP(ds, queueUDP, nodeIp));
        Thread receiverUDP = new Thread(new ServerReceiverUDP(ds, queueUDP));
        Thread senderTCP = new Thread(new ServerSenderTCP(queueTCP));
        Thread receiverTCP = new Thread(new ServerReceiverTCP(ss, queueTCP, bs));

        senderUDP.start();
        receiverUDP.start();
        senderTCP.start();
        receiverTCP.start();
    }
}
