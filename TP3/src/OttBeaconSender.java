import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Set;

import static java.lang.Thread.sleep;

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

                Set<String> routes = at.getRoutes();
                for(String r : routes) {
                    try {
                        Socket s = new Socket(r, 8080);
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());
                        Packet.send(out, new Packet(ip, r, 18, null));
                        out.close();
                        s.close();
                    } catch (ConnectException e) {
                        Set<String> neighbours = at.getNeighbours();
                        neighbours.remove(r);

                        for(String n : routes) {
                            neighbours.remove(n);
                            queue.add(new Packet(ip, n, 10, null));
                        }

                        if(neighbours.isEmpty()) {
                            queue.add(new Packet(ip, at.getSenderSender(), 4, null));
                        } else {
                            for (String n : neighbours) {
                                queue.add(new Packet(ip, n, 4, null));
                            }
                        }

                        queue.add(new Packet(ip, at.getSender(), 14, null));
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
