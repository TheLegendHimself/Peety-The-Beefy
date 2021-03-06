/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gdx.peetythebeefy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import gdx.peetythebeefy.cookiecutters.*;

import static gdx.peetythebeefy.PeetyTheBeefy.assetManager;

import java.util.ArrayList;

import static gdx.peetythebeefy.cookiecutters.Constants.PPM;
import static gdx.peetythebeefy.cookiecutters.Constants.isPlayerDead;


public class ScrLvl1 implements Screen, InputProcessor {

    PeetyTheBeefy game;
    SpriteBatch batch, fixedBatch;
    FreeTypeFontGenerator generator;
    FreeTypeFontParameter parameter;
    BitmapFont font;
    Text tLvl1;
    TextBox tbCharacter;
    PlayerGUI pGUI;
    ShapeRenderer SR;
    World world;
    float fX, fY, fW, fH;
    EntityCreation ecPlayer;
    OrthographicCamera camera;
    OrthogonalTiledMapRenderer otmr;
    ArrayList<Buttons> alButtons = new ArrayList<Buttons>();
    ArrayList<EntityCreation> alBullet = new ArrayList<EntityCreation>();
    ArrayList<EntityCreation> alEnemy = new ArrayList<EntityCreation>();
    ArrayList<Text> alDialogue = new ArrayList<Text>();
    TiledMap tMapLvl1;
    TiledPolyLines tplLvl1;
    Vector2 v2Target, vMousePosition;
    int nLevelHeight, nLevelWidth, nSpawnrate = 0, nCount = 0, nEnemies = 0, nMaxEnemies = 2, nWaveCount = 1, nDialogue = 0, nCharacter, nDialogueDelay = 0;
    Texture txBackground, txSky;
    boolean isDialogueStart, isDialogueDone, isLevelDialogue = true, isBack;
    static boolean isChangedToLvl2 = false;
    static float fAlpha = 1, fTransitWidth = 0, fTransitHeight = 0;
    Sound sPew, sNoammo;

    public ScrLvl1(PeetyTheBeefy game) {
        this.game = game;
        this.batch = game.batch;
        this.SR = game.SR;
        this.font = game.font;
        //Drawing things like GUI requires fixedBatch (not updated with camera)
        fixedBatch = new SpriteBatch();
        this.camera = game.camera;
        this.parameter = game.parameter;
        world = new World(new Vector2(0f, -18f), false);
        world.setContactListener(new ContactListener1());
        generator = new FreeTypeFontGenerator(Gdx.files.internal("slkscr.ttf"));
        world.setVelocityThreshold(0f);
        fX = Constants.SCREENWIDTH / 2;
        fY = Constants.SCREENHEIGHT / 2;
        fW = 32;
        fH = 32;
        txBackground = assetManager.get("level1Background.png");
        txSky = assetManager.get("sky.png");
        sPew = assetManager.get("sound/Pew.mp3", Sound.class);
        sNoammo = assetManager.get("sound/No ammo.mp3", Sound.class);

        createButtons();
        createText();
        tMapLvl1 = new TmxMapLoader().load("PeetytheBeefy1.tmx");
        tplLvl1 = new TiledPolyLines(world, tMapLvl1.getLayers().get("collision-layer").getObjects(), Constants.BIT_WALL,
                (short) (Constants.BIT_PLAYER | Constants.BIT_BULLET | Constants.BIT_ENEMY), (short) 0);
        otmr = new OrthogonalTiledMapRenderer(tMapLvl1);

        //Gets the properties from the tiledmap, used for the Camera Boundary
        MapProperties props = tMapLvl1.getProperties();
        nLevelWidth = props.get("width", Integer.class);
        nLevelHeight = props.get("height", Integer.class);

        //Entity Creation handles all creation of objects
        ecPlayer = new EntityCreation(world, "PLAYER", fX, fY, fW, fH, batch, 9.2f, 0, 0,
                0, 4, 6, "PTBsprite.png", 1,
                Constants.BIT_PLAYER, (short) (Constants.BIT_WALL | Constants.BIT_ENEMY), (short) 0, new Vector2(0, 0), 4,
                nLevelWidth, nLevelHeight);
        v2Target = new Vector2(nLevelWidth * PPM / 2, nLevelHeight * PPM / 2);
        tbCharacter = new TextBox(fixedBatch, nCharacter, isDialogueStart, font, generator, parameter, alDialogue.get(0));
        pGUI = new PlayerGUI(fixedBatch, batch, ecPlayer.body.getPosition(), new Vector2(0, 0), font, generator, parameter);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        tLvl1 = new Text(generator, parameter, font, "Peety The Beefy Takes It Easy", 32, Gdx.graphics.getWidth() / 2,
                Gdx.graphics.getHeight() / 2, fixedBatch, 1, 1, "Level");
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        world.step(1 / 60f, 6, 2);
        cameraUpdate();
        batch.setProjectionMatrix(camera.combined);
        lvl1Reset();

        fixedBatch.begin();
        fixedBatch.draw(txSky, 0, 0, Constants.SCREENWIDTH, Constants.SCREENHEIGHT);
        fixedBatch.end();

        batch.begin();
        batch.draw(txBackground, 0, 0, Constants.SCREENWIDTH, Constants.SCREENHEIGHT);
        batch.end();

        if (isChangedToLvl2) { //puts the player outside the door so they don't trigger the transition
            ecPlayer.body.setTransform((float) (690 / PPM), (float) (450 / PPM), 0);
            isChangedToLvl2 = false;
        }
        ecPlayer.Update();
        moveEnemy();

        otmr.setView(camera);
        otmr.render();


        if (Constants.isGameStart && !isBack) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) { //button is currently being drawn behind the tiled map
                game.fMouseX = Constants.SCREENWIDTH; // just moves mouse away from button
                game.fMouseY = Constants.SCREENHEIGHT;
                Constants.isShowing = !Constants.isShowing; //its like a pop up menu, if you want to go back press p to bring up back button
            }
            if (!isDialogueStart) {
                playerShoot(ecPlayer.body.getPosition(), vMousePosition, alBullet, world, nLevelWidth, nLevelHeight);
            }
            playerDeath();

            //Bullet Collection
            for (int i = 0; i < alBullet.size(); i++) {
                alBullet.get(i).Update();
                if (Constants.isShowing && isDialogueStart) {
                    alBullet.get(i).body.setAwake(false);
                } else if (!Constants.isShowing && !alBullet.get(i).isStuck) {
                    alBullet.get(i).body.setAwake(true);
                }
                if (alBullet.get(i).canCollect) {
                    if (ecPlayer.body.getPosition().x - (ecPlayer.body.getMass() / 2) <= alBullet.get(i).body.getPosition().x + (alBullet.get(i).body.getMass() * 2) &&
                            ecPlayer.body.getPosition().x + (ecPlayer.body.getMass() / 2) >= alBullet.get(i).body.getPosition().x - (alBullet.get(i).body.getMass() * 2) &&
                            ecPlayer.body.getPosition().y - (ecPlayer.body.getMass() / 2) <= alBullet.get(i).body.getPosition().y + (alBullet.get(i).body.getMass() * 2) &&
                            ecPlayer.body.getPosition().y + (ecPlayer.body.getMass() / 2) >= alBullet.get(i).body.getPosition().y - (alBullet.get(i).body.getMass() * 2)
                            || isPlayerDead) {
                        alBullet.get(i).world.destroyBody(alBullet.get(i).body);
                        Constants.nBulletCount++;
                        alBullet.remove(i);
                    }
                }
            }
            tLvl1.Update();


            changeBox();

            //used for the gun following the mouse
            vMousePosition = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            pGUI.vMousePosition = vMousePosition;
            pGUI.v2PlayerPosition = ecPlayer.body.getPosition();
            if (!isDialogueStart) {
                pGUI.Update();
            } else {
                if (alDialogue.size() != 0) {
                    tbCharacter.Update();
                }
            }

            dialogueLogic();
        }


        if (!Constants.isGameStart || isDialogueStart) {
            ecPlayer.body.setAwake(false);
            ecPlayer.isMoving = false;
            if (Constants.isShowing && !isDialogueStart) {
                drawButtons();
            }
        } else {
            if (Constants.isShowing) {
                drawButtons();
                ecPlayer.body.setAwake(false);
                ecPlayer.isMoving = false;
            } else {
                ecPlayer.body.setAwake(true);
                ecPlayer.isMoving = true;
                if (tLvl1.isFinished && !isDialogueStart) {
                    createEnemy();
                }
            }
        }

        screenTransition();
        transitionBlock();

    }

    private void lvl1Reset() {
        if (game.isReset) {
            ecPlayer.body.setTransform(Gdx.graphics.getWidth() / 2 / PPM, Gdx.graphics.getHeight() / 2 / PPM, 0);
            Constants.isFadeOut[0] = true;
            fAlpha = 1;
            Constants.isGameStart = false;
            nEnemies = 0;
            nMaxEnemies = 3;
            nWaveCount = 1;
            nSpawnrate = 0;
            game.isReset = false;
        }
    }

    private void dialogueLogic() {
        tbCharacter.isTransition = isDialogueStart;
        if (alDialogue.size() != 0) {
            tbCharacter.tText = alDialogue.get(0);
            if (tLvl1.isFinished && !isDialogueDone) {
                alDialogue.get(0).Update();
                isDialogueStart = true;
            }
            if (alDialogue.get(0).isFinished && tbCharacter.fOpacity2 >= 1) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    if (isDialogueStart) {
                        nDialogue++;
                    }
                    if (nDialogue == 5 || nDialogue == 15 || nDialogue == 19) {
                        System.out.println("here");
                        isDialogueDone = true;
                        isDialogueStart = false;
                    }
                    tbCharacter.fOpacity2 = 0;
                    alDialogue.get(0).sbDisplay.delete(0, alDialogue.get(0).sbDisplay.length());
                    alDialogue.remove(0);
                }
            }
            if (game.isLeveledup && isLevelDialogue && nDialogue < 14) {
                nDialogueDelay++;
                if (nDialogueDelay >= 75) {
                    tbCharacter.fOpacity = 0;
                    isDialogueDone = false;
                    nDialogueDelay = 0;
                    isLevelDialogue = false;
                }
            }
        }
    }

    private void createEnemy() { //Makes the enemies in entity creation, based on spawn locations and when they spawn
        if (nSpawnrate > 200 && nEnemies < nMaxEnemies && nWaveCount != 3) {
            int nSpawnLocation = (int) (Math.random() * 3 + 1);
            if (nSpawnLocation == 1) {
                fX = Gdx.graphics.getWidth() / 2 - 328;
                fY = Gdx.graphics.getHeight() / 2 + 50;
            } else if (nSpawnLocation == 2) {
                fX = Gdx.graphics.getWidth() / 2 + 328;
                fY = Gdx.graphics.getHeight() / 2 + 50;
            } else if (nSpawnLocation == 3) {
                fX = Gdx.graphics.getWidth() / 2;
                fY = Gdx.graphics.getHeight() / 2 + 160;
            }
            nEnemies++;
            alEnemy.add(new EntityCreation(world, "ENEMY", fX, fY, fW - 10, fH, batch, 9.2f,
                    0, 0, 0, 4, 1, "MTMsprite.png", 2,
                    Constants.BIT_ENEMY, (short) (Constants.BIT_WALL | Constants.BIT_PLAYER | Constants.BIT_BULLET | Constants.BIT_ENEMY), (short) 0,
                    new Vector2(0, 0), 2, nLevelWidth, nLevelHeight));
            nCount++;
            nSpawnrate = 0;
        }
        if (alEnemy.size() == 0 && nEnemies == nMaxEnemies) {
            nWaveCount++;
            nMaxEnemies++;
            nEnemies = 0;
        }
        if (nWaveCount == 3 && alEnemy.size() == 0 && isDialogueDone) {
            nDialogueDelay++;
            if (nDialogueDelay >= 75) {
                tbCharacter.fOpacity = 0;
                nDialogueDelay = 0;
                isDialogueDone = false;
            }
        }
        if (nWaveCount == 3 && !isPlayerDead && (ecPlayer.body.getPosition().x * PPM > 710 && ecPlayer.body.getPosition().x * PPM < 750) &&
                (ecPlayer.body.getPosition().y * PPM > 410 && ecPlayer.body.getPosition().y * PPM < 480)) {
            Constants.isLevelFinished[0] = true;
            Constants.isLevelUnlocked[1] = true;
            Constants.isGameStart = false;
            Constants.isFadeIn[1] = true;
        }
        nSpawnrate++;
    }

    private void moveEnemy() {
        for (int i = 0; i < alEnemy.size(); i++) {
            alEnemy.get(i).Update();
            if (Constants.isShowing || !Constants.isGameStart || isDialogueStart) {
                alEnemy.get(i).body.setAwake(false);
                alEnemy.get(i).isMoving = false;
            } else {
                alEnemy.get(i).body.setAwake(true);
                alEnemy.get(i).isMoving = true;
            }
            if (ecPlayer.body.getPosition().y < alEnemy.get(i).body.getPosition().y + 100 / PPM &&
                    ecPlayer.body.getPosition().y >= alEnemy.get(i).body.getPosition().y ||
                    ecPlayer.body.getPosition().y > alEnemy.get(i).body.getPosition().y - 100 / PPM &&
                            ecPlayer.body.getPosition().y < alEnemy.get(i).body.getPosition().y) {
                if (ecPlayer.body.getPosition().x < alEnemy.get(i).body.getPosition().x + 100 / PPM &&
                        ecPlayer.body.getPosition().x > alEnemy.get(i).body.getPosition().x) {
                    alEnemy.get(i).isInRange = true;
                } else if (ecPlayer.body.getPosition().x > alEnemy.get(i).body.getPosition().x - 100 / PPM &&
                        ecPlayer.body.getPosition().x < alEnemy.get(i).body.getPosition().x) {
                    alEnemy.get(i).isInRange = true;
                }
            } else {
                alEnemy.get(i).isInRange = false;
            }
            if (alEnemy.get(i).isDeath || isPlayerDead) {
                alEnemy.get(i).world.destroyBody(alEnemy.get(i).body);
                if(!isPlayerDead) {
                    Constants.fBeefyProgression++;
                }
                nCount--;
                alEnemy.remove(i);
            }

        }
    }

    private void cameraUpdate() {
        //CameraStyles.java explains camera movement
        CameraStyles.lerpAverageBetweenTargets(camera, v2Target, ecPlayer.body.getPosition().scl(PPM), false);
        float fStartX = camera.viewportWidth / 2;
        float fStartY = camera.viewportHeight / 2;
        camera.zoom = 0.8f;
        CameraStyles.boundary(camera, fStartX, fStartY, Constants.SCREENWIDTH * (float) 0.4, Constants.SCREENWIDTH * (float) 0.6, nLevelHeight * PPM);
        camera.update();
    }

    private void createButtons() {
        alButtons.add(new Buttons("backButton", fixedBatch, -8, 0, 96, 32));
    }

    private void drawButtons() {
        for(int i = 0; i < alButtons.size(); i++) {
            alButtons.get(i).Update();
            if (game.fMouseX > alButtons.get(i).fX && game.fMouseX < alButtons.get(i).fX + alButtons.get(i).fW
                    && game.fMouseY > alButtons.get(i).fY && game.fMouseY < alButtons.get(i).fY + alButtons.get(i).fH) {
                System.out.println("move to main menu ");
                game.fMouseX = Constants.SCREENWIDTH; // just moves mouse away from button
                game.fMouseY = Constants.SCREENHEIGHT;
                Constants.isGameStart = false;
                Constants.isShowing = false;
                ScrMainMenu.fAlpha = 0;
                ScrStageSelect.fAlpha = 0;
                Constants.nCurrentScreen = 3;
                isBack = true;
            }
        }
    }


    private void playerDeath() {
        if (isPlayerDead && alEnemy.size() == 0 && alBullet.size() == 0) {
            Constants.nCurrentScreen = 3;
            game.updateScreen(15);
        }
    }

    public void playerShoot(Vector2 playerPosition, Vector2 mousePosition, ArrayList<EntityCreation> Bullets, World world, int nLevelWidth, int nLevelHeight) {
        //moved mouse position vector to draw because we need it for other things
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched()) {
            if(!Constants.isShowing) {
                if (Constants.nBulletCount > 0) {
                    Vector2 vbulletPosition = new Vector2(playerPosition.x * PPM, playerPosition.y * PPM);
                    Vector2 vDir = mousePosition.sub(vbulletPosition);
                    Bullets.add(new EntityCreation(world, "Bullet", vbulletPosition.x, vbulletPosition.y, fW, fH, batch, 9.2f, 0, 0,
                            0, 4, 6, "bulletTexture.png", 3,
                            Constants.BIT_BULLET, (short) (Constants.BIT_WALL | Constants.BIT_BULLET | Constants.BIT_ENEMY), (short) 0,
                            vDir, 0, nLevelWidth, nLevelHeight));
                    sPew.play(0.3f);
                    Constants.nBulletCount--;
                } else if (Constants.nBulletCount <= 0) {
                    sNoammo.play();
                }
            }
        }
    }

    private void transitionBlock() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        SR.begin(ShapeRenderer.ShapeType.Filled);
        SR.setColor(new Color(0f, 0f, 0f, fAlpha));
        SR.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        SR.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        SR.begin(ShapeRenderer.ShapeType.Filled);
        SR.setColor(0, 0, 0, 1);
        SR.rect(Gdx.graphics.getWidth() / 2 - (fTransitWidth / 2), Gdx.graphics.getHeight() / 2 - (fTransitHeight / 2), fTransitWidth, fTransitHeight);
        SR.end();
    }

    private void screenTransition() {
        if (Constants.isFadeOut[0] && fAlpha > 0) {
            fAlpha -= 0.02f;
        }
        if (fAlpha < 0 && !Constants.isFadeIn[1] && !isBack) {
            Constants.isFadeOut[0] = false;
            Constants.isGameStart = true;
        }
        if (isBack) {
            if (fAlpha < 1) {
                fAlpha += 0.02;
            } else if (fAlpha >= 1) {
                game.mGame.stop();
                game.updateScreen(0);
                game.mBackground.setLooping(true);
                game.mBackground.setVolume(0.1f);
                game.mBackground.play();
                isBack = false;
            }
        }
        if (Constants.isFadeIn[1] && fTransitWidth <= Gdx.graphics.getWidth()) {
            fTransitHeight += 16;
            fTransitWidth += 16;
        }
        if (fTransitWidth > Gdx.graphics.getWidth()) {
            Constants.isFadeOut[1] = true;
            Constants.isFadeIn[1] = false;
            ScrLvl2.fTransitWidth = Gdx.graphics.getWidth() * (float) 1.5;
            ScrLvl2.fTransitHeight = Gdx.graphics.getHeight() * (float) 1.5;
            game.updateScreen(4);
        }
    }

    private void createText() {
        alDialogue.add(new Text(generator, parameter, font, "Peety: Wow I am an utter GOD! I can't believe how great I am. " +
                "I know for a fact that I am not delusional, don't get me twisted.", 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: LMAO!!! Yo you hear this mans? Absolute buffoonery! Bro trust, I'm gon sit you down boi.",
                26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: Whatchu mean B? Y'all think you are up to my level? I am a king and you are the peasants!",
                26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: Dude... that's too far man. You know that my parents were peasants! OK. That's. It. Enough's Enough!",
                26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: I can't wait to show these Matty The Meaties whose the beefiest in the school!"
                , 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: Yo, what the hell? The BLV thingy went up by 1."
                , 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: Yea didn't you know that you have a Beefiness Level."
                , 26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: Wait a Beefi-what?"
                , 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: Wait you don't know what a Beefiness Level is?"
                , 26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: Hell nah b you trippin'."
                , 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: ......................... Your Beefiness Level, BLV, is what determines your muscle thickness. " +
                "The higher you BLV the thicker your muscles.", 26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: Also, the more levels you gain the more damage you deal. If you die then your muscles shrink, lowering your BLV.",
                26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: Wow that sounds painful!", 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: Yea the doctor said it was terminal.", 26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: Wait its Termin...... Well whatever enough of this small talk!", 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: LMAOOOOOOO! Yo y'all trash at this game. Just uninstall now. While you're at it delete system 32."
                , 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "Matty: Relax bro... I wasn't even using my maximum power. Catch me in level 2 and I'll show you who's boss."
                , 26, 30, 200, fixedBatch, 2, 15, "Matty"));
        alDialogue.add(new Text(generator, parameter, font, "Peety: Pffftttt... Alright Matty I'll see about that."
                , 26, 30, 200, fixedBatch, 2, 15, "Peety"));
        alDialogue.add(new Text(generator, parameter, font, "To proceed to the next level just run into the doors. " +
                "Yes I sincerely apologize for the terrible level design (will be the same for all levels)", 26,
                30, 200, fixedBatch, 2, 15, "TextBox"));
    }

    private void changeBox() {
        if (alDialogue.size() != 0) {
            if (alDialogue.get(0).sId.contentEquals("Peety")) {
                tbCharacter.nType = 1;
            } else if (alDialogue.get(0).sId.contentEquals("Matty")) {
                tbCharacter.nType = 2;
            } else if (alDialogue.get(0).sId.contentEquals("TextBox")) {
                tbCharacter.nType = 3;
            }
        }
    }

    @Override
    public void resize(int i, int i1) {
        camera.setToOrtho(false, Constants.SCREENWIDTH, Constants.SCREENHEIGHT);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        fixedBatch.dispose();
        generator.dispose();
        world.dispose();
        otmr.dispose();
        tMapLvl1.dispose();
    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        if (Constants.isShowing) {  // going to have to set fMouseX and fMouseY here because of the problem with setting the input processor
            game.fMouseX = Gdx.input.getX();
            game.fMouseY = Constants.SCREENHEIGHT - Gdx.input.getY();
        }
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }
}