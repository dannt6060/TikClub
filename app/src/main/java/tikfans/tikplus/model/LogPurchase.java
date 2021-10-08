package tikfans.tikplus.model;

public class LogPurchase {
    public String ownID;
    public String orderID;
    public String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String sku;
    public Long purchaseTime;
    public String token;
    public long coin;

    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOwnID() {
        return ownID;
    }

    public void setOwnID(String ownID) {
        this.ownID = ownID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public LogPurchase(String ownID, String orderID, String packageName, String sku, Long purchaseTime, String token, long coin) {

        this.ownID = ownID;
        this.orderID = orderID;
        this.packageName = packageName;
        this.sku = sku;
        this.purchaseTime = purchaseTime;
        this.token = token;
        this.coin = coin;
    }

    public LogPurchase() {

    }
}
