package Ott;

import Packet.Packet;
import Packet.PacketQueue;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class OttReceiverTCP implements Runnable {
    private ServerSocket ss;
    private PacketQueue queue;
    private Set<String> neighbours;

    public OttReceiverTCP(ServerSocket ss, PacketQueue queue, Set<String> neighbours) {
        this.ss = ss;
        this.queue = queue;
        this.neighbours = neighbours;
    }

    public void run() {
        try {
            while(true) {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                Packet p = new Packet(in.readAllBytes());

                if(p.getType() == 2) {
                    String n = new String(p.getData(), StandardCharsets.UTF_8);
                    neighbours.addAll(List.of(n.split(",")));
                    System.out.println(neighbours);
                }

                in.close();
                s.close();
            }
        } catch (IOException ignored) {}
    }
}
