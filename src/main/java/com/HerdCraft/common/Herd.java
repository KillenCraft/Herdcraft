package com.HerdCraft.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
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
    private final ChunkCoordinates centerHelper = new ChunkCoordinates(0, 0, 0);

    /** This is the actual herd center. */
    private final ChunkCoordinates center = new ChunkCoordinates(0, 0, 0);
    public Vec3 fleeIn = Vec3.createVectorHelper(0.0d, 0.0d, 0.0d);
    private Class myClass;
    private int herdRadius = 0;
    private int tickCounter = 0;
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
	    	this.tickCounter = tick;
	    	timeSinceBreed++;
	
	        if (tick % 20 == 0)
	        {
	        	//this.updateHerdRadiusAndCenter(); Must do members first to maintain herds after load.
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
        					member.func_146082_f(null);//Animal is fed by null
        					first.func_146082_f(null);//Animal is fed by null
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
        herdMembers = this.worldObj.getEntitiesWithinAABB(myClass, AxisAlignedBB.getBoundingBox((double)(center.posX - herdRadius), (double)(center.posY - 6), (double)(center.posZ - herdRadius), (double)(center.posX + herdRadius), (double)(this.center.posY + 6), (double)(center.posZ + herdRadius)));
        centerHelper.posX = centerHelper.posY = centerHelper.posZ = 0;
        Iterator i = herdMembers.iterator();
        EntityLivingBase curr = null;
        for (; i.hasNext(); curr = (EntityLivingBase)i.next()){
        	if (curr != null){
	        	centerHelper.posX += curr.posX;
	        	centerHelper.posY += curr.posY;
	        	centerHelper.posZ += curr.posZ;
        	}
        }
        if (curr != null){
        	centerHelper.posX += curr.posX;
        	centerHelper.posY += curr.posY;
        	centerHelper.posZ += curr.posZ;
    	}
    }
    
    protected void addMember(EntityLivingBase curr){
    	herdMembers.add(curr);
    	centerHelper.posX += curr.posX;
    	centerHelper.posY += curr.posY;
    	centerHelper.posZ += curr.posZ;
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
    
    public ChunkCoordinates getCenter()
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
		if (enemy != null)
		{
			fleeIn.xCoord = center.posX - enemy.posX;
			fleeIn.zCoord = center.posZ - enemy.posZ;
			fleeIn = fleeIn.normalize();
		}
		else
		{
			fleeIn.xCoord = 0;
			fleeIn.zCoord = 0;
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
        return this.center.getDistanceSquared(par1, par2, par3) < (float)(this.herdRadius * this.herdRadius);
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

        if (size == 0)
        {
            this.center.set(0, 0, 0);
            this.centerHelper.set(0, 0, 0);
            this.herdRadius = 0;
        }
        else
        {
            this.center.set(this.centerHelper.posX / size, this.centerHelper.posY / size, this.centerHelper.posZ / size);
            int radius = 0;
            EntityLivingBase curr;

            for (Iterator i = this.herdMembers.iterator(); i.hasNext(); radius = (int) Math.max(curr.getDistanceSq(this.center.posX, this.center.posY, this.center.posZ), radius))
            {
                curr = (EntityLivingBase)i.next();
            }

            this.herdRadius = Math.min(64, Math.max(16, 5 + size * size));
        }
    }

    public void shuntCenter(ChunkCoordinates otherCenter, int otherSize)
    {
    	int mySize = herdMembers.size();
    	if (otherSize > mySize)
    	{
    		Vec3 direction = Vec3.createVectorHelper(center.posX - otherCenter.posX, center.posY - otherCenter.posY, center.posZ - otherCenter.posZ);
    		direction.normalize();
    		int xShunt = (int) (Math.max(8.0D, (double)otherSize / (double)mySize) * direction.xCoord);
    		int yShunt = (int) (Math.max(8.0D, (double)otherSize / (double)mySize) * direction.yCoord); 
    		int zShunt = (int) (Math.max(8.0D, (double)otherSize / (double)mySize) * direction.zCoord); 
    		center.set(center.posX + xShunt, center.posY + yShunt, center.posZ + zShunt);
    	}
    }
    
	public Class getType() {
		return myClass;
	}

	public int getSize() {
		return herdMembers.size();
	}
}
