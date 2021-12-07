package Ott;

import java.util.HashMap;
import java.util.Map;

public class AddressingTable {
    private Map<String, AddressingCollumn> map;

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
        return map.get(destination).getNextIP();
    }

    public void addAddress(String destination, String nextIP) {
        AddressingCollumn ac = new AddressingCollumn(destination, nextIP);
        map.put(destination, ac);
    }
}

