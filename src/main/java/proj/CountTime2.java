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

public class CountTime2 extends KeyedProcessFunction<String,CustomPair2, String> {
    private ValueState<CountValues> state;

    @Override
    public void open(Configuration parameters) throws Exception{
        state = getRuntimeContext().getState(new ValueStateDescriptor<>("myState", CountValues.class));
    }

    @Override
    public void processElement(CustomPair2 element, Context ctx, Collector<String> out) throws Exception{
//        System.out.println("Step 4: In process Element function");
//        System.out.println(element.getTimestamp());
//        System.out.println(element.getIpAddress());
        CountValues current = state.value();
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
//        System.out.println(current.pktCount + " " + current.sumTime + " "+ current.lastTime);
        state.update(current);
        if(current.pktCount != 1){
            String s = "pktcount: " + current.pktCount + "; IP: " + element.getIpAddress() + " FlowRate: " + (1000000000L) * current.pktCount / current.sumTime + " pkts/sec; Interarrival Time upto now is: " + current.sumTime / (current.pktCount - 1) + " ns";

            //        if(current.pktCount != 1)out.collect(new Tuple2<String, Long>(s, current.sumTime/(current.pktCount - 1)));
            out.collect(s);
        }


//            ctx.timerService().registerEventTimeTimer(ctx.timestamp() + );
    }
}
