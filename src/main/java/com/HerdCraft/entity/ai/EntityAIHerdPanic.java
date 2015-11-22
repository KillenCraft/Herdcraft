package com.HerdCraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.Vec3;

import com.HerdCraft.common.Herd;
import com.HerdCraft.common.HerdCraft;

public class EntityAIHerdPanic extends EntityAIBase
{
    private EntityCreature entity;
    private Class entityEffectiveClass;
    private double speed;
    private double randPosX;
    private double randPosY;
    private double randPosZ;
	private int minBreed, maxBreed;//Numbers
	private int baseBreed, varBreed;//Times

    public EntityAIHerdPanic(EntityCreature entity, double speed, int minBreed, int maxBreed, int baseBreed, int varBreed, Class... effective)
    {
        this.entity = entity;
        if (effective != null && effective.length > 0)	//optional class to treat this as.
        {
        	entityEffectiveClass = effective[0];
        }
        else
        {
        	entityEffectiveClass = entity.getClass();
        }
        this.speed = speed;
        this.setMutexBits(1);
        this.minBreed = minBreed;
        this.maxBreed = maxBreed;
        this.baseBreed = baseBreed;
        this.varBreed = varBreed;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	Herd myHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entity, entityEffectiveClass, minBreed, maxBreed, baseBreed, varBreed); 
    	if (this.entity.getAITarget() != null)
    	{
    		myHerd.setEnemy(entity.getAITarget());
    	}
        if (myHerd.getEnemy() == null)
        {
            return false;
        }
        
        Vec3 pathTo = RandomPositionGenerator.findRandomTarget(this.entity, 5, 4);
        if (pathTo == null)
        {
            return false;
        }
        else
        {
            this.randPosX = pathTo.xCoord;
            this.randPosY = pathTo.yCoord;
            this.randPosZ = pathTo.zCoord;
            return this.entity.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        //this.entity.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed); Side effect of starting
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.entity.getNavigator().noPath();
    }
    
    public void resetTask()
    {
    	HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entity, entityEffectiveClass, minBreed, maxBreed, baseBreed, varBreed).setEnemy(null);
    }
}
