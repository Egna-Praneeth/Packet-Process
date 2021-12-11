package utils;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import proj.ReadPacketFile;

import java.io.EOFException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class FlinkPcap {
    private static final int COUNT = 15;

    private static final String PCAP_FILE_KEY = ReadPacketFile.class.getName() + ".pcapFile";
    private static final String PCAP_FILE =
            System.getProperty(PCAP_FILE_KEY, "src/main/resources/fuzz-2006-07-12-21273.pcap");

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // readpacketsfrom file and convert them to a list
        List<Pair<Packet, Timestamp>> packetList = ReadPacketFile();
        // give list as input source
//        System.out.println(packetList[0]);
        DataStream<Pair<Packet, Timestamp>> dataStream = env.fromCollection(packetList);
        dataStream.filter(new FilterFunction<Pair<Packet, Timestamp>>() {
            @Override
            public boolean filter(Pair<Packet, Timestamp> pair) throws Exception {
                System.out.println("received");
                if (pair.getKey() == null) {
                    System.out.println("null packet");
                    return false;
                }
                Packet payload = pair.getKey().getPayload();
                System.out.println(payload.getClass());
                System.out.println(pair.getKey());
                return payload instanceof TcpPacket;
            }
        })
//                .map(new MapFunction<Packet, Packet>() {
//                    @Override
//                    public Packet map(Packet p) throws Exception {
//                        System.out.println(p);
//                        Packet payload = p.getPayload(); //tcp packet
//                        TcpPacket.TcpHeader header = (TcpPacket.TcpHeader) payload.getHeader();
//                        Packet tcpData = payload.getPayload(); // tcp payload
//                        return tcpData;
//                    }
//                })
//                .filter(new FilterFunction<Packet>() {
//                    @Override
//                    public boolean filter(Packet packet) throws Exception {
//                        System.out.println("received on length filter");
//                        return packet.getRawData().length > 0;
//                    }
//                })
//                .map(new MapFunction<Packet, String>() {
//
//                    @Override
//                    public String map(Packet packet) throws Exception {
//                        return new String(packet.getRawData());
//                    }
//                })
                .print();
                 
        try {
            env.execute("Packet process");
        }catch(Exception e){
            e.printStackTrace();
        }
        // Set as event time and analyze the interarrival time
        // add the interarrival time to a document
    }

    private static List<Pair<Packet, Timestamp>> ReadPacketFile() throws PcapNativeException, NotOpenException {
        PcapHandle handle;
//        List<Packet> packetList = new ArrayList<>();
        List<Pair<Packet, Timestamp>> packetList = new ArrayList<Pair<Packet, Timestamp>>();
        try {
            handle = Pcaps.openOffline(PCAP_FILE, TimestampPrecision.NANO);
        }catch(PcapNativeException e){
            handle = Pcaps.openOffline(PCAP_FILE);
        }
        for (int i = 0; i < COUNT; i++) {
            try {
                Packet packet = handle.getNextPacketEx();
                Timestamp timestamp = handle.getTimestamp();
                packetList.add(new MutablePair<Packet, Timestamp>(packet, handle.getTimestamp()));
                //System.out.println(handle.getTimestamp());
                //System.out.println(packet.getHeader());
                //System.out.println(packet);
            } catch (TimeoutException e) {
            } catch (EOFException e) {
                System.out.println("EOF");
                break;
            }
        }

        handle.close();
        return packetList;
    }

}
