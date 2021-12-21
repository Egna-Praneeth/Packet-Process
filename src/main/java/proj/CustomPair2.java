package proj;

import org.pcap4j.packet.Packet;

import java.net.Inet4Address;
import java.sql.Time;

public class CustomPair2 {
    Long Timestamp;
    Packet packet;
    String ipAddress;

    public CustomPair2(Long Timestamp, Packet packet, String ipAddress) {
        this.packet = packet;
        this.Timestamp = Timestamp;
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public Long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(Long timestamp) {
        Timestamp = timestamp;
    }
}
