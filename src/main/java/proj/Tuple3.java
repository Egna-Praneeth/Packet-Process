package proj;

public class Tuple3<K, V, T> {

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