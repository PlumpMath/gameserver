package co.selim.gameserver.entity;

import co.selim.gameserver.executor.GameExecutor;
import co.selim.gameserver.messaging.Messenger;
import co.selim.gameserver.model.GameMap;
import co.selim.gameserver.model.dto.outgoing.PlayerScoreChanged;
import co.selim.gameserver.model.dto.outgoing.PlayerScored;
import co.selim.gameserver.model.dto.outgoing.SnowballCoordinates;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static co.selim.gameserver.model.GameMap.MAP_SIZE;

public class Snowball implements GameEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(Snowball.class);

    private final GameMap map;
    private final Player myPlayer;

    private float moveDistance = 360;
    private float angle;

    private Body body;
    private volatile boolean destroyed;

    private final String id;
    private final Vector2 startPosition;

    public Snowball(GameExecutor executor, Messenger messenger, GameMap map, short groupIndex,
                    Player myPlayer, int pointerX, int pointerY) {
        this.map = map;
        this.myPlayer = myPlayer;

        BodyDef bodyDef = new BodyDef();
        startPosition = new Vector2(myPlayer.getPosition());
        bodyDef.position.set(startPosition);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(8);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;
        fixtureDef.filter.groupIndex = groupIndex;
        this.body = map.createBody(bodyDef);
        map.doInLock(() -> {
            body.setUserData(this);
            body.createFixture(fixtureDef);
        });

        this.id = UUID.randomUUID()
                .toString();

        this.angle = MathUtils.atan2(pointerY - myPlayer.getPosition().y, pointerX - myPlayer
                .getPosition().x);

        Vector2 velocity = new Vector2(MathUtils.cos(angle) * moveDistance, MathUtils.sin(angle)
                * moveDistance);
        body.setLinearVelocity(velocity);

        executor.submitOnce(() -> {
            Vector2 snowballPosition = body.getPosition();
            messenger.broadCast(new SnowballCoordinates(snowballPosition.x, snowballPosition.y, angle, moveDistance, false, id));
        });

        executor.submit(() -> {
            Vector2 pos = body.getPosition();
            if (destroyed || (!destroyed && (pos.x < 0 || pos.x > MAP_SIZE.x || pos.y < 0 || pos
                    .y > MAP_SIZE.y))) {
                destroy();
                LOGGER.info("Destroyed snowball with ID {}", id);
                Vector2 snowballPosition = body.getPosition();
                messenger.broadCast(new SnowballCoordinates(snowballPosition.x, snowballPosition.y, angle, moveDistance, true, id));
            }
        }, () -> destroyed);
    }

    @Override
    public void destroy() {
        if (!destroyed) {
            destroyed = true;
            map.destroyBody(body);
        }
    }

    @Override
    public void collided(GameEntity other) {
        if (other.getType() == Type.PLAYER) {
            Vector2 endPosition = new Vector2(body.getPosition());

            float distance = (float) Math.sqrt(Math.pow(startPosition.x - endPosition.x, 2)
                    +
                    Math.pow(startPosition.y - endPosition.y, 2));
            float minDistance = 100;
            float maxDistance = 400 - minDistance;
            float distancePercentage = (distance - minDistance) / maxDistance;
            int scoreDelta = Math.round(Math.max(0, Math.min(distancePercentage, 1)) * 10);

            Player player = (Player) other;
            player.sendMessage(new PlayerScored(player.getId(), -scoreDelta));
            player.sendMessage(new PlayerScored(myPlayer.getId(), scoreDelta));
            player.changeScore(scoreDelta);
            player.broadCastMessage(new PlayerScoreChanged(player.getId(), player.getScore()));
            myPlayer.sendMessage(new PlayerScored(myPlayer.getId(), scoreDelta));
            myPlayer.sendMessage(new PlayerScored(player.getId(), -scoreDelta));
            myPlayer.changeScore(scoreDelta);
            myPlayer.broadCastMessage(new PlayerScoreChanged(myPlayer.getId(), myPlayer.getScore
                    ()));
        }
        if (other.getType() == Type.SNOWBALL) {
            // for the unlikely event that we collide with a sibling snowball
            if (((Snowball) other).myPlayer.getGroupIndex() != myPlayer.getGroupIndex()) {
                destroy();
            }
        } else if (other.getType() == Type.PLAYER) {
            // or our owner
            if (((Player) other).getGroupIndex() != myPlayer.getGroupIndex()) {
                destroy();
            }
        } else {
            destroy();
        }
    }

    @Override
    public Type getType() {
        return Type.SNOWBALL;
    }
}
