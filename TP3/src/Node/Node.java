package Node;


import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Node implements Runnable{
    private DatagramSocket ds;
    private PacketQueue queue;
    private Set<String> neighbours;

    public Node(String[] args) throws SocketException {
        this.ds = new DatagramSocket(8888);
        this.queue = new PacketQueue();
        this.neighbours = new TreeSet<>();
        for(String arg : args) {
            if(validIP(arg))
                neighbours.add(arg);
        }
    }

    public static boolean validIP(String arg) {
        if (arg == null || arg.isEmpty()) return false;
        arg = arg.trim();
        if ((arg.length() < 6) & (arg.length() > 15)) return false;

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(arg);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    public void run() {
        Thread streamSender = new Thread(new StreamingSender(ds, queue));
        Thread streamReceiver = new Thread(new StreamingReceiver(ds, queue, neighbours));

        streamSender.start();
        streamReceiver.start();
    }
}
