public class ServerSenderUDP implements Runnable{
    private int streamID;
    private String name;
    private AddressingTable at;

    public ServerSenderUDP(int streamID, String name, AddressingTable at) {
        this.streamID = streamID;
        this.name = name;
        this.at = at;
    }

    public void run() {
        ServerStream.execute(streamID, name, at);
    }
}
