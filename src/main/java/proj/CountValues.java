package proj;


public class CountValues{
    public int pktCount;
    public long lastTime;
    public long sumTime;

    public void setPktCount(int pktCount) {
        this.pktCount = pktCount;
    }

    public int getPktCount() {
        return pktCount;
    }

    public long getSumTime() {
        return sumTime;
    }

    public void setSumTime(long sumTime) {
        this.sumTime = sumTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }
}