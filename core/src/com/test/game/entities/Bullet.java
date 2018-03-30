package com.test.game.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Pool;
import com.test.game.utils.Constants;
import com.test.game.utils.Enums.AmmoType;
import com.test.game.utils.Enums.Direction;
import com.test.game.utils.MaterialEntity;

public class Bullet extends MaterialEntity implements Pool.Poolable {

    ///////////////////////////////////////////
    /// Constants Settings                  ///
    ///////////////////////////////////////////

    public static final int NORMAL_BULLET_MAX_HP = 1;
    public static final int PLASMA_BULLET_MAX_HP = 1;
    public static final int AP_BULLET_MAX_HP = 2;
    public static final int RAP_BULLET_MAX_HP = 2;

    public static final int NORMAL_BULLET_DAMAGE = 1;
    public static final int PLASMA_BULLET_DAMAGE = 1;
    public static final int AP_BULLET_DAMAGE = 3;
    public static final int RAP_BULLET_DAMAGE = 5;

    public static final float BULLET_WIDTH = 0.7f;
    public static final float BULLET_HEIGHT = 0.35f;
    public static final float BULLET_WIDTH_H = BULLET_WIDTH * 0.5f;
    public static final float BULLET_HEIGHT_H = BULLET_HEIGHT * 0.5f;

    public static final float BULLET_EPS_SPAWN = 0.1f;
    public static final float DOUBLE_BULLET_EPS_SPAWN = 0.2f;
    public static final float DOUBLE_BULLET_EPS_SPAWN_H = DOUBLE_BULLET_EPS_SPAWN * 0.5f;

    public static final float DOUBLE_NORMAL_BULLET_RELOAD = 1.0f;
    public static final float DOUBLE_PLASMA_BULLET_RELOAD = 0.75f;
    public static final float NORMAL_BULLET_RELOAD = 1.0f;
    public static final float PLASMA_BULLET_RELOAD = 0.75f;
    public static final float AP_NORMAL_BULLET_RELOAD = 1.0f;
    public static final float RAP_BULLET_RELOAD = 1.0f;

    ///////////////////////////////////////////
    /// Constants Physics                   ///
    ///////////////////////////////////////////

    public static final float NORMAL_BULLET_DENSITY = 0.7f;
    public static final float PLASMA_BULLET_DENSITY = 0.5f;
    public static final float AP_BULLET_DENSITY = 0.3f; // RAP_BULLET_DENSITY is equal
    public static final float BULLET_FRICTION = 0f;
    public static final float BULLET_RESTITUTION = 0f;

    public static final float BULLET_IMPULSE = 2.0f;
    public static final Vector2 BULLET_UP_IMPULSE = new Vector2(0, BULLET_IMPULSE);
    public static final Vector2 BULLET_DOWN_IMPULSE = new Vector2(0, -BULLET_IMPULSE);
    public static final Vector2 BULLET_RIGHT_IMPULSE = new Vector2(BULLET_IMPULSE, 0);
    public static final Vector2 BULLET_LEFT_IMPULSE = new Vector2(-BULLET_IMPULSE, 0);

    ///////////////////////////////////////////
    /// END CONSTANTS                       ///
    ///////////////////////////////////////////

    public AmmoType type;

    public void init(Body body) {
        this.setAlive(true);
        this.setBody(body);
        this.setGridCoordinates((short)-1, (short)-1);
    }

    public boolean configureBulletType(short category, AmmoType type) {
        this.setCategory(category);
        switch (type){
            case NORMAL_BULLET:
                hp = NORMAL_BULLET_MAX_HP;
                break;
            case PLASMA_BULLET:
                hp = PLASMA_BULLET_MAX_HP;
                break;
            case AP_BULLET:
                hp = AP_BULLET_MAX_HP;
                break;
            case RAP_BULLET:
                hp = RAP_BULLET_MAX_HP;
                break;
            default:
                return false;
        }
        this.type = type;
        return true;
    }

    public void launch(Direction direction){
        switch (direction) {
            case UP:
                body.applyLinearImpulse(BULLET_UP_IMPULSE, body.getWorldCenter(), true);
                break;
            case DOWN:
                body.applyLinearImpulse(BULLET_DOWN_IMPULSE, body.getWorldCenter(), true);
                break;
            case LEFT:
                body.applyLinearImpulse(BULLET_LEFT_IMPULSE, body.getWorldCenter(), true);
                break;
            case RIGHT:
                body.applyLinearImpulse(BULLET_RIGHT_IMPULSE, body.getWorldCenter(), true);
                break;
        }
    }

    public boolean takeDamage(int damage){
        return decreaseHp(damage);
    }

    @Override
    public void reset() {
        this.setAlive(false);
        this.setBody(null);
        this.setCategory((short) 0);
        this.setHp(0);
    }
}
