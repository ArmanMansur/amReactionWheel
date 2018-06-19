package com.revsup.reactwheel.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

import static java.lang.Math.cos;


public class Wheel extends ShapeRenderer {
        private Vector2 center;
        private float radius;
        private Vector2 arm;
        private boolean isClockwise;
        private float dir;
        private Vector2 target;
        private Vector2 prevTarget;
        private double dist;
        private float targetRadius;
        private static final Color BACKGROUND = new Color(38/255f, 50/255f, 56/255f, 1f);
        private static final Color RED = new Color(231/255f ,76/255f, 60/255f, 1f);
        private static final Color BLUE = new Color( 52/255f, 152/255f, 219/255f, 1f);
        private static final Color WHITE = new Color( 1f, 1f, 1f, 1f);
        private static final Color LB = new Color( 204/255f, 204/255f, 255/255f, 1f);
        private static final Color PINK = new Color( 255/255f, 0/255f, 255/255f, 1f);
        private static final Color BLK = new Color( 0/255f, 0/255f, 0/255f, 1f);
        private static final Color AQA = new Color( 0/255f, 255/255f, 128/255f, 1f);

        //animation
        private SpriteBatch batch;
        private static final int FRAME_COLS = 8;
        private static final int FRAME_ROWS = 8;
        private Animation<TextureRegion> explosionAnimation;
        private Texture explosionSheet;
        private float stateTime;
        private boolean targetHit;
        private boolean gameRunning;
        private float angleSpeed;
        private boolean targetWasInRange;





    public Wheel(SpriteBatch batch){
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            center = new Vector2(screenWidth/2f,screenWidth-screenHeight/10f);
            radius = screenWidth/2.5f;
            arm = new Vector2(center.x+radius/1f, center.y);
            isClockwise = false;
            prevTarget = target;
            dir = 1f;
            target = new Vector2(center.x, center.y+radius/1.05f);
            dist = 0.0;
            targetRadius = 45f;
            this.batch = batch;
            initAnimation();
            gameRunning = false;
            angleSpeed = 0.0f;
            targetWasInRange = false;

    }
    public void initAnimation(){
        explosionSheet = new Texture("explosion.png");
        TextureRegion[][] tmp = TextureRegion.split(
                explosionSheet,
                explosionSheet.getWidth()/ FRAME_COLS,
                explosionSheet.getHeight()/ FRAME_ROWS
        );
        TextureRegion[] explosionFrames = new TextureRegion[FRAME_COLS*FRAME_ROWS];
        int index = 0;
        for(int i=0; i < FRAME_ROWS; i++){
            for(int j=0; j < FRAME_COLS; j++){
                explosionFrames[index++] = tmp[i][j];

            }

        }

        explosionAnimation = new Animation<TextureRegion>(.0055f, explosionFrames);
        stateTime = 0.0f;

    }
    private void showExplosion(){
        TextureRegion currentFrame = explosionAnimation.getKeyFrame(stateTime, false);
        if(!explosionAnimation.isAnimationFinished(stateTime)){
            stateTime+= Gdx.graphics.getDeltaTime();
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
            this.circle(center.x, center.y, radius, 100);
            this.setColor(AQA);
            this.circle(center.x, center.y, radius, 10);
            this.setColor(BACKGROUND);
            this.circle(center.x, center.y, radius/1.1f, 100);
            //arm

            setColor(WHITE);
            this.rectLine(center, arm, 25f);

            //inner circle
            setColor(AQA);
            circle(center.x, center.y, radius/1.6f, 6);

            setColor(AQA);
            circle(center.x, center.y, radius/1.3f, 8);

            setColor(WHITE);
            circle(center.x, center.y, radius/1.7f, 10);

            setColor(AQA);
            circle(center.x, center.y, radius/2.5f, 12);

            setColor(WHITE);
            circle(center.x, center.y, radius/3f, 14);

            setColor(BLK);
            circle(center.x, center.y, radius/5f, 4);

            setColor(WHITE);
            circle(center.x, center.y, radius/6f, 6);

            setColor(BLK);
            circle(center.x, center.y, radius/10F, 100);





        renderTarget();
            this.end();

            //draw sprites
        batch.begin();
        if(targetHit)
            showExplosion();
        batch.end();


        }
        public void renderTarget(){
            setColor(AQA);
            circle(target.x, target.y, targetRadius, 100);
            setColor(WHITE);
            circle(target.x, target.y, 30f, 100);
            setColor(PINK);
            circle(target.x, target.y, 20f, 100);
        }
        public void checkInput(){
            boolean touched = Gdx.input.justTouched();

        if(gameRunning) {

            if (touched && targetInRange()) {
                isClockwise = !isClockwise;

                Random rnd = new Random();
                float ang = 0.0f + rnd.nextFloat() + (360f - 0f);

                targetHit = true;
                prevTarget = new Vector2(target.x, target.y);
                target = rotate(target, ang);
            }else if(touched && !targetInRange()){
                stopGame();
            } else if(!touched && targetWasInRange && dist > 100.0);
            stopGame();
        }      else{
                if(touched)
            startGame();

        }
        }
        public void startGame(){
            gameRunning = true;
            angleSpeed = 0.06f;

        }
        public void stopGame(){
        gameRunning = false;
        angleSpeed = 0.0f;
        }
        private boolean targetInRange(){
            float hitRange = targetRadius * 2f;
            boolean inRange = (dist <= hitRange);
            return inRange;
        }
        public Vector2 rotate(Vector2 p, float theta){
            float s = (float) Math.sin(theta);
            float c = (float) Math.cos(theta);

            p.x -= center.x;
            p.y -= center.y;

            float xNew = (p.x * c - dir*p.y * s);
            float yNew = (dir*p.x * s + p.y * c);

            p.x = (xNew + center.x);
            p.y = (yNew + center.y);

            return p;

        }



        public void update(){

            if(dist > 80.0 && dist < 90.0)
                targetWasInRange = true;

            checkInput();
            dir = (isClockwise) ? -1f : 1f;

            arm = rotate(arm, angleSpeed);

            dist = Math.sqrt(Math.pow((target.x - arm.x), 2) +
                             Math.pow((target.y - arm.y), 2));

        }
}
