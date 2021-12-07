package Server;

import Packet.Packet;
import Packet.PacketQueue;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerReceiverTCP implements Runnable{
    private ServerSocket ss;
    private PacketQueue queue;
    private Bootstrapper bs;

    public ServerReceiverTCP(ServerSocket ss, PacketQueue queue, Bootstrapper bs) {
        this.ss = ss;
        this.queue = queue;
        this.bs = bs;
    }

    public void run() {
        try {
            while(true) {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                Packet p = new Packet(in.readAllBytes());

                if(p.getType() == 1) {
                    String neighbours = bs.get(new String(p.getData(), StandardCharsets.UTF_8));

                    Packet newp = new Packet(p.getDestination(), p.getSource(), 2, neighbours.getBytes(StandardCharsets.UTF_8));
                    queue.add(newp);
                }

                in.close();
                s.close();
            }
        } catch (IOException ignored) {}
    }
}
