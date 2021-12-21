package proj;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class aggregate {
    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        String FILENAME = "src/main/resources/input.txt";
        int IP_INDEX = 3, PKT_COUNT_INDEX = 1, FLOW_RATE_INDEX = 5, INTER_ARRIVAL_INDEX = 12;

        try{
            FileInputStream fis = new FileInputStream(FILENAME);
            Scanner sc = new Scanner(fis);

            TreeMap<String, Tuple3> map = new TreeMap<String, Tuple3>();
            String key;
            int pktCount = 0;
            Long flowRate = 0L;
            Long interTime = 0L;


            while(sc.hasNextLine()){
                key = sc.nextLine();
                String[] arr = (key.split("\\s"));

                key = arr[IP_INDEX].substring(1);
                pktCount = Integer.parseInt(arr[PKT_COUNT_INDEX].substring(0, arr[PKT_COUNT_INDEX].length()-1));
                flowRate = Long.parseLong(arr[FLOW_RATE_INDEX]);
                interTime = Long.parseLong(arr[INTER_ARRIVAL_INDEX]);
                Tuple3<Integer, Long, Long> value = new Tuple3<Integer, Long, Long>(0, 0L, 0L);
                value.setFirst(pktCount);
                if(map.containsKey((key)))
                    value.setSecond(Math.max(flowRate, (long)map.get(key).getSecond()));
                else
                    value.setSecond(flowRate);
                value.setThird(interTime);
                map.put(key, value);
            }
            sc.close();

            for (Map.Entry<String, Tuple3> m: map.entrySet()){
                System.out.println("IP: " + m.getKey() + " -> Packet Count = " + m.getValue().getFirst() + "; Flow Rate = " + m.getValue().getSecond() + " pkts/sec; Average Inter-Arrival Time = " + m.getValue().getThird() + " ns");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        final long endTime = System.currentTimeMillis();
        System.out.println("Total Execution Time = " + (endTime - startTime));
    }
}
