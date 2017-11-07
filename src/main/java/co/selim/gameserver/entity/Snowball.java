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
    private double x;
    private double y;

    private double moveDistance = 15;

    public Snowball(GameExecutor executor, Messenger messenger, double playerX, double playerY,
                    int pointerX, int pointerY) {
        this.executor = executor;
        this.messenger = messenger;

        double xLength = pointerX - playerX;
        double yLength = pointerY - playerY;

        double angle = Math.atan(xLength / yLength);

        this.x = playerX;
        this.y = playerY;
        executor.submit(() -> {
            x += moveDistance * Math.sin(angle);
            y += moveDistance * Math.cos(angle);

            messenger.sendMessage(gson.toJson(new SnowballCoordinates(x, y, false, String.valueOf
                    (hashCode()))));
        });
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
