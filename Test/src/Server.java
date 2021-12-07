import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);

        while(true) {
            Socket s = ss.accept();

            DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            byte[] arr = new byte[4096];
            int size = in.read(arr);
            byte[] content = new byte[size];
            System.arraycopy(arr, 0, content, 0, size);

            String message = new String(content, StandardCharsets.UTF_8);
            message = "Response " + message;

            System.out.println("Recebi");

            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}
