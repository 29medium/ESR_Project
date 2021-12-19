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

                        System.out.println("Aceitou novo caminho com " + hops + " hops do nodo " + p.getSource());

                        if (sender != null) {
                            queue.add(new Packet(ip, sender, 5, null));
                            System.out.println("Informou antigo caminho que encontrou nova alternativa");
                        }

                        at.setSender(p.getSource());
                        at.setHops(hops);

                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 3, null));

                        Set<String> neighbours = at.getNeighbours();
                        hops++;

                        for (String n : neighbours)
                            if (!n.equals(p.getSource())) {
                                queue.add(new Packet(ip, n, 2, String.valueOf(hops).getBytes(StandardCharsets.UTF_8)));

                                System.out.println("Enviou novo caminho com " + hops + " hops ao nodo " + n);
                            }

                    } else {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 4, null));

                        System.out.println("Rejeitou novo caminho com " + hops + " hops do nodo " + p.getSource());
                    }
                } else if(p.getType() == 5) {
                    at.removeAddress(p.getSource());

                    System.out.println("Caminho para o nodo " + p.getSource() + " removido");
                } else if(p.getType() == 6) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    System.out.println("Nodo " + p.getSource() + " quer receber stream " + streamID);

                    if(!at.isStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 6, p.getData()));

                        System.out.println("Informa caminho que quer receber stream " + streamID);
                    }
                    at.setStatus(p.getSource(), true, streamID);
                } else if(p.getType() == 7) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    System.out.println("Nodo " + p.getSource() + " não quer receber stream " + streamID);

                    at.setStatus(p.getSource(), false, streamID);
                    if(!at.isStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 7, p.getData()));

                        System.out.println("Informa caminho que não quer receber stream " + streamID);
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
