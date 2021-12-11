package proj;

import org.pcap4j.packet.Packet;

import java.net.Inet4Address;
import java.sql.Time;

public class CustomPair {
    Long Timestamp;
    Packet packet;
    Inet4Address ipAddress;

    public CustomPair(Long Timestamp, Packet packet, Inet4Address ipAddress) {
        this.packet = packet;
        this.Timestamp = Timestamp;
        this.ipAddress = ipAddress;
    }

    public Inet4Address getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(Inet4Address ipAddress) {
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
