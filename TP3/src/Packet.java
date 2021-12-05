import java.net.InetAddress;
import java.net.UnknownHostException;

public class Packet {
    private String source;
    private String destination;
    private byte[] data;

    public Packet(String source, String destination, byte[] data) {
        this.source = source;
        this.destination = destination;
        this.data = data;
    }

    public Packet (byte[] content) throws UnknownHostException {
        byte[] aux = new byte[4];

        System.arraycopy(content, 0, aux, 0, 4);
        this.source = InetAddress.getByAddress(aux).getHostAddress();

        System.arraycopy(content, 4, aux, 0, 4);
        this.destination = InetAddress.getByAddress(aux).getHostAddress();

        byte[] newData = new byte[content.length - 8];
        System.arraycopy(content, 8, newData, 0, content.length-8);
        this.data = newData;
    }

    byte[] toBytes() throws UnknownHostException {

        byte[] arr = new byte[8 + this.data.length];

        byte[] source = InetAddress.getByName(this.source).getAddress();
        System.arraycopy(source,0,arr,0,4);
        byte[] destination = InetAddress.getByName(this.destination).getAddress();
        System.arraycopy(destination,0,arr,4,4);

        System.arraycopy(this.data,0,arr,8,this.data.length);

        return arr;
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
