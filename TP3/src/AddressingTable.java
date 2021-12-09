import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

class AddressingCollumn {
    private String ip;
    private boolean status;

    public AddressingCollumn(String ip) {
        this.ip = ip;
        this.status = false;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
}

public class AddressingTable {
    private Set<String> neighbours;
    private Map<String, AddressingCollumn> map;
    private String sender;
    private int hops;
    private boolean clientStream;
    private ReentrantLock lock;

    public AddressingTable() {
        this.map = new HashMap<>();
        this.hops = Integer.MAX_VALUE;
        this.clientStream = false;
        this.lock = new ReentrantLock();
    }

    public void addNeighbours(Set<String> neighbours) {
        lock.lock();
        try {
            this.neighbours = neighbours;
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getNeighbours() {
        lock.lock();
        try {
            return neighbours;
        } finally {
            lock.unlock();
        }
    }

    public void addAddress(String ip) {
        lock.lock();
        try {
            map.put(ip, new AddressingCollumn(ip));
        } finally {
            lock.unlock();
        }
    }

    public void removeAddress(String ip) {
        lock.lock();
        try {
            map.remove(ip);
        } finally {
            lock.unlock();
        }
    }

    public int getHops() {
        lock.lock();
        try {
            return hops;
        } finally {
            lock.unlock();
        }
    }

    public void setHops(int hops) {
        lock.lock();
        try {
            this.hops = hops;
        } finally {
            lock.unlock();
        }
    }

    public String getSender() {
        lock.lock();
        try {
            return sender;
        } finally {
            lock.unlock();
        }
    }

    public void setSender(String sender) {
        lock.lock();
        try {
            this.sender = sender;
        } finally {
            lock.unlock();
        }
    }

    public boolean isClientStream() {
        lock.lock();
        try {
            return clientStream;
        } finally {
            lock.unlock();
        }
    }

    public void setClientStream(boolean clientStream) {
        lock.lock();
        try {
            this.clientStream = clientStream;
        } finally {
            lock.unlock();
        }
    }

    public boolean isStreaming() {
        lock.lock();
        try {
            return map.values().stream().anyMatch(AddressingCollumn::isStatus) || clientStream;
        } finally {
            lock.unlock();
        }
    }

    public void setStatus(String ip, boolean status) {
        lock.lock();
        try {
            map.get(ip).setStatus(status);
        } finally {
            lock.unlock();
        }
    }
}

