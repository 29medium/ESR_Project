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
import java.util.Set;

public class ClientStream implements Runnable {
    private JFrame f = new JFrame("Cliente de Testes");
    private JButton setupButton = new JButton("Setup");
    private JButton playButton = new JButton("Play");
    private JButton pauseButton = new JButton("Pause");
    private JButton tearButton = new JButton("Teardown");
    private JPanel mainPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private JLabel iconLabel = new JLabel();
    private ImageIcon icon;

    private DatagramPacket rcvdp;
    private DatagramPacket senddp;
    private DatagramSocket RTPsocket;
    private static int RTP_RCV_PORT = 25000;
    private int RTP_dest_port = 25000;

    private Timer cTimer;
    private byte[] cBuf;

    private AddressingTable at;

    public ClientStream(AddressingTable at) {
        this.at = at;
    }

    public void run() {
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.setLayout(new GridLayout(1,0));
        buttonPanel.add(setupButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(tearButton);

        playButton.addActionListener(new playButtonListener());
        tearButton.addActionListener(new tearButtonListener());

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

        try {
            RTPsocket = new DatagramSocket(RTP_RCV_PORT);
            RTPsocket.setSoTimeout(5000);
        } catch (SocketException e) {
            System.out.println("Cliente: erro no socket: " + e.getMessage());
        }
    }

    class playButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            System.out.println("Play Button pressed !");
            //start the timers ...
            cTimer.start();
        }
    }

    //Handler for tear button
    //-----------------------
    class tearButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            System.out.println("Teardown Button pressed !");
            //stop the timer
            cTimer.stop();
            //exit
            System.exit(0);
        }
    }

    class clientTimerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rcvdp = new DatagramPacket(cBuf, cBuf.length);

            try{
                RTPsocket.receive(rcvdp);

                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

                Set<String> streamIPs = at.getStreamIPs(rtp_packet.StreamID);
                for(String ip : streamIPs) {
                    senddp = new DatagramPacket(rcvdp.getData(), rcvdp.getData().length, InetAddress.getByName(ip), RTP_dest_port);
                    RTPsocket.send(senddp);
                }

                System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());

                rtp_packet.printheader();

                int payload_length = rtp_packet.getpayload_length();
                byte [] payload = new byte[payload_length];
                rtp_packet.getpayload(payload);

                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Image image = toolkit.createImage(payload, 0, payload_length);

                icon = new ImageIcon(image);
                iconLabel.setIcon(icon);
            }
            catch (InterruptedIOException iioe){
                System.out.println("Nothing to read");
            }
            catch (IOException ioe) {
                System.out.println("Exception caught: "+ioe);
            }
        }
    }
}
