import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Packet {
    private final String source;
    private final String destination;
    private final int type;
    private final byte[] data;

    public Packet(String source, String destination, int type, byte[] data) {
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.data = data;
    }

    public Packet(byte[] content) throws UnknownHostException {
        byte[] aux = new byte[4];
        int pos = 0;

        this.type = ByteBuffer.wrap(content,pos,4).getInt();
        pos += 4;

        System.arraycopy(content, pos, aux, 0, 4);
        this.source = InetAddress.getByAddress(aux).getHostAddress();
        pos += 4;

        System.arraycopy(content, pos, aux, 0, 4);
        this.destination = InetAddress.getByAddress(aux).getHostAddress();
        pos += 4;

        if(content.length == pos) {
            this.data = null;
        } else {
            byte[] newData = new byte[content.length - pos];
            System.arraycopy(content, pos, newData, 0, content.length - pos);
            this.data = newData;
        }
    }

    public byte[] toBytes() throws UnknownHostException {
        byte[] arr;

        if(this.data != null) {
            arr = new byte[12 + this.data.length];
        } else {
            arr = new byte[12];
        }

        int pos = 0;

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(type);
        System.arraycopy(byteBuffer.array(),0,arr,pos,4);
        pos += 4;

        byte[] source = InetAddress.getByName(this.source).getAddress();
        System.arraycopy(source,0,arr,pos,4);
        pos += 4;

        byte[] destination = InetAddress.getByName(this.destination).getAddress();
        System.arraycopy(destination,0,arr,pos,4);
        pos += 4;

        if(data != null)
            System.arraycopy(this.data,0,arr,pos,this.data.length);

        return arr;
    }

    public static Packet receive(DataInputStream in) throws IOException {
        byte[] arr = new byte[4096];
        int size = in.read(arr, 0, 4096);
        byte[] content = new byte[size];
        System.arraycopy(arr, 0, content, 0, size);

        Packet p = new Packet(content);
        p.printReceive();

        return p;
    }

    public static void send(DataOutputStream out, Packet p) throws IOException {
        p.printSent();
        out.write(p.toBytes());
        out.flush();
    }

    public int getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public byte[] getData() {
        return data;
    }

    public void printReceive() {
        if(type != 18) {
            String msg = "[RCVD] ";
            msg += "Type=" + type + " ";
            msg += "Source=" + source + " ";
            msg += "MSG=";

            switch (type) {
                case 1 -> msg += "Recebi pedido de vizinhos";
                case 2 -> msg += "Recebi vizinhos e server est?? off";
                case 3 -> msg += "Recebi vizinhos e server est?? on";
                case 4 -> msg += "Recebi pedido de fload";
                case 5 -> msg += "Recebi foad com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 6 -> msg += "Recebi confirma????o de caminho aceite";
                case 8 -> msg += "Recebi informa????o de caminho substituido";
                case 9 -> msg += "Recebi informa????o que o sender vai sair";
                case 13 -> msg += "Recebi informa????o para limpar as minhas rotas";
                case 11 -> msg += "Recebi pedido de stream " + new String(data, StandardCharsets.UTF_8);
                case 12 -> msg += "Recebi pedido de cancelamento de stream " + new String(data, StandardCharsets.UTF_8);
                case 14 -> msg += "Recebi pedido de informa????o ao servidor que o nodo vai sair";
                case 15 -> msg += "Recebi pedido de escrita para ficheiro de log";
                case 16 -> msg += "Recebi pedido de fload sem redirecionamento";
                case 17 -> msg += "Recebi fload sem redirecionamento com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 19 -> msg += "Recebi pedido para remover vizinhos tempor??rios";
                case 20 -> msg += "Recebi pedido para adicionar vizinhos tempor??rios";
                default -> {}
            }

            System.out.println(msg);
        }
    }

    public void printSent() {
        if(type != 18) {
            String msg = "[SENT] ";
            msg += "Type=" + type + " ";
            msg += "Destionation=" + destination + " ";
            msg += "MSG=";

            switch (type) {
                case 1 -> msg += "Enviei pedido de vizinhos";
                case 2 -> msg += "Enviei vizinhos e server est?? off";
                case 3 -> msg += "Enviei vizinhos e server est?? on";
                case 4 -> msg += "Enviei pedido de fload";
                case 5 -> msg += "Enviei foad com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 6 -> msg += "Enviei confirma????o de caminho aceite";
                case 8 -> msg += "Enviei informa????o de caminho substituido";
                case 9 -> msg += "Enviei informa????o que o sender vai sair";
                case 13 -> msg += "Enviei informa????o para limpar as minhas rotas";
                case 11 -> msg += "Enviei pedido de stream " + new String(data, StandardCharsets.UTF_8);
                case 12 -> msg += "Enviei pedido de cancelamento de stream " + new String(data, StandardCharsets.UTF_8);
                case 14 -> msg += "Enviei pedido de informa????o ao servidor que o nodo vai sair";
                case 15 -> msg += "Enviei pedido de escrita para ficheiro de log";
                case 16 -> msg += "Enviei pedido de fload sem redirecionamento";
                case 17 -> msg += "Enviei fload sem redirecionamento com " + new String(data, StandardCharsets.UTF_8).split(" ")[0] + "hops";
                case 19 -> msg += "Enviei pedido para remover vizinhos tempor??rios";
                case 20 -> msg += "Enviei pedido para adicionar vizinhos tempor??rios";
                default -> {
                }
            }

            System.out.println(msg);
        }
    }
}
