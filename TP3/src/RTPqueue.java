import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RTPqueue {
    private final LinkedList<RTPpacket> packets;
    private final ReentrantLock lock;
    private final Condition notEmpty;

    public RTPqueue() {
        packets = new LinkedList<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    public void add(RTPpacket packet) {
        lock.lock();
        try {
            packets.add(packet);

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public RTPpacket remove() throws InterruptedException {
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