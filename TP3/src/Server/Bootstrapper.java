package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Bootstrapper {
    private Map<String, Set<String>> bootstrapper;

    public Bootstrapper(String path) throws FileNotFoundException {
        bootstrapper = new HashMap<>();

        File file = new File(path);
        Scanner s = new Scanner(file);

        String[] arr;
        while(s.hasNextLine()) {
            arr = s.nextLine().split(":");
            Set<String> neighbours = new TreeSet<>(List.of(arr[1].split(",")));
            bootstrapper.put(arr[0], neighbours);
        }
    }

    public String get(String name) {
        Set<String> neighbours = bootstrapper.get(name);
        Iterator<String> it = neighbours.iterator();
        StringBuilder res = new StringBuilder();

        while(it.hasNext()) {
            res.append(it.next());
            if(it.hasNext())
                res.append(",");
        }
        return res.toString();
    }
}
