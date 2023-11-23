package tikfans.tikfollow.tik.tok.followers.likes.model;

public class LogAdsReward {
    public Object time;
    public long coin;

    public LogAdsReward() {
    }

    public LogAdsReward(long coin, Object time) {

        this.time = time;
        this.coin = coin;
    }

    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }

}
