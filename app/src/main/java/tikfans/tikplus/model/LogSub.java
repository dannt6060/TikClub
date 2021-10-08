package tikfans.tikplus.model;

public class LogSub {
    public String subId;
    public String subPhoto;
    public String campId;
    public String subUName;
    public long coin;
    public Object time;

    public LogSub() {
    }

    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public LogSub(String subId, String subPhoto, String campId, String subUName, long coin, Object time) {
        this.subId = subId;
        this.subPhoto = subPhoto;
        this.campId = campId;
        this.subUName = subUName;
        this.time = time;
        this.coin = coin;

    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getSubPhoto() {
        return subPhoto;
    }

    public void setSubPhoto(String subPhoto) {
        this.subPhoto = subPhoto;
    }

    public String getCampId() {
        return campId;
    }

    public void setCampId(String campId) {
        this.campId = campId;
    }

    public String getSubUName() {
        return subUName;
    }

    public void setSubUName(String subUName) {
        this.subUName = subUName;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }
}
