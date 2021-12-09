import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PacketQueue {
    private LinkedList<Packet> packets;
    private ReentrantLock lock;
    private Condition notEmpty;

    public PacketQueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    public void add(Packet packet) {
        lock.lock();
        try {
            packets.add(packet);

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public Packet remove() throws InterruptedException {
        lock.lock();
        try {
            while(packets.isEmpty())
                notEmpty.await();

            return packets.remove();
        } finally {
            lock.unlock();
        }
    }
}
