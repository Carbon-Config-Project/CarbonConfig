package carbonconfiglib.impl;

import java.util.ArrayList;

import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Reflects
{
	@SideOnly(Side.CLIENT)
	public static ModContainer getSelectedMod(GuiModList list) {
		return ObfuscationReflectionHelper.getPrivateValue(GuiModList.class, list, "selectedMod");
	}
	
	@SideOnly(Side.CLIENT)
	public static ArrayList<ModContainer> getModList(GuiModList list) {
		return ObfuscationReflectionHelper.getPrivateValue(GuiModList.class, list, "mods");
	}
}
