import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

class Tuple3<K, V, T> {

    private K first;
    private V second;
    private T third;

    public Tuple3(K first, V second, T third){
        this.first = first;
        this.second = second;
        this.third = third;
    }

    // getters and setters

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    public T getThird() {
        return third;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public void setSecond(V second) {
        this.second = second;
    }

    public void setThird(T third) {
        this.third = third;
    }
}

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
            Tuple3<Integer, Long, Long> value = new Tuple3<Integer, Long, Long>(0, 0L, 0L);

            while(sc.hasNextLine()){
                key = sc.nextLine();
                String[] arr = (key.split("\\s"));

                key = arr[IP_INDEX].substring(1);
                pktCount = Integer.parseInt(arr[PKT_COUNT_INDEX].substring(0, arr[PKT_COUNT_INDEX].length()-1));
                flowRate = Long.parseLong(arr[FLOW_RATE_INDEX]);
                interTime = Long.parseLong(arr[INTER_ARRIVAL_INDEX]);
                value.setFirst(pktCount);
                value.setSecond(flowRate);
                value.setThird(interTime);
                map.put(key, value);
            }
            sc.close();

            for (Map.Entry<String, Tuple3> m: map.entrySet()){
                System.out.println("IP: " + m.getKey() + " -> Packet Count = " + m.getValue().getFirst() + " Flow Rate = " + m.getValue().getSecond() + " pkts/sec Average Inter-Arrival Time = " + m.getValue().getThird() + " ns");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        final long endTime = System.currentTimeMillis();
        System.out.println("Total Execution Time = " + (endTime - startTime));
    }
}
