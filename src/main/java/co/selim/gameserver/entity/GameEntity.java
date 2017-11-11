package co.selim.gameserver.entity;

public interface GameEntity {
    void destroy();

    void collided(GameEntity other);
}
