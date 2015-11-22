package com.HerdCraft.common.guiconfig;

import java.util.ArrayList;
import java.util.List;

import com.HerdCraft.common.HerdCraft;

import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.client.config.DummyConfigElement.DummyCategoryElement;
import cpw.mods.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.gui.ForgeGuiFactory.ForgeConfigGui.GeneralEntry;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class HerdGuiScreen extends GuiConfig {
	public HerdGuiScreen(GuiScreen parent) {
		super(parent,
                getConfigClasses(),
                "herdCraft", false, false, "HerdCraft", GuiConfig.getAbridgedConfigPath(HerdCraft.config.toString()));
	}
	
	
	static List<IConfigElement> getConfigClasses()
	{
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new DummyCategoryElement("Chicken", "com.HerdCraft.common.guiconfig.ConfigGUIChicken", ConfigGUIChicken.class));
		list.add(new DummyCategoryElement("Cow", "com.HerdCraft.common.guiconfig.ConfigGUICow", ConfigGUICow.class));
		list.add(new DummyCategoryElement("Horse", "com.HerdCraft.common.guiconfig.ConfigGUIHorse", ConfigGUIHorse.class));
		list.add(new DummyCategoryElement("Pig", "com.HerdCraft.common.guiconfig.ConfigGUIPig", ConfigGUIPig.class));
		list.add(new DummyCategoryElement("Sheep", "com.HerdCraft.common.guiconfig.ConfigGUISheep", ConfigGUISheep.class));
		
		list.add(new DummyCategoryElement("Zombie", "com.HerdCraft.common.guiconfig.ConfigGUIZombie", ConfigGUIZombie.class));
		list.add(new DummyCategoryElement("Zombie Pigman", "com.HerdCraft.common.guiconfig.ConfigGUIZombiePigman", ConfigGUIZombiePigman.class));
		list.add(new DummyCategoryElement("Skeleton", "com.HerdCraft.common.guiconfig.ConfigGUISkeleton", ConfigGUISkeleton.class));
		list.add(new DummyCategoryElement("Creeper", "com.HerdCraft.common.guiconfig.ConfigGUICreeper", ConfigGUICreeper.class));
		
		list.add(new DummyCategoryElement("Magnet", "com.HerdCraft.common.guiconfig.ConfigGUIMagnet", ConfigGUIMagnet.class));
		return list;
	}
	
}
