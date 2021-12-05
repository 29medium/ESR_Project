import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.TreeSet;

public class Ott {

    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(8888);
        ServerSocket ss = new ServerSocket(8080);
        PacketQueue queue = new PacketQueue();
        Set<String> neighbours = new TreeSet<>();
        AddressingTable at = new AddressingTable();

        at.addAddress("10.0.2.10", "10.0.2.10");

        Thread streamSender = new Thread(new StreamingSender(ds, queue));
        Thread streamReceiver = new Thread(new StreamingReceiver(ds, queue, neighbours, at));

        streamSender.start();
        streamReceiver.start();
    }
}
