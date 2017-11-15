package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;
import co.selim.gameserver.model.GameMap;
import co.selim.gameserver.model.dto.outgoing.PlayerMoved;
import co.selim.gameserver.model.dto.outgoing.PlayerStopped;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static co.selim.gameserver.model.GameMap.MAP_SIZE;

public class Player implements GameEntity {
    private final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    private static short nCount;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    private final GameExecutor executor;
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

    public Player(String address, GameMap map, Messenger messenger) {
        this.GROUP_INDEX = --nCount;
        executor = new GameExecutor("Player-" + address + "-UpdateExecutor");
        this.messenger = messenger;

        float x = MAP_SIZE.x / 2;
        float y = MAP_SIZE.y / 2;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        float halfSize = 24;
        PolygonShape edgeShape = new PolygonShape();
        edgeShape.setAsBox(halfSize, halfSize);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edgeShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;
        fixtureDef.filter.groupIndex = GROUP_INDEX;
        body = map.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setUserData(this);
        this.map = map;

        this.moveDistance = 15;

        executor.submitConnectionBoundTask(() -> {
            Vector2 velocity = body.getLinearVelocity();

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

            if (!body.getLinearVelocity().isZero() && velocity.isZero()) {
                messenger.sendMessage(gson.toJson(new PlayerStopped(bodyPosition.x, bodyPosition.y)));
            }

            body.setLinearVelocity(velocity);
            moveDistance = 15;

            float angle = MathUtils.atan2(velocity.y - bodyPosition.y, velocity.x - bodyPosition.x);

            if (!lastVelocity.epsilonEquals(body.getLinearVelocity())) {
                messenger.sendMessage(gson.toJson(new PlayerMoved(bodyPosition.x, bodyPosition.y, angle, moveDistance)));
                lastVelocity.set(body.getLinearVelocity());
            }
        });
        executor.submitOnce(() -> {
            messenger.sendMessage(gson.toJson(new PlayerStopped(body.getPosition().x, body.getPosition().y)));
        });
    }

    public void move(int xDirection, int yDirection) {
        movingX = xDirection != 0;
        movingY = yDirection != 0;

        this.xDirection = xDirection;
        this.yDirection = yDirection;
    }

    public void throwSnowball(int pointerX, int pointerY) {
        new Snowball(executor, messenger, map, GROUP_INDEX, body.getPosition().x, body
                .getPosition().y, pointerX, pointerY);
    }

    public void disconnect() {
        destroy();
        executor.stop();
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

    public void sendMessage(String message) {
        executor.submitOnce(() -> {
            messenger.sendMessage(message);
        });
    }

    @Override
    public void destroy() {
        map.destroyBody(body);
    }

    @Override
    public void collided(GameEntity other) {
        if (other.getType().equals(Type.OBSTACLE) && (!movingX && !movingY)) {
            LOGGER.info("Player collided with obstacle and not moving diagonally, sending stop");
            executor.submitOnce(() -> {
                Vector2 bodyPos = body.getPosition();
                messenger.sendMessage(gson.toJson(new PlayerStopped(bodyPos.x, bodyPos.y)));
            });
        }
    }

    @Override
    public Type getType() {
        return Type.PLAYER;
    }
}
