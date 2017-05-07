package inc.a13xis.legacy.HerdCraft.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class Herd
{
    private World worldObj;

    /** list of all included objects */
    private List herdMembers = new ArrayList();

    /**
     * This is the sum of all member coordinates and used to calculate the actual herd center by dividing by the number
     * of members.
     */
    private BlockPos centerHelper = new BlockPos(0, 0, 0);

    /** This is the actual herd center. */
    private BlockPos center = new BlockPos(0, 0, 0);
    public Vec3d fleeIn = new Vec3d(0.0, 0.0, 0.0);
    private Class myClass;
    private int herdRadius = 0;
    private int minBreed = 0;
    private int maxBreed = 0;
    private int timeSinceBreed = 0;
    private int BREED_BASE_TIME, BREED_VAR_TIME;

    private EntityLivingBase enemy = null;	//used to determine if we need to panic/attack
    private EntityPlayer tempter = null;
    
    public Herd() {}

    public Herd(Class parClass, World parWorld, int parMinBreed, int parMaxBreed, int BREED_BASE_TIME, int BREED_VAR_TIME)
    {
        myClass = parClass;
        worldObj = parWorld;
        minBreed = parMinBreed;
        maxBreed = parMaxBreed;
        this.BREED_BASE_TIME = BREED_BASE_TIME;
        this.BREED_VAR_TIME = BREED_VAR_TIME;
    }

    /**
     * Called periodically by HerdCollection
     */
    public void tick(int tick)
    {
    	if(worldObj != null && !worldObj.isRemote)
    	{
	    	timeSinceBreed++;
	
	        if (tick % 20 == 0)
	        {
	        	this.updateMembers();
	            updateHerdRadiusAndCenter();
	            checkBreed();
	        }
        }
    }

	private void checkBreed() {
		if (minBreed >= 2 && herdMembers.size() >= minBreed && herdMembers.size() <= maxBreed)
        {
        	int median = (maxBreed + minBreed) / 2;
        	int range = (maxBreed - minBreed) / 2;
        	int size = herdMembers.size();
        	int offset = median < size ? size - median : median - size;	//aka: |size-median|
        	if (timeSinceBreed > (BREED_BASE_TIME + (int)(BREED_VAR_TIME * ((double) offset / (double)range))))	//between 2.5 min and 5 depending on distance from median size
        	{
        		EntityAnimal first = null;
        		for(EntityAnimal member: (List<EntityAnimal>)herdMembers)
        		{
        			if (member.getGrowingAge() == 0)
        			{
        				if (first == null)
        				{
        					first = member;
        				}
        				else
        				{
        					member.setInLove(null);//Animal is fed by null
        					first.setInLove(null);//Animal is fed by null
        					timeSinceBreed = 0;
        					break;	//only 1 pair at once.
        				}
        			}
        		}
        	}
        }
        else
        {
        	timeSinceBreed = 0;
        }
	}

	public void updateMembers()
    {
    	//finds all "myClass" in radius.
        herdMembers = this.worldObj.getEntitiesWithinAABB(myClass, new AxisAlignedBB((double)(center.getX() - herdRadius), (double)(center.getY() - 6), (double)(center.getZ() - herdRadius), (double)(center.getX() + herdRadius), (double)(this.center.getY() + 6), (double)(center.getZ() + herdRadius)));
        Vec3i vector = new Vec3i(centerHelper.getX(),centerHelper.getY(),centerHelper.getZ());
		centerHelper = centerHelper.subtract(vector);
        Iterator i = herdMembers.iterator();
        EntityLivingBase curr = null;
        for (; i.hasNext(); curr = (EntityLivingBase)i.next()){
        	if (curr != null){
        		int currx,curry,currz;
        		currx = curr.posX < 0? (int)Math.floor(curr.posX) : (int)Math.ceil(curr.posX);
				curry = curr.posY< 0? (int)Math.floor(curr.posY) : (int)Math.ceil(curr.posY);
				currz = curr.posZ < 0? (int)Math.floor(curr.posZ) : (int)Math.ceil(curr.posZ);
				centerHelper = centerHelper.east(currx);
				centerHelper = centerHelper.up(curry);
				centerHelper = centerHelper.south(currz);
        	}
        }
        if (curr != null){
			int currx,curry,currz;
			currx = curr.posX < 0? (int)Math.floor(curr.posX) : (int)Math.ceil(curr.posX);
			curry = curr.posY< 0? (int)Math.floor(curr.posY) : (int)Math.ceil(curr.posY);
			currz = curr.posZ < 0? (int)Math.floor(curr.posZ) : (int)Math.ceil(curr.posZ);
			centerHelper = centerHelper.east(currx);
			centerHelper = centerHelper.up(curry);
			centerHelper = centerHelper.south(currz);
    	}
    }
    
    protected void addMember(EntityLivingBase curr){
    	herdMembers.add(curr);
		int currx,curry,currz;
		currx = curr.posX < 0? (int)Math.floor(curr.posX) : (int)Math.ceil(curr.posX);
		curry = curr.posY< 0? (int)Math.floor(curr.posY) : (int)Math.ceil(curr.posY);
		currz = curr.posZ < 0? (int)Math.floor(curr.posZ) : (int)Math.ceil(curr.posZ);
		centerHelper = centerHelper.east(currx);
		centerHelper = centerHelper.up(curry);
		centerHelper = centerHelper.south(currz);
    	updateHerdRadiusAndCenter();
    }

    //Destroy self to bolster other herd.
    public void mergeHerd(Herd otherHerd){
    	EntityLivingBase curr = null;
    	if (enemy != null)
    	{
    		otherHerd.setEnemy(enemy);
    	}
    	for (Iterator i = herdMembers.iterator(); i.hasNext(); curr = (EntityLivingBase)i.next()){
    		if (curr != null){
    			otherHerd.addMember(curr);
    			i.remove();
    		}
    	}
    	herdRadius = 0;
    }
    
    public BlockPos getCenter()
    {
        return this.center;
    }

    public int getHerdRadius()
    {
        return this.herdRadius;
    }
    
    public World getWorldObj()
    {
    	return worldObj;
    }

    public EntityLivingBase getEnemy()
    {
    	return this.enemy;
    }
    
	public void setEnemy(EntityLivingBase enemy2) {
		if (myClass.isInstance(enemy2)) return; //Our herd cannot hate itself.
		this.enemy = enemy2;
		fleeIn.subtract(fleeIn.xCoord,0,fleeIn.zCoord);
		if (enemy != null)
		{
			fleeIn.addVector(center.getX() - enemy.posX,0, center.getZ() - enemy.posZ);
			fleeIn = fleeIn.normalize();
		}
	}
	
	public EntityPlayer getTempter()
    {
    	return this.tempter;
    }
    
	public void setTempter(EntityPlayer aiTarget) {
		this.tempter = aiTarget;
	}
    
	/*
	 * Returns an entity does see the herd's target.
	 */
	public EntityLivingBase getForwardEnemySeer(EntityLiving source)
	{
		if (enemy == null)
		{
			return null;
		}
		
		EntityLivingBase curr = null;
		double currDistSqrd = 0;
		EntityLivingBase nearest = null;
		double nearestDistSqrd = Double.MAX_VALUE;
		Iterator i = herdMembers.iterator();
		while (i.hasNext())
		{
			curr = (EntityLivingBase) i.next();
			currDistSqrd = curr.getDistanceSqToEntity(enemy); 
			if (currDistSqrd < nearestDistSqrd && curr != source && curr.getDistanceSqToEntity(source) < 16 * 16) //closest thing not me, that I can see.
    		{
    			nearest = curr;
    			nearestDistSqrd = currDistSqrd;
    		}
   		}

		if (nearest != null && nearestDistSqrd < source.getDistanceSqToEntity(enemy))	//if I'm closer, give up.
		{
			return nearest;
		}
		enemy = null;	//No-one sees it. Forget about it.
		return null;
	}
	
	public EntityLivingBase getForwardTempterSeer(EntityLiving source)
	{
		if (tempter == null)
		{
			return null;
		}
		
		EntityLivingBase curr = null;
		double currDistSqrd = 0;
		EntityLivingBase nearest = null;
		double nearestDistSqrd = Double.MAX_VALUE;
		Iterator i = herdMembers.iterator();
		while (i.hasNext())
		{
			curr = (EntityLivingBase) i.next();
			currDistSqrd = curr.getDistanceSqToEntity(tempter); 
			if (currDistSqrd < nearestDistSqrd && curr != source && curr.getDistanceSqToEntity(source) < 16 * 16) //closest thing not me, that I can see.
    		{
    			nearest = curr;
    			nearestDistSqrd = currDistSqrd;
    		}
   		}

		if (nearest != null && nearestDistSqrd < source.getDistanceSqToEntity(tempter))	//if I'm closer, give up.
		{
			return nearest;
		}
		tempter = null;	//No-one sees it. Forget about it.
		return null;
	}
	
    public int getNumMembers()
    {
        return this.herdMembers.size();
    }

    /**
     * Returns true, if the given coordinates are within the bounding box of the herd.
     */
    public boolean isInRange(int par1, int par2, int par3)
    {
        return this.center.getDistance(par1, par2, par3) * this.center.getDistance(par1, par2, par3) < (float)(this.herdRadius * this.herdRadius);
    }

    /**
     * called only by class EntityAIMoveThroughHerd
     */
    public List getHerdMembers()
    {
        return this.herdMembers;
    }

    /**
     * Returns true, if there is not a single member left. Called by HerdCollection
     */
    public boolean isAnnihilated()
    {
        return this.herdMembers.isEmpty();
    }


    protected void updateHerdRadiusAndCenter()
    {
        int size = this.herdMembers.size();
		center = center.subtract(new Vec3i(center.getX(),center.getY(),center.getZ()));
        if (size == 0)
        {
			centerHelper = centerHelper.subtract(new Vec3i(centerHelper.getX(),centerHelper.getY(),centerHelper.getZ()));
            this.herdRadius = 0;
        }
        else
        {
			center = center.add(this.centerHelper.getX() / size, this.centerHelper.getY() / size, this.centerHelper.getZ() / size);
            int radius = 0;
            EntityLivingBase curr;

            for (Iterator i = this.herdMembers.iterator(); i.hasNext(); radius = (int) Math.max(curr.getDistanceSq(this.center.getX(), this.center.getY(), this.center.getZ()), radius))
            {
                curr = (EntityLivingBase)i.next();
            }

            this.herdRadius = Math.min(64, Math.max(16, 5 + size * size));
        }
    }

    public void shuntCenter(BlockPos otherCenter, int otherSize)
    {
    	int mySize = herdMembers.size();
    	if (otherSize > mySize)
    	{
    		Vec3d direction = new Vec3d(center.getX() - otherCenter.getX(), center.getY() - otherCenter.getY(), center.getZ() - otherCenter.getZ());
    		direction.normalize();
    		int xShunt = (int) (Math.max(8.0D, (double)otherSize / (double)mySize) * direction.xCoord);
    		int yShunt = (int) (Math.max(8.0D, (double)otherSize / (double)mySize) * direction.yCoord); 
    		int zShunt = (int) (Math.max(8.0D, (double)otherSize / (double)mySize) * direction.zCoord);
			center = center.add(center.getX() + xShunt, center.getY() + yShunt, center.getZ() + zShunt);
			center = center.subtract(new Vec3i(center.getX(),center.getY(),center.getZ()));
    	}
    }
    
	public Class getType() {
		return myClass;
	}

	public int getSize() {
		return herdMembers.size();
	}
}
