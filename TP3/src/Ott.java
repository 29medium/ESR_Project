import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

public class Ott {

    public static void main(String[] args) throws IOException {
        if(args.length == 1 && args[0].equals("-server")) {

        } else if (args.length == 1 && args[0].equals("-client")) {

        } else if (args.length != 0) {
            System.out.println("Wrong arguments");
            return;
        }

        DatagramSocket ds = new DatagramSocket(8888);
        ServerSocket ss = new ServerSocket(8080);
        PacketQueue queue = new PacketQueue();
        String ip = Inet4Address.getLocalHost().getHostAddress();
        Set<String> neighbours = new TreeSet<>();
        AddressingTable at = new AddressingTable();

        // preencher set com vizinhos
        byte arr[] = "Hello".getBytes(StandardCharsets.UTF_8);
        ds.send(new DatagramPacket(arr, arr.length, InetAddress.getByName("10.0.3.2"), 8888));

        Thread streamSender = new Thread(new StreamingSender(ds, queue));
        Thread streamReceiver = new Thread(new StreamingReceiver(ds, queue, neighbours));

        streamSender.start();
        streamReceiver.start();
    }
}
