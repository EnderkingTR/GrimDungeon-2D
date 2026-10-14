import java.awt.Color;

public enum TileType {
    VOID(0, true, new Color(15, 15, 20), new Color(15, 15, 20)),
    FLOOR(1, false, new Color(48, 48, 60), new Color(40, 40, 50)),
    WALL(2, true, new Color(95, 85, 105), new Color(65, 55, 75));

    public final int id;
    public final boolean solid; // Çarpışma var mı? (Duvar mı?)
    public final Color baseColor;
    public final Color borderColor;

    TileType(int id, boolean solid, Color baseColor, Color borderColor) {
        this.id = id;
        this.solid = solid;
        this.baseColor = baseColor;
        this.borderColor = borderColor;
    }
}