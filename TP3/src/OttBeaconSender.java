import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;

public class OttBeaconSender implements Runnable {
    private final AddressingTable at;
    private final PacketQueue queue;
    private final String ip;

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
                        Socket s = new Socket(n, 8080);
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());
                        Packet.send(out, new Packet(ip, n, 18, null));

                        if (!at.getNeighbourState(n))
                            at.setNeighbours(n, true);

                        out.close();
                        s.close();
                    } catch (IOException e) {
                        if(at.isSender(n)) {
                            Set<String> routes = at.getRoutes();
                            Set<String> nei = at.getNeighboursOn();
                            nei.remove(at.getSender());

                            for(String r : routes) {
                                nei.remove(r);
                                queue.add(new Packet(ip, r, 13, null));
                            }

                            String sender = at.getSender();
                            String senderSender = at.getSenderSender();

                            at.reset();
                            
                            if(nei.isEmpty()) {
                                queue.add(new Packet(ip, senderSender, 4, null));
                                queue.add(new Packet(ip, senderSender, 14, null));
                                queue.add(new Packet(ip, senderSender, 20, null));
                            } else {
                                for (String rn : nei)
                                    queue.add(new Packet(ip, rn, 4, null));
                                queue.add(new Packet(ip, sender, 14, null));
                            }
                        } else if(at.isRoute(n)) {
                            at.removeAddress(n);
                        }

                        if(at.getNeighbourState(n))
                            at.setNeighbours(n, false);
                    }
                }
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/*

 */
