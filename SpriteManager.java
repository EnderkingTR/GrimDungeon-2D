import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Texture & Sprite Loader with automatic caching.
 * Looks for custom PNG sprites in "assets/sprites/<name>.png".
 * If a file is not present, returns null so the engine seamlessly uses its
 * built-in procedural character/tile renderer.
 */
public class SpriteManager {
    private static final Map<String, BufferedImage> cache = new HashMap<>();

    public static BufferedImage getSprite(String fileName) {
        if (cache.containsKey(fileName)) {
            return cache.get(fileName);
        }
        File file = new File("assets/sprites/" + fileName);
        if (!file.exists()) {
            cache.put(fileName, null);
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(file);
            cache.put(fileName, img);
            return img;
        } catch (Exception e) {
            cache.put(fileName, null);
            return null;
        }
    }

    public static void reloadAll() {
        cache.clear();
    }
}

