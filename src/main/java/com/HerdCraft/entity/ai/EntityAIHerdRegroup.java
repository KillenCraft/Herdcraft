package com.HerdCraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

import com.HerdCraft.common.Herd;
import com.HerdCraft.common.HerdCraft;

public class EntityAIHerdRegroup extends EntityAIBase
{
    private EntityCreature entity;
    private Class entityEffectiveClass;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private double speed;
	private int minBreed, maxBreed; // Numbers
	private int baseBreed, varBreed;//Times

    public EntityAIHerdRegroup(EntityCreature par1EntityCreature, double speed, int minBreed, int maxBreed, int baseBreed, int varBreed, Class... effective)
    {
        this.entity = par1EntityCreature;
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
        if (this.entity.getAge() >= 100)
        {
            return false;
        }
        Herd nearHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entity, entityEffectiveClass, minBreed, maxBreed, baseBreed, varBreed);
        ChunkCoordinates center = nearHerd.getCenter();
        
        if (center.getDistanceSquared((int)entity.posX, (int)entity.posY, (int)entity.posZ) < (nearHerd.getHerdRadius() / 3) * (nearHerd.getHerdRadius() / 3)){
        	return false;
        }
        if (this.entity.getRNG().nextInt(20) != 0){
        	return false;
        }
        
        Vec3 var1 = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 10, 4, Vec3.createVectorHelper(center.posX, center.posY, center.posZ));
        if (var1 == null)
        {
        	return false;
        }
        xPosition = var1.xCoord;
        yPosition = var1.yCoord;
        zPosition = var1.zCoord;
        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.entity.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.entity.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}
