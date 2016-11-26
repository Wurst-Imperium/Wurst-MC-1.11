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
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import tk.wurst_client.mods.Mod.Bypasses;

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
		if(mc.player.inventory.getStackInSlot(0) != null)
		{
			wurst.chat.error("Please clear the first slot in your hotbar.");
			setEnabled(false);
			return;
		}else if(!mc.player.capabilities.isCreativeMode)
		{
			wurst.chat.error("Creative mode only.");
			setEnabled(false);
			return;
		}
		
		ItemStack stack = new ItemStack(Items.SPLASH_POTION);
		NBTTagList effects = new NBTTagList();
		NBTTagCompound effect = new NBTTagCompound();
		effect.setInteger("Amplifier", 125);
		effect.setInteger("Duration", 2000);
		effect.setInteger("Id", 6);
		effects.appendTag(effect);
		stack.setTagInfo("CustomPotionEffects", effects);
		stack.setStackDisplayName("§c§lKiller§6§lPotion");
		
		mc.player.connection
			.sendPacket(new CPacketCreativeInventoryAction(36, stack));
		wurst.chat.message("Potion created.");
		setEnabled(false);
	}
}
