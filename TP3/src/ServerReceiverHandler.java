import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerReceiverHandler implements Runnable {
    private DataInputStream in;
    private DataOutputStream out;
    private Bootstrapper bs;
    private AddressingTable at;

    public ServerReceiverHandler(DataInputStream in, DataOutputStream out, Bootstrapper bs, AddressingTable at) {
        this.in = in;
        this.out = out;
        this.bs = bs;
        this.at = at;
    }

    public void run() {
        while(true) {
            try {
                Packet p = Packet.receive(in);

                if(p!=null){
                    if(p.getType() == 1) {
                        System.out.println("Pediu vizinhos");
                        String data = Ott.streams + " " + bs.get(p.getSource());
                        if (!Ott.isON) {
                            Packet.send(out, new Packet(p.getDestination(), p.getSource(), 2, data.getBytes(StandardCharsets.UTF_8)));
                        } else {
                            Packet.send(out, new Packet(p.getDestination(), p.getSource(), 3, data.getBytes(StandardCharsets.UTF_8)));
                            Ott.changed = true;
                        }
                    } else if (p.getType() == 6) {
                        System.out.println("Adicionei a tabela");
                        at.addAddress(p.getSource());
                    } else if(p.getType() == 11) {
                        int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                        System.out.println("Quer stream " + streamID);
                        at.setStatus(p.getSource(), true, streamID);
                    } else if(p.getType() == 12) {
                        int streamID = Integer.parseInt(new String(p.getData(), StandardCharsets.UTF_8));
                        System.out.println("Não quer stream " + streamID);
                        at.setStatus(p.getSource(), false, streamID);
                    } else if(p.getType() == 14) {
                        System.out.println("Houve mudanças no servidor");
                        Ott.changed = true;
                    } else if (p.getType() == 18) {
                        Socket s = new Socket(p.getSource(), 8080);
                        at.setDataOutputStream(p.getSource(), new DataOutputStream(s.getOutputStream()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
