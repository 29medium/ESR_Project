import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class BootstrapperCollumn {
    private Set<String> neighbours;
    private boolean visited;

    BootstrapperCollumn(Set<String> neighbours) {
        this.neighbours = neighbours;
        this.visited = false;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Set<String> getNeighbours() {
        return neighbours;
    }
}

public class Bootstrapper {
    private Map<String, BootstrapperCollumn> bootstrapper;
    private ReentrantLock lock;
    private Condition full;

    public Bootstrapper(String path) throws FileNotFoundException {
        bootstrapper = new HashMap<>();
        lock = new ReentrantLock();
        full = lock.newCondition();

        File file = new File(path);
        Scanner s = new Scanner(file);

        String[] arr;
        while(s.hasNextLine()) {
            arr = s.nextLine().split(":");
            Set<String> neighbours = new TreeSet<>(List.of(arr[1].split(",")));
            bootstrapper.put(arr[0], new BootstrapperCollumn(neighbours));
        }
    }

    public String get(String name) {
        lock.lock();
        try {
            Set<String> neighbours = bootstrapper.get(name).getNeighbours();
            bootstrapper.get(name).setVisited(true);
            full.signal();
            Iterator<String> it = neighbours.iterator();
            StringBuilder res = new StringBuilder();

            while(it.hasNext()) {
                res.append(it.next());
                if(it.hasNext())
                    res.append(",");
            }
            return res.toString();
        } finally {
            lock.unlock();
        }

    }

    public void full() throws InterruptedException {
        lock.lock();
        try {
            while (count_visits() != bootstrapper.size()) {
                full.await();
            }
        } finally {
            lock.unlock();
        }
    }

    private int count_visits() {
        int count = 0;
        for(BootstrapperCollumn b : bootstrapper.values()) {
            if(b.isVisited())
                count++;
        }
        return count;
    }
}
