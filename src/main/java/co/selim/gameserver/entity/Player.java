package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;
import co.selim.gameserver.model.dto.outgoing.PlayerCoordinates;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class Player {
    private final Logger logger = LoggerFactory.getLogger(Player.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    private final GameExecutor executor;
    private Messenger messenger;
    private double x;
    private double y;

    private int xDirection;
    private int yDirection;

    private boolean movingX;
    private boolean movingY;

    private double moveDistance;
    private volatile AtomicBoolean connected = new AtomicBoolean(true);

    private double lastSentX;
    private double lastSentY;

    public Player(String address, Messenger messenger) {
        executor = new GameExecutor("Player + " + address + "-UpdateExecutor", connected);
        this.messenger = messenger;

        this.x = 960;
        this.y = 960;

        this.moveDistance = 5;

        executor.submit(() -> {
            if (movingX && movingY) {
                moveDistance *= Math.cos(Math.PI / 4);
            }

            if (movingX) {
                x += xDirection * moveDistance;
            }
            if (movingY) {
                y += yDirection * moveDistance;
            }

            moveDistance = 5;
            if (x != lastSentX || y != lastSentY) {
                messenger.sendMessage(gson.toJson(new PlayerCoordinates(x, y)));
                lastSentX = x;
                lastSentY = y;
            }
        });
    }

    public void move(int xDirection, int yDirection) {
        movingX = xDirection != 0;
        movingY = yDirection != 0;

        this.xDirection = xDirection;
        this.yDirection = yDirection;
    }

    public void throwSnowball(int pointerX, int pointerY) {
        new Snowball(executor, messenger, x, y, pointerX, pointerY);
    }

    public void disconnect() {
        connected.set(false);
    }

    public double getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
