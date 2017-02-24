/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import tk.wurst_client.utils.ChatUtils;
import tk.wurst_client.utils.InventoryUtils;

@Mod.Info(
	description = "Generates an incredibly annoying potion.\n"
		+ "Tip: AntiBlind makes you partially immune to it.",
	name = "TrollPotion",
	tags = "troll potion",
	help = "Mods/TrollPotion")
@Mod.Bypasses
public final class TrollPotionMod extends Mod
{
	@Override
	public void onEnable()
	{
		// check gamemode
		if(!mc.player.capabilities.isCreativeMode)
		{
			ChatUtils.error("Creative mode only.");
			setEnabled(false);
			return;
		}
		
		// generate potion
		ItemStack stack = new ItemStack(Items.SPLASH_POTION);
		NBTTagList effects = new NBTTagList();
		for(int i = 1; i <= 23; i++)
		{
			NBTTagCompound effect = new NBTTagCompound();
			effect.setInteger("Amplifier", Integer.MAX_VALUE);
			effect.setInteger("Duration", Integer.MAX_VALUE);
			effect.setInteger("Id", i);
			effects.appendTag(effect);
		}
		stack.setTagInfo("CustomPotionEffects", effects);
		stack.setStackDisplayName("§rSplash Potion of Trolling");
		
		// give potion
		if(InventoryUtils.placeStackInHotbar(stack))
			ChatUtils.message("Potion created.");
		else
			ChatUtils.error("Please clear a slot in your hotbar.");
		
		setEnabled(false);
	}
}
