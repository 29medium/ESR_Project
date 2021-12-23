import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

                if (p.getType() == 4) {
                    int hops = at.getHops() + 1;
                    String msg = hops + " " + at.getSender();
                    queue.add(new Packet(ip, p.getSource(), 5, msg.getBytes(StandardCharsets.UTF_8)));
                } else if (p.getType() == 5) {
                    String[] lines = new String(p.getData(), StandardCharsets.UTF_8).split(" ");
                    int hops = Integer.parseInt(lines[0]);
                    String senderSender = lines[1];

                    if (hops < at.getHops()) {
                        String sender = at.getSender();
                        at.setSender(p.getSource());

                        if (senderSender.equals("null"))
                            at.setSenderSender(null);
                        else
                            at.setSenderSender(senderSender);

                        at.setHops(hops);

                        Map<Integer, Boolean> isClientStream = at.getIsClientStream();

                        if (sender != null) {
                            for (Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                                if (e.getValue()) {
                                    queue.add(new Packet(p.getDestination(), sender, 12, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                                }
                            }
                            queue.add(new Packet(ip, sender, 8, null));
                        }

                        queue.add(new Packet(p.getDestination(), p.getSource(), 6, null));


                        for (Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                            if (e.getValue()) {
                                queue.add(new Packet(p.getDestination(), p.getSource(), 11, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                            }
                        }

                        Set<String> neighbours = at.getNeighboursOn();
                        hops++;

                        for (String n : neighbours)
                            if (!n.equals(p.getSource())) {
                                String msg = hops + " " + at.getSender();
                                queue.add(new Packet(ip, n, 5, msg.getBytes(StandardCharsets.UTF_8)));
                            }

                        Set<String> neighboursTemp = at.getNeighbourTemp();

                        for (String n : neighboursTemp)
                            if (!n.equals(p.getSource())) {
                                String msg = hops + " " + at.getSender();
                                queue.add(new Packet(ip, n, 5, msg.getBytes(StandardCharsets.UTF_8)));
                            }

                    }
                } else if (p.getType() == 6) {
                    at.addAddress(p.getSource());
                } else if (p.getType() == 8) {
                    at.removeAddress(p.getSource());
                } else if (p.getType() == 9) {
                    Set<String> routes = at.getRoutes();
                    Set<String> nei = at.getNeighboursOn();
                    nei.remove(at.getSender());

                    for (String r : routes) {
                        nei.remove(r);
                        queue.add(new Packet(ip, r, 10, null));
                    }

                    String sender = at.getSender();
                    String senderSender = at.getSenderSender();

                    at.reset();

                    if (nei.isEmpty()) {
                        queue.add(new Packet(ip, senderSender, 4, null));
                        queue.add(new Packet(ip, senderSender, 14, ip.getBytes(StandardCharsets.UTF_8)));
                    } else {
                        for (String rn : nei)
                            queue.add(new Packet(ip, rn, 4, null));
                        queue.add(new Packet(ip, sender, 14, null));
                    }
                } else if (p.getType() == 10) {
                    Set<String> routes = at.getRoutes();

                    for (String n : routes) {
                        queue.add(new Packet(ip, n, 10, null));
                    }

                    Set<String> neighboursTemp = at.getNeighbourTemp();

                    for (String n : neighboursTemp) {
                        queue.add(new Packet(ip, n, 13, null));
                    }

                    at.reset();
                } else if (p.getType() == 11) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if (at.isNotStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 11, p.getData()));
                    }

                    at.setStatus(p.getSource(), true, streamID);
                } else if (p.getType() == 12) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    at.setStatus(p.getSource(), false, streamID);
                    if (at.isNotStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 12, p.getData()));
                    }
                } else if (p.getType() == 13) {
                    Set<String> routes = at.getRoutes();

                    for (String n : routes) {
                        queue.add(new Packet(ip, n, 13, null));
                    }

                    Set<String> neighboursTemp = at.getNeighbourTemp();

                    for (String n : neighboursTemp) {
                        queue.add(new Packet(ip, n, 13, null));
                    }

                    at.reset();
                } else if (p.getType() == 14) {
                    if(p.getData()!=null)
                        at.addNeighbourTemp(new String(p.getData(), StandardCharsets.UTF_8));
                    queue.add(new Packet(ip, at.getSender(), 14, null));
                } else if (p.getType() == 15) {
                    at.ping();

                    Set<String> routes = at.getRoutes();
                    for (String r : routes) {
                        queue.add(new Packet(ip, r, 15, null));
                    }
                } else if (p.getType() == 16) {
                    int hops = at.getHops() + 1;
                    String msg = hops + " " + at.getSender();
                    queue.add(new Packet(ip, p.getSource(), 17, msg.getBytes(StandardCharsets.UTF_8)));
                    String temp = at.getNeighbourTempString();
                    if(temp!=null)
                        queue.add(new Packet(ip, p.getSource(), 19, temp.getBytes(StandardCharsets.UTF_8)));
                } else if (p.getType() == 17) {
                    String[] lines = new String(p.getData(), StandardCharsets.UTF_8).split(" ");
                    int hops = Integer.parseInt(lines[0]);
                    String senderSender = lines[1];

                    if (hops < at.getHops()) {
                        String sender = at.getSender();
                        at.setSender(p.getSource());
                        if (senderSender.equals("null"))
                            at.setSenderSender(null);
                        else
                            at.setSenderSender(senderSender);
                        at.setHops(hops);

                        if (sender != null) {
                            queue.add(new Packet(ip, sender, 8, null));
                        }

                        queue.add(new Packet(p.getDestination(), p.getSource(), 6, null));
                    }
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
                }

                in.close();
                out.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
