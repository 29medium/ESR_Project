import javax.swing.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;

public class OttStream implements Runnable {
    private DatagramPacket rcvdp;
    private DatagramPacket senddp;
    private DatagramSocket RTPsocket;
    private static int RTP_RCV_PORT = 25000;
    private int RTP_dest_port = 25000;

    private byte[] cBuf;

    private AddressingTable at;

    public OttStream(AddressingTable at) {
        this.at = at;
    }

    public void run() {

        try {
            RTPsocket = new DatagramSocket(RTP_RCV_PORT);
            //RTPsocket.setSoTimeout(5000);

            while(true) {
                cBuf = new byte[15000];
                rcvdp = new DatagramPacket(cBuf, cBuf.length);

                RTPsocket.receive(rcvdp);

                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

                //at.isOff();

                Set<String> streamIPs = at.getStreamIPs(rtp_packet.StreamID);
                for (String ip : streamIPs) {
                    senddp = new DatagramPacket(rcvdp.getData(), rcvdp.getData().length, InetAddress.getByName(ip), RTP_dest_port);
                    RTPsocket.send(senddp);
                }

                //System.out.println("Got RTP packet with SeqNum # " + rtp_packet.getsequencenumber() + " TimeStamp " + rtp_packet.gettimestamp() + " ms, of type " + rtp_packet.getpayloadtype());

                //rtp_packet.printheader();
            }
        }
        catch (InterruptedIOException iioe){
            System.out.println("Nothing to read");
        }
        catch (IOException ioe) {
            System.out.println("Exception caught: "+ioe);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        }
    }
}
