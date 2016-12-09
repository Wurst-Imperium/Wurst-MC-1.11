/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.utils.InventoryUtils;

@Mod.Info(
	description = "Generates a potion that can kill players in Creative mode.\n"
		+ "Requires Creative mode.",
	name = "KillerPotion",
	tags = "killer potion",
	help = "Mods/KillerPotion")
@Bypasses
public class KillerPotionMod extends Mod
{
	@Override
	public void onEnable()
	{
		// check gamemode
		if(!mc.player.capabilities.isCreativeMode)
		{
			wurst.chat.error("Creative mode only.");
			setEnabled(false);
			return;
		}
		
		// generate potion
		ItemStack stack = new ItemStack(Items.SPLASH_POTION);
		NBTTagCompound effect = new NBTTagCompound();
		effect.setInteger("Amplifier", 125);
		effect.setInteger("Duration", 2000);
		effect.setInteger("Id", 6);
		NBTTagList effects = new NBTTagList();
		effects.appendTag(effect);
		stack.setTagInfo("CustomPotionEffects", effects);
		stack.setStackDisplayName("§rSplash Potion of §4§lDEATH");
		
		// give potion
		if(InventoryUtils.placeStackInHotbar(stack))
			wurst.chat.message("Potion created.");
		else
			wurst.chat.error("Please clear a slot in your hotbar.");
		
		setEnabled(false);
	}
}
