package co.selim.gameserver.model;

import co.selim.gameserver.entity.GameEntity;
import co.selim.gameserver.entity.Player;
import co.selim.gameserver.entity.Tree;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class GameMap {
    public static final Vector2 MAP_SIZE = new Vector2(1600, 900);
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMap.class);

    private final World world;
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<Body> bodiesToRemove = ConcurrentHashMap.newKeySet();
    private static final Set<Tree> trees = new HashSet<>();
    private volatile long lastUpdate = System.nanoTime();

    public GameMap() {
        Vector2 gravity = new Vector2(0, 0);
        this.world = new World(gravity, true);

        float mapWidth = MAP_SIZE.x;
        float mapHeight = MAP_SIZE.y;
        createWall(mapWidth, 0, mapWidth, 50); // top
        createWall(0, mapHeight, 50, mapHeight); // left
        createWall(mapWidth, mapHeight + 50, mapWidth, 50); // bottom
        createWall(mapWidth + 50, mapHeight, 50, mapHeight); // right
        createTree(mapWidth * 0.25f, mapHeight * 0.33f, Tree.TreeType.PINALE);
        createTree(mapWidth * 0.85f, mapHeight * 0.5f, Tree.TreeType.BROADLEAF);
        createTree(mapWidth * 0.5f, mapHeight * 0.75f, Tree.TreeType.BROADLEAF);

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                GameEntity a = (GameEntity) contact.getFixtureA()
                        .getBody()
                        .getUserData();
                GameEntity b = (GameEntity) contact.getFixtureB()
                        .getBody()
                        .getUserData();

                // TODO: fix native CME
                a.collided(b);
                b.collided(a);
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                GameEntity a = (GameEntity) contact.getFixtureA()
                        .getBody()
                        .getUserData();
                GameEntity b = (GameEntity) contact.getFixtureB()
                        .getBody()
                        .getUserData();

                if (!(a.shouldCollide(b) && b.shouldCollide(a))) {
                    contact.setEnabled(false);
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });

        new Thread(() -> {
            while (true) {
                try {
                    lock.lock();
                    long now = System.nanoTime();
                    long delta = now - lastUpdate;
                    float deltaS = delta / 1_000_000_000f;
                    lastUpdate = now;
                    world.step(deltaS, 6, 3);

                    bodiesToRemove.forEach(world::destroyBody);
                    bodiesToRemove.clear();
                } finally {
                    lock.unlock();
                }
                try {
                    Thread.sleep(1L);
                } catch (Exception e) {
                    break;
                }
            }
        }, "WorldUpdateThread").start();
    }

    public Body createBody(BodyDef bodyDef) {
        return getInsideLock(world -> world.createBody(bodyDef));
    }

    public void destroyBody(Body body) {
        bodiesToRemove.add(body);
    }

    private Body getInsideLock(Function<World, Body> function) {
        try {
            lock.lock();
            return function.apply(world);
        } finally {
            lock.unlock();
        }
    }

    public void doInLock(Runnable task) {
        try {
            lock.lock();
            task.run();
        } finally {
            lock.unlock();
        }
    }

    public static Set<Tree> getTrees() {
        return new HashSet<>(trees);
    }

    private void createWall(float rightX, float bottomY, float w, float h) {
        // TODO: fix wall coordinates
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(rightX - w / 2, bottomY - h / 2);
        bodyDef.type = BodyDef.BodyType.StaticBody;
        PolygonShape edgeShape = new PolygonShape();
        edgeShape.setAsBox(w / 2, h / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edgeShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;
        fixtureDef.filter.groupIndex = Short.MIN_VALUE;
        createBody(bodyDef).createFixture(fixtureDef)
                .getBody()
                .setUserData(new GameEntity() {
                    @Override
                    public void destroy() {
                    }

                    @Override
                    public void collided(GameEntity other) {
                        LOGGER.info("Someone collided with a wall");
                    }

                    @Override
                    public Type getType() {
                        return Type.WALL;
                    }
                });
    }

    private void createTree(float x, float y, Tree.TreeType treeType) {
        BodyDef bodyDef = new BodyDef();
        float w = treeType.getSize().x;
        float h = treeType.getSize().y;
        bodyDef.position.set(x - w / 2, y - h / 2);
        bodyDef.type = BodyDef.BodyType.StaticBody;
        PolygonShape edgeShape = new PolygonShape();
        edgeShape.setAsBox(w / 2, h / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edgeShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;
        fixtureDef.filter.groupIndex = Short.MIN_VALUE;
        Tree treeEntity = new Tree(treeType, bodyDef.position);
        createBody(bodyDef).createFixture(fixtureDef)
                .getBody()
                .setUserData(treeEntity);
        trees.add(treeEntity);
    }
}
