package tikfans.tikfollow.tik.tok.followers.likes.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultVideo {
    @SerializedName("result")
    @Expose
    private List<ItemVideo> result = null;

    public List<ItemVideo> getResult() {
        return result;
    }

    public void setResult(List<ItemVideo> result) {
        this.result = result;
    }
}
