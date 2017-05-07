package inc.a13xis.legacy.HerdCraft.common.guiconfig;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ConfigGUICow extends ConfigGUIGeneric {

	private static String category = "cow";
	
	public ConfigGUICow(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
    {
        super(owningScreen, owningEntryList, prop);
    }
    
	@Override
	protected GuiScreen buildChildScreen() {
		return buildChildScreen(category);
	}
}
