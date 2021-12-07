package Ott;

import Packet.Packet;
import Packet.PacketQueue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class OttSenderTCP implements Runnable {
    private String name;
    private String ip;
    private String bootstrapperIP;
    private PacketQueue queue;

    public OttSenderTCP(String name, String ip, String bootstrapperIP, PacketQueue queue) {
        this.name = name;
        this.ip = ip;
        this.bootstrapperIP = bootstrapperIP;
        this.queue = queue;
    }

    public void run() {
        try {
            Packet np = new Packet(ip, bootstrapperIP, 1, name.getBytes(StandardCharsets.UTF_8));
            queue.add(np);

            while(true) {
                Packet p = queue.remove();

                Socket s = new Socket(p.getDestination(), 8080);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                out.write(p.toBytes());
                out.flush();

                out.close();
                s.close();
            }
        } catch (InterruptedException | IOException ignored) { }
    }
}
