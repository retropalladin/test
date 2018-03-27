package com.test.game.utils;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.test.game.Level;
import com.test.game.entities.Bullet;
import com.test.game.entities.NpcTank;
import com.test.game.entities.Wall;

public class LevelContactListener implements ContactListener {

    private Level level;
    private MaterialEntity materialEntityA;
    private MaterialEntity materialEntityB;

    public LevelContactListener(Level level){
        this.level = level;
    }

    @Override
    public void beginContact(Contact contact) {}
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        contact.setEnabled(false);
        materialEntityA = (MaterialEntity)contact.getFixtureA().getBody().getUserData();
        materialEntityB = (MaterialEntity)contact.getFixtureB().getBody().getUserData();
        if(materialEntityA.isAlive() & materialEntityB.isAlive()){
            if(materialEntityA.category == Constants.CATEGORY_ALLY_BULLET && materialEntityB.category == Constants.CATEGORY_ENEMY_TANK ||
                    materialEntityA.category == Constants.CATEGORY_ENEMY_BULLET && materialEntityB.category == Constants.CATEGORY_ALLY_TANK){
                level.bulletAndTankCollisionProcedure((Bullet)materialEntityA,(NpcTank)materialEntityB);
                return;
            }
            if(materialEntityB.category == Constants.CATEGORY_ALLY_BULLET && materialEntityA.category == Constants.CATEGORY_ENEMY_TANK ||
                    materialEntityB.category == Constants.CATEGORY_ENEMY_BULLET && materialEntityA.category == Constants.CATEGORY_ALLY_TANK) {
                level.bulletAndTankCollisionProcedure((Bullet)materialEntityB,(NpcTank)materialEntityA);
                return;
            }
            if(materialEntityA.category == Constants.CATEGORY_ALLY_BULLET && materialEntityB.category == Constants.CATEGORY_ENEMY_BULLET ||
                    materialEntityA.category == Constants.CATEGORY_ENEMY_BULLET && materialEntityB.category == Constants.CATEGORY_ALLY_BULLET){
                level.bulletAndBulletCollisionProcedure((Bullet)materialEntityA,(Bullet)materialEntityB);
                return;
            }
            if((materialEntityA.category == Constants.CATEGORY_ALLY_BULLET || materialEntityA.category == Constants.CATEGORY_ENEMY_BULLET) && materialEntityB.category == Constants.CATEGORY_WALL){
                level.bulletAndWallCollisionProcedure((Bullet)materialEntityA,(Wall)materialEntityB);
                return;
            }
            if((materialEntityB.category == Constants.CATEGORY_ALLY_BULLET || materialEntityB.category == Constants.CATEGORY_ENEMY_BULLET) && materialEntityA.category == Constants.CATEGORY_WALL){
                level.bulletAndWallCollisionProcedure((Bullet)materialEntityB,(Wall)materialEntityA);
                return;
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    @Override
    public void endContact(Contact contact) {}

}
