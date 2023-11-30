package carbonconfiglib.impl;

import java.util.ArrayList;

import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
