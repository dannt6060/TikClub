package tikfans.tikfollow.tik.tok.followers.likes.model;

public class Message {
    public String uId;
    public String uName;
    public String uImg;

    public String getuImg() {
        return uImg;
    }

    public void setuImg(String uImg) {
        this.uImg = uImg;
    }

    public String mess;
    public long coin;
    public Object time;

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
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

    public Message(String uId, String uName, String uImg, String mess, long coin, Object time) {
        this.uId = uId;
        this.uName = uName;
        this.mess = mess;
        this.coin = coin;
        this.time = time;
        this.uImg = uImg;
    }

    public Message() {
    }

}
