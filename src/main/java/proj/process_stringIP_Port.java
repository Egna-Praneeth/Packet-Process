package proj;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.packet.namednumber.UdpPort;

import java.io.EOFException;
import java.net.Inet4Address;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class process_stringIP_Port {
    private static final int COUNT = 14261;

    private static final String PCAP_FILE_KEY = ReadPacketFile.class.getName() + ".pcapFile";
    //    private static final String PCAP_FILE =
//            System.getProperty(PCAP_FILE_KEY, "src/main/resources/fuzz-2006-07-12-21273.pcap");
    private static final String PCAP_FILE =
            System.getProperty(PCAP_FILE_KEY, "src/main/resources/smallFlows.pcap");
    public static void main(String[] args) throws Exception {
        final long startTime = System.currentTimeMillis();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // readpacketsfrom file and convert them to a list
        List<CustomPair3> packetList = ReadPacketFile();
        DataStream<CustomPair3> dataStream = env.fromCollection(packetList);
//        dataStream.map(new MapFunction<CustomPair2, String>() {
//            @Override
//            public String map(CustomPair2 pair) throws Exception{
//                return "In first print: " + pair.getTimestamp() + " " + pair.getIpAddress();
//            }
//        }).print();
        DataStream<CustomPair3> dataStream2 = dataStream.filter(new FilterFunction<CustomPair3>() {
            @Override
            public boolean filter(CustomPair3 pair) throws Exception {
//                System.out.println("received");
                if (pair == null || pair.getPacket() == null) {
                    System.out.println("null packet");
                    return false;
                }
                Packet payload = pair.getPacket().getPayload();
                return payload instanceof IpV4Packet;
            }
        });
//        DataStream<CustomPair3>  ipaddressStream = dataStream2.map(new MapFunction<MutablePair<Long, Packet>, CustomPair3>(){
//            @Override
//            public CustomPair3 map(MutablePair<Long, Packet> p) throws Exception {
//                Packet packet = p.getRight();
////                System.out.println("Step 2: In First map function");
//                IpV4Packet ipV4Packet = (IpV4Packet) packet.getPayload();
//                Inet4Address ipAddress = ipV4Packet.getHeader().getDstAddr();
//                System.out.println(ipAddress);
////                MutablePair<Inet4Address, MutablePair<Long, Packet>> element = new MutablePair<>();
////                element.setRight(p);
////                element.setLeft(ipAddress);
//                CustomPair3 element = new CustomPair3(p.getLeft(),p.getRight(),ipAddress);
////                System.out.println(element.getTimestamp());
////                System.out.println(element.getIpAddress());
//                return element;
//            }
//        });
        DataStream<String> outputstream = dataStream2.keyBy(CustomPair3::getIpAddressPort)
                .process(new CountTime3());

//        long ten = 10;
        final StreamingFileSink<String> sink = StreamingFileSink.<String>forRowFormat(new Path("src/main/resources/output3"), new SimpleStringEncoder<String>("UTF-8"))
                .withRollingPolicy(
                        DefaultRollingPolicy.builder()
//                                .withRolloverInterval(1000)
                                .build())
                .build();
        outputstream.addSink(sink);

        JobExecutionResult myJobExecutionResult = env.execute("Packet process");

//        int pktcount = myJobExecutionResult.getAccumulatorResult("packet-Counter");
//        System.out.println("THE NUMBER OF PACKETS IS: " + pktcount);
        // Set as event time and analyze the interarrival time
        // add the interarrival time to a document
        final long endTime = System.currentTimeMillis();
        System.out.println("Total Execution Time = " + (endTime - startTime));
    }

    //    private static final class IpPortKey{
//        Inet4Address IP;
//        TcpPort port;
//    }
    private static List<CustomPair3> ReadPacketFile() throws PcapNativeException, NotOpenException {
        PcapHandle handle;
        List<CustomPair3> packetList = new ArrayList<CustomPair3>();

        try {
            handle = Pcaps.openOffline(PCAP_FILE, TimestampPrecision.NANO);
        }catch(PcapNativeException e){
            handle = Pcaps.openOffline(PCAP_FILE);
        }

        for (int i = 1; i <= COUNT; i++) {
            try {
                Packet packet = handle.getNextPacketEx();
                Timestamp timestamp = handle.getTimestamp();
                /*
                    timestamp.getTime() returns time in milli seconds. Divide it by 1000 to get seconds int.
                    Add it to nano object to get Nano precision. Time is in Nano seconds
                */
                if(!(packet.getPayload() instanceof IpV4Packet))
                    continue;

                IpV4Packet ipV4Packet = (IpV4Packet) packet.getPayload();
                String port_string = "packet_other_than_tcp_or_udp";
                if(ipV4Packet.getPayload() instanceof TcpPacket) {
                    TcpPacket tcpPacket = (TcpPacket) ipV4Packet.getPayload();
                    TcpPort port = tcpPacket.getHeader().getDstPort();
                    port_string = port.valueAsString();
                }
                else if(ipV4Packet.getPayload() instanceof UdpPacket){
                    UdpPacket udpPacket = (UdpPacket) ipV4Packet.getPayload();
                    UdpPort port = udpPacket.getHeader().getDstPort();
                    port_string = port.valueAsString();
                }
                long time = (timestamp.getTime()/1000)*1000000000 + timestamp.getNanos();
                CustomPair3 pair = new CustomPair3(time, packet,ipV4Packet.getHeader().getDstAddr().toString() + ":"+ port_string);
//                System.out.println(port.valueAsString());
//                System.out.println(port.toString());
//                System.out.println(pair.getIpAddress());
                //                System.out.println(pair);
//                IpV4Packet ipV4Packet = (IpV4Packet) packet.getPayload();
//                System.out.println(ipV4Packet.getHeader().getSrcAddr());
                packetList.add(pair);
                //System.out.println(handle.getTimestamp());
                //System.out.println(packet.getHeader());
                //System.out.println(packet);
            } catch (TimeoutException e) {
            } catch (EOFException e) {
//                System.out.println("EOF");
                break;
            }
        }
//        MutablePair<Long, Packet> pair = new MutablePair<Long, Packet>(null, null);
        handle.close();
        return packetList;
    }
}
