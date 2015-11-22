package com.HerdCraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.world.World;

import com.HerdCraft.common.Herd;
import com.HerdCraft.common.HerdCraft;

public class EntityAIHerdAttackOnCollide extends EntityAIBase
{
    World worldObj;
    EntityLiving attacker;
    EntityLivingBase entityTarget;
    private Class attackerEffectiveClass;

    /**
     * An amount of decrementing ticks that allows the entity to attack once the tick reaches 0.
     */
    int attackTick;
    double moveSpeed;
    boolean seesThroughWalls;

    /** The PathEntity of our entity. */
    PathEntity entityPathEntity;
    Class classTarget;
    private int navThrottler;

    public EntityAIHerdAttackOnCollide(EntityLiving par1EntityLiving, Class par2Class, double moveSpeed,
    		boolean seesThroughWalls, Class effective)
    {
        this(par1EntityLiving, moveSpeed, seesThroughWalls, effective);
        this.classTarget = par2Class;
    }

    public EntityAIHerdAttackOnCollide(EntityLiving attacker, double moveSpeed, boolean seesThroughWalls, Class effective)
    {
    	if (effective != null)	//optional class to treat this as.
        {
        	attackerEffectiveClass = effective;
        }
        else
        {
        	attackerEffectiveClass = attacker.getClass();
        }
        this.attackTick = 0;
        this.attacker = attacker;
        this.worldObj = attacker.worldObj;
        this.moveSpeed = moveSpeed;
        this.seesThroughWalls = seesThroughWalls;
        this.setMutexBits(3);
    }

	/**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase var1 = this.attacker.getAttackTarget();
        EntityLivingBase var2 = this.attacker.getAITarget();
        Herd ourHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(attacker, attackerEffectiveClass, 0, 0, 0, 0);	//attackers never breed.

        if (var1 == null && var2 == null && ourHerd.getEnemy() == null)
        {
            return false;
        }
        else if (var1 != null && this.classTarget != null && !this.classTarget.isAssignableFrom(var1.getClass()))
        {
            return false;
        }

        if (var1 != null)
        {
        	ourHerd.setEnemy(var1);
        }
        if (var2 != null)
        {
        	ourHerd.setEnemy(var2);
        }
        entityTarget = ourHerd.getEnemy();
        this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(this.entityTarget);
        EntityLivingBase seer = ourHerd.getForwardEnemySeer(attacker);
        if (entityPathEntity == null && seer != null)
        {
        	entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(seer);
        }
        return this.entityPathEntity != null;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        Herd myHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(attacker, attackerEffectiveClass, 0, 0, 0, 0);
    	EntityLivingBase var1 = this.attacker.getAttackTarget();
        if (var1 == null)
        {
        	var1 = myHerd.getEnemy();
        }
        else
        {
        	myHerd.setEnemy(var1);
        }
        return var1 == null ? false : (!this.entityTarget.isEntityAlive() ? false : !this.attacker.getNavigator().noPath());
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.attacker.getNavigator().setPath(this.entityPathEntity, this.moveSpeed);
        this.navThrottler = 0;
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.entityTarget = null;
        this.attacker.getNavigator().clearPathEntity();
        HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(attacker, attackerEffectiveClass,0,0,0,0).setEnemy(null);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	Herd myHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(attacker, attackerEffectiveClass,0,0,0,0);
    	
    	
        this.attacker.getLookHelper().setLookPositionWithEntity(this.entityTarget, 30.0F, 30.0F);

        if ((this.seesThroughWalls || this.attacker.getEntitySenses().canSee(this.entityTarget)) && --this.navThrottler <= 0)
        {
            this.navThrottler = 4 + this.attacker.getRNG().nextInt(7);
            if (!this.attacker.getNavigator().tryMoveToEntityLiving(this.entityTarget, this.moveSpeed))
            {
            	EntityLivingBase seer = myHerd.getForwardEnemySeer(attacker);
            	if (seer != null){
            		this.attacker.getNavigator().tryMoveToEntityLiving(seer, this.moveSpeed);
            	}
            }
        }

        this.attackTick = Math.max(this.attackTick - 1, 0);
        double var1 = (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F);

        if (this.attacker.getDistanceSq(this.entityTarget.posX, this.entityTarget.boundingBox.minY, this.entityTarget.posZ) <= var1)
        {
            if (this.attackTick <= 0)
            {
                this.attackTick = 20;

                if (this.attacker.getHeldItem() != null)
                {
                    this.attacker.swingItem();
                }

                this.attacker.attackEntityAsMob(this.entityTarget);
            }
        }
    }
}
