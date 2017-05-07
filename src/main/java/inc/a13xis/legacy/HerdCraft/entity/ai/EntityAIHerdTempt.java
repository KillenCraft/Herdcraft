package inc.a13xis.legacy.HerdCraft.entity.ai;

import inc.a13xis.legacy.HerdCraft.common.Herd;
import inc.a13xis.legacy.HerdCraft.common.HerdCraft;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class EntityAIHerdTempt extends EntityAIBase {
	private Class effectiveClass;
	/** The entity using this AI that is tempted by the player. */
    private EntityCreature temptedEntity;
    private double moveSpeed;
    private double lastKnownPosX;
    private double lastKnownPosY;
    private double lastKnownPosZ;
    private double lastKnownPitch;
    private double lastKnownYaw;

    /** The player that is tempting the entity that is using this AI. */
    private EntityPlayer temptingPlayer;

    /**
     * A counter that is decremented each time the shouldExecute method is called. The shouldExecute method will always
     * return false if delayTemptCounter is greater than 0.
     */
    private int delayTemptCounter = 0;

    /**
     * This field saves the ID of the items that can be used to breed entities with this behaviour.
     */
    private Item breedingFood;

    /**
     * Whether the entity using this AI will be scared by the tempter's sudden movement.
     */
    private boolean scaredByPlayerMovement;
    private boolean temptedAvoidsWater;
	private int maxBreed, minBreed; //count
	private int baseBreed, varBreed;//time
	private EntityLivingBase seerEntity;	//used to path to our forward seer.

    public EntityAIHerdTempt(EntityCreature temptedEntity, double speed, Item breedingFood, boolean scaredByPlayerMovement, int minBreed, int maxBreed, int baseBreed, int varBreed, Class... effective)
    {
    	if (effective != null && effective.length > 0)	//optional class to treat this as.
        {
        	effectiveClass = effective[0];
        }
        else
        {
        	effectiveClass = temptedEntity.getClass();
        }
    	this.minBreed = minBreed;
    	this.maxBreed = maxBreed;
        this.temptedEntity = temptedEntity;
        this.moveSpeed = speed;
        this.breedingFood = breedingFood;
        this.scaredByPlayerMovement = scaredByPlayerMovement;
        this.setMutexBits(3);
    }

    public boolean shouldExecute()
    {
        if (this.delayTemptCounter > 0)
        {
            --this.delayTemptCounter;
            return false;
        }
        else
        {
        	Herd ourHerd = HerdCraft.herdCollectionObj.handleNearestHerdOrMakeNew(temptedEntity, effectiveClass, minBreed, maxBreed, baseBreed, varBreed);	//only care if timer is up
            this.temptingPlayer = this.temptedEntity.worldObj.getClosestPlayerToEntity(this.temptedEntity, 10.0D);

            if (this.temptingPlayer == null)
            {
            	if (ourHerd.getTempter() == null)
            	{
            		return false;
            	}
            	else
            	{
            		EntityLivingBase seer = ourHerd.getForwardTempterSeer(temptedEntity);
            		if (seer != null)
            		{
            			ItemStack heldStack = ourHerd.getTempter().getHeldItem(EnumHand.MAIN_HAND);
            			if (heldStack != null && heldStack.getItem() == breedingFood)
            			{
            				seerEntity = temptedEntity.getNavigator().getPathToEntityLiving(seer) == null?null:seer;
            				return seerEntity != null;
            			}
            		}
            		return false;
            	}
            }
            else
            {
            	seerEntity = null;
            	ItemStack var1 = this.temptingPlayer.getHeldItem(EnumHand.MAIN_HAND);
            	if (var1 != null && var1.getItem() == this.breedingFood)
            	{
            		ourHerd.setTempter(temptingPlayer);	//We found a nearby player.
            		return true;
            	}
            	else
            	{
            		return false;
            	}
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        if (this.scaredByPlayerMovement)
        {
            if (this.temptedEntity.getDistanceSqToEntity(this.temptingPlayer) < 36.0D)
            {
                if (this.temptingPlayer.getDistanceSq(this.lastKnownPosX, this.lastKnownPosY, this.lastKnownPosZ) > 0.010000000000000002D)
                {
                    return false;
                }

                if (Math.abs((double)this.temptingPlayer.rotationPitch - this.lastKnownPitch) > 5.0D || Math.abs((double)this.temptingPlayer.rotationYaw - this.lastKnownYaw) > 5.0D)
                {
                    return false;
                }
            }
            else
            {
                this.lastKnownPosX = this.temptingPlayer.posX;
                this.lastKnownPosY = this.temptingPlayer.posY;
                this.lastKnownPosZ = this.temptingPlayer.posZ;
            }

            this.lastKnownPitch = (double)this.temptingPlayer.rotationPitch;
            this.lastKnownYaw = (double)this.temptingPlayer.rotationYaw;
        }

        return this.shouldExecute();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        if (temptingPlayer != null)
        {
	    	this.lastKnownPosX = this.temptingPlayer.posX;
	        this.lastKnownPosY = this.temptingPlayer.posY;
	        this.lastKnownPosZ = this.temptingPlayer.posZ;
        }
        this.temptedAvoidsWater = this.temptedEntity.getNavigator().getNodeProcessor().getCanSwim();
        this.temptedEntity.getNavigator().getNodeProcessor().setCanSwim(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.temptingPlayer = null;
        this.temptedEntity.getNavigator().clearPathEntity();
        this.delayTemptCounter = 100;
        this.temptedEntity.getNavigator().getNodeProcessor().setCanSwim(this.temptedAvoidsWater);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	if (temptingPlayer != null)
    	{
	    	this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.temptingPlayer, 30.0F, (float)this.temptedEntity.getVerticalFaceSpeed());
	        double distanceToEntity = temptedEntity.getDistanceSqToEntity(this.temptingPlayer); 
	        if (distanceToEntity < 6.25D)
	        {
	            this.temptedEntity.getNavigator().clearPathEntity();
	        }
	        else
	        {
	            this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.moveSpeed);
	        }
    	}
    	else
    	{
    		this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.seerEntity, 30.0F, (float)this.temptedEntity.getVerticalFaceSpeed());
    		double distanceToEntity = temptedEntity.getDistanceSqToEntity(this.seerEntity); 
	        if (distanceToEntity < 6.25D)
	        {
	            this.temptedEntity.getNavigator().clearPathEntity();
	        }
	        else
	        {
	            this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.seerEntity, this.moveSpeed + (float)(Math.sqrt(distanceToEntity) / 250.0));
	        }
    	}
    }
}
