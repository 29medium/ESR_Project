import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;


public class ServerStream extends JFrame implements Runnable, ActionListener {
    private JLabel label;
    private DatagramPacket senddp;
    private DatagramSocket RTPsocket;
    private int RTP_dest_port = 25000;
    private static String VideoFileName;

    private int imagenb = 0;
    private VideoStream video;
    private static int MJPEG_TYPE = 26;
    private static int FRAME_PERIOD = 100;
    private static int VIDEO_LENGTH = 500;

    private Timer sTimer;
    private byte[] sBuf;

    private AddressingTable at;
    private int streamID;

    public ServerStream(int streamID, String name, AddressingTable at) {
        VideoFileName = name;
        this.at = at;
        this.streamID = streamID;
    }

    public void run() {
        sTimer = new Timer(FRAME_PERIOD, this); //init Timer para servidor
        sTimer.setInitialDelay(0);
        sTimer.setCoalesce(true);
        sBuf = new byte[15000];

        try {
            RTPsocket = new DatagramSocket();
            video = new VideoStream(VideoFileName);

        } catch (Exception ignored) {}

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sTimer.stop();
                System.exit(0);
            }});

        label = new JLabel("Send frame #        ", JLabel.CENTER);
        getContentPane().add(label, BorderLayout.CENTER);

        sTimer.start();
    }

    public void actionPerformed(ActionEvent e) {

        if (imagenb < VIDEO_LENGTH)
        {
            imagenb++;

            try {
                int image_length = video.getnextframe(sBuf);

                RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb*FRAME_PERIOD, streamID, sBuf, image_length);

                int packet_length = rtp_packet.getlength();

                byte[] packet_bits = new byte[packet_length];
                rtp_packet.getpacket(packet_bits);

                Set<String> streamIPs = at.getStreamIPs(streamID);
                for(String ip : streamIPs) {
                    senddp = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(ip), RTP_dest_port);
                    RTPsocket.send(senddp);
                }

                rtp_packet.printheader();
            }
            catch(Exception ignored){}
        }
        else
        {
            sTimer.stop();
        }
    }
}
