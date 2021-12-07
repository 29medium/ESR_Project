import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerSenderTCP implements Runnable{
    private Bootstrapper bs;

    public ServerSenderTCP(Bootstrapper bs) {
        this.bs = bs;
    }

    public void run() {
        try {
            while(true) {
                bs.full();

                // fload
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
