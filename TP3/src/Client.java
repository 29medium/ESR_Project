import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Client implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private AddressingTable at;
    private PacketQueue queue;
    private String ip;

    public Client (AddressingTable at, PacketQueue queue, String ip) {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = new PrintWriter(System.out);
        this.at = at;
        this.queue = queue;
        this.ip = ip;
    }

    public void run() {
        try {
            String line;
            boolean stream;
            while((line = in.readLine())!= null) {
                stream = at.isClientStream();
                if(line.equals("y") && !stream) {
                    if(!at.isStreaming()) {
                        queue.add(new Packet(ip, at.getSender(), 6, null));
                    }
                    at.setClientStream(true);
                } else if(line.equals("n") && stream) {
                    at.setClientStream(false);
                    if(!at.isStreaming()) {
                        queue.add(new Packet(ip, at.getSender(), 7, null));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
