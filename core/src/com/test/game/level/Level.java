package com.test.game.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.test.game.entities.Bullet;
import com.test.game.entities.NpcTank;
import com.test.game.player.PlayerTank;
import com.test.game.entities.Wall;
import com.test.game.level.endCheckers.LevelEndCheckerKAorDie;
import com.test.game.utils.Constants;
import com.test.game.utils.Enums.AmmoType;
import com.test.game.utils.Enums.Direction;
import com.test.game.utils.Enums.TankType;
import com.test.game.utils.Enums.WallType;
import com.test.game.utils.LevelEndChecker;
import com.test.game.utils.MaterialEntity;
import com.test.game.player.PlayerManager;
import com.test.game.utils.Utils;


public class Level {

    ///////////////////////////////////////////
    /// Constants Settings                  ///
    ///////////////////////////////////////////

    public static final byte GOD_DAMAGE = Byte.MAX_VALUE;
    public static final float RESPAWN_INVIS = 3;

    public static final float TIME_STOP_RELOAD = 1;
    public static final float FREEZE_TIME = 20;
    public static final float FREEZE_INCREASE = 5;

    public static final float ENERGY_DRINK_RELOAD = 1;
    public static final float SPEED_UP_TIME = 20;
    public static final float SPEED_UP_INCREASE = 5;
    ///////////////////////////////////////////
    /// END CONSTANTS                       ///
    ///////////////////////////////////////////

    public boolean levelFail = false;
    public boolean levelWin = false;
    private LevelEndChecker levelEndChecker;

    private float speedUpPlayerTime = 0;
    private boolean speedUpPlayer = false;
    private boolean needPlayerSpeedUp;
    private boolean needPlayerSpeedDown;

    private float freezeEnemyTime = 0;
    private boolean enemyFreeze = false;
    private boolean needEnemyFreeze = false;
    private boolean needEnemyUnfreeze = false;

    private boolean needPlayerRespawn = false;
    private boolean needPlayerDispawn = false;
    public short allySpawnX;
    public short allySpawnY;

    public short[][] objectsMatrix = null; //warning : she is reversed. coord gridX = 0, gridY = 0 is equal to objectMatrix[gridY,gridX] so gridY axis is reversed.
    public short[][] landMatrix = null; //warning : she is too

    public short matrixWidth = 0;
    public short matrixHeight = 0;

    public float levelWidth = 0;
    public float levelHeight = 0;

    public PlayerTank playerTank;
    public PlayerTank deadPlayerTank;

    public Array<Bullet> aliveBullets;
    private static Pool<Bullet> bulletPool = Pools.get(Bullet.class);
    public Array<NpcTank> aliveNpcTanks;
    private static Pool<NpcTank> npcTankPool = Pools.get(NpcTank.class);
    public Array<Wall> aliveWalls;
    private static Pool<Wall> wallPool = Pools.get(Wall.class);
    private int aliveIterator;

    private Array<MaterialEntity> deadEntities;
    private MaterialEntity deadEntity;
    private int deadIterator;

    public World world;
    public LevelContactListener levelContactListener;

    private float frameTime;
    private float accumulator;

    private Vector2 wallRectangleCenter;

    private BodyDef wallBodyDef;
    private PolygonShape wallRectangle;
    private FixtureDef wallFixtureDef;

    private Vector2 tankCenter;
    private BodyDef tankBodyDef;
    private PolygonShape tankRectangle;
    private FixtureDef tankFixtureDef;

    private Vector2 horizontalBulletCenter;
    private Vector2 verticalBulletCenter;
    private BodyDef bulletBodyDef;
    private PolygonShape horizontalBulletRectangle;
    private PolygonShape verticalBulletRectangle;
    private FixtureDef bulletFixtureDef;

    public Level() {
        aliveBullets = new Array<Bullet>();
        aliveNpcTanks = new Array<NpcTank>();
        aliveWalls = new Array<Wall>();
        deadEntities = new Array<MaterialEntity>();

        world = new World(Vector2.Zero, false);
        levelContactListener = new LevelContactListener(this);
        world.setContactListener(levelContactListener);

        wallRectangleCenter = new Vector2(Constants.Physics.CELL_SIZE * 0.5f, Constants.Physics.CELL_SIZE * 0.5f);

        wallBodyDef = new BodyDef();
        wallBodyDef.type = BodyType.StaticBody;
        wallBodyDef.fixedRotation = true;

        wallRectangle = new PolygonShape();
        wallRectangle.setAsBox(Constants.Physics.CELL_SIZE * 0.5f, Constants.Physics.CELL_SIZE * 0.5f, wallRectangleCenter, 0);

        wallFixtureDef = new FixtureDef();
        wallFixtureDef.shape = wallRectangle;
        wallFixtureDef.friction = Wall.WALL_FRICTION;
        wallFixtureDef.restitution = Wall.WALL_RESTITUTION;
        wallFixtureDef.filter.categoryBits = Constants.Physics.CATEGORY_WALL;
        wallFixtureDef.filter.maskBits = Constants.Physics.MASK_WALL;

        tankCenter = new Vector2(NpcTank.TANK_WIDTH_H, NpcTank.TANK_HEIGHT_H);

        tankBodyDef = new BodyDef();
        tankBodyDef.type = BodyType.DynamicBody;
        tankBodyDef.fixedRotation = true;

        tankRectangle = new PolygonShape();
        tankRectangle.setAsBox(NpcTank.TANK_WIDTH_H, NpcTank.TANK_HEIGHT_H, tankCenter, 0);

        tankFixtureDef = new FixtureDef();
        tankFixtureDef.shape = tankRectangle;
        tankFixtureDef.friction = NpcTank.TANK_FRICTION;
        tankFixtureDef.restitution = NpcTank.TANK_RESTITUTION;

        horizontalBulletCenter = new Vector2(Bullet.BULLET_WIDTH_H, Bullet.BULLET_HEIGHT_H);
        verticalBulletCenter = new Vector2(Bullet.BULLET_HEIGHT_H, Bullet.BULLET_WIDTH_H);

        bulletBodyDef = new BodyDef();
        bulletBodyDef.type = BodyType.DynamicBody;
        bulletBodyDef.fixedRotation = true;

        horizontalBulletRectangle = new PolygonShape();
        verticalBulletRectangle = new PolygonShape();

        horizontalBulletRectangle.setAsBox(Bullet.BULLET_WIDTH_H, Bullet.BULLET_HEIGHT_H, horizontalBulletCenter , 0);
        verticalBulletRectangle.setAsBox(Bullet.BULLET_HEIGHT_H, Bullet.BULLET_WIDTH_H, verticalBulletCenter, 0);

        bulletFixtureDef = new FixtureDef();
        bulletFixtureDef.shape = horizontalBulletRectangle;
        bulletFixtureDef.friction = Bullet.BULLET_FRICTION;
        bulletFixtureDef.restitution = Bullet.BULLET_RESTITUTION;
    }

    public static Level debugLevel() {
        Level level = new Level();
        level.initializeDebugLevel();
        return level;
    }

    private void initializeDebugLevel() {
        short height = 10;
        short width = 10;
        objectsMatrix = new short[height][width];
        landMatrix = new short[height][width];
        for(int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                objectsMatrix[i][j] = Constants.Physics.CATEGORY_EMPTY;
                //if(Utils.random.nextBoolean())
                    landMatrix[i][j] = Constants.Physics.LAND_GROUND;
                //else
                //    landMatrix[i][j] = Constants.Physics.LAND_SAND;
            }
        matrixHeight = (short) (height - 1);
        levelHeight = (height - 2) * Constants.Physics.CELL_SIZE;
        matrixWidth = (short) (width - 1);
        levelWidth = (width - 2) * Constants.Physics.CELL_SIZE;
        spawnLevelBorders();
        allySpawnX = 1;
        allySpawnY = 1;
        objectsMatrix[allySpawnY][allySpawnX] = Constants.Physics.CATEGORY_SPAWN;
        playerTank = spawnGridDefinedPlayerTank(allySpawnX,allySpawnY, PlayerManager.debugPlayerManager(), Direction.UP);
        objectsMatrix[5][7] = Constants.Physics.CATEGORY_SPAWN;
        spawnGridDefinedNpcTank((short)7,(short)5, (byte) 5, TankType.LIGHT_TANK, AmmoType.NORMAL_BULLET, Direction.LEFT, false);
        levelEndChecker = new LevelEndCheckerKAorDie();
    }


    //player
    public PlayerTank spawnGridDefinedPlayerTank(short posX, short posY, PlayerManager playerManager, Direction direction){
        if(objectsMatrix[posY][posX] == Constants.Physics.CATEGORY_SPAWN) {
            objectsMatrix[posY][posX] = Constants.Physics.CATEGORY_ALLY_TANK | Constants.Physics.CATEGORY_SPAWN;
            PlayerTank playerTank = spawnDefinedPlayerTank(posX * Constants.Physics.CELL_SIZE + NpcTank.TANK_MARGIN, posY * Constants.Physics.CELL_SIZE + NpcTank.TANK_MARGIN,
                    playerManager, direction);
            playerTank.setGridCoordinates(posX, posY);
            return playerTank;
        }
        return null;
    }

    private PlayerTank spawnDefinedPlayerTank(float posX, float posY, PlayerManager playerManager, Direction direction) {
        PlayerTank playerTank = spawnPlayerTank(posX,posY);
        configurePlayerTankFixture(playerManager.getTankType());
        playerTank.configurePlayerTankType(playerManager, direction);
        playerTank.createFixture(tankFixtureDef);
        return playerTank;
    }

    private PlayerTank spawnPlayerTank(float posX, float posY) {
        tankBodyDef.position.set(posX,posY);
        Body body = world.createBody(tankBodyDef);

        PlayerTank playerTank = new PlayerTank(this, body);
        body.setUserData(playerTank);

        return playerTank;
    }

    private void configurePlayerTankFixture(TankType type) {
        tankFixtureDef.filter.categoryBits = Constants.Physics.CATEGORY_ALLY_TANK;
        tankFixtureDef.filter.maskBits = Constants.Physics.MASK_ALLY_TANK;

        switch (type){
            case LIGHT_TANK:
                tankFixtureDef.density = NpcTank.LIGHT_TANK_DENSITY;
                break;
            case HEAVY_TANK:
                tankFixtureDef.density = NpcTank.HEAVY_TANK_DENSITY;
                break;
        }
    }
    //end player
    //wall
    public void spawnLevelBorders(){
        short i;
        for(i = 0; i <= matrixWidth; i++)
        {
            spawnGridDefinedWall(i, (short) 0,WallType.LEVEL_BORDER);
            spawnGridDefinedWall(i, matrixHeight,WallType.LEVEL_BORDER);
        }
        for(i = 1; i < matrixHeight; i++)
        {
            spawnGridDefinedWall((short) 0, i,WallType.LEVEL_BORDER);
            spawnGridDefinedWall(matrixWidth, i,WallType.LEVEL_BORDER);
        }
    }

    public void spawnGridDefinedWall(short posX, short posY, WallType type) {
        if(objectsMatrix[posY][posX] == Constants.Physics.CATEGORY_EMPTY) {
            objectsMatrix[posY][posX] = Constants.Physics.CATEGORY_WALL;
            Wall wall = spawnDefinedWall(posX * Constants.Physics.CELL_SIZE, posY * Constants.Physics.CELL_SIZE, type);
            wall.setGridCoordinates(posX, posY);
        }
    }

    private Wall spawnDefinedWall(float posX, float posY, WallType type) {
        Wall wall = spawnWall(posX,posY);
        wall.configureWallType(Constants.Physics.CATEGORY_WALL, type);
        wall.createFixture(wallFixtureDef);
        return wall;
    }

    private Wall spawnWall(float posX, float posY) {
        wallBodyDef.position.set(posX,posY);
        Body body = world.createBody(wallBodyDef);
        Wall wall = wallPool.obtain();

        wall.init(this, body);
        body.setUserData(wall);

        aliveWalls.add(wall);
        return wall;
    }
    //end wall
    //npc
    public void spawnGridDefinedNpcTank(short posX, short posY, byte hp,  TankType type, AmmoType ammoType, Direction direction, boolean isAlly){
        if(objectsMatrix[posY][posX] == Constants.Physics.CATEGORY_SPAWN) {
            if (isAlly)
                objectsMatrix[posY][posX] = Constants.Physics.CATEGORY_ALLY_TANK;
            else
                objectsMatrix[posY][posX] = Constants.Physics.CATEGORY_ENEMY_TANK;
            NpcTank npcTank = spawnDefinedNpcTank(posX * Constants.Physics.CELL_SIZE + NpcTank.TANK_MARGIN, posY * Constants.Physics.CELL_SIZE + NpcTank.TANK_MARGIN,
                    hp,  type, ammoType, direction, isAlly);
            npcTank.setGridCoordinates(posX, posY);
        }
    }

    private NpcTank spawnDefinedNpcTank(float posX, float posY, byte hp,  TankType type, AmmoType ammoType,Direction direction, boolean isAlly) {
        NpcTank npcTank = spawnNpcTank(posX,posY);
        configureNpcTankFixture(type, isAlly);
        npcTank.configureNpcTankType(tankFixtureDef.filter.categoryBits, hp,  type, ammoType, direction);
        npcTank.createFixture(tankFixtureDef);
        return npcTank;
    }

    private NpcTank spawnNpcTank(float posX, float posY) {
        tankBodyDef.position.set(posX,posY);
        Body body = world.createBody(tankBodyDef);
        NpcTank npcTank = npcTankPool.obtain();

        npcTank.init(this,body);
        body.setUserData(npcTank);

        aliveNpcTanks.add(npcTank);
        return npcTank;
    }

    private void configureNpcTankFixture(TankType type, boolean isAlly) {
        if(isAlly) {
            tankFixtureDef.filter.categoryBits = Constants.Physics.CATEGORY_ALLY_TANK;
            tankFixtureDef.filter.maskBits = Constants.Physics.MASK_ALLY_TANK;
        }else{
            tankFixtureDef.filter.categoryBits = Constants.Physics.CATEGORY_ENEMY_TANK;
            tankFixtureDef.filter.maskBits = Constants.Physics.MASK_ENEMY_TANK;
        }

        switch (type){
            case LIGHT_TANK:
                tankFixtureDef.density = NpcTank.LIGHT_TANK_DENSITY;
                break;
            case HEAVY_TANK:
                tankFixtureDef.density = NpcTank.HEAVY_TANK_DENSITY;
                break;
        }
    }
    //end npc
    //bullet
    //warning: bullet doesn't have grid coordinates
    public void spawnCorrectedBullet(float posX, float posY, AmmoType type, Direction direction, boolean isAlly) {
        switch (direction)
        {
            case UP:
                posX += tankCenter.x - Bullet.BULLET_HEIGHT_H;
                posY += NpcTank.TANK_HEIGHT - Bullet.BULLET_WIDTH - Bullet.BULLET_EPS_SPAWN;
                break;
            case DOWN:
                posX += tankCenter.x - Bullet.BULLET_HEIGHT_H;
                posY += Bullet.BULLET_EPS_SPAWN;
                break;
            case LEFT:
                posY += tankCenter.y - Bullet.BULLET_HEIGHT_H;
                posX += Bullet.BULLET_EPS_SPAWN;
                break;
            case RIGHT:
                posX += NpcTank.TANK_WIDTH - Bullet.BULLET_WIDTH - Bullet.BULLET_EPS_SPAWN;
                posY += tankCenter.y - Bullet.BULLET_HEIGHT_H;
                break;
        }
        spawnDefinedBullet(posX,posY,type,direction,isAlly);
    }

    public void spawnCorrectedDoubleBullet(float posX, float posY, AmmoType type, Direction direction, boolean isAlly) {
        switch (direction)
        {
            case UP:
                posX += tankCenter.x - Bullet.BULLET_HEIGHT - Bullet.DOUBLE_BULLET_EPS_SPAWN_H;
                posY += NpcTank.TANK_HEIGHT - Bullet.BULLET_WIDTH - Bullet.BULLET_EPS_SPAWN;
                break;
            case DOWN:
                posX += tankCenter.x - Bullet.BULLET_HEIGHT - Bullet.DOUBLE_BULLET_EPS_SPAWN_H;
                posY += Bullet.BULLET_EPS_SPAWN;
                break;
            case LEFT:
                posY += tankCenter.y - Bullet.BULLET_HEIGHT - Bullet.DOUBLE_BULLET_EPS_SPAWN_H;
                posX += Bullet.BULLET_EPS_SPAWN;
                break;
            case RIGHT:
                posX += NpcTank.TANK_WIDTH - Bullet.BULLET_WIDTH - Bullet.BULLET_EPS_SPAWN;
                posY += tankCenter.y - Bullet.BULLET_HEIGHT - Bullet.DOUBLE_BULLET_EPS_SPAWN_H;
                break;
        }
        spawnDefinedDoubleBullet(posX,posY,type,direction,isAlly);
    }

    private void spawnDefinedBullet(float posX, float posY, AmmoType type, Direction direction, boolean isAlly) {
        if(configureBulletFixture(direction, type, isAlly)) {
            Bullet bullet = spawnBullet(posX, posY);
            bullet.configureBulletType(bulletFixtureDef.filter.categoryBits, type);
            bullet.createFixture(bulletFixtureDef);
            bullet.launch(direction);
        }
    }

    private void spawnDefinedDoubleBullet(float posX, float posY, AmmoType type, Direction direction, boolean isAlly) {
        switch(type){
            case DOUBLE_NORMAL_BULLET:
                configureBulletFixture(direction, AmmoType.NORMAL_BULLET, isAlly);
                break;
            case DOUBLE_PLASMA_BULLET:
                configureBulletFixture(direction, AmmoType.PLASMA_BULLET, isAlly);
                break;
            default:
                return;
        }
        Bullet bullet1 = spawnBullet(posX,posY);
        switch (direction){
            case UP:
            case DOWN:
                posX += Bullet.BULLET_HEIGHT + Bullet.DOUBLE_BULLET_EPS_SPAWN;
                break;
            case RIGHT:
            case LEFT:
                posY += Bullet.BULLET_HEIGHT + Bullet.DOUBLE_BULLET_EPS_SPAWN;
                break;
        }
        Bullet bullet2 = spawnBullet(posX,posY);
        switch(type){
            case DOUBLE_NORMAL_BULLET:
                configureBulletFixture(direction, AmmoType.NORMAL_BULLET, isAlly);
                bullet1.configureBulletType(bulletFixtureDef.filter.categoryBits, AmmoType.NORMAL_BULLET);
                bullet2.configureBulletType(bulletFixtureDef.filter.categoryBits, AmmoType.NORMAL_BULLET);
                break;
            case DOUBLE_PLASMA_BULLET:
                configureBulletFixture(direction, AmmoType.PLASMA_BULLET, isAlly);
                bullet1.configureBulletType(bulletFixtureDef.filter.categoryBits, AmmoType.PLASMA_BULLET);
                bullet2.configureBulletType(bulletFixtureDef.filter.categoryBits, AmmoType.PLASMA_BULLET);
                break;
        }
        bullet1.createFixture(bulletFixtureDef);
        bullet2.createFixture(bulletFixtureDef);
        bullet1.launch(direction);
        bullet2.launch(direction);
    }

    private Bullet spawnBullet(float posX, float posY) {
        bulletBodyDef.position.set(posX,posY);
        Body body = world.createBody(bulletBodyDef);
        Bullet bullet = bulletPool.obtain();

        bullet.init(body);
        body.setUserData(bullet);

        aliveBullets.add(bullet);
        return bullet;
    }

    private boolean configureBulletFixture(Direction direction, AmmoType type, boolean isAlly) {
        switch (type){
            case NORMAL_BULLET:
                bulletFixtureDef.density = Bullet.NORMAL_BULLET_DENSITY;
                break;
            case PLASMA_BULLET:
                bulletFixtureDef.density = Bullet.PLASMA_BULLET_DENSITY;
                break;
            case AP_BULLET:
                bulletFixtureDef.density = Bullet.AP_BULLET_DENSITY;
                break;
            case RAP_BULLET:
                bulletFixtureDef.density = Bullet.AP_BULLET_DENSITY;
                break;
            default:
                return false;
        }

        if(isAlly) {
            bulletFixtureDef.filter.categoryBits = Constants.Physics.CATEGORY_ALLY_BULLET;
            bulletFixtureDef.filter.maskBits = Constants.Physics.MASK_ALLY_BULLET;
        }else{
            bulletFixtureDef.filter.categoryBits = Constants.Physics.CATEGORY_ENEMY_BULLET;
            bulletFixtureDef.filter.maskBits = Constants.Physics.MASK_ENEMY_BULLET;
        }

        switch (direction) {
            case UP:
            case DOWN:
                bulletFixtureDef.shape = verticalBulletRectangle;
                break;
            case LEFT:
            case RIGHT:
                bulletFixtureDef.shape = horizontalBulletRectangle;
                break;
        }
        return true;
    }
    // end bullet
    // collisions
    public void bulletAndTankCollisionProcedure(Bullet bullet, NpcTank tank) {
        if(tank == playerTank && ((PlayerTank)tank).respawnInvisibility != 0)
            return;
        switch (bullet.type){
            case NORMAL_BULLET:
                tank.takeDamage(Bullet.NORMAL_BULLET_DAMAGE);
                break;
            case PLASMA_BULLET:
                tank.takeDamage(Bullet.PLASMA_BULLET_DAMAGE);
                break;
            case AP_BULLET:
                tank.takeDamage(Bullet.AP_BULLET_DAMAGE);
                break;
            case RAP_BULLET:
                tank.takeDamage(Bullet.RAP_BULLET_DAMAGE);
                break;
        }
        if(!bullet.takeDamage(GOD_DAMAGE)){
            deadEntities.add(bullet);
        }
        if(!tank.isAlive()){
            if(tank == playerTank) {
                needPlayerDispawn = true;
            } else {
                deadEntities.add(tank);
            }
        }
    }

    public void bulletAndWallCollisionProcedure(Bullet bullet, Wall wall) {
        switch (bullet.type) {
            case NORMAL_BULLET:
            case PLASMA_BULLET:
                if (wall.bulletDamage)
                    wall.takeDamage((byte) 1);
                break;
            case AP_BULLET:
            case RAP_BULLET:
                if (wall.bulletDamage)
                    wall.takeDamage((byte) 2);
                break;
        }
        if(!bullet.takeDamage(GOD_DAMAGE)){
            deadEntities.add(bullet);
        }
        if(!wall.isAlive()){
            deadEntities.add(wall);
        }
    }

    public void bulletAndBulletCollisionProcedure(Bullet bullet1, Bullet bullet2) {
        if(!bullet1.takeDamage((byte) 1)){
            deadEntities.add(bullet1);
        }
        if(!bullet2.takeDamage((byte) 1)){
            deadEntities.add(bullet2);
        }
    }
    // end collisions
    // updating level
    public void update(float delta) {
        frameTime = Math.min(delta, Constants.Settings.FRAME_TIME_MAX);

        updateLevelState(frameTime);

        if(playerTank != null)
            playerTank.update(frameTime);

        for(aliveIterator = aliveNpcTanks.size - 1; aliveIterator >=0; aliveIterator --)
            aliveNpcTanks.get(aliveIterator).update(frameTime);

        accumulator += frameTime;
        while (accumulator >= Constants.Physics.PHYSICS_STEP) {
            world.step(Constants.Physics.PHYSICS_STEP,
                    Constants.Physics.VELOCITY_ITERATIONS,
                    Constants.Physics.POSITION_ITERATIONS);
            removeDead();
            accumulator -= Constants.Physics.PHYSICS_STEP;
        }
    }

    private void updateLevelState(float frameTime) {
        levelWin = levelEndChecker.checkWinCondition(this);
        levelFail = levelEndChecker.checkFailCondition(this);

        if(playerTank  != null)
            endPlayerSpeedUp(frameTime);
        endEnemyFreeze(frameTime);

        if(needPlayerSpeedDown){
            playerTank.setSpeedNormal();
            needPlayerSpeedDown = false;
            speedUpPlayer = false;
        }
        if(needEnemyUnfreeze){
            for(aliveIterator = aliveBullets.size - 1; aliveIterator >=0; aliveIterator --)
                aliveBullets.get(aliveIterator).unfreeze();
            for(aliveIterator = aliveNpcTanks.size - 1; aliveIterator >=0; aliveIterator --)
                aliveNpcTanks.get(aliveIterator).unfreeze();
            needEnemyUnfreeze = false;
            enemyFreeze = false;
        }

        if(needPlayerSpeedUp){
            if(playerTank != null)
                playerTank.setSpeedUp();
            else
                deadPlayerTank.setSpeedUp();
            needPlayerSpeedUp = false;
            speedUpPlayer = true;
        }
        if(needEnemyFreeze){
            for(aliveIterator = aliveBullets.size - 1; aliveIterator >=0; aliveIterator --)
                aliveBullets.get(aliveIterator).freeze();
            for(aliveIterator = aliveNpcTanks.size - 1; aliveIterator >=0; aliveIterator --)
                aliveNpcTanks.get(aliveIterator).freeze();
            needEnemyFreeze = false;
            enemyFreeze = true;
        }
        if(needPlayerRespawn)
            respawnPlayer();
    }

    private void respawnPlayer(){
        if(objectsMatrix[allySpawnY][allySpawnX] == Constants.Physics.CATEGORY_SPAWN && !levelFail) {
            playerTank = deadPlayerTank;
            objectsMatrix[allySpawnY][allySpawnX] = Constants.Physics.CATEGORY_ALLY_TANK | Constants.Physics.CATEGORY_SPAWN;
            playerTank.respawn(allySpawnX,allySpawnY);
            deadPlayerTank = null;
            needPlayerRespawn = false;
        }
    }

    public void beginPlayerSpeedUp(){
        if(this.speedUpPlayerTime == 0)
            needPlayerSpeedUp = true;
        this.speedUpPlayerTime = playerTank.playerManager.getTankType() ==  TankType.LIGHT_TANK ? SPEED_UP_TIME : SPEED_UP_TIME + SPEED_UP_INCREASE;
    }

    public void endPlayerSpeedUp(float delta){
        if(speedUpPlayer) {
            speedUpPlayerTime -= delta;
            if(speedUpPlayerTime <= 0){
                needPlayerSpeedDown = true;
                speedUpPlayerTime = 0;
            }
        }
    }

    public void beginEnemyFreeze(){
        if(this.freezeEnemyTime == 0)
            needEnemyFreeze = true;
        this.freezeEnemyTime = playerTank.playerManager.getTankType() == TankType.LIGHT_TANK? FREEZE_TIME : FREEZE_TIME + FREEZE_INCREASE;
    }

    private void endEnemyFreeze(float delta){
        if(enemyFreeze) {
            freezeEnemyTime -= delta;
            if(freezeEnemyTime <= 0) {
                needEnemyUnfreeze = true;
                freezeEnemyTime = 0;
            }
        }
    }

    public void removeDead(){
        if(needPlayerDispawn){
            playerTank.getBody().setTransform(Constants.Physics.FAR_FAR,Constants.Physics.FAR_FAR,0);
            playerTank.playerManager.stats[PlayerManager.LIVES_ID]--;
            deadPlayerTank = playerTank;
            playerTank = null;
            needPlayerDispawn = false;
            if(deadPlayerTank.playerManager.getLives() >= 0 )
                needPlayerRespawn = true;
        }
        for(deadIterator = deadEntities.size - 1; deadIterator >= 0; deadIterator--){
            deadEntity = deadEntities.get(deadIterator);
            world.destroyBody(deadEntity.getBody());
            switch (deadEntity.getCategory()){
                case Constants.Physics.CATEGORY_ALLY_BULLET:
                case Constants.Physics.CATEGORY_ENEMY_BULLET:
                    aliveBullets.removeValue((Bullet) deadEntity, true);
                    bulletPool.free((Bullet) deadEntity);
                    break;
                case Constants.Physics.CATEGORY_ALLY_TANK:
                case Constants.Physics.CATEGORY_ENEMY_TANK:
                    aliveNpcTanks.removeValue((NpcTank) deadEntity,true);
                    npcTankPool.free((NpcTank) deadEntity);
                    break;
                case Constants.Physics.CATEGORY_WALL:
                    aliveWalls.removeValue((Wall) deadEntity,true);
                    wallPool.free((Wall) deadEntity);
                    break;
            }
        }
        deadEntities.clear();
    }
    // updating level end
    //disposing level
    public void dispose() {
        world.dispose();
        wallRectangle.dispose();
        tankRectangle.dispose();
        horizontalBulletRectangle.dispose();
        verticalBulletRectangle.dispose();
        freeAliveArrays();
    }

    private void freeAliveArrays() {
        bulletPool.freeAll(aliveBullets);
        npcTankPool.freeAll(aliveNpcTanks);
        wallPool.freeAll(aliveWalls);
    }
    //disposing level end
}
