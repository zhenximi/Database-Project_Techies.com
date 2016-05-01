package etc;

/**
 * Created by sulei on 4/25/16.
 */
public enum PermissionType {

    PUBLIC(0),
    FOF(1),
    FRIENDS(2),
    PRIVATE(3);

    private int value;

    private PermissionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}