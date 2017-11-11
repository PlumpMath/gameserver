package co.selim.gameserver.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.concurrent.locks.ReentrantLock;

public class GameMap {
    private final World world;
    private final ReentrantLock lock = new ReentrantLock();

    public GameMap() {
        Vector2 gravity = new Vector2(0, 0);
        this.world = new World(gravity, true);

        new Thread(() -> {
            while (true) {
                try {
                    lock.lock();
                    world.step(1.0f / 60.0f, 6, 3);
                } finally {
                    lock.unlock();
                }
                try {
                    Thread.sleep(2L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "WorldUpdateThread").start();
    }

    public Body createBody(BodyDef bodyDef) {
        try {
            lock.lock();
            return world.createBody(bodyDef);
        } finally {
            lock.unlock();
        }
    }
}
