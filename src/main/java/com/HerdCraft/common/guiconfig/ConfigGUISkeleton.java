package com.HerdCraft.common.guiconfig;

import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

public class ConfigGUISkeleton extends ConfigGUIGeneric {
		
	//Override
	protected static String category = "skeleton";
	
	public ConfigGUISkeleton(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
			IConfigElement prop) {
		super(owningScreen, owningEntryList, prop);
	}

	@Override
	protected GuiScreen buildChildScreen() {
		return buildChildScreen(category);
	}

}
