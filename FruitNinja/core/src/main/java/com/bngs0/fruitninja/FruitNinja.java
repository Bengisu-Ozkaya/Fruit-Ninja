package com.bngs0.fruitninja;

import static com.badlogic.gdx.math.MathUtils.random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import javax.swing.SpringLayout;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class FruitNinja extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private Texture background,apple,bill,ruby,cherry;
    FreeTypeFontGenerator fontGen;
    BitmapFont font;

    int lives = 0;
    int score = 0;
    double currentTime;
    double gameOverTime = -1.0f;
    Array<Fruit> fruitArray = new Array<Fruit>();
    float genCounter;
    private final float startGenSpeed = 1.1f;
    float genSpeed = startGenSpeed;

    //highscore
    int highScore;
    Preferences prefs;
    boolean first;


    @Override
    public void create() {
        first = true;

        batch = new SpriteBatch();
        background = new Texture("ninjabackground.png");
        apple = new Texture("apple.png");
        bill = new Texture("bill.png");
        ruby = new Texture("ruby.png");
        cherry = new Texture("cherry.png");
        Fruit.radius = Math.max(Gdx.graphics.getWidth(),Gdx.graphics.getHeight())/20f;

        Gdx.input.setInputProcessor(this);

        fontGen = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.color = Color.WHITE;
        params.size = 50;
        params.characters = "0123456789 HighScreCutoplay:.+-";
        font = fontGen.generateFont(params);

        //highscore
        prefs = Gdx.app.getPreferences("FruitNinjaPrefs");
        highScore = prefs.getInteger("highScore", 0); // Varsayılan değer 0
    }

    @Override
    public void render() {
        batch.begin();
        batch.draw(background,0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        double newTime = TimeUtils.millis()/1000.0;
        //System.out.println("newTime: " + newTime);
        double frameTime = Math.max(newTime - currentTime,0.03);
        System.out.println("frameTime: "+ frameTime);
        float deltaTime = (float) frameTime;
        System.out.println("deltaTime: "+ deltaTime);
        currentTime = newTime;



        if (lives <= 0 && gameOverTime == 0f){
            //GAME OVER
            gameOverTime = currentTime;

            //highscore güncellemesi
            if (score > highScore) {
                highScore = score;
                prefs.putInteger("highScore", highScore);
                prefs.flush(); // Kaydet
            }
        }

        if (lives > 0){
            genSpeed -= Math.sqrt(deltaTime)*0.001f; //item spawn hızı
            if (genSpeed < 0){
                genSpeed = startGenSpeed;
            }

            System.out.println("genSpeed: " + genSpeed);
            System.out.println("genCounter: " + genCounter);

            if (genCounter <= 0){
                genCounter = genSpeed;
                addItem();
            }else{
                genCounter -= deltaTime;
            }

            // ELMALARIN YERI (CAN BARI)
            for (int i = 0; i < lives; i++) {
                batch.draw(apple,i*50+60f,Gdx.graphics.getHeight()-70f,50,50);
            }

            // oluşturulan item'ın konumu
            for (Fruit f : fruitArray) {
                f.update(deltaTime);

                switch (f.type){
                    case REGULAR:
                        batch.draw(apple,f.getPos().x,f.getPos().y,Fruit.radius,Fruit.radius);
                        break;
                    case EXTRA:
                        batch.draw(cherry,f.getPos().x,f.getPos().y,Fruit.radius,Fruit.radius);
                        break;
                    case ENEMY:
                        batch.draw(ruby,f.getPos().x,f.getPos().y,Fruit.radius,Fruit.radius);
                        break;
                    case LIFE:
                        batch.draw(bill,f.getPos().x,f.getPos().y,Fruit.radius,Fruit.radius);
                        break;
                }

            }

            boolean holdLives = false;
            Array<Fruit> removeArray = new Array<>();
            for (Fruit f: fruitArray){
                if (f.outOfScreen()){
                    removeArray.add(f);

                    if (f.living && f.type == Fruit.Type.REGULAR){
                        lives--;
                        holdLives = true;
                        break;
                    }
                }
            }

            // yeni gelen meyvelereden biri düşerse can kaybedelim
            if (holdLives){
                 for (Fruit f: fruitArray){
                     f.living = false;
                 }
            }

            // yere düşen itemları sil
            for (Fruit f: removeArray){
                fruitArray.removeValue(f,true);

            }

        }

        // SCORE YAZISI
        font.draw(batch,"Score: " + score,60,50);
        if (lives <= 0){
            // START YAZISI
            float centerX = Gdx.graphics.getWidth() * 0.45f;
            float centerY = Gdx.graphics.getHeight() * 0.6f;

            if (first){
                font.draw(batch, "Cut to play", centerX, centerY);
                font.draw(batch, "High Score: " + highScore, centerX, centerY - 60);
            }
            else{
                font.draw(batch, "Cut to play", centerX, centerY);
                font.draw(batch, "Score: " + score, centerX, centerY - 60);
                font.draw(batch, "High Score: " + highScore, centerX, centerY - 120);
            }

        }

        batch.end();
    }

    private void addItem() {
        //oluşan itemların ihtimalleri
        float pos = random.nextFloat() * Gdx.graphics.getWidth();
        float velocityY = Gdx.graphics.getHeight() * (0.45f + random.nextFloat() * 0.15f);

        Fruit item = new Fruit(new Vector2(pos, -Fruit.radius), new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * 0.3f + (random.nextFloat() - 0.5f), velocityY));

        float type = random.nextFloat();

        if(type>0.98){
            item.type = Fruit.Type.LIFE;
        } else if(type>0.88){
            item.type = Fruit.Type.EXTRA;
        } else if(type>0.78){
            item.type = Fruit.Type.ENEMY;
        }
        fruitArray.add(item);
    }


    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        fontGen.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    // kullanıcının dokunup sürüklemesi (kullanacağımız)
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (lives <= 0 && currentTime - gameOverTime > 2f){
            //menu
            gameOverTime = 0;
            score = 0;
            lives = 4;
            genSpeed = startGenSpeed;
            fruitArray.clear();

            if (first) {
                first = false; // İlk oyun başladığında false yap
            }
        } else{
            //game
            Array<Fruit> toRemove = new Array<>();
            Vector2 pos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY); // kullanıcının tıkladığı pozisyon
            int plusScore = 0; // kesilen itemın skora ne kadar etki edeceği
            for (Fruit f: fruitArray){
                if (f.clicked(pos)){
                    toRemove.add(f);

                    switch (f.type){
                        case REGULAR:
                            plusScore++;
                            break;
                        case EXTRA:
                            plusScore += 2;
                            break;
                        case ENEMY:
                            lives--;
                            break;
                        case LIFE:
                            lives++;
                            break;
                    }
                    break; // sadece 1 item kesebilmek için
                }
            }
            score += plusScore * plusScore;

            //kesilen nesneyi sil
            for (Fruit f: toRemove){
                fruitArray.removeValue(f,true);

            }

        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
