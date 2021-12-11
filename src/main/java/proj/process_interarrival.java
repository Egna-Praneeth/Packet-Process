//package proj;
//// FUNCTION TO CALCULATE interarrival between packets with no key.
//
//import org.apache.commons.lang3.tuple.MutablePair;
//import org.apache.flink.api.common.JobExecutionResult;
//import org.apache.flink.api.common.accumulators.IntCounter;
//import org.apache.flink.api.common.functions.*;
//import org.apache.flink.api.common.serialization.SimpleStringEncoder;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.core.fs.Path;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
//import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
//import org.pcap4j.core.NotOpenException;
//import org.pcap4j.core.PcapHandle;
//import org.pcap4j.core.PcapHandle.TimestampPrecision;
//import org.pcap4j.core.PcapNativeException;
//import org.pcap4j.core.Pcaps;
//import org.pcap4j.packet.IpV4Packet;
//import org.pcap4j.packet.Packet;
//import org.pcap4j.packet.TcpPacket;
//import org.pcap4j.packet.namednumber.TcpPort;
//
//import java.io.EOFException;
//import java.net.Inet4Address;
//import java.sql.Timestamp;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeoutException;
//
//import org.apache.flink.api.common.state.ValueState;
//import org.apache.flink.api.common.state.ValueStateDescriptor;
//import org.apache.flink.api.java.tuple.Tuple;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
//import org.apache.flink.streaming.api.functions.KeyedProcessFunction.Context;
//import org.apache.flink.streaming.api.functions.KeyedProcessFunction.OnTimerContext;
//import org.apache.flink.util.Collector;
//
//import javax.sound.midi.SysexMessage;
//
//import static org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy.build;
//
//
//public class process_interarrival {
//    private static final int COUNT = 100;
//
//    private static final String PCAP_FILE_KEY = ReadPacketFile.class.getName() + ".pcapFile";
//    //    private static final String PCAP_FILE =
////            System.getProperty(PCAP_FILE_KEY, "src/main/resources/fuzz-2006-07-12-21273.pcap");
//    private static final String PCAP_FILE =
//            System.getProperty(PCAP_FILE_KEY, "/home/gucci/Documents/SOP/pcapFiles/smallFlows.pcap");
//
//    public static void main(String[] args) throws Exception {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);
//        // readpacketsfrom file and convert them to a list
//        List<MutablePair<Long, Packet>> packetList = ReadPacketFile();
//        // give list as input source
//
////        System.out.println(packetList.get(0));
////         IpV4Packet ipV4Packet = (IpV4Packet) packetList.get(0).getRight().getPayload();
////         System.out.println("dest ip address: " + ipV4Packet.getHeader().getDstAddr());
//        DataStream<MutablePair<Long, Packet>> dataStream = env.fromCollection(packetList);
//        DataStream<MutablePair<Long,Packet>> dataStream2 = dataStream.filter(new FilterFunction<MutablePair<Long, Packet>>() {
//            @Override
//            public boolean filter(MutablePair<Long, Packet> pair) throws Exception {
////                System.out.println("received");
//                if (pair == null || pair.getValue() == null) {
//                    System.out.println("null packet");
//                    return false;
//                }
//                Packet payload = pair.getRight().getPayload();
//
////                System.out.println(payload.getClass());
//                return payload instanceof IpV4Packet;
//            }
//        });
////        DataStream<CustomPair>  ipaddressStream = dataStream2.map(new MapFunction<MutablePair<Long, Packet>, CustomPair>(){
////            @Override
////            public CustomPair map(MutablePair<Long, Packet> p) throws Exception {
////                Packet packet = p.getRight();
////
////                CustomPair element = new CustomPair(p.getLeft(),p.getRight(),ipAddress);
//////                System.out.println(element.getTimestamp());
//////                System.out.println(element.getIpAddress());
////                return element;
////            }
////        });
//        DataStream<Tuple2<String, Long>> outputstream = dataStream2
//                .keyBy()
//                .process(new CountTime());
////                 .print();
////        long ten = 10;
//        final StreamingFileSink<Tuple2<String, Long>> sink = StreamingFileSink.<Tuple2<String, Long>>forRowFormat(new Path("src/main/resources/output"), new SimpleStringEncoder<Tuple2<String,Long>>("UTF-8"))
//                .withRollingPolicy(
//                        DefaultRollingPolicy.builder()
//                                .withRolloverInterval(10)
//                                .build())
//                .build();
//        outputstream.addSink(sink);
//
//        JobExecutionResult myJobExecutionResult = env.execute("Packet process");
//
////        int pktcount = myJobExecutionResult.getAccumulatorResult("packet-Counter");
////        System.out.println("THE NUMBER OF PACKETS IS: " + pktcount);
//        // Set as event time and analyze the interarrival time
//        // add the interarrival time to a document
//    }
//
//    private static final class IpPortKey{
//        Inet4Address IP;
//        TcpPort port;
//    }
//    private static List<MutablePair<Long, Packet>> ReadPacketFile() throws PcapNativeException, NotOpenException {
//        PcapHandle handle;
//        List<MutablePair<Long, Packet>> packetList = new ArrayList<MutablePair<Long, Packet>>();
//
//        try {
//            handle = Pcaps.openOffline(PCAP_FILE, TimestampPrecision.NANO);
//        }catch(PcapNativeException e){
//            handle = Pcaps.openOffline(PCAP_FILE);
//        }
//
//        for (int i = 1; i <= COUNT; i++) {
//            try {
//                Packet packet = handle.getNextPacketEx();
//                Timestamp timestamp = handle.getTimestamp();
//                /*
//                    timestamp.getTime() returns time in milli seconds. Divide it by 1000 to get seconds int.
//                    Add it to nano object to get Nano precision. Time is in Nano seconds
//                */
//
//                long time = (timestamp.getTime()/1000)*1000000000 + timestamp.getNanos();
//                MutablePair<Long, Packet> pair = new MutablePair<Long, Packet>(time, packet);
////                System.out.println(pair);
//
//                packetList.add(pair);
//                //System.out.println(handle.getTimestamp());
//                //System.out.println(packet.getHeader());
//                //System.out.println(packet);
//            } catch (TimeoutException e) {
//            } catch (EOFException e) {
////                System.out.println("EOF");
//                break;
//            }
//        }
////        MutablePair<Long, Packet> pair = new MutablePair<Long, Packet>(null, null);
//        handle.close();
//        return packetList;
//    }
//}
