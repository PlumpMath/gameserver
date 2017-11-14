package co.selim.gameserver.entity;

public interface GameEntity {
    enum Type {
        PLAYER, SNOWBALL, OBSTACLE
    }

    void destroy();

    void collided(GameEntity other);

    Type getType();
}
