import java.io.BufferedInputStream;
import java.io.DataInputStream;
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
                        Packet.receive(new DataInputStream(new BufferedInputStream(s.getInputStream())));

                        if (!at.getNeighbourState(n))
                            at.setNeighbours(n, true);
                        if (n.equals("10.0.3.1"))
                            System.out.println("Fodam se, sou mais fino que voces");
                        out.close();
                        s.close();
                    } catch (IOException | NegativeArraySizeException e) {
                        if(at.isSender(n)) {
                            Set<String> routes = at.getRoutes();
                            Set<String> nei = at.getNeighboursOn();
                            nei.remove(at.getSender());

                            for(String r : routes) {
                                nei.remove(r);
                                queue.add(new Packet(ip, r, 10, null));
                            }

                            String sender = at.getSender();
                            String senderSender = at.getSenderSender();

                            at.reset();

                            System.out.println(nei);

                            if(nei.isEmpty()) {
                                queue.add(new Packet(ip, senderSender, 4, null));
                                queue.add(new Packet(ip, senderSender, 14, null));
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
