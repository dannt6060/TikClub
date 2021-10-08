package tikfans.tikplus.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    @SerializedName("secUid")
    @Expose
    private String secUid;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("isSecret")
    @Expose
    private Boolean isSecret;
    @SerializedName("uniqueId")
    @Expose
    private String uniqueId;
    @SerializedName("nickName")
    @Expose
    private String nickName;
    @SerializedName("signature")
    @Expose
    private String signature;
    @SerializedName("covers")
    @Expose
    private List<String> covers = null;
    @SerializedName("coversMedium")
    @Expose
    private List<String> coversMedium = null;
    @SerializedName("following")
    @Expose
    private Integer following;
    @SerializedName("fans")
    @Expose
    private Integer fans;
    @SerializedName("heart")
    @Expose
    private String heart;
    @SerializedName("video")
    @Expose
    private Integer video;
    @SerializedName("verified")
    @Expose
    private Boolean verified;
    @SerializedName("digg")
    @Expose
    private Integer digg;
    @SerializedName("ftc")
    @Expose
    private Boolean ftc;
    @SerializedName("relation")
    @Expose
    private Integer relation;
    @SerializedName("openFavorite")
    @Expose
    private Boolean openFavorite;

    public String getSecUid() {
        return secUid;
    }

    public void setSecUid(String secUid) {
        this.secUid = secUid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getIsSecret() {
        return isSecret;
    }

    public void setIsSecret(Boolean isSecret) {
        this.isSecret = isSecret;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getCovers() {
        return covers;
    }

    public void setCovers(List<String> covers) {
        this.covers = covers;
    }

    public List<String> getCoversMedium() {
        return coversMedium;
    }

    public void setCoversMedium(List<String> coversMedium) {
        this.coversMedium = coversMedium;
    }

    public Integer getFollowing() {
        return following;
    }

    public void setFollowing(Integer following) {
        this.following = following;
    }

    public Integer getFans() {
        return fans;
    }

    public void setFans(Integer fans) {
        this.fans = fans;
    }

    public String getHeart() {
        return heart;
    }

    public void setHeart(String heart) {
        this.heart = heart;
    }

    public Integer getVideo() {
        return video;
    }

    public void setVideo(Integer video) {
        this.video = video;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Integer getDigg() {
        return digg;
    }

    public void setDigg(Integer digg) {
        this.digg = digg;
    }

    public Boolean getFtc() {
        return ftc;
    }

    public void setFtc(Boolean ftc) {
        this.ftc = ftc;
    }

    public Integer getRelation() {
        return relation;
    }

    public void setRelation(Integer relation) {
        this.relation = relation;
    }

    public Boolean getOpenFavorite() {
        return openFavorite;
    }

    public void setOpenFavorite(Boolean openFavorite) {
        this.openFavorite = openFavorite;
    }
}
