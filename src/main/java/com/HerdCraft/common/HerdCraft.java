package com.HerdCraft.common;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

import com.HerdCraft.entity.ai.EntityAIHerdArrowAttack;
import com.HerdCraft.entity.ai.EntityAIHerdAttackOnCollide;
import com.HerdCraft.entity.ai.EntityAIHerdPanic;
import com.HerdCraft.entity.ai.EntityAIHerdRegroup;
import com.HerdCraft.entity.ai.EntityAIHerdStampede;
import com.HerdCraft.entity.ai.EntityAIHerdTempt;
import com.HerdCraft.entity.ai.EntityAIShyFrom;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "herdCraft", name = "HerdCraft", version = "1.1", guiFactory = "com.HerdCraft.common.guiconfig.HerdConfigGUIFactory")
public class HerdCraft 
{
	@Instance(value = "herdCraft")
	public static HerdCraft herdCraftInst;
	
	Random rand = new Random();
	
	public static Configuration config;
	
	private boolean COWS_HERD;
	private boolean COWS_STAMPEDE;
	private int COW_MIN_BREED;
	private int COW_MAX_BREED;
	private int COW_BASE_TIME;
	private int COW_VAR_TIME;
	
	private boolean HORSES_HERD;
	private boolean HORSES_STAMPEDE;
	private int HORSE_MIN_BREED;
	private int HORSE_MAX_BREED;
	private int HORSE_BASE_TIME;
	private int HORSE_VAR_TIME;
	
	private boolean SHEEP_HERD;
	private boolean SHEEP_STAMPEDE;
	private int SHEEP_MIN_BREED;
	private int SHEEP_MAX_BREED;
	private int SHEEP_BASE_TIME;
	private int SHEEP_VAR_TIME;
	
	private boolean PIGS_HERD;
	private boolean PIGS_STAMPEDE;
	private int PIG_MIN_BREED;
	private int PIG_MAX_BREED;
	private int PIG_BASE_TIME;
	private int PIG_VAR_TIME;
	
	private boolean CHICKENS_HERD;
	private boolean CHICKENS_STAMPEDE;
	private int CHICKEN_MIN_BREED;
	private int CHICKEN_MAX_BREED;
	private int CHICKEN_BASE_TIME;
	private int CHICKEN_VAR_TIME;

	private boolean ZOMBIES_HORDE;
	private boolean EXPLICIT_ZOMBIE_HORDING;
	private int ZOMBIE_HORDE_BASE_PROB;
	private int ZOMBIE_HORDE_FULLMOON_PROB;
	private int ZOMBIE_HORDE_NEWMOON_PROB;
	private int ZOMBIE_HORDE_RAINING_PROB;
	private int ZOMBIE_HORDE_COLD_PROB;
	private int ZOMBIE_HORDE_WARM_PROB;
	private int ZOMBIE_HORDE_HOT_PROB;
	private int ZOMBIE_HORDE_DRY_PROB;
	private int ZOMBIE_HORDE_DAMP_PROB;
	private int ZOMBIE_HORDE_WET_PROB;
	private int ZOMBIE_HORDE_FLAT_PROB;
	private int ZOMBIE_HORDE_HILLY_PROB;
	private int ZOMBIE_HORDE_JAGGED_PROB;
	private int ZOMBIE_HORDE_UNDERGROUND_PROB;
	private int ZOMBIE_HORDE_MIN;
	private int ZOMBIE_HORDE_MAX;
	
	//private boolean CAVE_SPIDER_HORDE;
	
	private boolean ZOMBIE_PIGMAN_HORDE;
	private boolean ZOMBIE_PIGMAN_HORDE_AS_ZOMBIE;
	
	private boolean SKELETONS_HERD;
	private float SKELETON_FAN_RANGE;
	private int SKELETON_CAUTION_ANGLE;
	private boolean SKELETON_FEAR_MELEE;
	
	private float CREEPERS_FAN_RANGE;
	
	private boolean MAGNET;
	private int MAGNET_RADIUS;
	
	
	public static HerdCollection herdCollectionObj;
	
	@EventHandler
	public void configure(FMLPreInitializationEvent event)
	{
		FMLCommonHandler.instance().bus().register(this);
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		refreshConfig();
	}
	
	@SubscribeEvent
	 public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if(eventArgs.modID.equals("herdCraft"))
            refreshConfig();
    }
	
	private void refreshConfig()
	{
		Property curr;
		curr = config.get("Cow", "Base Breed Time", 4000);
		curr.comment = "Shortest time for cows to breed from herd";
		COW_BASE_TIME = curr.getInt();
		
		curr = config.get("Cow", "Variable Breed Time", 3000);
		curr.comment = "Extra time added to base time for longest time between breeds.\nNote: Animals will not breed faster than feeding would allow.";
		COW_VAR_TIME = curr.getInt();
		
		curr = config.get("Cow", "Max Cows", 10);
		curr.comment = "Number of cows required for automatic breeding.";
		COW_MAX_BREED = curr.getInt();
		COW_MIN_BREED = config.get("Cow", "Min Cows", 6).getInt();
		
		curr = config.get("Cow", "Herds", true);
		curr.comment = "Whether or not cows participate in herding behaviors.";
		COWS_HERD = curr.getBoolean(true);
		
		curr = config.get("Cow", "Stampedes", true);
		curr.comment = "Whether or not cows stampede rather than panic";
		COWS_STAMPEDE = curr.getBoolean(true);
		
		HORSES_HERD = config.get("Horse", "Herds", true).getBoolean(true);
		HORSES_STAMPEDE = config.get("Horse", "Stampedes", true).getBoolean(true);
		HORSE_BASE_TIME = config.get("Horse", "Base Breed Time", 9000).getInt();
		HORSE_VAR_TIME = config.get("Horse", "Variable Breed Time", 3000).getInt();
		HORSE_MIN_BREED = config.get("Horse", "Min Horses", 6).getInt();
		HORSE_MAX_BREED = config.get("Horse", "Max Horses", 10).getInt();
		
		SHEEP_HERD = config.get("Sheep", "Herds", true).getBoolean(true);
		SHEEP_STAMPEDE = config.get("Sheep", "Stampedes", true).getBoolean(true);
		SHEEP_BASE_TIME = config.get("Sheep", "Base Breed Time", 3500).getInt();
		SHEEP_VAR_TIME = config.get("Sheep", "Variable Breed Time", 2500).getInt();
		SHEEP_MIN_BREED = config.get("Sheep", "Min Sheep", 6).getInt();
		SHEEP_MAX_BREED = config.get("Sheep", "Max Sheep", 10).getInt();
		
		CHICKENS_HERD = config.get("Chicken", "Herds", true).getBoolean(true);
		CHICKENS_STAMPEDE = config.get("Chicken", "Stampedes", false).getBoolean(false);
		CHICKEN_BASE_TIME = config.get("Chicken", "Base Breed Time", 3500).getInt();
		CHICKEN_VAR_TIME = config.get("Chicken", "Variable Breed Time", 2500).getInt();
		CHICKEN_MIN_BREED = config.get("Chicken", "Min Chickens", 6).getInt();
		CHICKEN_MAX_BREED = config.get("Chicken", "Max Chickens", 10).getInt();
		
		PIGS_HERD = config.get("Pig", "Herds", true).getBoolean(true);
		PIGS_STAMPEDE = config.get("Pig", "Stampedes", false).getBoolean(false);
		PIG_BASE_TIME = config.get("Pig", "Base Breed Time", 3000).getInt();
		PIG_VAR_TIME = config.get("Pig", "Variable Breed Time", 1500).getInt();
		PIG_MIN_BREED = config.get("Pig", "Min Pigs", 6).getInt();
		PIG_MAX_BREED = config.get("Pig", "Max Pigs", 12).getInt();
		
		//CAVE_SPIDER_HORDE = config.get("Cave Spider", "Hordes", true).getBoolean(true);
		
		ZOMBIE_PIGMAN_HORDE = config.get("Zombie Pigman", "Hordes", true).getBoolean(true);
		curr =config.get("Zombie Pigman", "Is A Zombie", true);
		curr.comment = "Whether or not Zombie Pigmen try to stay together with Zombies. This is forced true by \"Explicitly Zombie\"";
		ZOMBIE_PIGMAN_HORDE_AS_ZOMBIE = curr.getBoolean(true);
		
		
		
		curr = config.get("Zombie", "Hordes", true);
		curr.comment = "Whether or not zombies participate in hording behavior.\nFalse means they will not form groups, but may still spawn in large groups.\nIf You do not want hordes to spawn, it is best to set Max Zombies < Min Zombies.";
		ZOMBIES_HORDE = curr.getBoolean(true);
		
		curr = config.get("Zombie", "Explicitly Zombie", true);
		curr.comment = "Whether or not various types of zombies group with vanilla zombies. Only works if they extend the zombie class.";
		EXPLICIT_ZOMBIE_HORDING = curr.getBoolean(true);
		
		curr = config.get("Zombie", "Base Horde Probability", 20);
		curr.comment = "Basic probability of Zombie hordes.\nA value of 100 is a 1% chance per spawned zombie.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_BASE_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Full Moon Horde Probability", -200);
		curr.comment = "Probability modifier of Zombie hordes occuring on a full moon.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_FULLMOON_PROB = curr.getInt();
		
		curr = config.get("Zombie", "New Moon Horde Probability", 200);
		curr.comment = "Probability modifier of Zombie hordes occuring on a new moon.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_NEWMOON_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Raining Horde Probability", 100);
		curr.comment = "Probability modifier of Zombie hordes occuring in the rain.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_RAINING_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Cold Horde Probability", -5);
		curr.comment = "Probability modifier of Zombie hordes occuring in cold biomes.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_COLD_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Warm Horde Probability", 0);
		curr.comment = "Probability modifier of Zombie hordes occuring in middle temperature biomes.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_WARM_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Hot Horde Probability", 10);
		curr.comment = "Probability modifier of Zombie hordes occuring in hot biomes.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_HOT_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Dry Horde Probability", -5);
		curr.comment = "Probability modifier of Zombie hordes occuring in dry biomes.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_DRY_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Damp Horde Probability", 10);
		curr.comment = "Probability modifier of Zombie hordes occuring in middle moisture biomes.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_DAMP_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Wet Horde Probability", 20);
		curr.comment = "Probability modifier of Zombie hordes occuring in wet biomes.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_WET_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Flat Terrain Horde Probability", 15);
		curr.comment = "Probability modifier of Zombie hordes occuring on flatter terrain.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_FLAT_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Normal Terrain Horde Probability", 5);
		curr.comment = "Probability modifier of Zombie hordes occuring on typicaly rough terrain.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_HILLY_PROB = curr.getInt();

		curr = config.get("Zombie", "Jagged Terrain Horde Probability", -20);
		curr.comment = "Probability modifier of Zombie hordes occuring on jagged terrain.\nThis value is added with all other relevant spawn values.";
		ZOMBIE_HORDE_JAGGED_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Underground Horde Probability", -10);
		curr.comment = "Probability modifier of Zombie hordes occuring underground.\nThis is only added to the base rate.\nNo others apply underground";
		ZOMBIE_HORDE_UNDERGROUND_PROB = curr.getInt();
		
		curr = config.get("Zombie", "Max Zombies", 15);
		curr.comment = "Extra zombies to be spawned in a horde. If Max < Min, no hordes.";
		ZOMBIE_HORDE_MAX = curr.getInt();
		ZOMBIE_HORDE_MIN = config.get("Zombie", "Min Zombies", 10).getInt();
		
		curr = config.get("Skeleton", "Clack", true);
		curr.comment = "Whether or not skeletons participate in group behavior.";
		SKELETONS_HERD = curr.getBoolean(true);
		
		curr = config.get("Skeleton", "Fan Radius", 1.5F);
		curr.comment = "How far away they want to be from eachother when they're not busy killing you. Use 0 to disable.";
		SKELETON_FAN_RANGE = (float) curr.getDouble(1.5F);
		
		curr = config.get("Skeleton", "Caution Angle", 50);
		curr.comment = "Acceptable degree difference required to shoot at target. Use 0 to disable.";
		SKELETON_CAUTION_ANGLE = curr.getInt(50);
		
		curr = config.get("Skeleton", "Fear Melee", true);
		curr.comment = "Whether or not skeletons try to flee charging players.";
		SKELETON_FEAR_MELEE = curr.getBoolean(true);
		
		curr = config.get("Creeper", "Fan Radius", 6.0F);
		curr.comment = "How far away they want to be from eachother. Use 0 to disable.";
		CREEPERS_FAN_RANGE = (float) curr.getDouble(6.0F);
		
		curr = config.get("Magnet", "Magnet", true);
		curr.comment = "Whether or not larger herds attract smaller ones.";
		MAGNET = curr.getBoolean(true);
		
		curr = config.get("Magnet", "Magnet Radius", 150);
		curr.comment = "Radius from which larger herds attract smaller ones.";
		MAGNET_RADIUS = curr.getInt();
			
		if (config.hasChanged())
		{
			config.save();
		}
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		herdCollectionObj = new HerdCollection(MAGNET, MAGNET_RADIUS);
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(herdCollectionObj);
	}
	
	@SubscribeEvent
    public void onEntityJoinedWorld (EntityJoinWorldEvent event)
    {
    	EntityCreature animal = null;
    	if (event.entity instanceof EntityCreature)
    		animal = (EntityCreature) event.entity;
    	if (animal != null)
    	{       			
    		if (animal instanceof EntityCow && COWS_HERD)
    		{
    			handleCow(animal);
    		}
    		if (animal instanceof EntityHorse && HORSES_HERD)
    		{
    			handleHorse(animal);
    		}
    		else if (animal instanceof EntitySheep && SHEEP_HERD)
    		{
    			handleSheep(animal);
    		}
    		else if (animal instanceof EntityChicken && CHICKENS_HERD)
    		{
    			handleChicken(animal);
    		}
    		else if (animal instanceof EntityPig && PIGS_HERD)
    		{
    			handlePig(animal);
    		}
    		else if (animal instanceof EntityZombie && !(animal instanceof EntityPigZombie) && ZOMBIES_HORDE)
    		{
    			handleZombie(animal);
    		}
    		else if (animal instanceof EntitySkeleton && SKELETONS_HERD)
    		{
    			handleSkeleton(animal);
    		}
    		/*else if (animal instanceof EntityCaveSpider && CAVE_SPIDER_HORDE)
    		{
    			animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 0.23F, 0, 0, 0, 0));	//only EntityAnimals may breed.
    			//spider AI does not seem to work with the task system.
    		}*/
    		else if (animal instanceof EntityPigZombie && ZOMBIE_PIGMAN_HORDE)
    		{
    			animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 0.23F, 0, 0, 0, 0,(ZOMBIE_PIGMAN_HORDE_AS_ZOMBIE?EntityZombie.class:null)));	//only EntityAnimals may breed.
    		}
    		else if (animal instanceof EntityCreeper)
    		{	//does not interrupt wandering, but is more likely if the conditions are met.
    			if (CREEPERS_FAN_RANGE > 0)
    			{
    				animal.tasks.addTask(5, new EntityAIShyFrom(animal, EntityCreeper.class,CREEPERS_FAN_RANGE, 1.0D, 1.2D, 16));
    			}
    		}
    	}
	}
    

	private void handleCow(EntityCreature animal)
	{
		//System.out.println("COW!");
		animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 1.0D,COW_MIN_BREED, COW_MAX_BREED, COW_BASE_TIME, COW_VAR_TIME));
		if (COWS_STAMPEDE)
		{
			animal.tasks.addTask(1, new EntityAIHerdStampede(animal, 2.0D, COW_MIN_BREED, COW_MAX_BREED, COW_BASE_TIME, COW_VAR_TIME));
		}
		else
		{
			animal.tasks.addTask(1, new EntityAIHerdPanic(animal, 2.0D, COW_MIN_BREED, COW_MAX_BREED, COW_BASE_TIME, COW_VAR_TIME));
		}
		animal.tasks.addTask(3, new EntityAIHerdTempt(animal, 1.25D, Items.wheat, false, COW_MIN_BREED, COW_MAX_BREED, COW_BASE_TIME, COW_VAR_TIME));
		
		Iterator i = animal.tasks.taskEntries.iterator();
    	EntityAITaskEntry aiTask;
		while (i.hasNext()) 
		{
			try 
			{ 
				aiTask = (EntityAITaskEntry)i.next();
			}
			catch (java.util.ConcurrentModificationException e)
			{
				System.out.println("Iterator Concurrency Exception: HerdCraft... continuing");
				break;
			}
			finally 
			{ }
			if (aiTask.action instanceof EntityAIPanic || aiTask.action instanceof EntityAITempt) {
				i.remove();
			}         
		}
	}
	
	private void handleHorse(EntityCreature animal)
	{
		animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 1.0D,HORSE_MIN_BREED, HORSE_MAX_BREED, HORSE_BASE_TIME, HORSE_VAR_TIME));
		if (HORSES_STAMPEDE)
		{
			animal.tasks.addTask(1, new EntityAIHerdStampede(animal, 2.0D, HORSE_MIN_BREED, HORSE_MAX_BREED, HORSE_BASE_TIME, HORSE_VAR_TIME));
		}
		else
		{
			animal.tasks.addTask(1, new EntityAIHerdPanic(animal, 2.0D, HORSE_MIN_BREED, HORSE_MAX_BREED, HORSE_BASE_TIME, HORSE_VAR_TIME));
		}
		
		Iterator i = animal.tasks.taskEntries.iterator();
    	EntityAITaskEntry aiTask;
		while (i.hasNext()) 
		{
			try 
			{ 
				aiTask = (EntityAITaskEntry)i.next();
			}
			catch (java.util.ConcurrentModificationException e)
			{
				System.out.println("Iterator Concurrency Exception: HerdCraft... continuing");
				break;
			}
			finally 
			{ }
			if (aiTask.action instanceof EntityAIPanic) {
				i.remove();
			}         
		}
		
	}
	
	private void handleChicken(EntityCreature animal)
	{
		animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 1.0D,CHICKEN_MIN_BREED, CHICKEN_MAX_BREED, CHICKEN_BASE_TIME, CHICKEN_VAR_TIME));
		if (CHICKENS_STAMPEDE)
		{
			animal.tasks.addTask(1, new EntityAIHerdStampede(animal, 1.4D, CHICKEN_MIN_BREED, CHICKEN_MAX_BREED, CHICKEN_BASE_TIME, CHICKEN_VAR_TIME));
		}
		else
		{
			animal.tasks.addTask(1, new EntityAIHerdPanic(animal, 1.4D, CHICKEN_MIN_BREED, CHICKEN_MAX_BREED, CHICKEN_BASE_TIME, CHICKEN_VAR_TIME));
		}
		animal.tasks.addTask(3, new EntityAIHerdTempt(animal, 1.0D, Items.wheat_seeds, false, CHICKEN_MIN_BREED, CHICKEN_MAX_BREED, CHICKEN_BASE_TIME, CHICKEN_VAR_TIME));
		
		Iterator i = animal.tasks.taskEntries.iterator();
    	EntityAITaskEntry aiTask;
		while (i.hasNext()) 
		{
			try 
			{ 
				aiTask = (EntityAITaskEntry)i.next();
			}
			catch (java.util.ConcurrentModificationException e)
			{
				System.out.println("Iterator Concurrency Exception: HerdCraft... continuing");
				break;
			}
			finally 
			{ }
			if (aiTask.action instanceof EntityAIPanic || aiTask.action instanceof EntityAITempt) {
				i.remove();
			}         
		}
	}
	
	private void handleSheep(EntityCreature animal)
	{
		animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 1.0D,SHEEP_MIN_BREED, SHEEP_MAX_BREED, SHEEP_BASE_TIME, SHEEP_VAR_TIME));
		if (SHEEP_STAMPEDE)
		{
			animal.tasks.addTask(1, new EntityAIHerdStampede(animal, 1.25D, SHEEP_MIN_BREED, SHEEP_MAX_BREED, SHEEP_BASE_TIME, SHEEP_VAR_TIME));
		}
		else
		{
			animal.tasks.addTask(1, new EntityAIHerdPanic(animal, 1.25D, SHEEP_MIN_BREED, SHEEP_MAX_BREED, SHEEP_BASE_TIME, SHEEP_VAR_TIME));
		}
		animal.tasks.addTask(3, new EntityAIHerdTempt(animal, 1.1D, Items.wheat, false, SHEEP_MIN_BREED, SHEEP_MAX_BREED, SHEEP_BASE_TIME, SHEEP_VAR_TIME));
		
		Iterator i = animal.tasks.taskEntries.iterator();
    	EntityAITaskEntry aiTask;
		while (i.hasNext()) 
		{
			try 
			{ 
				aiTask = (EntityAITaskEntry)i.next();
			}
			catch (java.util.ConcurrentModificationException e)
			{
				System.out.println("Iterator Concurrency Exception: HerdCraft... continuing");
				break;
			}
			finally 
			{ }
			if (aiTask.action instanceof EntityAIPanic || aiTask.action instanceof EntityAITempt) {
				i.remove();
			}         
		}
	}
	
	private void handlePig(EntityCreature animal) {
		animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 1.0D,PIG_MIN_BREED, PIG_MAX_BREED, PIG_BASE_TIME, PIG_VAR_TIME));
		animal.tasks.addTask(3, new EntityAIHerdTempt(animal, 1.2D, Items.carrot_on_a_stick, false, PIG_MIN_BREED, PIG_MAX_BREED, PIG_BASE_TIME, PIG_VAR_TIME));
		animal.tasks.addTask(3, new EntityAIHerdTempt(animal, 1.2D, Items.carrot, false, PIG_MIN_BREED, PIG_MAX_BREED, PIG_BASE_TIME, PIG_VAR_TIME));
		if (PIGS_STAMPEDE)
		{
			animal.tasks.addTask(1, new EntityAIHerdStampede(animal, 1.25D, PIG_MIN_BREED, PIG_MAX_BREED, PIG_BASE_TIME, PIG_VAR_TIME));
		}
		else
		{
			animal.tasks.addTask(1, new EntityAIHerdPanic(animal, 1.25D, PIG_MIN_BREED, PIG_MAX_BREED, PIG_BASE_TIME, PIG_VAR_TIME));
		}
		
		Iterator i = animal.tasks.taskEntries.iterator();
    	EntityAITaskEntry aiTask;
		while (i.hasNext()) 
		{
			try 
			{ 
				aiTask = (EntityAITaskEntry)i.next();
			}
			catch (java.util.ConcurrentModificationException e)
			{
				System.out.println("Iterator Concurrency Exception: HerdCraft... continuing");
				break;
			}
			finally 
			{ }
			if (aiTask.action instanceof EntityAITempt || aiTask.action instanceof EntityAIPanic) {
				i.remove();
			}         
		}
	}
	
	private void handleZombie(EntityCreature animal)
	{
		animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 1.0D, 0, 0, 0, 0,(EXPLICIT_ZOMBIE_HORDING?EntityZombie.class:null)));	//only EntityAnimals may breed.
		animal.tasks.addTask(2, new EntityAIHerdAttackOnCollide(animal, EntityPlayer.class, 1.0D, false,(EXPLICIT_ZOMBIE_HORDING?EntityZombie.class:null)));
        animal.tasks.addTask(3, new EntityAIHerdAttackOnCollide(animal, EntityVillager.class, 1.0D, true,(EXPLICIT_ZOMBIE_HORDING?EntityZombie.class:null)));

		Iterator i = animal.tasks.taskEntries.iterator();
    	EntityAITaskEntry aiTask;
		while (i.hasNext()) 
		{
			try 
			{ 
				aiTask = (EntityAITaskEntry)i.next();
			}
			catch (java.util.ConcurrentModificationException e)
			{
				System.out.println("Iterator Concurrency Exception: HerdCraft... continuing");
				break;
			}
			finally 
			{ }
			if (aiTask.action instanceof EntityAIAttackOnCollide) {
				i.remove();
			}     
		}
	}
	
	private void handleSkeleton(EntityCreature animal) {
		animal.tasks.addTask(5, new EntityAIHerdRegroup(animal, 1.0D, 0, 0, 0, 0));	//only EntityAnimals may breed.
		if (SKELETON_FAN_RANGE > 0)
		{
			animal.tasks.addTask(5, new EntityAIShyFrom(animal, EntitySkeleton.class, SKELETON_FAN_RANGE, 1.0D, 1.2D, 6));
		}
		
		Iterator i = animal.tasks.taskEntries.iterator();
    	EntityAITaskEntry aiTask;
    	boolean hadArrow = false;
		while (i.hasNext()) 
		{
			try 
			{ 
				aiTask = (EntityAITaskEntry)i.next();
			}
			catch (java.util.ConcurrentModificationException e)
			{
				System.out.println("Iterator Concurrency Exception: HerdCraft... continuing");
				break;
			}
			finally 
			{ }
			if (aiTask.action instanceof EntityAIArrowAttack) {
				i.remove();
				hadArrow = true;
			}     
		}
		if (hadArrow)
		{
			animal.tasks.addTask(4, new EntityAIHerdArrowAttack((IRangedAttackMob) animal, 1.0D, 20, 60, 15.0F,SKELETON_CAUTION_ANGLE, rand.nextBoolean(), SKELETON_FEAR_MELEE));
			if(SKELETON_FEAR_MELEE)
			{
				animal.tasks.addTask(3, new EntityAIShyFrom(animal, EntityPlayer.class, 5.0F, 1.0, 1.2, 12));
			}
		}
	}
	
    @SubscribeEvent
    public void onLivingSpawnEvent(LivingSpawnEvent e)
    {
    	
    	if (e.entity.getClass() != EntityZombie.class) return;
    	EntityZombie animal = (EntityZombie)e.entity;
    	double currProb = (ZOMBIE_HORDE_BASE_PROB + hordeAdjustments(animal))/10000D;
    	if (animal.getCanSpawnHere()
    			&& ZOMBIE_HORDE_MAX - ZOMBIE_HORDE_MIN + 1 > 0
				&& rand.nextDouble() < currProb)	
		{
			spawnZombieHordeOn(animal, animal.worldObj);
		}
    }
    


	private int hordeAdjustments(EntityZombie entity) {
		int change = 0;
		World world = entity.worldObj;

		int y;
		for(y = (int)entity.posY;y < world.getActualHeight() && world.isAirBlock((int)entity.posX, y, (int)entity.posZ);y++);//stops at first non-air block
		boolean aboveGround = y == world.getActualHeight();
		if (aboveGround)
    	{// Above ground
			int moonPhase = (int)(world.getCurrentMoonPhaseFactor() * 4);
			change += moonPhase == 4 ? ZOMBIE_HORDE_FULLMOON_PROB: moonPhase == 0 ? ZOMBIE_HORDE_NEWMOON_PROB : 0;
		 	
			
			if (world.isRaining()){
				change += ZOMBIE_HORDE_RAINING_PROB;
			}
			
			
			BiomeGenBase biome = world.getBiomeGenForCoords((int)entity.posX, (int)entity.posZ);
		
			switch(biome.getTempCategory())
	    	{
	    		case COLD:
	    			change += ZOMBIE_HORDE_COLD_PROB;
	    			break;
	    		case MEDIUM:
	    			change += ZOMBIE_HORDE_WARM_PROB;
	    			break;
	    		case WARM:
	    			change += ZOMBIE_HORDE_HOT_PROB;
	    			break;
	    	}
	    	
	    	float rain = biome.rainfall;
	    	if (rain < 0.2F)
	    	{
	    		change += ZOMBIE_HORDE_DRY_PROB;
	    	}
	    	else if (rain < 0.8)
	    	{
	    		change += ZOMBIE_HORDE_DAMP_PROB;
	    	}
	    	else
	    	{	
	    		change += ZOMBIE_HORDE_WET_PROB;
	    	}
	    	
	    	
	    	float hills = biome.heightVariation;
	    	if (hills < 0.2)
	    	{
	    		change += ZOMBIE_HORDE_FLAT_PROB;
	    	}
	    	else if (hills < .5)
	    	{
	    		change += ZOMBIE_HORDE_HILLY_PROB;
	    	}
	    	else
	    	{
	    		change += ZOMBIE_HORDE_JAGGED_PROB;
	    	}
	    	
    	}
		else
		{
			change += ZOMBIE_HORDE_UNDERGROUND_PROB;
		}
		
		return change;
	}

	private void spawnZombieHordeOn(EntityZombie target, World targetWorld)
    {
    	int numToSpawn = 0;
    	if (ZOMBIE_HORDE_MAX - ZOMBIE_HORDE_MIN + 1 > 0)
    	{
    		numToSpawn = ZOMBIE_HORDE_MIN + rand.nextInt(ZOMBIE_HORDE_MAX - ZOMBIE_HORDE_MIN + 1);
    	}

    	for(int i = 0; i < numToSpawn; i++)
    	{
    		EntityZombie curr = new EntityZombie(targetWorld);
    		curr.setPosition(target.posX, target.posY, target.posZ);
    		targetWorld.spawnEntityInWorld(curr);
    	}
    }
}
