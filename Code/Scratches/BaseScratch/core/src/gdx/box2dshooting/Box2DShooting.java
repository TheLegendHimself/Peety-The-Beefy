package gdx.box2dshooting;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import gdx.box2dshooting.Box2D;


public class Box2DShooting extends ApplicationAdapter {

    SpriteBatch batch;
    Texture img;
    OrthographicCamera camera;
    private World world;
    private Box2D playerBody;
    private Box2DDebugRenderer b2dr;
    OrthogonalTiledMapRenderer otmr;
    TiledMap tMap;

    int nJumpInterval, nJumpCount;
    boolean isCounterStart;

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        world = new World(new Vector2(0f, -18f), false);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 0, 0);
        b2dr = new Box2DDebugRenderer();
        tMap = new TmxMapLoader().load("PeetytheBeefy1.tmx");
        otmr = new OrthogonalTiledMapRenderer(tMap);

        playerBody = new Box2D(world, "PLAYER", Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 32, 32);

        TiledPolyLines.parseTiledObjectLayer(world, tMap.getLayers().get("collision-layer").getObjects());
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        world.step(1 / 60f, 6, 2);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        move();

        otmr.setView(camera);
        otmr.render();
        b2dr.render(world, camera.combined.scl(32));
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
    public void move() {
        float fHForce = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            fHForce -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            fHForce += 1;
        }
        if(playerBody.body.getLinearVelocity().y == 0) {
            nJumpCount = 2;
            nJumpInterval = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
           if(nJumpCount == 2) {
               playerBody.body.applyForceToCenter(0, 500, false);
               isCounterStart = true;
           }
           if(nJumpCount == 1) {
               if(nJumpInterval >= 20) {
                   playerBody.body.setLinearVelocity(0,0);
                   playerBody.body.applyForceToCenter(0,400, false);
                   nJumpInterval = 0;
               }
           }
           if(nJumpCount > 0) {
               nJumpCount--;
           }
        }
        if(isCounterStart) {
            nJumpInterval++;
            if(nJumpInterval >= 20) {
                isCounterStart = false;
            }
        }
        if (playerBody.body.getPosition().y < 0) {
//            player.getPosition().set(player.getPosition().x, 100);
            playerBody.body.setTransform(playerBody.body.getPosition().x, Gdx.graphics.getHeight() / 32, 0);
            //System.out.println(playerBody.getPosition());
        }
        playerBody.body.setLinearVelocity(fHForce * 5, playerBody.body.getLinearVelocity().y);

    }

    @Override
    public void resize(int i, int i1) {
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
}
