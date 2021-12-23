import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

public class ClientStream implements Runnable{
    private final AddressingTable at;
    private final Map<Integer,RTPqueue> queueMap;

    public ClientStream(AddressingTable at, Map<Integer,RTPqueue> queueMap) {
        this.at = at;
        this.queueMap = queueMap;
    }

    public void run() {

        try {
            int RTP_RCV_PORT = 25000;
            DatagramSocket RTPsocket = new DatagramSocket(RTP_RCV_PORT);

            while(true) {
                byte[] cBuf = new byte[15000];
                DatagramPacket rcvdp = new DatagramPacket(cBuf, cBuf.length);

                RTPsocket.receive(rcvdp);

                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

                Set<String> streamIPs = at.getStreamIPs(rtp_packet.StreamID);
                for (String ip : streamIPs) {
                    int RTP_dest_port = 25000;
                    DatagramPacket senddp = new DatagramPacket(rcvdp.getData(), rcvdp.getData().length, InetAddress.getByName(ip), RTP_dest_port);
                    RTPsocket.send(senddp);
                }

                if (at.isClientStream(rtp_packet.StreamID)) {
                    queueMap.get(rtp_packet.StreamID).add(rtp_packet);
                }
            }
        }
        catch (InterruptedIOException iioe){
            System.out.println("Nothing to read");
        }
        catch (IOException ioe) {
            System.out.println("Exception caught: "+ioe);
        }
    }
}
