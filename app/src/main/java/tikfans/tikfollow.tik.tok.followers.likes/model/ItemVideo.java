package tikfans.tikfollow.tik.tok.followers.likes.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ItemVideo {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("diggCount")
    @Expose
    private Integer diggCount;
    @SerializedName("commentCount")
    @Expose
    private Integer commentCount;
    @SerializedName("videoUrl")
    @Expose
    private String videoUrl;
    @SerializedName("webVideoUrl")
    @Expose
    private String webVideoUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDiggCount() {
        return diggCount;
    }

    public void setDiggCount(Integer diggCount) {
        this.diggCount = diggCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getWebVideoUrl() {
        return webVideoUrl;
    }

    public void setWebVideoUrl(String webVideoUrl) {
        this.webVideoUrl = webVideoUrl;
    }
}
