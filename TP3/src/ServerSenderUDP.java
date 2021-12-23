public class ServerSenderUDP implements Runnable{
    private int streamID;
    private String name;
    private AddressingTable at;
    private int wait;

    public ServerSenderUDP(int streamID, String name, AddressingTable at, int wait) {
        this.streamID = streamID;
        this.name = name;
        this.at = at;
        this.wait = wait;
    }

    public void run() {
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ServerStream.execute(streamID, name, at);
    }
}
