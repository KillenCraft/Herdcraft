package inc.a13xis.legacy.HerdCraft.common.guiconfig;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

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
