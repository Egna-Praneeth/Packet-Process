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

//Function to calculate average inter-arrival time between packets with same Destination IP and Destination Port
public class process_stringIP_Port {
    private static final int COUNT = 14261;

    private static final String PCAP_FILE_KEY = ReadPacketFile.class.getName() + ".pcapFile";
    private static final String PCAP_FILE = System.getProperty(PCAP_FILE_KEY, "src/main/resources/smallFlows.pcap");
    
    public static void main(String[] args) throws Exception {
        final long startTime = System.currentTimeMillis();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        //To have only one thread processing the packets
        env.setParallelism(1);
        
        // read packets from the pcap file, extract relevant information and convert them to a list
        List<CustomPair3> packetList = ReadPacketFile();
        
        DataStream<CustomPair3> dataStream = env.fromCollection(packetList);
        
        // Filtering out null packets and packets with no IP datagrams
        DataStream<CustomPair3> dataStream2 = dataStream.filter(new FilterFunction<CustomPair3>() {
            @Override
            public boolean filter(CustomPair3 pair) throws Exception {
                if (pair == null || pair.getPacket() == null) {
                    System.out.println("null packet");
                    return false;
                }
                Packet payload = pair.getPacket().getPayload();
                return payload instanceof IpV4Packet;
            }
        });
        
        // Processing the packets
        DataStream<String> outputstream = dataStream2
                .keyBy(CustomPair3::getIpAddressPort)
                .process(new CountTime3());
        
        // Saving the packets in a file
        final StreamingFileSink<String> sink = StreamingFileSink.<String>forRowFormat(new Path("src/main/resources/output3"), new SimpleStringEncoder<String>("UTF-8"))
                .withRollingPolicy(
                        DefaultRollingPolicy.builder()
//                                .withRolloverInterval(1000)
                                .build())
                .build();
        outputstream.addSink(sink);

        JobExecutionResult myJobExecutionResult = env.execute("Packet process");

        final long endTime = System.currentTimeMillis();
        System.out.println("Total Execution Time = " + (endTime - startTime));
    }

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
                else{
                    continue;
                }
                
                //    timestamp.getTime() returns time in milli seconds. Divide it by 1000 to get seconds int.
                //    Add it to nano object to get Nano precision. Time is in Nano second 
                long time = (timestamp.getTime()/1000)*1000000000 + timestamp.getNanos();
                
                CustomPair3 pair = new CustomPair3(time, packet,ipV4Packet.getHeader().getDstAddr().toString() + ":"+ port_string);
                packetList.add(pair);
                
            } catch (TimeoutException e) {
            } catch (EOFException e) {
//                System.out.println("EOF");
                break;
            }
        }
        handle.close();
        return packetList;
    }
}
