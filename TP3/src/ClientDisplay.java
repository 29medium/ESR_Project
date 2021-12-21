import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ClientDisplay implements Runnable {
    private JFrame f = new JFrame("Cliente");
    private JButton playButton = new JButton("Play");
    private JButton stopButton = new JButton("Stop");
    private JPanel mainPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private JLabel iconLabel = new JLabel();
    private ImageIcon icon;

    private Timer cTimer;
    private byte[] cBuf;

    private AddressingTable at;
    private RTPqueue queue;
    private PacketQueue queueTCP;
    private int streamID;
    private String ip;

    public ClientDisplay(AddressingTable at, RTPqueue queue, PacketQueue queueTCP, int streamID, String ip) {
        this.at = at;
        this.queue = queue;
        this.queueTCP = queueTCP;
        this.streamID = streamID;
        this.ip = ip;
    }

    public void run() {
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.setLayout(new GridLayout(1,0));
        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);

        playButton.addActionListener(new playButtonListener());
        stopButton.addActionListener(new stopButtonListener());

        iconLabel.setIcon(null);

        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0,0,380,280);
        buttonPanel.setBounds(0,280,380,50);

        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390,370));
        f.setVisible(true);

        cTimer = new Timer(20, new clientTimerListener());
        cTimer.setInitialDelay(0);
        cTimer.setCoalesce(true);
        cBuf = new byte[15000];
    }

    class playButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            //System.out.println("Play Button pressed !");
            cTimer.start();
        }
    }


    class stopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            //System.out.println("Teardown Button pressed !");
            cTimer.stop();

            f.dispose();

            at.setClientStream(false, streamID);
            if(!at.isStreaming(streamID)) {
                queueTCP.add(new Packet(ip, at.getSender(), 12, String.valueOf(streamID).getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    class clientTimerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try{
                RTPpacket rtp_packet = queue.remove();

                int payload_length = rtp_packet.getpayload_length();
                byte [] payload = new byte[payload_length];
                rtp_packet.getpayload(payload);

                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Image image = toolkit.createImage(payload, 0, payload_length);

                icon = new ImageIcon(image);
                iconLabel.setIcon(icon);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
