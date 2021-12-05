import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PacketQueue {
    private LinkedList<DatagramPacket> packets;
    private ReentrantLock lock;
    private Condition notEmpty;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    public void add(DatagramPacket packet) {
        lock.lock();
        try {
            packets.add(packet);

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public DatagramPacket remove() throws InterruptedException {
        lock.lock();
        try {
            while(packets.isEmpty())
                notEmpty.await();

            return packets.remove();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return packets.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}
