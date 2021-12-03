package Node;

import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class PacketQueue {
    private LinkedList<DatagramPacket> packets;
    public ReentrantLock lock;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
    }

    public void add(DatagramPacket packet) {
        lock.lock();
        try {
            packets.add(packet);
        } finally {
            lock.unlock();
        }
    }

    public DatagramPacket remove() {
        lock.lock();
        try {
            return packets.remove();
        } finally {
            lock.unlock();
        }
    }
}
