package tikfans.tikfollow.tik.tok.followers.likes.model;

import java.io.Serializable;

public class UnFollowUSer implements Serializable {
    public String userName;
    public String userImg;
    public String videoId;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public UnFollowUSer(String userName, String userImg, String videoId) {
        this.userName = userName;
        this.videoId = videoId;
        this.userImg = userImg;
    }

    public UnFollowUSer() {
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
