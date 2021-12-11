package proj;

import java.io.EOFException;
import java.sql.Timestamp;
import java.util.concurrent.TimeoutException;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

public class ReadPacketFile {

    private static final int COUNT = 2000;

    private static final String PCAP_FILE_KEY = ReadPacketFile.class.getName() + ".pcapFile";
//    private static final String PCAP_FILE =
//            System.getProperty(PCAP_FILE_KEY, "/home/gucci/Documents/SOP/pcapFiles/smallFlows.pcap");
    private static final String PCAP_FILE =
            System.getProperty(PCAP_FILE_KEY, "src/main/resources/fuzz-2006-07-12-21273.pcap");
///home/gucci/Documents/SOP/pcapFiles
    // path to fuzz.pcap file: src/main/resources/fuzz-2006-07-12-21273.pcap
    private ReadPacketFile() {}

    public static void main(String[] args) throws PcapNativeException, NotOpenException {
        PcapHandle handle;
        try {
            handle = Pcaps.openOffline(PCAP_FILE, TimestampPrecision.NANO);
        } catch (PcapNativeException e) {
            handle = Pcaps.openOffline(PCAP_FILE);
        }
        Timestamp t1  = new Timestamp(0);
        Timestamp t2  = new Timestamp(0);

        for (int i = 0; i < COUNT; i++) {
            try {
                Packet packet = handle.getNextPacketEx();
                Timestamp timestamp = handle.getTimestamp();
                IpV4Packet ipV4Packet = (IpV4Packet) packet.getPayload();
                System.out.println(ipV4Packet.getHeader().getSrcAddr());
//                TcpPacket tcpPacket = (TcpPacket) ipV4Packet.getPayload();
//                TcpPort tcpPortDest= tcpPacket.getHeader().getDstPort();
//                System.out.println(tcpPortDest.)
                System.out.println(ipV4Packet.getHeader().getDstAddr());
                System.out.println(handle.getTimestamp());
//                System.out.println(handle.getTimestamp().getTime());
//                System.out.println(handle.getTimestamp().getNanos());
                long time = (timestamp.getTime()/1000)*1000000000 + timestamp.getNanos();
                System.out.println(time);
//                System.out.println("Timestamp precision is: " + handle.getTimestampPrecision());
                if( i == 1) {
                    t1 = handle.getTimestamp();
                }
                else if (i == 2){
                    t2 = handle.getTimestamp();
                }
               // //System.out.println(packet.getHeader());
//                System.out.println(packet);
            } catch (TimeoutException e) {
            } catch (EOFException e) {
                System.out.println("EOF");
                break;
            }
        }
//        System.out.println(t2.compareTo(t2));
//            System.out.println(t2.getTime() - t1.getTime());

        handle.close();
    }
}