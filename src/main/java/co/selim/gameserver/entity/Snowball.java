package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;
import co.selim.gameserver.model.GameMap;
import co.selim.gameserver.model.dto.outgoing.SnowballCoordinates;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static co.selim.gameserver.model.GameMap.MAP_SIZE;

public class Snowball implements GameEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(Snowball.class);

    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    private final GameExecutor executor;
    private final Messenger messenger;
    private final GameMap map;

    private float moveDistance = 45;
    private float angle;

    private Body body;
    private boolean destroyed;

    public Snowball(GameExecutor executor, Messenger messenger, GameMap map, short groupIndex,
                    float playerX, float playerY, int pointerX, int pointerY) {
        this.executor = executor;
        this.messenger = messenger;
        this.map = map;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(playerX, playerY);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(8);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;
        fixtureDef.filter.groupIndex = groupIndex;
        body = map.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);

        this.angle = MathUtils.atan2(pointerY - playerY, pointerX - playerX);

        Vector2 velocity = new Vector2(MathUtils.cos(angle) * moveDistance, MathUtils.sin(angle)
                * moveDistance);
        body.setLinearVelocity(velocity);

        executor.submitOnce(() -> {
            Vector2 snowballPosition = body.getPosition();
            messenger.sendMessage(gson.toJson(new SnowballCoordinates(snowballPosition.x, snowballPosition.y, angle, moveDistance, false, String.valueOf(hashCode()))));
        });

        executor.submit(() -> {
            Vector2 pos = body.getPosition();
            if (!destroyed && (pos.x < 0 || pos.x > MAP_SIZE.x || pos.y < 0 || pos.y > MAP_SIZE
                    .y)) {
                map.destroyBody(body);
                destroyed = true;
                LOGGER.info("Destroyed snowball with ID " + hashCode());
                Vector2 snowballPosition = body.getPosition();
                messenger.sendMessage(gson.toJson(new SnowballCoordinates(snowballPosition.x,
                        snowballPosition.y, angle, moveDistance, true, String.valueOf(hashCode())
                )));
            }
        }, () -> destroyed);
    }

    @Override
    public void destroy() {
        map.destroyBody(body);
    }

    @Override
    public void collided(GameEntity other) {
        LOGGER.info("Snowball collided");
    }
}
