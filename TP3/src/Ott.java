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
            //if(args.length == 2 && args[1].equals("-client")) {}

            ott(ip, ss, at, args[0]);
        }
    }

    public static void server(String ip, ServerSocket ss, AddressingTable at, Bootstrapper bs) {
        at.addAddress(new TreeSet<>(List.of(bs.get(ip).split(","))));

        Thread senderTCP = new Thread(new ServerSenderTCP(bs));
        Thread receiverTCP = new Thread(new ServerReceiverTCP(ss, bs));

        senderTCP.start();
        receiverTCP.start();
    }

    public static void ott(String ip, ServerSocket ss, AddressingTable at, String bootstrapperIP) {
        Thread senderTCP = new Thread(new OttSenderTCP(ip, bootstrapperIP, at));
        Thread receiverTCP = new Thread(new OttReceiverTCP());

        senderTCP.start();
        receiverTCP.start();
    }
}
