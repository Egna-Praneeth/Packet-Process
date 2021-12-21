package proj;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.pcap4j.packet.Packet;

import java.net.Inet4Address;

public class CountCurrentTimewise3 extends KeyedProcessFunction<String,CustomPair3, String> {
    private ValueState<CountValues> state;

    @Override
    public void open(Configuration parameters) throws Exception{
        state = getRuntimeContext().getState(new ValueStateDescriptor<>("myState", CountValues.class));
    }

    @Override
    public void processElement(CustomPair3 element, Context ctx, Collector<String> out) throws Exception{
        CountValues current = state.value();
        element.setTimestamp(System.nanoTime());
        System.out.println(element.getTimestamp());
        if(current == null){
            current = new CountValues();
            current.lastTime = element.getTimestamp();
            current.sumTime = 0;
            current.pktCount = 1;
        }
        else {
            current.pktCount++;
            current.sumTime += element.getTimestamp() - current.lastTime;
            current.lastTime = element.getTimestamp();
        }
        state.update(current);
        if(current.pktCount != 1){
            String s = "pktcount: " + current.pktCount + "; IP: " + element.getIpAddressPort() + " FlowRate: " + (1000000000L) * current.pktCount / current.sumTime + " pkts/sec; Interarrival Time upto now is: " + current.sumTime / (current.pktCount - 1) + " ns";         
            out.collect(s);
        }
    }
}
