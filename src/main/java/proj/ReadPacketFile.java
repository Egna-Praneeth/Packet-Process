package proj;

import java.io.EOFException;
import java.net.Inet4Address;
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

    private static final int COUNT = 100;

    private static final String PCAP_FILE_KEY = ReadPacketFile.class.getName() + ".pcapFile";
    private static final String PCAP_FILE =
            System.getProperty(PCAP_FILE_KEY, "/home/gucci/Documents/SOP/pcapFiles/smallFlows.pcap");
//    private static final String PCAP_FILE =
//            System.getProperty(PCAP_FILE_KEY, "src/main/resources/fuzz-2006-07-12-21273.pcap");
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
        int countip1 = 0, countip2 = 0, countip3 =0, countip4 = 0;
        String ip1 = null, ip2 = null, ip3 = null, ip4 = null;
        for (int i = 0; i < COUNT; i++) {
            try {

                Packet packet = handle.getNextPacketEx();
                Timestamp timestamp = handle.getTimestamp();
                IpV4Packet ipV4Packet = (IpV4Packet) packet.getPayload();
                System.out.println(ipV4Packet.getHeader().getSrcAddr());
//                TcpPacket tcpPacket = (TcpPacket) ipV4Packet.getPayload();
//                TcpPort tcpPortDest= tcpPacket.getHeader().getDstPort();
//                System.out.println(tcpPortDest.)
                Inet4Address ipAddress = ipV4Packet.getHeader().getDstAddr();
                System.out.println(ipAddress.toString());
//                if(ip1 == null) ip1 = ipAddress.toString() ;
//                else if(ip2 == null) ip2 = ipAddress.toString();
//                else if(ip3 == null) ip3 = ipAddress.toString();
//                else if(ip4 == null) ip4 = ipAddress.toString();
//
//                if(ipAddress.toString().equals(ip1))countip1++;
//                else if(ipAddress.toString().equals(ip2))countip2++;
//                else if(ipAddress.toString().equals(ip3))countip3++;
//                else if(ipAddress.toString().equals(ip4))countip4++;

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
//        System.out.println(" 1, 2, 3 are: " + countip1 + " " + countip2 + " " + countip3 + " " + countip4);
//        System.out.println(ip1 + " " + ip2 + " " +ip3+ " " + ip4);
//        System.out.println(t2.compareTo(t2));
//            System.out.println(t2.getTime() - t1.getTime());

        handle.close();
    }
}