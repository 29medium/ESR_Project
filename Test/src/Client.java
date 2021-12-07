import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost", 8080);

        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        DataOutputStream out = new DataOutputStream(s.getOutputStream());

        out.write("Hello".getBytes(StandardCharsets.UTF_8));
        out.flush();

        byte[] arr = new byte[4096];
        int size = in.read(arr);
        byte[] content = new byte[size];
        System.arraycopy(arr, 0, content, 0, size);
        System.out.println(new String(content, StandardCharsets.UTF_8));
    }
}
