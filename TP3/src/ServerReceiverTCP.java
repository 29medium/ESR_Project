import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerReceiverTCP implements Runnable{
    private ServerSocket ss;
    private Bootstrapper bs;
    private AddressingTable at;

    public ServerReceiverTCP(ServerSocket ss, Bootstrapper bs, AddressingTable at) {
        this.ss = ss;
        this.bs = bs;
        this.at = at;
    }

    public void run() {
        while(true) {
            try {
                Socket s = ss.accept();
                Thread receiverHandler = new Thread(new ServerReceiverHandler(new DataInputStream(new BufferedInputStream(s.getInputStream())), new DataOutputStream(s.getOutputStream()), bs, at));
                receiverHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
