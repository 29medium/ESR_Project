import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class AddressingTable {
    private Map<Integer, AddressingCollumn> table;
    private Set<String> neighbours;
    private String sender;
    private int hops;
    private ReentrantLock lock;

    static class AddressingCollumn {
        private boolean clientStream;
        private Map<String, Boolean> collumn;

        AddressingCollumn() {
            this.clientStream = false;
            collumn = new HashMap<>();
        }

        public void addAddress(String ip) {
            collumn.put(ip, false);
        }

        public void removeAddress(String ip) {
            collumn.remove(ip);
        }

        public boolean isClientStream() {
            return clientStream;
        }

        public void setClientStream(boolean clientStream) {
            this.clientStream = clientStream;
        }

        private boolean isStatus(String ip) {
            return collumn.get(ip);
        }

        public boolean isStreaming() {
            return collumn.containsValue(true) || clientStream;
        }

        public void setStatus(String ip, boolean status) {
            collumn.put(ip, status);
        }

        public Set<String> getStreamIPs() {
            Set<String> res = new TreeSet<>();
            for(Map.Entry<String, Boolean> entry : collumn.entrySet())
                if(entry.getValue())
                    res.add(entry.getKey());
            return res;
        }

        public Set<String> getRoutes() {
            return collumn.keySet();
        }
    }

    public AddressingTable() {
        this.table = new HashMap<>();
        this.hops = Integer.MAX_VALUE;
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

    public void addStream(int streamID) {
        lock.lock();
        try {
            table.put(streamID, new AddressingCollumn());
        } finally {
            lock.unlock();
        }
    }

    public void addAddress(String ip) {
        lock.lock();
        try {
            for(AddressingCollumn ac : table.values())
                ac.addAddress(ip);
        } finally {
            lock.unlock();
        }
    }

    public void removeAddress(String ip) {
        lock.lock();
        try {
            for(AddressingCollumn ac : table.values())
                ac.removeAddress(ip);
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

    public boolean isClientStream(int streamID) {
        lock.lock();
        try {
            return table.get(streamID).isClientStream();
        } finally {
            lock.unlock();
        }
    }

    public void setClientStream(boolean clientStream, int streamID) {
        lock.lock();
        try {
            table.get(streamID).setClientStream(clientStream);
        } finally {
            lock.unlock();
        }
    }

    public boolean isStreaming(int streamID) {
        lock.lock();
        try {
            return table.get(streamID).isStreaming();
        } finally {
            lock.unlock();
        }
    }

    public void setStatus(String ip, boolean status, int streamID) {
        lock.lock();
        try {
            table.get(streamID).setStatus(ip, status);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getStreamIPs(int streamID) {
        lock.lock();
        try {
            return table.get(streamID).getStreamIPs();
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getRoutes() {
        lock.lock();
        try {
            return table.get(1).getRoutes();
        } finally {
            lock.unlock();
        }
    }

    public void reset() {
        lock.lock();
        try {
            this.hops = Integer.MAX_VALUE;
            this.sender = null;
        } finally {
            lock.unlock();
        }
    }
}

