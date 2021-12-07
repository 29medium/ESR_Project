import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class AddressingCollumn {
    private String ip;
    private int status; //  0 - not active, 1 - active

    public AddressingCollumn(String ip) {
        this.ip = ip;
        this.status = 0;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

public class AddressingTable {
    private Map<String, AddressingCollumn> map;

    public AddressingTable() {
        this.map = new HashMap<>();
    }

    public void addAddress(Set<String> neighbours) {
        for (String n : neighbours) {
            System.out.println(n);
            AddressingCollumn ac = new AddressingCollumn(n);
            map.put(n, ac);
        }
    }
}

