/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gdx.peetythebeefy.cookiecutters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import static gdx.peetythebeefy.PeetyTheBeefy.assetManager;

/**
 * @author benny
 */
//This class handles every entity created in the game
//This includes player, enemies and bullets
public class EntityCreation {

    public Texture txSheet;
    public Sprite sprAni;
    public Animation[] araniCharacter;
    public Body body;
    public String sId, sTexture;
    public int nJumpInterval, nJumpCount, nJump, nDir = 1, nPos, nFrame, nSpriteDir, nRows, nColumns, nCount = 0,
            nShootCount = 0, nType, nPlatDir = 1;
    public boolean isCounterStart, isDeath = false, canCollect = false, isStuck, isInRange, isMoving = true, canDamage = true;
    public float fAniSpeed, fHealth;
    SpriteBatch batch;
    TextureRegion trTemp;
    Vector2 vDir;
    public World world;
    float nLevelWidth, nLevelHeight;


    public EntityCreation(World world, String sId, float fX, float fY, float fWidth, float fHeight, SpriteBatch batch, float fAniSpeed,
                          int nPos, int nFrame, int nSpriteDir, int nRows, int nColumns, String sTexture, int nType, short cBits, short mBits,
                          short gIndex, Vector2 vDir, float fHealth, float _nLevelWidth, float _nLevelHeight) {

        txSheet = assetManager.get(sTexture);
        araniCharacter = new Animation[nRows * nColumns];
        this.sId = sId;
        this.batch = batch;
        this.fAniSpeed = fAniSpeed;
        this.nPos = nPos;
        this.nFrame = nFrame;
        this.nSpriteDir = nSpriteDir;
        this.nRows = nRows;
        this.nColumns = nColumns;
        this.sTexture = sTexture;
        this.nType = nType;
        this.vDir = vDir;
        this.world = world;
        this.fHealth = fHealth;
        this.nLevelWidth = _nLevelWidth;
        this.nLevelHeight = _nLevelHeight;
        if(sId.contentEquals("EnemyBullet")) {
            vDir.setLength(0.4f);
        } else {
            vDir.setLength(0.7f);
        }
        this.createBody(world, fX, fY, fWidth, fHeight, cBits, mBits, gIndex);
        this.playerSprite(araniCharacter);
    }

    public void Update() {
        // 1 - Player, 2 - Enemy, 3 - Bullet, 4 - platform
        if (nType == 2) {
            enemyMove();
            frameAnimation();
            drawAnimation();
        } else if (nType == 1) {
            playerMove();
            frameAnimation();
            drawAnimation();
        } else if (nType == 3) {
            bulletMove();
            drawTexture();
        }
        if (body.getPosition().y < 0) {
            body.setTransform(body.getPosition().x, nLevelHeight, 0);
        } else if (body.getPosition().y > nLevelHeight) {
            body.setTransform(body.getPosition().x, 0, 0);
        }
        if (body.getPosition().x < 0) {
            body.setTransform(nLevelWidth, body.getPosition().y, 0);
        } else if (body.getPosition().x > nLevelWidth) {
            body.setTransform(0, body.getPosition().y, 0);
        }
    }

    private void createBody(World world, float fX, float fY, float fWidth, float fHeight, short cBits, short mBits, short gIndex) {
        BodyDef bdef = new BodyDef();
        bdef.fixedRotation = true;
        if (nType == 4) {
            bdef.type = BodyDef.BodyType.StaticBody;
        } else {
            bdef.type = BodyDef.BodyType.DynamicBody;
        }
        bdef.position.set(fX / 32.0F, fY / 32.0F);
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        if (nType != 3) {
            shape.setAsBox(fWidth / 32.0F / 2.0F, fHeight / 32.0F / 2.0F);
            fixtureDef.friction = 0.01f;
            bdef.fixedRotation = true;
        } else {
            shape.setAsBox(fWidth / 32.0f / 8.0f, fHeight / 32.0f / 8.0f);
            fixtureDef.friction = 10f;
            fixtureDef.restitution = 0.1f;
            bdef.bullet = true;
        }
        fixtureDef.filter.categoryBits = cBits;
        fixtureDef.filter.maskBits = mBits;
        fixtureDef.filter.groupIndex = gIndex;
        this.body = world.createBody(bdef);
        this.body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    private void playerMove() {
        float fHForce = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            fHForce -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            fHForce += 1;
        }
        if (body.getLinearVelocity().y == 0) {
            nJumpCount = 2;
            nJumpInterval = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (nJumpCount == 2) {
                body.applyForceToCenter(0, 500, false);
                isCounterStart = true;
            }
            if (nJumpCount == 1) {
                if (nJumpInterval >= 20) {
                    body.setLinearVelocity(0, 0);
                    body.applyForceToCenter(0, 400, false);
                    nJumpInterval = 0;
                }
            }
            if (nJumpCount > 0) {
                nJumpCount--;
            }
        }
        if (isCounterStart) {
            nJumpInterval++;
            if (nJumpInterval >= 20) {
                isCounterStart = false;
            }
        }
        body.setLinearVelocity(fHForce * 5, body.getLinearVelocity().y);

    }

    private void enemyMove() {
        float fhForce = 0;
        if (body.getLinearVelocity().x == 0) {
            if (nDir == 1) {
                nDir = 2;
            } else if (nDir == 2) {
                nDir = 1;
            }
        }
        nJump = (int) (Math.random() * 99 + 1);
        if (nDir == 1) {
            fhForce = (isInRange) ? (float) 1.5 : 1;
        } else if (nDir == 2) {
            fhForce = (isInRange) ? (float) -1.5 : -1;
        }
        if (nJump == 5 && body.getLinearVelocity().y == 0) {
            body.applyForceToCenter(0, 350, false);
        }
        body.setLinearVelocity(fhForce * 2, body.getLinearVelocity().y);
        if (fHealth <= 0) {
            isDeath = true;
        }
    }

    private void bulletMove() {
        if (nCount < 2) {
            body.applyLinearImpulse(vDir, body.getPosition(), false);
        }
        if (nCount >= 90) {
            canCollect = true;
        }
        if (isStuck) {
            body.setAwake(false);
        }
//        if(sId.contentEquals("EnemyBullet")) {
//            body.setLinearVelocity(body.getLinearVelocity().x, vDir.y);
//        }
        nCount++;
    }


    //          ANIMATION           //

    private Animation[] playerSprite(Animation araniCharacter[]) {
        int nW, nH, nSx, nSy;
        nW = txSheet.getWidth() / nRows;
        nH = txSheet.getHeight() / nColumns;
        for (int i = 0; i < nColumns; i++) {
            Sprite[] arSprPeety = new Sprite[nRows];
            for (int j = 0; j < nRows; j++) {
                nSx = j * nW;
                nSy = i * nH;
                sprAni = new Sprite(txSheet, nSx, nSy, nW, nH);
                arSprPeety[j] = new Sprite(sprAni);
            }
            araniCharacter[i] = new Animation<TextureRegion>(fAniSpeed, arSprPeety);
        }
        return araniCharacter;
    }

    private void drawAnimation() {
        trTemp = (TextureRegion) araniCharacter[nPos].getKeyFrame(nFrame, true);
        batch.begin();
        batch.draw(trTemp, body.getPosition().x * 32 - 32 / 2, body.getPosition().y * 32 - 32 / 2, 32, 32);
        batch.end();
    }

    private void drawTexture() {
        batch.begin();
        batch.draw(txSheet, body.getPosition().x * 32 - 8 / 2, body.getPosition().y * 32 - 8 / 2, 8, 8);
        batch.end();
    }

    private void frameAnimation() {
        if (nType != 3) {
            if (nType != 2 && isMoving) {
                if (body.getLinearVelocity().x != 0 || body.getLinearVelocity().y != 0) {
                    nFrame++;
                }
                if (nFrame > 32) {
                    nFrame = 0;
                }

                if (Gdx.input.isKeyPressed(Input.Keys.A) && body.getLinearVelocity().y >= 0) { //going left
                    nSpriteDir = 1;
                    nPos = 1;
                    fAniSpeed = 9.2f;
                    araniCharacter[nPos].setFrameDuration(fAniSpeed);
                    //playerSprite(aniPeety.fAniSpeed);
                } else if (Gdx.input.isKeyPressed(Input.Keys.D) && body.getLinearVelocity().y >= 0) { //going right
                    nSpriteDir = 0;
                    nPos = 0;
                    fAniSpeed = 9.2f;
                    araniCharacter[nPos].setFrameDuration(fAniSpeed);
                }
                if (body.getLinearVelocity().y < 0) { //falling animation + speed up
                    nPos = 5;
                    if (fAniSpeed >= 2.3f) {
                        araniCharacter[nPos].setFrameDuration(fAniSpeed -= 0.1f);
                    }
                }
                if (body.getLinearVelocity().x == 0 && body.getLinearVelocity().y == 0) { //reset to last direction when stationary
                    if (nSpriteDir == 1) {
                        nPos = 1;
                    } else if (nSpriteDir == 0) {
                        nPos = 0;
                    }
                    fAniSpeed = 9.2f;
                    araniCharacter[nPos].setFrameDuration(fAniSpeed);
                }
            } else {
                if (nFrame > 32) {
                    nFrame = 0;
                }
                if (isMoving) {
                    nFrame++;
                }
            }
        }
    }

}
