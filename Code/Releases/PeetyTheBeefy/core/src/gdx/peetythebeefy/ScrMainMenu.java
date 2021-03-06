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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import gdx.peetythebeefy.cookiecutters.Buttons;
import static gdx.peetythebeefy.PeetyTheBeefy.assetManager;

import java.util.ArrayList;

import gdx.peetythebeefy.cookiecutters.Constants;

/**
 * @author tn200
 */
public class ScrMainMenu implements Screen, InputProcessor {

    PeetyTheBeefy game;
    SpriteBatch batch;
    ShapeRenderer SR;
    Texture texMenu;
    OrthographicCamera camera;
    ArrayList<Buttons> alButtons = new ArrayList<Buttons>();
    Vector2 v2MousePosition;
    Sound sHover, sPlay;
    float fAudio;
    boolean isStart;
    static float fAlpha = 0;


    public ScrMainMenu(PeetyTheBeefy game) {
        this.game = game;
        this.batch = game.batch;
        this.camera = game.camera;
        this.SR = game.SR;
        fAudio = 0.1f;
        texMenu = assetManager.get("mainMenu.png");
        sHover = assetManager.get("sound/Hover.mp3", Sound.class);
        sPlay = assetManager.get("sound/Play.mp3", Sound.class);
        Constants.isLevelUnlocked[0] = true;
        createButtons();
        for (int i = 1; i < 12; i++) {
            Constants.isLevelUnlocked[i] = false;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        camera.zoom = 1f;
    }

    @Override
    public void render(float f) {

        Gdx.gl.glClearColor(1, 1, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        game.mBackground.setLooping(true);
        game.mBackground.setVolume(fAudio);
        game.mBackground.play();
        batch.begin();

        batch.draw(texMenu, 0, 0);

        batch.end();

        v2MousePosition = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        drawButtons();
        screenTransition();
        transitionBlock();

    }

    private void createButtons() {
        alButtons.add(new Buttons("playButton", batch, -8, Constants.SCREENHEIGHT / 2, 192, 64));
        alButtons.add(new Buttons("controlsButton", batch, -8, Constants.SCREENHEIGHT / 2 - 128, 192, 64));
        alButtons.add(new Buttons("stagesButton", batch, -8, Constants.SCREENHEIGHT / 2 - 256, 192, 64));
    }

    private void drawButtons() {
        for (int i = 0; i < alButtons.size(); i++) {
            alButtons.get(i).Update();
            if (game.fMouseX > alButtons.get(i).fX && game.fMouseX < alButtons.get(i).fX + alButtons.get(i).fW
                    && game.fMouseY > alButtons.get(i).fY && game.fMouseY < alButtons.get(i).fY + alButtons.get(i).fH && isStart) {
                if (i == 0) {
                    System.out.println("moves to Lvl 1 screen");
                    sPlay.play();
                    Constants.isShowing = false;
                    Constants.isPlayerDead = false;
                    Constants.isFadeIn[0] = true;
                    ScrLvl1.fAlpha = 1;
                    Constants.isGameStart = false;
                } else if (i == 1) {
                    System.out.println("moves to the controls");
                    game.updateScreen(2);
                } else if (i == 2) {
                    System.out.println("moves to the stage select");
                    game.updateScreen(1);
                }
                game.fMouseX = Constants.SCREENWIDTH; // just moves mouse away from button
                game.fMouseY = Constants.SCREENHEIGHT;
            }
            if (!Constants.isFadeIn[0]) {
                if (v2MousePosition.x > alButtons.get(i).fX && v2MousePosition.x < alButtons.get(i).fX + alButtons.get(i).fW
                        && v2MousePosition.y > alButtons.get(i).fY && v2MousePosition.y < alButtons.get(i).fY + alButtons.get(i).fH) {
                    // alButtons.get(i).sprButton.setAlpha(250);
                    alButtons.get(i).sprButton.setColor(1, 1, 1, 1);
                    alButtons.get(i).nCount ++;
                    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                        alButtons.get(i).sprButton.setColor(Color.GRAY);
                        alButtons.get(i).sprButton.setAlpha(10);
                    } else {
                        if(alButtons.get(i).nCount <=1 ) {
                            sHover.play(0.2f);
                        }
                    }
                } else {
                    alButtons.get(i).sprButton.setColor(0.8f, 0.8f, 0.8f, 0.8f);
                    alButtons.get(i).nCount = 0;
                }
            }
        }
    }

    private void screenTransition() {
        if (Constants.isFadeIn[0] && fAlpha < 1) {
            fAlpha += 0.005f;
            fAudio -= 0.0002f;
        }
        if (fAlpha > 1) {
            game.mBackground.stop();
            game.updateScreen(Constants.nCurrentScreen);
            Constants.isFadeIn[0] = false;
            Constants.isFadeOut[Constants.nCurrentScreen - 3] = true;
            game.mGame.setLooping(true);
            game.mGame.setVolume(0.1f);
            game.mGame.play();
        }
        if (Constants.isMainMenuOut && fAlpha > 0) {
            fAlpha -= 0.02f;
        }
        if (fAlpha <= 0) {
            isStart = true;
            Constants.isMainMenuOut = false;
        }
    }

    private void transitionBlock() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        SR.begin(ShapeType.Filled);
        SR.setColor(new Color(0f, 0f, 0f, fAlpha));
        SR.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        SR.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
        game.fMouseX = Gdx.input.getX();
        game.fMouseY = Constants.SCREENHEIGHT - Gdx.input.getY();
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
