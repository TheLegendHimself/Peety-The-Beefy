package gdx.peetythebeefy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import gdx.peetythebeefy.cookiecutters.Constants;

public class PeetyTheBeefy extends Game implements InputProcessor {

    SpriteBatch batch;
    ScrMainMenu scrMainMenu;
    ScrStageSelect scrStageSelect;
    ScrControls scrControls;
    ScrLvl1 scrLvl1;
    ScrLvl2 scrLvl2;
    float fMouseX, fMouseY;

    @Override
    public void create() {
        batch = new SpriteBatch();

        scrMainMenu = new ScrMainMenu(this);
        scrStageSelect = new ScrStageSelect(this);
        scrLvl1 = new ScrLvl1(this);
        scrControls = new ScrControls(this);
        scrLvl2 = new ScrLvl2(this);

        Gdx.input.setInputProcessor(this);
        setScreen(scrMainMenu);
//      Buttons();       
    }

    @Override
    public void render() {
        super.render();
//        drawButtons();
//        System.out.println(fMouseX + " " + fMouseY);
    }

    public void updateScreen(int nScreen) {
        //lol
        if (nScreen == 0) {
            setScreen(scrMainMenu);
        } else if (nScreen == 1) {
            setScreen(scrStageSelect);
        } else if (nScreen == 2) {
            setScreen(scrControls);
        } else if (nScreen == 3) {
            setScreen(scrLvl1);
        } else if (nScreen == 4) {
            setScreen(scrLvl2);
        }

    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
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
        fMouseX = Gdx.input.getX();
        fMouseY = Constants.SCREENHEIGHT - Gdx.input.getY();
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
