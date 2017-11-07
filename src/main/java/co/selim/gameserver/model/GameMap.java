package co.selim.gameserver.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class GameMap {
    private final World world;

    public GameMap() {
        Vector2 gravity = new Vector2(0, 0);
        this.world = new World(gravity, true);

        new Thread(() -> {
            while (true) {
                world.step(1.0f / 60.0f, 6, 3);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "WorldUpdateThread").start();
    }

    public World getWorld() {
        return world;
    }
}
