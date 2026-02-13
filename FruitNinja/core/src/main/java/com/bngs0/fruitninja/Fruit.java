package com.bngs0.fruitninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Fruit {
    public static float radius = 60f;

    public enum Type{
        REGULAR, EXTRA, ENEMY, LIFE
        //elma, kiraz, ruby, para
    }
    public Type type;
    Vector2 pos, velocity;
    public boolean living = true;
    Fruit(Vector2 pos, Vector2 velocity){
        this.pos = pos; // pozisyon
        this.velocity = velocity; // hız
        type = Type.REGULAR;
    }

    public boolean clicked(Vector2 click){
        if(pos.dst2(click)<=radius * radius + 1) return true;
        return false;
    }

    public final Vector2 getPos(){
        return pos;
    }

    public boolean outOfScreen(){
        return (pos.y<-2f*radius);
    }
    public void update(float dt){
        velocity.y-=dt*(Gdx.graphics.getHeight()*0.2f);
        //velocity.x-=dt*(Gdx.graphics.getWidth()*0.2f);
        velocity.x-=dt*Math.signum(velocity.x) * 5f;
        //The java.lang.Math.signum(float f) returns the signum function of the argument; zero if the argument is zero, 1.0f if the argument is greater than zero, -1.0f if the argument is less than zero.Special Cases −
        pos.mulAdd(velocity, dt);
    }



}
