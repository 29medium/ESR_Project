import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Ott {

    public static void main(String[] args) throws IOException {
        if(args.length > 2 || args.length < 1) {
            System.out.println("Wrong number of arguments");
            return;
        }

        String ip = InetAddress.getLocalHost().getHostAddress();
        DatagramSocket ds = new DatagramSocket(8888);
        ServerSocket ss = new ServerSocket(8080);
        PacketQueue queue = new PacketQueue();
        Set<String> neighbours = new TreeSet<>();
        AddressingTable at = new AddressingTable();

        if(args[0].equals("-server")) {
            Bootstrapper bs = new Bootstrapper(args[1]);

            neighbours.addAll(List.of(bs.get(ip).split(",")));

            //Thread senderUDP = new Thread(new ServerSenderUDP(ds, queue, at));
            //Thread receiverUDP = new Thread(new ServerReceiverUDP(ds, queue));
            Thread senderTCP = new Thread(new ServerSenderTCP());
            Thread receiverTCP = new Thread(new ServerReceiverTCP(ss, bs));

            //senderUDP.start();
            //receiverUDP.start();
            senderTCP.start();
            receiverTCP.start();
        } else {
            if(args.length == 2 && args[1].equals("-client")) {
                // is client
            }

            String bootstrapperIP = args[0];

            //Thread senderUDP = new Thread(new OttSenderUDP(ds, queue, at));
            //Thread receiverUDP = new Thread(new OttReceiverUDP(ds, queue, neighbours, at));
            Thread senderTCP = new Thread(new OttSenderTCP(ip, bootstrapperIP, neighbours));
            Thread receiverTCP = new Thread(new OttReceiverTCP());

            //senderUDP.start();
            //receiverUDP.start();
            senderTCP.start();
            receiverTCP.start();
        }
    }
}
