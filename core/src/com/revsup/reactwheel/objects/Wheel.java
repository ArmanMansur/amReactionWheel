package com.revsup.reactwheel.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;


public class Wheel extends ShapeRenderer {
    private Vector2 center;
    private float radius;
    private Vector2 arm;
    private boolean isClockwise;
    private float dir;
    private Vector2 target;
    private Vector2 prevTarget;
    private float targetRadius;
    private boolean gameRunning;
    private boolean targetWasInRange;
    private Vector2 hitPoint;

    //colors
    private static final Color BACKGROUND = new Color(204/255f,0/255f,204/255f, 1f);
    private static final Color RED = new Color(231/255f,76/255f,60/255f,1f);
    private static final Color BLUE = new Color(106/255f,255/255f,255/255f,1f);
    private static final Color WHITE = new Color(1f,1f,1f,1f);

    private SpriteBatch batch;
    private static final int FRAME_COLS = 8;
    private static final int FRAME_ROWS = 8;
    private Animation<TextureRegion> explosionAnimation;
    private Texture explosionSheet;
    private float stateTime;
    private boolean targetHit;
    private float angleSpeed;
    private float screenWidth = Gdx.graphics.getWidth();
    private float screenHeight = Gdx.graphics.getHeight();
    private BitmapFont scoreFont;
    private Texture highScore;
    private BitmapFont highScoreFont;

    private GlyphLayout layout;

    private Texture tapToPlay;
    private Texture gameName;

    private int score;

    private Sound explosionSound;
    private Sound victorySound;
    private Music music;


    public Wheel(SpriteBatch batch) {
        center = new Vector2(screenWidth/2f, screenHeight-screenHeight/2f);
        radius = screenWidth/2.5f;

        arm = new Vector2(center.x+radius/1.1f,center.y);
        angleSpeed = 0.0f;

        hitPoint = new Vector2((arm.x*1.1f)/1.17f, arm.y);

        isClockwise = false;
        dir = 1f;

        target = new Vector2(center.x, center.y+radius/1.3f);

        targetRadius=45f;

        this.batch = batch;

        targetHit = false;

        prevTarget = target;

        gameRunning = false;

        targetWasInRange = false;

        initAnimation();

        tapToPlay = new Texture("taptoplay.png");

        gameName = new Texture( "title.png");

        highScore = new Texture("highscore3.png");

        scoreFont = new BitmapFont();
        scoreFont.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
        scoreFont.getData().setScale(6f);
        highScoreFont = new BitmapFont();
        highScoreFont.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
        highScoreFont.getData().setScale(6f);
        SavedDataManager.getInstance().load();
        layout = new GlyphLayout(scoreFont, String.valueOf(score));
        score = 0;

        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explode.wav"));
        victorySound = Gdx.audio.newSound(Gdx.files.internal("thisvictory.mp3"));

        music = Gdx.audio.newMusic(Gdx.files.internal("thismusic.mp3"));
        music.setVolume(0.5f);
        music.play();
        music.setLooping(true);




    }

    public void initAnimation(){
        explosionSheet = new Texture("explosion.png");
        TextureRegion[][] tmp = TextureRegion.split(
                explosionSheet,
                explosionSheet.getWidth()/FRAME_COLS,
                explosionSheet.getHeight()/ FRAME_ROWS
        );
        TextureRegion[] explosionFrames = new TextureRegion[FRAME_COLS*FRAME_ROWS];
        int index = 0;
        for(int i=0; i < FRAME_ROWS; i++){
            for(int j=0; j < FRAME_COLS; j++){
                explosionFrames[index++] = tmp[i][j];
            }
        }
        explosionAnimation = new Animation<TextureRegion>(0.0055f, explosionFrames);
        stateTime= 0.0f;

    }
    private void showExplosion(){

        TextureRegion currentFrame = explosionAnimation.getKeyFrame(stateTime, false);
        if(!explosionAnimation.isAnimationFinished(stateTime)){
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(currentFrame, (int)prevTarget.x-256, (int)prevTarget.y-256);
            if(explosionAnimation.isAnimationFinished(stateTime)){
                stateTime = 0.0f;
                targetHit = false;
            }
        }
    }

    public void render(){
        update();
        this.begin(ShapeType.Filled);
        //outer ring

        this.setColor(Color.WHITE);
        this.circle(center.x,center.y,radius,14);


        this.setColor(BACKGROUND);
        this.circle(center.x,center.y,radius/1.1f,12);
        //arm

        setColor(WHITE);
        this.rectLine(center, arm, 25f);

        //center circle
        setColor(WHITE);
        circle(center.x,center.y,radius/1.6f,10);

        setColor(Color.WHITE);
        circle(center.x,center.y,radius/1.8f,10);

        setColor(BLUE);
        circle(center.x,center.y,radius/3f,10);



        setColor(BACKGROUND);
        triangle(90,0,screenWidth/2f,130,screenWidth-90,0);

        setColor(WHITE);
        triangle(90,1490,screenWidth/2f,1370,screenWidth-90,1490);

        setColor(WHITE);
        triangle(90,300,screenWidth/2f,430,screenWidth-90,300);

        setColor(BACKGROUND);
        triangle(90,1790,screenWidth/2f,1670,screenWidth-90,1790);

        setColor(BLUE);
        circle(hitPoint.x, hitPoint.y, 10f, 100);


        renderTarget();
        this.end();

        batch.begin();

        if(targetHit)
            showExplosion();

        scoreFont.draw(batch, layout, center.x - layout.width/2, center.y + layout.height/2);

        if(!gameRunning)
            batch.draw(tapToPlay, (screenWidth - tapToPlay.getWidth())/2, screenHeight/9.7f);
        batch.draw(gameName, (screenWidth - gameName.getWidth())/2, screenHeight/1.2f);

        batch.draw(highScore, 260f, 50f);

        highScoreFont.draw(batch, String.valueOf(SavedDataManager.getInstance().getHighScore()),
                highScore.getWidth()+280, highScore.getHeight()*1.45f);

        batch.end();
    }
    public void renderTarget() {
        setColor(WHITE);
        circle(target.x, target.y, targetRadius, 9);
        setColor(WHITE);
        circle(target.x, target.y, targetRadius/1.5f, 7);
        setColor(BLUE);
        circle(target.x, target.y, targetRadius/3.0f, 5);

    }
    public void checkInput() {
        boolean touched = Gdx.input.justTouched();

        if (gameRunning) {


            if (touched && targetInRange()) {
                isClockwise = !isClockwise;
                targetWasInRange = false;
                Random rnd = new Random();
                float ang = 0.0f + rnd.nextFloat() * (360f - 0f);
                explosionSound.play();

                targetHit = true;

                prevTarget = new Vector2(target.x, target.y);

                score++;


                do{
                    target = rotate(target, ang);
                }while(distance(target, prevTarget) < 200);



            } else if(touched && !targetInRange()){
                stopGame();
            } else if(!touched && targetWasInRange && distance(hitPoint,target) > 100){
                stopGame();
            }
        }else{
            if(touched)

                startGame();
            else
                resetGame();
        }


    }
    public void resetGame(){
        arm.x = center.x+radius/1.1f;
        arm.y = center.y;
        hitPoint.x = arm.x/1.08f;
        hitPoint.y = arm.y;
        target.x = center.x;
        target.y = center.y+radius/1.3f;
        isClockwise = true;
        targetWasInRange = false;
    }

    public void startGame(){
        gameRunning = true;
        angleSpeed = 0.03f;
        score = 0;

    }
    public void stopGame(){
        gameRunning = false;
        angleSpeed = 0.0f;
        if(score > SavedDataManager.getInstance().getHighScore())
            victorySound.play();
        SavedDataManager.getInstance().setHighScore(score);
        SavedDataManager.getInstance().save();
    }


    private boolean targetInRange(){
        float hitRange = 30f;
        boolean inRange = (distance(hitPoint,target)<= hitRange);
        return inRange;

    }


    public Vector2 rotate(Vector2 p, float theta){
        float s = (float) Math.sin(theta);
        float c = (float) Math.cos(theta);

        p.x -= center.x;
        p.y -= center.y;

        float xNew = (p.x * c -dir*p.y * s);
        float yNew = (dir*p.x * s + p.y * c);

        p.x = (xNew + center.x);
        p.y = (yNew + center.y);
        return p;

    }
    public double distance(Vector2 p1, Vector2 p2){
        return Math.sqrt(Math.pow((p2.x - p1.x), 2)+
                Math.pow((p2.y- p1.y),2));

    }

    public void update(){
        checkInput();
        layout.setText(scoreFont, String.valueOf(score));
        dir = (isClockwise) ? -1f : 1f;

        arm = rotate(arm, angleSpeed);
        hitPoint = rotate(hitPoint, angleSpeed);



    }
    public void dispose(){
        explosionSound.dispose();
        victorySound.dispose();
        music.dispose();
        this.dispose();
    }

}


