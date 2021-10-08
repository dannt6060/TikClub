package tikfans.tikplus.model;

public class ChienDichCuaNguoiDungHienTai {
    public String key;
    public String type;

    public ChienDichCuaNguoiDungHienTai(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public ChienDichCuaNguoiDungHienTai() {
        this.key = "wrongkey";
        this.type = "wrongtype";
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
