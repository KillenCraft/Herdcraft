package com.HerdCraft.entity.ai;

import java.util.List;

import scala.util.Random;

import com.HerdCraft.common.Herd;
import com.HerdCraft.common.HerdCraft;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class EntityAIHerdArrowAttack extends EntityAIBase
{
	public final IEntitySelector entityFilter = new IEntitySelector()
    {
        private static final String __OBFID = "CL_00001575";
        /**
         * Return whether the specified entity is applicable to this filter.
         */
        public boolean isEntityApplicable(Entity target)
        {
            return entityHost.getClass().isAssignableFrom(target.getClass()) && target.isEntityAlive() && entityHost.getEntitySenses().canSee(target);
        }
    };
	
    /** The entity the AI instance has been applied to */
    private final EntityLiving entityHost;
    /**
     * The entity (as a RangedAttackMob) the AI instance has been applied to.
     */
    private final IRangedAttackMob rangedAttackEntityHost;
    private EntityLivingBase attackTarget;
    /**
     * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
     * maxRangedAttackTime.
     */
    private int rangedAttackTime;
    private double entityMoveSpeed;
    private int timeTargetSeen;
    private int minRangedAttackTime;
    /**
     * The maximum time the AI has to wait before peforming another ranged attack.
     */
    private int maxRangedAttackTime;
    private float attackRange;
    private float attackRangeSquared;
    private static final String __OBFID = "CL_00001609";
    
    private int cautionAngle;
    private boolean clockwise;
    private boolean interuptableByRange;

	private int timeSinceBlockedCheck;
	private boolean wasBlocked;

    public EntityAIHerdArrowAttack(IRangedAttackMob host, double moveSpeed, int rangedAttackTime, float attackRange, int cautionAngle, boolean clockwise, boolean interuptableByRange)
    {
        this(host, moveSpeed, rangedAttackTime, rangedAttackTime, attackRange, cautionAngle, clockwise, interuptableByRange);
    }

    public EntityAIHerdArrowAttack(IRangedAttackMob host, double moveSpeed, int minRangedAttackTime, int maxRangedAttackTime, float attackRange, int cautionAngle, boolean clockwise, boolean interuptableByRange)
    {
        this.rangedAttackTime = -1;

        if (!(host instanceof EntityLivingBase))
        {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        else
        {
            this.rangedAttackEntityHost = host;
            this.entityHost = (EntityLiving)host;
            this.entityMoveSpeed = moveSpeed;
            this.minRangedAttackTime = minRangedAttackTime;
            this.maxRangedAttackTime = maxRangedAttackTime;
            this.attackRange = attackRange;
            this.attackRangeSquared = attackRange * attackRange;
            this.cautionAngle = cautionAngle;
            this.clockwise = clockwise;
            this.interuptableByRange = interuptableByRange;
            this.setMutexBits(3);
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();
        Herd ourHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entityHost, entityHost.getClass(), 0, 0, 0, 0);	//attackers never breed.

        if (entitylivingbase == null && ourHerd.getEnemy() == null)
        {
            return false;
        }
        else if (entitylivingbase != null)
        {
        	ourHerd.setEnemy(entitylivingbase);
        }
        attackTarget = ourHerd.getEnemy();
        if(interuptableByRange && entityHost.getDistanceToEntity(attackTarget) < 5.0F)
        {
        	return false;
        }
        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return this.shouldExecute() || !this.entityHost.getNavigator().noPath();
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.attackTarget = null;
        this.timeTargetSeen = 0;
        this.rangedAttackTime = -1;
        HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entityHost, entityHost.getClass(), 0, 0, 0, 0).setEnemy(null);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	if (attackTarget == null) //Shouldn't happen, but does. 
    	{
    		resetTask();
    		return; 
    	}
        double d0 = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.boundingBox.minY, this.attackTarget.posZ);
        boolean canSeeTarget = this.entityHost.getEntitySenses().canSee(this.attackTarget);

        if (canSeeTarget)
        {
            ++this.timeTargetSeen;
        }
        else
        {
            this.timeTargetSeen = 0;
        }
        wasBlocked = isBlockedByOther(MathHelper.sqrt_double(d0));
        if (d0 <= (double)this.attackRangeSquared)
        {
        	
        	if(wasBlocked)
            {
            	circleTarget();
            }
        	else if (this.timeTargetSeen >= 20)
        	{
        		this.entityHost.getNavigator().clearPathEntity();
        	}
            
        }
        else
        {
        	if (!this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, this.entityMoveSpeed))
        	{
        		EntityLivingBase seer = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(entityHost, entityHost.getClass(), 0, 0, 0, 0).getForwardEnemySeer(entityHost);
        		if (seer != null)
        		{
        			entityHost.getNavigator().tryMoveToEntityLiving(seer, entityMoveSpeed);
        		}
        	}
        }

        this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
        float f;

        if (--this.rangedAttackTime == 0)
        {
            if (d0 > (double)this.attackRangeSquared || !canSeeTarget || wasBlocked)
            {
                return;
            }

            f = MathHelper.sqrt_double(d0) / this.attackRange;
            float f1 = f;

            if (f < 0.1F)
            {
                f1 = 0.1F;
            }

            if (f1 > 1.0F)
            {
                f1 = 1.0F;
            }

            this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, f1);
            this.rangedAttackTime = MathHelper.floor_float(f * (float)(this.maxRangedAttackTime - this.minRangedAttackTime) + (float)this.minRangedAttackTime);
        }
        else if (this.rangedAttackTime < 0)
        {
            f = MathHelper.sqrt_double(d0) / this.attackRange;
            this.rangedAttackTime = MathHelper.floor_float(f * (float)(this.maxRangedAttackTime - this.minRangedAttackTime) + (float)this.minRangedAttackTime);
        }
    }

	private void circleTarget() {
		Vec3 entityPos = Vec3.createVectorHelper(entityHost.posX, entityHost.posY, entityHost.posZ);
		Vec3 targetPos = Vec3.createVectorHelper(attackTarget.posX, attackTarget.posY, attackTarget.posZ);
		Vec3 centerPos = targetPos.subtract(entityPos);
		centerPos.rotateAroundY(clockwise?cautionAngle:-cautionAngle);
		targetPos = centerPos.addVector(targetPos.xCoord, targetPos.yCoord, targetPos.zCoord);
		entityPos = RandomPositionGenerator.findRandomTargetBlockTowards((EntityCreature) entityHost, 4, 4, targetPos);
		if(entityPos != null)
		{
			entityHost.getNavigator().tryMoveToXYZ(entityPos.xCoord, entityPos.yCoord, entityPos.zCoord, entityMoveSpeed);
		}
	}

	private boolean isBlockedByOther(double distToTarget) {
		if(cautionAngle == 0) return false;
		if(--timeSinceBlockedCheck > 0)
		{
			return wasBlocked;
		}
		timeSinceBlockedCheck = 5;
		List<EntityLivingBase> list = this.entityHost.worldObj.selectEntitiesWithinAABB(this.entityHost.getClass(), this.entityHost.boundingBox.expand(distToTarget, 3.0D, distToTarget), this.entityFilter);
		list.remove(entityHost);
		list.remove(attackTarget);
		if(list.size() == 0) return false;
		Vec3 directionToTarget = Vec3.createVectorHelper(entityHost.posX, 0, entityHost.posZ).subtract(Vec3.createVectorHelper(attackTarget.posX, 0, attackTarget.posZ));
		double angleToTarget = Math.toDegrees(Math.atan2(directionToTarget.xCoord, directionToTarget.zCoord));
		for(EntityLivingBase other:list)
		{
			Vec3 directionToOther = Vec3.createVectorHelper(entityHost.posX, 0, entityHost.posZ).subtract(Vec3.createVectorHelper(other.posX, 0, other.posZ));
			double angleToOther = Math.toDegrees(Math.atan2(directionToOther.xCoord, directionToOther.zCoord));
			if (Math.abs(angleToOther - angleToTarget) < cautionAngle) 
			{
				return true;
			}
		}
		return false;
	}
}