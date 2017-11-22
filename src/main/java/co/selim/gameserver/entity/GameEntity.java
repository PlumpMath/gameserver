package co.selim.gameserver.entity;

public interface GameEntity {
    enum Type {
        PLAYER, SNOWBALL, WALL, TREE
    }

    void destroy();

    void collided(GameEntity other);

    Type getType();
}
