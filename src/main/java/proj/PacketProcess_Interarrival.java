package proj;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.io.EOFException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class PacketProcess_Interarrival {
    private static final int COUNT = 100;

    private static final String PCAP_FILE_KEY = ReadPacketFile.class.getName() + ".pcapFile";
    private static final String PCAP_FILE =
            System.getProperty(PCAP_FILE_KEY, "src/main/resources/fuzz-2006-07-12-21273.pcap");

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // readpacketsfrom file and convert them to a list
        List<MutablePair<Long, Packet>> packetList = ReadPacketFile();
        // give list as input source
       // System.out.println(packetList.get(0));

//        Long prevPktTime = 0L;
//        Long curPktTime = 0L;
//        Long sumTimes = 0L;
//        int pktCount = 0;
        DataStream<MutablePair<Long, Packet>> dataStream = env.fromCollection(packetList);
        dataStream.filter(new FilterFunction<MutablePair<Long, Packet>>() {
            @Override
            public boolean filter(MutablePair<Long, Packet> pair) throws Exception {
                System.out.println("received");
                if (pair == null || pair.getValue() == null) {
                    System.out.println("null packet");
                    return false;
                }
                Packet payload = pair.getValue().getPayload();
                System.out.println(payload.getClass());
//                if(pair.getValue() != null) System.out.println(pair.getValue().getRawData());
                return payload instanceof IpV4Packet;
            }
        }).map(new ProcessElements());
//                .map(new MapFunction<MutablePair<Long, Packet>, MutablePair<Long, Packet>>(){
//            @Override
//            public MutablePair<Long, Packet> map(MutablePair<Long, Packet> p) throws Exception {
//                pktCount++;
//                curPktTime = p.getKey();
//                sumTimes = sumTimes + (curPktTime - prevPktTime);
//                prevPktTime = curPktTime;
//                System.out.println("Average Interarrival Time till now: " + sumTimes/pktCount);
//                System.out.println(p);
//                return p;
//            }
//        });
//        final StreamingFileSink<MutablePair<Long, Packet>> sink = StreamingFileSink.<MutablePair<Long, Packet>>forRowFormat(new Path("src/main/resources/output"), new SimpleStringEncoder<MutablePair<Long, Packet>>("UTF-8"))
//                .withRollingPolicy(DefaultRollingPolicy.builder().build()).build();
//        dataStream.addSink(sink);


        JobExecutionResult myJobExecutionResult = env.execute("Packet process");

        int pktcount = myJobExecutionResult.getAccumulatorResult("packet-Counter");
        System.out.println("THE NUMBER OF PACKETS IS: " + pktcount);
        // Set as event time and analyze the interarrival time
        // add the interarrival time to a document
    }

    private static final class ProcessElements extends RichMapFunction<MutablePair<Long, Packet>, MutablePair<Long, Packet>> {
        private IntCounter pktCount = new IntCounter();
        @Override
        public void open(Configuration parameters){
            getRuntimeContext().addAccumulator("packet-Counter",this.pktCount);
        }
        @Override
        public MutablePair<Long, Packet> map(MutablePair<Long, Packet> p) throws Exception {
//                pktCount++;
//                curPktTime = p.getKey();
//                sumTimes = sumTimes + (curPktTime - prevPktTime);
//                prevPktTime = curPktTime;
//                System.out.println("Average Interarrival Time till now: " + sumTimes/pktCount);
//            System.out.println(p);
            this.pktCount.add(1);
            return p;
        }

    }
    private static List<MutablePair<Long, Packet>> ReadPacketFile() throws PcapNativeException, NotOpenException {
        PcapHandle handle;
        List<MutablePair<Long, Packet>> packetList = new ArrayList<MutablePair<Long, Packet>>();

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

                long time = (timestamp.getTime()/1000)*1000000000 + timestamp.getNanos();
                MutablePair<Long, Packet> pair = new MutablePair<Long, Packet>(time, packet);
//                System.out.println(pair);

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
