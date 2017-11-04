package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;

public class Snowball {
    private final GameExecutor executor;
    private final Messenger messenger;
    private int x;
    private int y;

    public Snowball(GameExecutor executor, Messenger messenger) {
        this.executor = executor;
        this.messenger = messenger;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
