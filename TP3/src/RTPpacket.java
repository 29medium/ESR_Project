public class RTPpacket{
    static int HEADER_SIZE = 13;

    public int Version;
    public int Padding;
    public int Extension;
    public int CC;
    public int Marker;
    public int PayloadType;
    public int SequenceNumber;
    public int TimeStamp;
    public int Ssrc;
    public int StreamID;

    public byte[] header;

    public int payload_size;
    public byte[] payload;


    public RTPpacket(int PType, int Framenb, int Time, int ID, byte[] data, int data_length){
        Version = 2;
        Padding = 0;
        Extension = 0;
        CC = 0;
        Marker = 0;
        Ssrc = 0;
        SequenceNumber = Framenb;
        TimeStamp = Time;
        PayloadType = PType;
        StreamID = ID;
        header = new byte[HEADER_SIZE];
        header[0] = (byte)(Version << 6 | Padding << 5 | Extension << 4 | CC);
        header[1] = (byte)(Marker << 7 | PayloadType & 0x000000FF);
        header[2] = (byte)(SequenceNumber >> 8);
        header[3] = (byte)(SequenceNumber & 0xFF);
        header[4] = (byte)(TimeStamp >> 24);
        header[5] = (byte)(TimeStamp >> 16);
        header[6] = (byte)(TimeStamp >> 8);
        header[7] = (byte)(TimeStamp & 0xFF);
        header[8] = (byte)(Ssrc >> 24);
        header[9] = (byte)(Ssrc >> 16);
        header[10] = (byte)(Ssrc >> 8);
        header[11] = (byte)(Ssrc & 0xFF);
        header[12] = (byte)(StreamID);
        payload_size = data_length;
        payload = new byte[data_length];
        for (int i=0; i < data_length; i++)
            payload[i] = data[i];
    }

    public RTPpacket(byte[] packet, int packet_size)
    {
        Version = 2;
        Padding = 0;
        Extension = 0;
        CC = 0;
        Marker = 0;
        Ssrc = 0;
        StreamID = 0;

        if (packet_size >= HEADER_SIZE)
        {
            header = new byte[HEADER_SIZE];
            for (int i=0; i < HEADER_SIZE; i++)
                header[i] = packet[i];

            payload_size = packet_size - HEADER_SIZE;
            payload = new byte[payload_size];
            for (int i=HEADER_SIZE; i < packet_size; i++)
                payload[i-HEADER_SIZE] = packet[i];

            PayloadType = header[1] & 127;
            SequenceNumber = unsigned_int(header[3]) + 256*unsigned_int(header[2]);
            TimeStamp = unsigned_int(header[7]) + 256*unsigned_int(header[6]) + 65536*unsigned_int(header[5]) + 16777216*unsigned_int(header[4]);
            StreamID = (unsigned_int(header[12]));
        }
    }

    public int getpayload(byte[] data) {
        for (int i=0; i < payload_size; i++)
            data[i] = payload[i];

        return(payload_size);
    }

    public int getpayload_length() {
        return(payload_size);
    }

    public int getlength() {
        return(payload_size + HEADER_SIZE);
    }

    public int getpacket(byte[] packet)
    {
        for (int i=0; i < HEADER_SIZE; i++)
            packet[i] = header[i];
        for (int i=0; i < payload_size; i++)
            packet[i+HEADER_SIZE] = payload[i];

        return(payload_size + HEADER_SIZE);
    }


    public int gettimestamp() {
        return(TimeStamp);
    }

    public int getsequencenumber() {
        return(SequenceNumber);
    }

    public int getpayloadtype() {
        return(PayloadType);
    }


    public void printheader()
    {
        System.out.print("[RTP-Header] ");
        System.out.println("Stream" + StreamID
                + ", Version: " + Version
                + ", Padding: " + Padding
                + ", Extension: " + Extension
                + ", CC: " + CC
                + ", Marker: " + Marker
                + ", PayloadType: " + PayloadType
                + ", SequenceNumber: " + SequenceNumber
                + ", TimeStamp: " + TimeStamp);
    }

    static int unsigned_int(int nb) {
        if (nb >= 0)
            return(nb);
        else
            return(256+nb);
    }

}
