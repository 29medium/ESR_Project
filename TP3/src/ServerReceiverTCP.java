import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ServerReceiverTCP implements Runnable{
    private final ServerSocket ss;
    private final Bootstrapper bs;
    private final AddressingTable at;
    private final PacketQueue queue;

    public ServerReceiverTCP(ServerSocket ss, Bootstrapper bs, AddressingTable at, PacketQueue queue) {
        this.ss = ss;
        this.bs = bs;
        this.at = at;
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.receive(in);

                if(p.getType() == 1) {
                    String data = Ott.streams + " " + bs.get(p.getSource());
                    if (!Ott.isON) {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 2, data.getBytes(StandardCharsets.UTF_8)));
                    } else {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 3, data.getBytes(StandardCharsets.UTF_8)));
                        Ott.changed = true;
                    }

                } else if (p.getType() == 4) {
                    queue.add(new Packet(p.getDestination(), p.getSource(), 5, "1 null".getBytes(StandardCharsets.UTF_8)));
                } else if (p.getType() == 6) {
                    at.addAddress(p.getSource());
                } else if(p.getType() == 11) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                    at.setStatus(p.getSource(), true, streamID);
                } else if(p.getType() == 12) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                    at.setStatus(p.getSource(), false, streamID);
                } else if(p.getType() == 14) {
                    Ott.changed = true;
                } else if (p.getType() == 16) {
                    int hops = at.getHops() + 1;
                    String msg = hops + " " + at.getSender();
                    queue.add(new Packet(p.getDestination(), p.getSource(), 17, msg.getBytes(StandardCharsets.UTF_8)));
                } else if(p.getType() == 19) {
                    Set<String> neighbours_temp = at.getNeighbourTemp();
                    Set<String> nei = new TreeSet<>(List.of(new String(p.getData(), StandardCharsets.UTF_8).split(",")));

                    for(String n : neighbours_temp) {
                        for(String ne : nei) {
                            if(n.equals(ne)) {
                                at.removeNeighbourTemp(ne);
                            }
                        }
                    }
                } else if(p.getType() == 20) {
                    at.addNeighbourTemp(p.getSource());
                }

                out.close();
                in.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
