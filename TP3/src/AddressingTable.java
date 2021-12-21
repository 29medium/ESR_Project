import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class AddressingTable {
    private Map<String, Map<Integer, Boolean>> table;
    private Map<Integer, Boolean> isClientStream;
    private Set<String> neighbours;
    private String ip;
    private String sender;
    private int hops;
    private int numStreams;
    private ReentrantLock lock;

    public AddressingTable(int numStreams, String ip) {
        this.table = new HashMap<>();
        this.hops = Integer.MAX_VALUE;
        this.lock = new ReentrantLock();
        this.ip = ip;
        this.isClientStream = new HashMap<>();
        this.numStreams = numStreams;
        for(int i=1; i<=numStreams; i++)
            isClientStream.put(i, false);
    }

    public void addNeighbours(Set<String> neighbours) {
        lock.lock();
        try {
            this.neighbours = new TreeSet<>(neighbours);
        } finally {
            lock.unlock();
        }
    }

    public Map<Integer, Boolean> getIsClientStream() {
        lock.lock();
        try {
            return new HashMap<>(isClientStream);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getNeighbours() {
        lock.lock();
        try {
            return new TreeSet<>(neighbours);
        } finally {
            lock.unlock();
        }
    }

    public void addAddress(String ip) {
        lock.lock();
        try {
            Map<Integer, Boolean> map = new HashMap<>();

            for(int i=1; i<=numStreams; i++)
                map.put(i, false);

            table.put(ip, map);
        } finally {
            lock.unlock();
        }
    }

    public void removeAddress(String ip) {
        lock.lock();
        try {
            table.remove(ip);
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
            return isClientStream.get(streamID);
        } finally {
            lock.unlock();
        }
    }

    public void setClientStream(boolean clientStream, int streamID) {
        lock.lock();
        try {
            isClientStream.put(streamID, clientStream);
        } finally {
            lock.unlock();
        }
    }

    public boolean isStreaming(int streamID) {
        lock.lock();
        try {
            return table.values().stream().map(m -> m.get(streamID)).collect(Collectors.toSet()).contains(true) || isClientStream.get(streamID);
        } finally {
            lock.unlock();
        }
    }

    public void setStatus(String ip, boolean status, int streamID) {
        lock.lock();
        try {
            table.get(ip).put(streamID, status);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getStreamIPs(int streamID) {
        lock.lock();
        try {
            Set<String> stream = new TreeSet<>();

            for(Map.Entry<String, Map<Integer, Boolean>> m : table.entrySet())
                if(m.getValue().get(streamID))
                    stream.add(m.getKey());

            return stream;
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getRoutes() {
        lock.lock();
        try {
            return table.keySet();
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

    public void fullReset() {
        lock.lock();
        try {
            this.hops = Integer.MAX_VALUE;
            this.sender = null;
            this.table = new HashMap<>();
        } finally {
            lock.unlock();
        }
    }

    public int getNumStreams() {
        lock.lock();
        try {
            return numStreams;
        } finally {
            lock.unlock();
        }
    }

    public void ping() throws FileNotFoundException {
        lock.lock();
        try {
            String name = "../files/log-" + ip;

            File file = new File(name);
            if(file.exists())
                file.delete();

            FileOutputStream out = new FileOutputStream(name);

            StringBuilder content = new StringBuilder("ip: " + ip + "\n");

            content.append("\nNeighbours:\n");
            for(Map.Entry<Integer, Boolean> f : isClientStream.entrySet()) {
                content.append(" - Stream ").append(f.getKey()).append(" :").append(f.getValue()).append("\n");
            }

            for(Map.Entry<String, Map<Integer, Boolean>> e : table.entrySet()) {
                content.append("\n").append(e.getKey()).append(":\n");
                for(Map.Entry<Integer, Boolean> f : e.getValue().entrySet()) {
                    content.append(" - Stream ").append(f.getKey()).append(" :").append(f.getValue()).append("\n");
                }
                System.out.println("\n");
            }

            out.write(content.toString().getBytes(StandardCharsets.UTF_8));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.lock();
        }
    }
}