import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;

public class ServerStream extends JFrame implements ActionListener {
    private DatagramSocket RTPsocket;

    private int imagenb = 0;
    private VideoStream video;
    private static final int FRAME_PERIOD = 100;

    private final Timer sTimer;
    private final byte[] sBuf;

    private final AddressingTable at;
    private final int streamID;
    private final String videoFileName;

    public ServerStream(AddressingTable at, int streamID, String name) {
        super("Server");

        this.at = at;
        this.streamID = streamID;
        this.videoFileName = name;

        sTimer = new Timer(FRAME_PERIOD, this);
        sTimer.setInitialDelay(0);
        sTimer.setCoalesce(true);
        sBuf = new byte[15000];

        try {
            RTPsocket = new DatagramSocket();
            video = new VideoStream(videoFileName);

        } catch (Exception ignored) {}

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sTimer.stop();
                System.exit(0);
            }});

        JLabel label = new JLabel("Send frame #        ", JLabel.CENTER);
        getContentPane().add(label, BorderLayout.CENTER);

        sTimer.start();
    }

    public static void execute(int streamID, String name, AddressingTable at) {
        File f = new File(name);
        if (f.exists()) {
            new ServerStream(at, streamID, name);
        } else
            System.out.println("Ficheiro de video não existe: " + name);
    }

    public void actionPerformed(ActionEvent e) {

        int VIDEO_LENGTH = 500;
        if (imagenb < VIDEO_LENGTH)
        {
            imagenb++;

            try {
                int image_length = video.getnextframe(sBuf);

                int MJPEG_TYPE = 26;
                RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb*FRAME_PERIOD, streamID, sBuf, image_length);

                int packet_length = rtp_packet.getlength();

                byte[] packet_bits = new byte[packet_length];
                rtp_packet.getpacket(packet_bits);

                Set<String> streamIPs = at.getStreamIPs(streamID);
                for(String ip : streamIPs) {
                    int RTP_dest_port = 25000;
                    DatagramPacket senddp = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(ip), RTP_dest_port);
                    RTPsocket.send(senddp);
                }
            }
            catch(Exception exp){
                exp.printStackTrace();
            }
        }
        else
        {
            try {
                video = new VideoStream(videoFileName);
                imagenb = 0;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
