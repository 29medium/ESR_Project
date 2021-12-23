import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;

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
                Set<String> neighbours = at.getNeighbours();
                for(String n : neighbours) {
                    try {
                        Socket s = new Socket(at.getSender(), 8080);
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());
                        Packet.send(out, new Packet(ip, at.getSender(), 18, null));
                        if (!at.getNeighbourState(n))
                            at.setNeighbours(n, true);
                        out.close();
                        s.close();
                    } catch (ConnectException e) {
                        if(at.isSender(n)) {
                            Set<String> routes = at.getRoutes();
                            Set<String> nei = at.getNeighboursOn();
                            nei.remove(at.getSender());

                            for(String r : routes) {
                                nei.remove(r);
                                queue.add(new Packet(ip, r, 10, null));
                            }

                            at.reset();

                            if(nei.isEmpty()) {
                                String senderSender = at.getSenderSender();
                                queue.add(new Packet(ip, senderSender, 4, null));
                                queue.add(new Packet(ip, senderSender, 14, null));
                            } else {
                                for (String rn : nei)
                                    queue.add(new Packet(ip, rn, 4, null));
                                queue.add(new Packet(ip, at.getSender(), 14, null));
                            }
                        } else if(at.isRoute(n)) {
                            at.removeAddress(n);
                        }

                        if(at.getNeighbourState(n))
                            at.setNeighbours(n, false);
                    }
                }
                Thread.sleep(200);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/*

 */
