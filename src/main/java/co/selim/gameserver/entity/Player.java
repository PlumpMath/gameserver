package co.selim.gameserver.entity;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Player {
    private final Session session;
    private int x;
    private int y;

    public Player(Session session) {
        this.session = session;
        this.x = 960;
        this.y = 960;
    }

    public void sendMessage(String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
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
