package Client;

import Packet.Packet;
import Packet.PacketQueue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientSenderUDP implements Runnable {
    private DatagramSocket ds;
    private PacketQueue queue;
    private String nodeIp;

    public ClientSenderUDP(DatagramSocket ds, PacketQueue queue, String nodeIp) {
        this.ds = ds;
        this.queue = queue;
        this.nodeIp = nodeIp;
    }

    public void run() {
        try {
            while (true) {
                Packet p = queue.remove();

                byte[] packet = p.toBytes();
                DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(nodeIp), 8888);

                ds.send(dp);
            }
        } catch (InterruptedException | IOException ignored) {}
    }
}
