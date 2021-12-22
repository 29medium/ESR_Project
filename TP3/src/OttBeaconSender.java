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
                sleep(5000);

                Set<String> routes = at.getRoutes();
                for(String r : routes) {
                    Socket s = new Socket(r, 8080);
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());

                    try {
                        Packet.send(out, new Packet(ip, r, 18, null));
                    } catch (ConnectException e) {
                        System.out.println("1");
                        Set<String> neighbours = at.getNeighbours();
                        System.out.println("2");
                        System.out.println(neighbours);
                        neighbours.remove(r);
                        System.out.println(neighbours);
                        neighbours.removeAll(routes);
                        System.out.println(neighbours);

                        System.out.println(routes);
                        System.out.println(neighbours);

                        for(String n : routes) {
                            System.out.println("Mandei limpar rotas");
                            queue.add(new Packet(ip, n, 10, null));
                        }

                        if(neighbours.isEmpty()) {
                            System.out.println("Pedi fload ao sender");
                            queue.add(new Packet(ip, at.getSenderSender(), 4, null));
                        } else {
                            for (String n : neighbours) {
                                System.out.println("Pedi fload");
                                queue.add(new Packet(ip, n, 4, null));
                            }
                        }

                        queue.add(new Packet(ip, at.getSender(), 14, null));
                    }

                    out.close();
                    s.close();
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
