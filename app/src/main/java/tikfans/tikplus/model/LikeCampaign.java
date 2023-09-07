package tikfans.tikplus.model;

public class LikeCampaign {
    public String ownId; //owner ID
    public String videoId;
    public String videoThumb;
    public int timeR;
    public String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Object creTime; // create timestamp
    public Object lasTime; // last change time stamp
    public Object finTime; // finish time stamp
    public int order; //total view order


    public int curLike; // current view
    public String key; //key
    public boolean ip; // in progress
    public long coinRate;

    public String getVideoThumb() {
        return videoThumb;
    }

    public void setVideoThumb(String videoThumb) {
        this.videoThumb = videoThumb;
    }

    public long getCoinRate() {
        return coinRate;
    }

    public void setCoinRate(long coinRate) {
        this.coinRate = coinRate;
    }

    public String getVideoId() {
        return videoId;
    }

    public int getTimeR() {
        return timeR;
    }

    public void setTimeR(int timeR) {
        this.timeR = timeR;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getOwnId() {
        return ownId;
    }

    public void setOwnId(String ownId) {
        this.ownId = ownId;
    }


    public LikeCampaign(String key, String ownId, String userName, String videoId, String videoThumb, int order, long coinRate, int timeR, Object creTime, Object lasTime, Object finTime) {
        this.ownId = ownId;
        this.userName = userName;
        this.videoId = videoId;
        this.creTime = creTime;
        this.lasTime = lasTime;
        this.finTime = finTime;
        this.order = order;
        this.coinRate = coinRate;
        this.timeR = timeR;
//        this.videoURL = videoURL;
        this.videoThumb = videoThumb;

        curLike = 0;
        this.key = key;
        ip = true;
    }

    public LikeCampaign() {
    }

    public Object getCreTime() {
        return creTime;
    }

    public void setCreTime(Object creTime) {
        this.creTime = creTime;
    }

    public Object getLasTime() {
        return lasTime;
    }

    public void setLasTime(Object lasTime) {
        this.lasTime = lasTime;
    }

    public Object getFinTime() {
        return finTime;
    }

    public void setFinTime(Object finTime) {
        this.finTime = finTime;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getCurLike() {
        return curLike;
    }

    public void setCurLike(int curLike) {
        this.curLike = curLike;
    }

    public boolean isIp() {
        return ip;
    }

    public void setIp(boolean ip) {
        this.ip = ip;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
