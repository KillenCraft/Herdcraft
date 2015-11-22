package com.HerdCraft.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class HerdCollection
{
    private final List herdList = new ArrayList();
    
    private int tickCounter = 0;
    private boolean magnet;
    private int magnetRadius;

    public HerdCollection(boolean magnet, int magnetRadius)
    {
    	this.magnet = magnet;
    	this.magnetRadius = magnetRadius;
    }

    /**
     * Runs a single tick for the herd collection
     */
	@SubscribeEvent
    public void tick(TickEvent tock)
    {
		if(!tock.side.isServer())return;
        ++this.tickCounter;
        Iterator i = this.herdList.iterator();

        while (i.hasNext())
        {
            Herd curr = (Herd)i.next();
            curr.tick(this.tickCounter);
            if (magnet && tickCounter % 400 == 0)
            {
            	Herd other = findNearestHerd(curr, magnetRadius);
            	if (other != null)
            	{
            		curr.shuntCenter(other.getCenter(), other.getSize());
            	}
            }
            if (this.tickCounter % 40 == 0){
            	Herd other = findNearestHerd(curr, 5);
            	if (other != null){
            		other.mergeHerd(curr);
            	}
            }
        }

        this.removeAnnihilatedHerds();
    }

    private void removeAnnihilatedHerds()
    {
        Iterator var1 = this.herdList.iterator();

        while (var1.hasNext())
        {
            Herd var2 = (Herd)var1.next();

            if (var2.isAnnihilated())
            {
                var1.remove();
            }
        }
    }

    /**
     * Get a list of herds.
     */
    public List getHerdList()
    {
        return this.herdList;
    }

    /**
     * Finds the nearest herd of a type, but only if the given coordinates are within it's bounding box 
     * plus the given distance.
     */
    public Herd findNearestHerd(int posX, int posY, int posZ, int distance, Class type, World world)
    {
        Herd nearest = null;
        float nearestDistSqrd = Float.MAX_VALUE;
        Iterator herd_iter = this.herdList.iterator();

        while (herd_iter.hasNext())
        {
            Herd curr = (Herd)herd_iter.next();
            float currDistSqrd = curr.getCenter().getDistanceSquared(posX, posY, posZ);
            if(curr.getWorldObj() == world && curr.getType() == type)	//if wrong type or world, move along.
            {
	            if (currDistSqrd < nearestDistSqrd)
	            {
	                int searchRadius = distance + curr.getHerdRadius();
	
	                if (currDistSqrd <= (float)(searchRadius * searchRadius))
	                {
	                    nearest = curr;
	                    nearestDistSqrd = currDistSqrd;
	                }
	            }
            }
        }

        return nearest;
    }
    
    /**
     * Finds the nearest herd, but only if the two herd's edges are within distance of each other. 
     */
    public Herd findNearestHerd(Herd center, int distance)
    {
        Herd nearest = null;
        float nearestDistSqrd = Float.MAX_VALUE;
        Iterator herd_iter = this.herdList.iterator();

        while (herd_iter.hasNext())
        {
            Herd curr = (Herd)herd_iter.next();
            float currDistSqrd = curr.getCenter().getDistanceSquared(center.getCenter().posX, center.getCenter().posY, center.getCenter().posZ);

            if (currDistSqrd < nearestDistSqrd)
            {
                int searchRadius = distance + curr.getHerdRadius() + center.getHerdRadius();

                if (currDistSqrd <= (float)(searchRadius * searchRadius) && curr.getType() == center.getType() && curr != center)
                {
                    nearest = curr;
                    nearestDistSqrd = currDistSqrd;
                }
            }
        }

        return nearest;
    }

    /**
     * Called by EntityAIs that want to herd.  
     */
    public Herd handleNearestHerdOrMakeNew(EntityLiving prospective, Class effectiveClass, int minBreed, int maxBreed, int baseBreed, int varBreed){
    	Herd nearest = findNearestHerd((int)prospective.posX, (int)prospective.posY, (int)prospective.posZ, 5, effectiveClass, prospective.worldObj);
    	if (nearest == null){	//make a new one
    		nearest = new Herd(effectiveClass,prospective.worldObj, minBreed, maxBreed, baseBreed, varBreed);
    		nearest.addMember(prospective);
    		nearest.updateMembers();
    		nearest.updateHerdRadiusAndCenter();
    		herdList.add(nearest);
    	}
    	
    	return nearest;
    }
}
