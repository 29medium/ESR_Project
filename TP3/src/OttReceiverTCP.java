import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;import java.util.List;
import java.util.Set;

public class OttReceiverTCP implements Runnable {
    private ServerSocket ss;
    private AddressingTable at;
    private PacketQueue queue;
    private String ip;

    public OttReceiverTCP(ServerSocket ss, AddressingTable at, PacketQueue queue, String ip) {
        this.ss = ss;
        this.at = at;
        this.queue = queue;
        this.ip = ip;
    }

    public void run() {
        try {
            while(true) {
                Socket s = ss.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.receive(in);

                if(p.getType() == 2) {
                    int hops = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if (hops < at.getHops()) {
                        String sender = at.getSender();

                        if (sender != null)
                            queue.add(new Packet(ip, sender, 5, null));

                        at.setSender(p.getSource());
                        at.setHops(hops);

                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 3, null));

                        Set<String> neighbours = at.getNeighbours();
                        hops++;

                        for (String n : neighbours)
                            if (!n.equals(p.getSource()))
                                queue.add(new Packet(ip, n, 2, String.valueOf(hops).getBytes(StandardCharsets.UTF_8)));

                    } else {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 4, null));
                    }
                } else if(p.getType() == 5) {
                    at.removeAddress(p.getSource());
                } else if(p.getType() == 6) {
                    if(!at.isStreaming()) {
                        queue.add(new Packet(ip, at.getSender(), 6, null));
                    }
                    at.setStatus(p.getSource(), true);
                } else if(p.getType() == 7) {
                    at.setStatus(p.getSource(), false);
                    if(!at.isStreaming()) {
                        queue.add(new Packet(ip, at.getSender(), 7, null));
                    }
                }

                in.close();
                out.close();
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
