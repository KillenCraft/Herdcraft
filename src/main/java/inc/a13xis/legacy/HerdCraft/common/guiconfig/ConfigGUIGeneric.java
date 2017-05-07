package inc.a13xis.legacy.HerdCraft.common.guiconfig;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import inc.a13xis.legacy.HerdCraft.common.HerdCraft;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;

public abstract class ConfigGUIGeneric extends CategoryEntry {

	public ConfigGUIGeneric(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
    {
        super(owningScreen, owningEntryList, prop);
    }
    
	 protected GuiScreen buildChildScreen(String category)
    {
        // This GuiConfig object specifies the configID of the object and as such will force-save when it is closed. The parent
        // GuiConfig object's entryList will also be refreshed to reflect the changes.
        return new GuiConfig(this.owningScreen, 
                (new ConfigElement(HerdCraft.config.getCategory(category))).getChildElements(), 
                this.owningScreen.modID, category, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart, 
                this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                "HerdCraft/"+ category);
    }
	 
	 @Override
	protected abstract GuiScreen buildChildScreen(); //Meant to pass in category.
}