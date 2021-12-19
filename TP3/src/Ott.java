import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

public class Ott {
    public static void main(String[] args) throws IOException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        ServerSocket ss = new ServerSocket(8080);
        AddressingTable at = new AddressingTable();

        if(args.length==1 && args[0].equals("-server")) {
            server(ip, ss, at, new Bootstrapper("../files/bootstrapper2"));
        } else if(args.length==2 && args[1].equals("-client")) {
            client(ip, ss, at, args[0], new PacketQueue());
        } else if(args.length==1) {
            ott(ip, ss, at, args[0], new PacketQueue());
        } else {
            System.out.println("Wrong number of arguments");
        }
    }

    public static void ott(String ip, ServerSocket ss, AddressingTable at, String bootstrapperIP, PacketQueue queueTCP) {
        Thread ottStream = new Thread(new OttStream(at));
        ottStream.start();

        Thread senderTCP = new Thread(new OttSenderTCP(ip, bootstrapperIP, at, queueTCP));
        Thread receiverTCP = new Thread(new OttReceiverTCP(ss, at, queueTCP, ip));

        senderTCP.start();
        receiverTCP.start();
    }

    public static void server(String ip, ServerSocket ss, AddressingTable at, Bootstrapper bs) throws FileNotFoundException {
        at.addNeighbours(new TreeSet<>(List.of(bs.get(ip).split(","))));

        File file = new File("../files/movies");
        Scanner s = new Scanner(file);
        int nstreams = 0;

        while(s.hasNextLine()) {
            String[] args = s.nextLine().split(" ");
            Thread serverStream = new Thread(new ServerStream(Integer.parseInt(args[0]), args[1], at));
            serverStream.start();
            nstreams++;
        }

        Thread senderTCP = new Thread(new ServerSenderTCP(bs, at, ip));
        Thread receiverTCP = new Thread(new ServerReceiverTCP(ss, bs, at, nstreams));

        senderTCP.start();
        receiverTCP.start();
    }

    public static void client(String ip, ServerSocket ss, AddressingTable at, String bootstrapperIP, PacketQueue queueTCP) {
        Thread clientStream = new Thread(new ClientStream(at));
        clientStream.start();

        Thread client = new Thread(new Client(at, queueTCP, ip));
        client.start();

        Thread senderTCP = new Thread(new OttSenderTCP(ip, bootstrapperIP, at, queueTCP));
        Thread receiverTCP = new Thread(new OttReceiverTCP(ss, at, queueTCP, ip));

        senderTCP.start();
        receiverTCP.start();
    }
}
