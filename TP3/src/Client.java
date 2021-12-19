import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Client implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private AddressingTable at;
    private PacketQueue queue;
    private String ip;
    private int streamID;

    public Client (AddressingTable at, PacketQueue queue, String ip) {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = new PrintWriter(System.out);
        this.at = at;
        this.queue = queue;
        this.ip = ip;
        this.streamID = 1;
    }

    public void run() {
        try {
            String line;
            boolean stream;

            while((line = in.readLine())!= null) {
                stream = at.isClientStream(streamID);
                if(line.equals("y") && !stream) {
                    if(!at.isStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 6, String.valueOf(streamID).getBytes(StandardCharsets.UTF_8)));
                    }
                    at.setClientStream(true, streamID);
                } else if(line.equals("n") && stream) {
                    at.setClientStream(false, streamID);
                    if(!at.isStreaming(streamID)) {
                        queue.add(new Packet(ip, at.getSender(), 7, String.valueOf(streamID).getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
