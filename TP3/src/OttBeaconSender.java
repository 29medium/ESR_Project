import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Set;

public class OttBeaconSender implements Runnable {
    private AddressingTable at;
    private PacketQueue queue;
    private String ip;

    public OttBeaconSender(AddressingTable at, PacketQueue queue, String ip) {
        this.at = at;
        this.queue = queue;
        this.ip = ip;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(5000);

                try {
                    Socket s = new Socket(at.getSender(), 8080);
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());
                    Packet.send(out, new Packet(ip, at.getSender(), 18, null));
                    out.close();
                    s.close();
                } catch (ConnectException e) {
                    Set<String> routes = at.getRoutes();
                    Set<String> neighbours = at.getNeighbours();
                    neighbours.remove(at.getSender());

                    for(String n : routes) {
                        neighbours.remove(n);
                        queue.add(new Packet(ip, n, 10, null));
                    }

                    at.reset();

                    if(neighbours.isEmpty()) {
                        String senderSender = at.getSenderSender();
                        queue.add(new Packet(ip, senderSender, 4, null));
                        queue.add(new Packet(ip, senderSender, 14, null));
                    } else {
                        for (String n : neighbours)
                            queue.add(new Packet(ip, n, 4, null));
                        queue.add(new Packet(ip, at.getSender(), 14, null));
                    }
                }

                Set<String> routes = at.getRoutes();
                for(String r : routes) {
                    try {
                        Socket s = new Socket(r, 8080);
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());
                        Packet.send(out, new Packet(ip, r, 18, null));
                        out.close();
                        s.close();
                    } catch (ConnectException e) {
                        at.removeAddress(r);
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/*

 */
