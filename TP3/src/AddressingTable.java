import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AddressingTable {
    private Map<String, AddressingCollumn> map;
    private ReentrantLock lock;

    static class AddressingCollumn {
        private String destination;
        private final String nextIP;

        public AddressingCollumn(String destination, String nextIP) {
            this.destination = destination;
            this.nextIP = nextIP;
        }

        public String getNextIP() {
            return nextIP;
        }
    }

    public AddressingTable() {
        this.map = new HashMap<>();
    }

    public String getNextIP(String destination) {
        lock.lock();
        try {
            return map.get(destination).getNextIP();
        } finally {
            lock.unlock();
        }
    }

    public void addAddress(String destination, String nextIP) {
        AddressingCollumn ac = new AddressingCollumn(destination, nextIP);
        lock.lock();
        try {
            map.put(destination, ac);
        } finally {
            lock.unlock();
        }
    }
}

