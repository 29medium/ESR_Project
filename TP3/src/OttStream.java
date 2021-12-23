import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;

public class OttStream implements Runnable {
    private final AddressingTable at;

    public OttStream(AddressingTable at) {
        this.at = at;
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
