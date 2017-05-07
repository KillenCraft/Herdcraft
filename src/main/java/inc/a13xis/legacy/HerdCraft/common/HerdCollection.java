package inc.a13xis.legacy.HerdCraft.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class HerdCollection
{
    private final List<Herd> herdList = new ArrayList<Herd>();
    
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
		if(!tock.side.isServer())
		    return;
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
            float currDistSqrd = (float)(curr.getCenter().getDistance(posX, posY, posZ)*curr.getCenter().getDistance(posX, posY, posZ));
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
            double currDistSqrd = Math.pow(curr.getCenter().getDistance(center.getCenter().getX(), center.getCenter().getY(), center.getCenter().getZ()),2);

            if (currDistSqrd < nearestDistSqrd)
            {
                int searchRadius = distance + curr.getHerdRadius() + center.getHerdRadius();

                if (currDistSqrd <= (float)(searchRadius * searchRadius) && curr.getType() == center.getType() && curr != center)
                {
                    nearest = curr;
                    nearestDistSqrd = (float)currDistSqrd;
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
