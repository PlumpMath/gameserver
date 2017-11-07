package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;
import co.selim.gameserver.model.dto.outgoing.SnowballCoordinates;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Snowball {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    private final GameExecutor executor;
    private final Messenger messenger;
    private float x;
    private float y;

    private float moveDistance = 15;

    public Snowball(GameExecutor executor, Messenger messenger, float playerX, float playerY,
                    int pointerX, int pointerY) {
        this.executor = executor;
        this.messenger = messenger;

        float xLength = pointerX - playerX;
        float yLength = pointerY - playerY;

        float angle = (float) Math.atan(xLength / yLength);

        this.x = playerX;
        this.y = playerY;
        executor.submit(() -> {
            x += moveDistance * Math.sin(angle);
            y += moveDistance * Math.cos(angle);

            messenger.sendMessage(gson.toJson(new SnowballCoordinates(x, y, false, String.valueOf
                    (hashCode()))));
        }, () -> x < 0 || x > 2000 || y < 0 || y > 2000);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
