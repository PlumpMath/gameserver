package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;
import co.selim.gameserver.model.GameMap;
import co.selim.gameserver.model.dto.outgoing.PlayerDisconnected;
import co.selim.gameserver.model.dto.outgoing.PlayerMoved;
import co.selim.gameserver.model.dto.outgoing.PlayerStopped;
import co.selim.gameserver.model.dto.outgoing.SnowballCount;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static co.selim.gameserver.model.GameMap.MAP_SIZE;

public class Player implements GameEntity {
    private final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    private static final int SNOWBALL_COOLDOWN = 1000;

    private static short nCount;
    private final GameExecutor executor;
    private final String id;
    private final Session session;
    private volatile boolean gameStarted;
    private Messenger messenger;

    private int xDirection;
    private int yDirection;

    private boolean movingX;
    private boolean movingY;

    private float moveDistance;

    private Body body;
    private final GameMap map;
    private final short GROUP_INDEX;

    private volatile Vector2 lastVelocity = new Vector2();

    private String name;
    private String skin;
    private int score;

    private volatile boolean connected;
    private final int MAX_SNOWBALLS = 5;
    private volatile AtomicInteger snowballCount = new AtomicInteger(5);

    private final AtomicLong nextSnowball = new AtomicLong(-1);

    public Player(Session session, String address, GameMap map, Messenger messenger) {
        this.session = session;
        this.GROUP_INDEX = --nCount;
        this.id = UUID.randomUUID()
                .toString();
        executor = new GameExecutor("Player-" + address + "-UpdateExecutor");
        this.messenger = messenger;

        float x = MAP_SIZE.x / 2;
        float y = MAP_SIZE.y / 2;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        float halfSize = 12;
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(halfSize);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;
        fixtureDef.filter.groupIndex = GROUP_INDEX;
        body = map.createBody(bodyDef);
        map.doInLock(() -> {
            body.createFixture(fixtureDef);
            body.setUserData(this);
        });
        this.map = map;

        this.moveDistance = 120;

        executor.submitConnectionBoundTask(() -> {
            Vector2 velocity = new Vector2();

            if (movingX && movingY) {
                moveDistance *= Math.cos(Math.PI / 4);
            }

            if (movingX) {
                velocity.x = xDirection * moveDistance;
            } else {
                velocity.x = 0;
            }
            if (movingY) {
                velocity.y = yDirection * moveDistance;
            } else {
                velocity.y = 0;
            }

            Vector2 bodyPosition = body.getPosition();

            if (gameStarted && !lastVelocity.epsilonEquals(velocity) && velocity.isZero()) {
                messenger.broadCast(new PlayerStopped(bodyPosition.x, bodyPosition.y, getId()));
                lastVelocity.set(velocity);
            }

            body.setLinearVelocity(velocity);
            moveDistance = 120;

            float angle = MathUtils.atan2(velocity.y, velocity.x);

            if (!lastVelocity.epsilonEquals(velocity)) {
                messenger.broadCast(new PlayerMoved(bodyPosition.x, bodyPosition.y, angle,
                        moveDistance, getId()));
                lastVelocity.set(velocity);
            }

            if (nextSnowball.get() == -1) {
                if (snowballCount.get() < MAX_SNOWBALLS) {
                    nextSnowball.compareAndSet(-1, System.currentTimeMillis() + SNOWBALL_COOLDOWN);
                }
                return;
            }

            long snowballTime = nextSnowball.get();

            if (System.currentTimeMillis() >= snowballTime) {
                snowballCount.incrementAndGet();
                sendSnowballCount();

                if (snowballCount.get() < MAX_SNOWBALLS) {
                    nextSnowball.addAndGet(SNOWBALL_COOLDOWN);
                } else {
                    nextSnowball.set(-1);
                }
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
        if (snowballCount.get() > 0) {
            snowballCount.decrementAndGet();

            nextSnowball.compareAndSet(-1, System.currentTimeMillis() + SNOWBALL_COOLDOWN);

            new Snowball(executor, messenger, map, GROUP_INDEX, this, pointerX, pointerY);
        }
        sendSnowballCount();
    }

    private void sendSnowballCount() {
        LOGGER.info("Sending snowballCount {}", snowballCount.get());
        executor.submitOnce(() -> {
            messenger.sendMessage(new SnowballCount(snowballCount.get()));
        });
    }

    public void disconnect() {
        executor.submitOnce(() -> {
            messenger.broadCastToOthers(new PlayerDisconnected(getId()));
            connected = false;
            destroy();
            executor.stop();
        });
    }

    public Vector2 getPosition() {
        return new Vector2(body.getPosition());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public void broadCastMessage(Object obj) {
        executor.submitOnce(() -> {
            messenger.broadCast(obj);
        });
    }

    public void broadCastToOthers(Object obj) {
        executor.submitOnce(() -> {
            messenger.broadCastToOthers(obj);
        });
    }

    public void sendMessage(Object obj) {
        executor.submitOnce(() -> {
            messenger.sendMessage(obj);
        });
    }

    @Override
    public void destroy() {
        map.destroyBody(body);
    }

    @Override
    public void collided(GameEntity other) {
        if (other.getType()
                .equals(Type.WALL) && (!movingX && !movingY)) {
            LOGGER.info("Player collided with obstacle and not moving diagonally, sending stop");
            executor.submitOnce(() -> {
                Vector2 bodyPos = body.getPosition();
                messenger.broadCast(new PlayerStopped(bodyPos.x, bodyPos.y, getId()));
            });
        }
    }

    public String getId() {
        return id;
    }

    public void changeScore(int delta) {
        this.score += delta;
    }

    public void setGameStarted() {
        this.gameStarted = true;
        this.connected = true;
    }

    @Override
    public Type getType() {
        return Type.PLAYER;
    }

    public int getScore() {
        return score;
    }

    public Session getSession() {
        return session;
    }

    public boolean isConnected() {
        return connected;
    }

    public short getGroupIndex() {
        return GROUP_INDEX;
    }
}
