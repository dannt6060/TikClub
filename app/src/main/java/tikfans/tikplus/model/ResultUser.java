package tikfans.tikplus.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultUser {
    @SerializedName("result")
    @Expose
    private User result;

    public User getResult() {
        return result;
    }

    public void setResult(User result) {
        this.result = result;
    }
}
