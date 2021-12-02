import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            Socket socket;

            while(true) {
                socket = serverSocket.accept();

                Thread t = new Thread(new ClientHandler(socket));
                t.start();
            }
        } catch (IOException ignored) {}
    }
}
