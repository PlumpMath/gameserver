package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;
import co.selim.gameserver.model.dto.outgoing.PlayerCoordinates;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class Player {
    private final Logger logger;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    private final GameExecutor executor;
    private Messenger messenger;

    private int xDirection;
    private int yDirection;

    private boolean movingX;
    private boolean movingY;

    private float moveDistance;
    private volatile AtomicBoolean connected = new AtomicBoolean(true);

    private float lastSentX;
    private float lastSentY;

    private Body body;

    public Player(String address, World world, Messenger messenger) {
        logger = LoggerFactory.getLogger("Player " + address);
        executor = new GameExecutor("Player + " + address + "-UpdateExecutor", connected);
        this.messenger = messenger;

        float x = 960;
        float y = 960;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        float halfSize = 32;
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(-halfSize, -halfSize, halfSize, halfSize);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edgeShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;
        body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);

        this.moveDistance = 50;

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

            body.setLinearVelocity(velocity);
            moveDistance = 50;

            Vector2 bodyPosition = body.getPosition();
            if (bodyPosition.x != lastSentX || bodyPosition.y != lastSentY) {
                messenger.sendMessage(gson.toJson(new PlayerCoordinates(bodyPosition.x, bodyPosition.y)));
                lastSentX = bodyPosition.x;
                lastSentY = bodyPosition.y;
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
        new Snowball(executor, messenger, body.getPosition().x, body.getPosition().y, pointerX, pointerY);
    }

    public void disconnect() {
        connected.set(false);
    }
}
