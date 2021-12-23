public class ServerSenderUDP implements Runnable{
    private final int streamID;
    private final String name;
    private final AddressingTable at;

    public ServerSenderUDP(int streamID, String name, AddressingTable at) {
        this.streamID = streamID;
        this.name = name;
        this.at = at;
    }

    public void run() {
        ServerStream.execute(streamID, name, at);
    }
}
