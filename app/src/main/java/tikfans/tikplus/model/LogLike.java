package tikfans.tikplus.model;

public class LogLike {
    public String likeId;
    public String likePhoto;
    public String likeUName;
    public long coin;
    public Object time;

    public LogLike() {
    }

    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public LogLike(String likeId, long coin, Object time) {
        this.likeId = likeId;
        this.time = time;
        this.coin = coin;

    }

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public String getLikePhoto() {
        return likePhoto;
    }

    public void setLikePhoto(String likePhoto) {
        this.likePhoto = likePhoto;
    }


    public String getLikeUName() {
        return likeUName;
    }

    public void setLikeUName(String likeUName) {
        this.likeUName = likeUName;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }
}
