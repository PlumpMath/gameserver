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

import static co.selim.gameserver.model.GameMap.MAP_SIZE;

public class Snowball implements GameEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(Snowball.class);

    private final GameMap map;
    private final Player myPlayer;

    private float moveDistance = 45;
    private float angle;

    private Body body;
    private volatile boolean destroyed;

    public Snowball(GameExecutor executor, Messenger messenger, GameMap map, short groupIndex, Player myPlayer, int pointerX, int pointerY) {
        this.map = map;
        this.myPlayer = myPlayer;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(myPlayer.getPosition().x, myPlayer.getPosition().y);
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

        this.angle = MathUtils.atan2(pointerY - myPlayer.getPosition().y, pointerX - myPlayer
                .getPosition().x);

        Vector2 velocity = new Vector2(MathUtils.cos(angle) * moveDistance, MathUtils.sin(angle)
                * moveDistance);
        body.setLinearVelocity(velocity);

        executor.submitOnce(() -> {
            Vector2 snowballPosition = body.getPosition();
            messenger.broadCast(new SnowballCoordinates(snowballPosition.x, snowballPosition.y,
                    angle, moveDistance, false, String.valueOf(hashCode())));
        });

        executor.submit(() -> {
            Vector2 pos = body.getPosition();
            if (!destroyed && (pos.x < 0 || pos.x > MAP_SIZE.x || pos.y < 0 || pos.y > MAP_SIZE
                    .y)) {
                destroy();
                LOGGER.info("Destroyed snowball with ID {}", hashCode());
                Vector2 snowballPosition = body.getPosition();
                messenger.broadCast(new SnowballCoordinates(snowballPosition.x, snowballPosition
                        .y, angle, moveDistance, true, String.valueOf(hashCode())));
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
        destroy();
        if (other.getType() == Type.PLAYER) {
            LOGGER.info("Player was hit by snowball");
            Player player = (Player) other;
            player.sendMessage(new PlayerScored(player.getId(), PlayerScored.ScoreType.GOT_HIT));
            player.sendMessage(new PlayerScored(myPlayer.getId(), PlayerScored.ScoreType.HIT));
            player.changeScore(PlayerScored.ScoreType.GOT_HIT.getDelta());
            player.broadCastMessage(new PlayerScoreChanged(player.getId(), player.getScore()));
            myPlayer.sendMessage(new PlayerScored(myPlayer.getId(), PlayerScored.ScoreType.HIT));
            myPlayer.sendMessage(new PlayerScored(player.getId(), PlayerScored.ScoreType.GOT_HIT));
            myPlayer.changeScore(PlayerScored.ScoreType.HIT.getDelta());
            myPlayer.broadCastMessage(new PlayerScoreChanged(myPlayer.getId(), myPlayer.getScore
                    ()));
        }
    }

    @Override
    public Type getType() {
        return Type.SNOWBALL;
    }
}
