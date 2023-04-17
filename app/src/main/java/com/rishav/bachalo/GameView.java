package com.rishav.bachalo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Runnable;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameView extends View {

Bitmap background, ground, rabbit;
Rect rectBackground, rectGround;
Context context;
Handler handler;
final long UPDATE_MILLIS = 37;
Runnable runnable;
Paint textPaint= new Paint();
Paint healthPaint= new Paint();
float TEXT_SIZE  = 120;
int points =0;
int life =3;
static int dWidth, dHeight;
Random random;
float rabbitX, rabbitY;
float oldX;
float oldRabbitX;
ArrayList<Spike> spikes;
CopyOnWriteArrayList<Explosion> explosions;

     public static MediaPlayer mediaPlayer;


    public GameView(Context context) {
        super(context);
        this.context = context;

        mediaPlayer = MediaPlayer.create(context, R.raw.theme);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.start());


        background= BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground= BitmapFactory.decodeResource(getResources(),R.drawable.ground);
        rabbit= BitmapFactory.decodeResource(getResources(),R.drawable.rabbit);
        Display display= ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size= new Point();
        display.getSize(size);
        dWidth=size.x;
        dHeight=size.y;
        rectBackground= new Rect(0,0,dWidth,dHeight);
        rectGround = new Rect(0,dHeight - ground.getHeight(), dWidth, dHeight);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        textPaint.setColor(Color.rgb(255,165,0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenny_blocks));
        healthPaint.setColor(Color.GREEN);
        random = new Random();
        rabbitX= dWidth/2- rabbit.getWidth()/2;
        rabbitY= dHeight - ground.getHeight() - rabbit.getHeight();
        spikes = new ArrayList<>();
        explosions = new CopyOnWriteArrayList<>();
        for(int i =0; i<3;i++){
            Spike spike = new Spike(context);
            spikes.add(spike);
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

            canvas.drawBitmap(background, null, rectBackground, null);
            canvas.drawBitmap(ground, null, rectGround, null);
            canvas.drawBitmap(rabbit, rabbitX, rabbitY, null);
            for (int i = 0; i < spikes.size(); i++) {
                canvas.drawBitmap(spikes.get(i).getSpike(spikes.get(i).spikeFrame), spikes.get(i).spikeX, spikes.get(i).spikeY, null);
                spikes.get(i).spikeFrame++;
                if (spikes.get(i).spikeFrame > 2) {
                    spikes.get(i).spikeFrame = 0;
                }
                spikes.get(i).spikeY += spikes.get(i).spikeVelocity;
                if (spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= dHeight - ground.getHeight()) {
                    points += 10;
                    Explosion explosion = new Explosion(context);
                    explosion.explosionX = spikes.get(i).spikeX;
                    explosion.explosionY = spikes.get(i).spikeY;
                    explosions.add(explosion);
                    spikes.get(i).resetPosition();

                }
            }
            for (int i = 0; i < spikes.size(); i++) {
                if (spikes.get(i).spikeX + spikes.get(i).getSpikeWidth() >= rabbitX
                        && spikes.get(i).spikeX <= rabbitX + rabbit.getWidth()
                        && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() >= rabbitY
                        && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() <= rabbitY + rabbit.getHeight()) {

                    life--;
                    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.warning);
                    mediaPlayer.start();
                    spikes.get(i).resetPosition();
                    if (life == 0) {
                        Intent intent = new Intent(context, GameOver.class);
                        intent.putExtra("points", points);
                        context.startActivity(intent);
                        ((Activity) context).finish();
                    }
                }
            }

            for (int i = 0; i < explosions.size(); i++) {
                if (explosions.get(i) != null && explosions.get(i).getExplosion(explosions.get(i).explosionFrame) != null) {
                    canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).explosionX,
                            explosions.get(i).explosionY, null);
                    explosions.get(i).explosionFrame++;
                    explosions.removeIf(explosion -> explosion.explosionFrame > 3);
                }
            }


                if (life == 2) {
                healthPaint.setColor(Color.YELLOW);

            } else if (life == 1) {
                healthPaint.setColor(Color.RED);
            }
            canvas.drawRect(dWidth - 200, 30, dWidth - 200 + 60 * life, 80, healthPaint);
            canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
            handler.postDelayed(runnable, UPDATE_MILLIS);

        }

    //@SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float touchX= event.getX();
        float touchY= event.getY();
        if(touchY >= rabbitY){
            int action = event.getAction();
            if(action== MotionEvent.ACTION_DOWN){
                oldX = event.getX();
                oldRabbitX = rabbitX;
            }
            if(action==MotionEvent.ACTION_MOVE){
                float shift = oldX - touchX;
                float newRabbitX = oldRabbitX - shift;
                if(newRabbitX <= 0)
                    rabbitX =0;
                else if(newRabbitX >= dWidth - rabbit.getWidth())
                    rabbitX = dWidth - rabbit.getWidth();
                else
                    rabbitX= newRabbitX;
            }
        }
        return true;
    }

    public static void stopMusic() {
        mediaPlayer.stop();
    }

}

