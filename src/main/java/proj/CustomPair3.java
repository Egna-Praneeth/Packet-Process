package proj;

import org.pcap4j.packet.Packet;

import java.net.Inet4Address;
import java.sql.Time;

public class CustomPair3 {
    Long Timestamp;
    Packet packet;
    String ipAddressPort;

    public CustomPair3(Long Timestamp, Packet packet, String ipAddressPort) {
        this.packet = packet;
        this.Timestamp = Timestamp;
        this.ipAddressPort = ipAddressPort;
    }

    public String getIpAddressPort() {
        return ipAddressPort;
    }

    public void setIpAddressPort(String ipAddressPort) {
        this.ipAddressPort = ipAddressPort;
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
