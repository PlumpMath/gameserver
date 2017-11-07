package co.selim.gameserver.util;

public class DistanceUtils {
    private static final float WIDTH = 2000;
    private static final float HEIGHT = 2000;

    public static float toPixelPosX(float posX) {
        float x = WIDTH * posX / 100f;
        return x;
    }

    public static float toPosX(float posX) {
        float x = (posX * 100f * 1f) / WIDTH;
        return x;
    }

    public static float toPixelPosY(float posY) {
        float y = HEIGHT - (1f * HEIGHT) * posY / 100f;
        return y;
    }

    public static float toPosY(float posY) {
        float y = 100f - ((posY * 100 * 1f) / HEIGHT);
        return y;
    }

    public static float toPixelWidth(float width) {
        return WIDTH * width / 100f;
    }

    public static float toPixelHeight(float height) {
        return HEIGHT * height / 100f;
    }
}
