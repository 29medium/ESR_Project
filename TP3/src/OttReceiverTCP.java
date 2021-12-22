import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
                    System.out.println("Pediu fload");
                    int hops = at.getHops() + 1;
                    System.out.println("Enviei hops");
                    queue.add(new Packet(ip, p.getSource(), 5, String.valueOf(hops).getBytes(StandardCharsets.UTF_8)));
                }
                else if(p.getType() == 5) {
                    System.out.println("Recebi caminho");
                    int hops = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if (hops < at.getHops()) {
                        String sender = at.getSender();

                        Map<Integer, Boolean> isClientStream = at.getIsClientStream();

                        if (sender != null) {
                            for(Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                                if (e.getValue()) {
                                    queue.add(new Packet(p.getDestination(), p.getSource(), 12, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                                    System.out.println("Avisei sender antigo que não quero stream " + e.getKey());
                                }
                            }
                            System.out.println("Avisei nodo antigo para me remover da tabela");
                            queue.add(new Packet(ip, sender, 8, null));
                        }

                        at.setSender(p.getSource());
                        at.setHops(hops);

                        System.out.println("Aceitei caminho");
                        queue.add(new Packet(p.getDestination(), p.getSource(), 6, null));


                        for(Map.Entry<Integer, Boolean> e : isClientStream.entrySet()) {
                            if(e.getValue()) {
                                queue.add(new Packet(p.getDestination(), p.getSource(), 11, String.valueOf(e.getKey()).getBytes(StandardCharsets.UTF_8)));
                                System.out.println("Avisei caminho que quero stream " + e.getKey());
                            }
                        }

                        Set<String> neighbours = at.getNeighbours();
                        hops++;

                        for (String n : neighbours)
                            if (!n.equals(p.getSource())) {
                                System.out.println("Enviei hops");
                                queue.add(new Packet(ip, n, 5, String.valueOf(hops).getBytes(StandardCharsets.UTF_8)));
                            }

                    }
                } else if (p.getType() == 6) {
                    System.out.println("Adicionei caminho a tabela");
                    at.addAddress(p.getSource());
                } else if(p.getType() == 8) {
                    System.out.println("Removi caminho a tabela");
                    at.removeAddress(p.getSource());
                } else if(p.getType() == 9) {
                    Set<String> routes = at.getRoutes();
                    Set<String> neighbours = at.getNeighbours();
                    System.out.println(neighbours);
                    neighbours.remove(p.getSource());
                    System.out.println(neighbours);
                    neighbours.removeAll(routes);
                    System.out.println(neighbours);

                    for(String n : routes) {
                        System.out.println("Mandei limpar rotas");
                        queue.add(new Packet(ip, n, 10, null));
                    }

                    at.reset();

                    if(neighbours.isEmpty()) {
                        System.out.println("Pedi fload ao sender");
                        queue.add(new Packet(ip, new String(p.getData(), StandardCharsets.UTF_8), 4, null));
                    } else {
                        for (String n : neighbours) {
                            System.out.println("Pedi fload");
                            queue.add(new Packet(ip, n, 4, null));
                        }
                    }

                    queue.add(new Packet(ip, at.getSender(), 14, null));
                } else if(p.getType() == 10) {
                    Set<String> routes = at.getRoutes();

                    for(String n : routes) {
                        System.out.println("Mandei limpar rotas");
                        queue.add(new Packet(ip, n, 10, null));
                    }

                    System.out.println("Limpei as minhas rotas");
                    at.reset();
                } else if(p.getType() == 11) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if(at.isNotStreaming(streamID)) {
                        System.out.println("Avisar que quero stream");
                        queue.add(new Packet(ip, at.getSender(), 11, p.getData()));
                    }

                    System.out.println("Quero stream");
                    at.setStatus(p.getSource(), true, streamID);
                } else if(p.getType() == 12) {
                    int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    System.out.println("Não quero stream");
                    at.setStatus(p.getSource(), false, streamID);
                    if(at.isNotStreaming(streamID)) {
                        System.out.println("Avisar que não quero stream");
                        queue.add(new Packet(ip, at.getSender(), 12, p.getData()));
                    }
                } else if(p.getType() == 13) {
                    Set<String> routes = at.getRoutes();

                    for(String n : routes) {
                        System.out.println("Mandar limpar full as rotas");
                        queue.add(new Packet(ip, n, 13, null));
                    }

                    System.out.println("Limpar full as rotas");
                    at.fullReset();
                } else if(p.getType() == 14) {
                    System.out.println("Mandar avisar o server que vai sair");
                    queue.add(new Packet(ip, at.getSender(), 14, null));
                } else if(p.getType() == 15) {
                    at.ping();
                    System.out.println("Escreve no ficheiro log");

                    Set<String> routes = at.getRoutes();
                    for (String r : routes) {
                        System.out.println("Manda escrever no ficheiro log");
                        queue.add(new Packet(ip, r, 15, null));
                    }
                } else if(p.getType() == 16) {
                    System.out.println("Pediu fload");
                    int hops = at.getHops() + 1;
                    queue.add(new Packet(ip, p.getSource(), 17, String.valueOf(hops).getBytes(StandardCharsets.UTF_8)));
                } else if(p.getType() == 17) {
                    System.out.println("Recebi caminho");
                    int hops = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));

                    if (hops < at.getHops()) {
                        String sender = at.getSender();

                        if (sender != null) {
                            System.out.println("Avisei nodo antigo para me remover da tabela");
                            queue.add(new Packet(ip, sender, 8, null));
                        }

                        at.setSender(p.getSource());
                        at.setHops(hops);

                        System.out.println("Aceitei caminho");
                        queue.add(new Packet(p.getDestination(), p.getSource(), 6, null));
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
