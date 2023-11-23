package tikfans.tikfollow.tik.tok.followers.likes.model;

import java.io.Serializable;

public class RecentVideo implements Serializable {
    public String videoId; // video id
    public String title; // video titile
    public String channelId;
    public String channelName;
    public String channelImg;
    public Object time;

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }

    public RecentVideo(String videoId, String title, String channelId, String channelName, String channelImg, Object time) {
        this.videoId = videoId;
        this.title = title;
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelImg = channelImg;
        this.time = time;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelImg() {
        return channelImg;
    }

    public void setChannelImg(String channelImg) {
        this.channelImg = channelImg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RecentVideo() {
    }


    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
