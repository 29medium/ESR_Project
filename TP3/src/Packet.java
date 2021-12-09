import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Packet {
    private String source;
    private String destination;
    private int type; // 1 - ask for neighbours (message and response) / 3 - tell neighbours / 4 - 3 response
    private byte[] data;

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
        byte[] arr = new byte[12 + this.data.length];
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

        return new Packet(content);
    }

    public static void send(DataOutputStream out, Packet p) throws IOException {
        out.write(p.toBytes());
        out.flush();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
