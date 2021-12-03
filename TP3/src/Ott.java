import Node.Node;

import java.net.SocketException;

public class Ott {
    public static void main(String[] args) {
        if(args.length == 0)
            return;
        try {
            if(args[0].equals("server")) {

            } else if(args[0].equals("client")) {

            } else if(args[0].equals("node")) {
                Thread node = null;
                node = new Thread(new Node(args));
                node.start();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
