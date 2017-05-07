package inc.a13xis.legacy.HerdCraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import inc.a13xis.legacy.HerdCraft.common.Herd;
import inc.a13xis.legacy.HerdCraft.common.HerdCraft;
import net.minecraft.util.math.Vec3d;

public class EntityAIHerdStampede extends EntityAIBase
{
    private EntityCreature entity;
    private Class entityEffectiveClass;
    private double speed;
	private int minBreed, maxBreed;//Numbers
	private int baseBreed, varBreed;//Times
	private double fleeX, fleeY, fleeZ;

    public EntityAIHerdStampede(EntityCreature entity, double speed, int minBreed, int maxBreed, int baseBreed, int varBreed, Class... effective)
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
        
        Vec3d var1 = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 15, 4, new Vec3d(entity.posX + myHerd.fleeIn.xCoord * 30.0d, entity.posY, entity.posZ + myHerd.fleeIn.zCoord * 30.0d));
        if (var1 == null)
        {
        	return false;
        }
        fleeX = var1.xCoord;
        fleeY = var1.yCoord;
        fleeZ = var1.zCoord;
        return true;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
    	Herd myHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entity, entityEffectiveClass, minBreed, maxBreed, baseBreed, varBreed);
        this.entity.getNavigator().tryMoveToXYZ(fleeX, fleeY, fleeZ, this.speed);
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
    	Herd myHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entity, entityEffectiveClass, minBreed, maxBreed, baseBreed, varBreed); 
    	myHerd.setEnemy(null);
    }
}
