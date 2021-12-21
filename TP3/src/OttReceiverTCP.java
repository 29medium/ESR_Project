import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;import java.util.List;
import java.util.Map;
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
        while(true) {
            try {
                Socket s = ss.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.receive(in);

                if(p.getType() == 4) {
                    int hops = at.getHops() + 1;
                    queue.add(new Packet(ip, p.getSource(), 5, String.valueOf(hops).getBytes(StandardCharsets.UTF_8)));
                }
                else if(p.getType() == 5) {
                    int hops = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if (hops < at.getHops()) {
                        String sender = at.getSender();

                        Map<Integer, Boolean> isClientStream = at.getIsClientStream();

                        if (sender != null) {
                            for(Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                                if(e.getValue())
                                    queue.add(new Packet(p.getDestination(), p.getSource(), 12, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                            }

                            queue.add(new Packet(ip, sender, 8, null));
                        }

                        at.setSender(p.getSource());
                        at.setHops(hops);

                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 6, null));

                        for(Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                            if(e.getValue())
                                queue.add(new Packet(p.getDestination(), p.getSource(), 11, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                        }

                        Set<String> neighbours = at.getNeighbours();
                        hops++;

                        for (String n : neighbours)
                            if (!n.equals(p.getSource())) {
                                queue.add(new Packet(ip, n, 5, String.valueOf(hops).getBytes(StandardCharsets.UTF_8)));
                            }

                    } else {
                        Packet.send(out, new Packet(p.getDestination(), p.getSource(), 7, null));
                    }
                } else if(p.getType() == 8) {
                    at.removeAddress(p.getSource());
                } else if(p.getType() == 9) {
                    at.reset();

                    Set<String> routes = at.getRoutes();
                    Set<String> neighbours = at.getNeighbours();
                    System.out.println(neighbours);
                    neighbours.remove(p.getSource());
                    System.out.println(neighbours);
                    neighbours.removeAll(routes);
                    System.out.println(neighbours);

                    // Mandar limpar rotas
                    for(String n : routes)
                        queue.add(new Packet(ip, n, 10, null));

                    // Se não tiver vizinhos adiciona o sender do que saiu
                    if(neighbours.isEmpty())
                        queue.add(new Packet(ip, new String(p.getData(), StandardCharsets.UTF_8), 4, null));

                    // Se tiver vizinhos pede para enviar os caminhos
                    else {
                        for (String n : neighbours)
                            queue.add(new Packet(ip, n, 4, null));
                    }

                    queue.add(new Packet(ip, at.getSender(), 14, null));
                } else if(p.getType() == 10) {
                    Set<String> routes = at.getRoutes();

                    for(String n : routes)
                        queue.add(new Packet(ip, n, 10, null));

                    at.reset();
                } else if(p.getType() == 11) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if(!at.isStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 11, p.getData()));
                    }
                    at.setStatus(p.getSource(), true, streamID);
                } else if(p.getType() == 12) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    at.setStatus(p.getSource(), false, streamID);
                    if(!at.isStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 12, p.getData()));
                    }
                } else if(p.getType() == 13) {
                    Set<String> routes = at.getRoutes();

                    for(String n : routes)
                        queue.add(new Packet(ip, n, 13, null));

                    at.fullReset();
                } else if(p.getType() == 14) {
                    queue.add(new Packet(ip, at.getSender(), 14, null));
                } else if(p.getType() == 15) {
                    at.ping();

                    Set<String> routes = at.getRoutes();
                    for(String r : routes) {
                        queue.add(new Packet(ip, r, 15, null));
                    }
                }

                in.close();
                out.close();
                s.close();
            } catch (IOException e) {
                System.out.println("Falha na conexão\n");
            }
        }
    }
}
