import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.TreeSet;

public class Ott {
    public static void main(String[] args) throws IOException {
        if(args.length > 3 || args.length < 1) {
            System.out.println("Wrong number of arguments");
            return;
        }

        String ip = InetAddress.getLocalHost().getHostAddress();
        ServerSocket ss = new ServerSocket(8080);
        AddressingTable at = new AddressingTable();

        if(args[0].equals("-server")) {
            server(ip, ss, at, new Bootstrapper("../files/bootstrapper"));
        } else {
            PacketQueue queueTCP = new PacketQueue();

            if(args[1].equals("-client")) {
                Thread client = new Thread(new Client(at, queueTCP, ip));

                client.start();
            }

            ott(ip, ss, at, args[0], queueTCP);
        }
    }

    public static void server(String ip, ServerSocket ss, AddressingTable at, Bootstrapper bs) {
        at.addNeighbours(new TreeSet<>(List.of(bs.get(ip).split(","))));

        Thread senderTCP = new Thread(new ServerSenderTCP(bs, at, ip));
        Thread receiverTCP = new Thread(new ServerReceiverTCP(ss, bs, at));

        senderTCP.start();
        receiverTCP.start();
    }

    public static void ott(String ip, ServerSocket ss, AddressingTable at, String bootstrapperIP, PacketQueue queueTCP) {
        Thread senderTCP = new Thread(new OttSenderTCP(ip, bootstrapperIP, at, queueTCP));
        Thread receiverTCP = new Thread(new OttReceiverTCP(ss, at, queueTCP, ip));

        senderTCP.start();
        receiverTCP.start();
    }
}
