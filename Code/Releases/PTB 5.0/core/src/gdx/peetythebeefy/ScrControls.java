/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gdx.peetythebeefy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import gdx.peetythebeefy.cookiecutters.Buttons;
import java.util.ArrayList;
import gdx.peetythebeefy.cookiecutters.Constants;
import gdx.peetythebeefy.cookiecutters.Buttons;

/**
 *
 * @author tn200
 */
public class ScrControls implements Screen {

    PeetyTheBeefy game;
    SpriteBatch batch;
    Texture txMenuNew, txMenuMain, txControls;
    float fMainX, fNewX;
    ArrayList<Buttons> alButtons = new ArrayList<Buttons>();

    public ScrControls(PeetyTheBeefy game) {
        this.game = game;
        this.batch = game.batch;
        txMenuMain = new Texture("mainMenu.png");
        txMenuNew = new Texture("mainMenu2.png");
        txControls = new Texture("Controls image.png");

    }

    @Override
    public void show() {
        fNewX = 768;
        fMainX = 0;
        createButtons();
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        screenTransition();

        drawButtons();
    }

    public void createButtons() {
        alButtons.add(new Buttons("backButton", batch, -8, 0, 96, 32));
    }

    public void drawButtons() {
        for (int i = 0; i < alButtons.size(); i++) {
            alButtons.get(i).Update();
            if (game.fMouseX > alButtons.get(i).fX && game.fMouseX < alButtons.get(i).fX + alButtons.get(i).fW
                    && game.fMouseY > alButtons.get(i).fY && game.fMouseY < alButtons.get(i).fY + alButtons.get(i).fH) {
                System.out.println("moves to main menu");
                game.updateScreen(0);
                game.fMouseX = Constants.SCREENWIDTH; // just moves mouse away from button
                game.fMouseY = Constants.SCREENHEIGHT;
            }
        }
    }

    @Override
    public void resize(int i, int i1) {
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
        txMenuMain.dispose();
        txMenuNew.dispose();
        txControls.dispose();
        batch.dispose();
    }

    public void screenTransition() {

        if (fMainX >= -768) {
            fMainX -= 16;
        }
        if (fNewX >= 16) {
            fNewX -= 16;
        }

        batch.begin();

        batch.draw(txMenuMain, fMainX, 0);
        batch.draw(txMenuNew, fNewX, 0);
        batch.draw(txControls, fNewX, 0);

        batch.end();

    }
}
