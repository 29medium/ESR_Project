import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Ott {
    public static boolean isON = false;
    public static boolean changed = false;

    public static void main(String[] args) throws IOException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        ServerSocket ss = new ServerSocket(8080);
        AddressingTable at = new AddressingTable();

        if(args.length==1 && args[0].equals("-server")) {
            server(ip, ss, at, new Bootstrapper("files/bootstrapper2"));
        } else if(args.length==2 && args[1].equals("-client")) {
            client(ip, ss, at, args[0], new PacketQueue(), new RTPqueue());
        } else if(args.length==1) {
            ott(ip, ss, at, args[0], new PacketQueue());
        } else {
            System.out.println("Wrong number of arguments");
        }
    }

    public static void ott(String ip, ServerSocket ss, AddressingTable at, String bootstrapperIP, PacketQueue queueTCP) throws IOException {
        //Thread ottStream = new Thread(new OttStream(at));
        //ottStream.start();

        Thread senderTCP = new Thread(new OttSenderTCP(ip, bootstrapperIP, at, queueTCP));
        Thread receiverTCP = new Thread(new OttReceiverTCP(ss, at, queueTCP, ip));

        senderTCP.start();
        receiverTCP.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while((line = in.readLine())!= null) {
            if(line.equals("exit")) {
                Set<String> neighbours = at.getRoutes();
                for(String n : neighbours) {
                    queueTCP.add(new Packet(ip, n, 9, at.getSender().getBytes(StandardCharsets.UTF_8)));
                }
                queueTCP.add(new Packet(ip, at.getSender(), 8, null));
            }
        }
    }

    public static void server(String ip, ServerSocket ss, AddressingTable at, Bootstrapper bs) throws FileNotFoundException {
        at.addNeighbours(new TreeSet<>(List.of(bs.get(ip).split(","))));

        File file = new File("files/movies");
        Scanner s = new Scanner(file);
        int nstreams = 0;

        while(s.hasNextLine()) {
            String[] args = s.nextLine().split(" ");
            //Thread serverStream = new Thread(new ServerStream(Integer.parseInt(args[0]), args[1], at));
            //serverStream.start();
            nstreams++;
        }

        Thread senderTCP = new Thread(new ServerSenderTCP(bs, at, ip));
        Thread receiverTCP = new Thread(new ServerReceiverTCP(ss, bs, at, nstreams));

        senderTCP.start();
        receiverTCP.start();
    }

    public static void client(String ip, ServerSocket ss, AddressingTable at, String bootstrapperIP, PacketQueue queueTCP, RTPqueue queueRTP) throws IOException {
        Thread senderTCP = new Thread(new OttSenderTCP(ip, bootstrapperIP, at, queueTCP));
        Thread receiverTCP = new Thread(new OttReceiverTCP(ss, at, queueTCP, ip));
        //Thread clientStream = new Thread(new ClientStream(at, queueRTP));

        senderTCP.start();
        receiverTCP.start();
        //clientStream.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        boolean stream;
        int streamID = 1;

        while((line = in.readLine())!= null) {
            stream = at.isClientStream(streamID);
            if(line.equals("y") && !stream) {
                if(!at.isStreaming(streamID)) {
                    queueTCP.add(new Packet(ip, at.getSender(), 11, String.valueOf(streamID).getBytes(StandardCharsets.UTF_8)));
                }
                at.setClientStream(true, streamID);

                Thread display = new Thread(new ClientDisplay(at, queueRTP));
                display.start();
            } else if(line.equals("n") && stream) {
                at.setClientStream(false, streamID);
                if(!at.isStreaming(streamID)) {
                    queueTCP.add(new Packet(ip, at.getSender(), 12, String.valueOf(streamID).getBytes(StandardCharsets.UTF_8)));
                }
            } else if(line.equals("exit")) {
                Set<String> neighbours = at.getRoutes();
                for(String n : neighbours) {
                    queueTCP.add(new Packet(ip, n, 9, at.getSender().getBytes(StandardCharsets.UTF_8)));
                }
                queueTCP.add(new Packet(ip, at.getSender(), 8, null));
            }
        }
    }
}
