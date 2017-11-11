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
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.function.Supplier;

public class Snowball {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    private final GameExecutor executor;
    private final Messenger messenger;

    private float moveDistance = 45;

    private Body body;

    public Snowball(GameExecutor executor, Messenger messenger, GameMap map, short groupIndex,
                    float playerX, float playerY, int pointerX, int pointerY) {
        this.executor = executor;
        this.messenger = messenger;

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
        body.createFixture(fixtureDef);

        float angle = MathUtils.atan2(pointerY - playerY, pointerX - playerX);

        Vector2 velocity = new Vector2(MathUtils.cos(angle) * moveDistance, MathUtils.sin(angle)
                * moveDistance);
        body.setLinearVelocity(velocity);

        executor.submit(() -> {
            Vector2 snowballPosition = body.getPosition();
            messenger.sendMessage(gson.toJson(new SnowballCoordinates(snowballPosition.x,
                    snowballPosition.y, didHit().get(), String.valueOf(hashCode()))));
        }, didHit());
    }

    private Supplier<Boolean> didHit() {
        Vector2 pos = body.getPosition();
        return () -> pos.x < 0 || pos.x > 2000 || pos.y < 0 || pos.y > 2000;
    }
}
