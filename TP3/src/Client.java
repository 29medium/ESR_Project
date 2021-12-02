import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;
            while((line = in.readLine()) != null) {
                out.println(line);
                out.flush();
            }

            in.close();
            out.close();
        } catch(IOException ignored) {}
    }
}
