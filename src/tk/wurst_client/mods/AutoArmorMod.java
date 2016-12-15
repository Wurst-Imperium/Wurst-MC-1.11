/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.InventoryUtils;

@Mod.Info(description = "Manages your armor automatically.",
	name = "AutoArmor",
	tags = "auto armor",
	help = "Mods/AutoArmor")
@Mod.Bypasses(ghostMode = false, latestNCP = false)
public class AutoArmorMod extends Mod implements UpdateListener
{
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// update timer
		updateMS();
		
		// check timer
		if(!hasTimePassedM(1000))
			return;
		
		// check screen
		if(mc.currentScreen instanceof GuiContainer
			&& !(mc.currentScreen instanceof InventoryEffectRenderer))
			return;
		
		// store slots and values of best armor pieces
		int[] bestArmorSlots = new int[4];
		int[] bestArmorValues = new int[4];
		
		// initialize with currently equipped armor
		for(int armorType = 0; armorType < 4; armorType++)
		{
			ItemStack oldArmor = mc.player.inventory.armorItemInSlot(armorType);
			if(oldArmor != null && oldArmor.getItem() instanceof ItemArmor)
				bestArmorValues[armorType] =
					((ItemArmor)oldArmor.getItem()).damageReduceAmount;
			
			bestArmorSlots[armorType] = -1;
		}
		
		// search inventory for better armor
		for(int slot = 0; slot < 36; slot++)
		{
			ItemStack stack = mc.player.inventory.getStackInSlot(slot);
			if(stack == null || !(stack.getItem() instanceof ItemArmor))
				continue;
			
			ItemArmor armor = (ItemArmor)stack.getItem();
			int armorType = armor.armorType.ordinal() - 2;
			int armorValue = armor.damageReduceAmount;
			
			if(armorValue > bestArmorValues[armorType])
			{
				bestArmorSlots[armorType] = slot;
				bestArmorValues[armorType] = armorValue;
			}
		}
		
		// equip better armor
		for(int armorType = 0; armorType < 4; armorType++)
		{
			// check if better armor was found
			int slot = bestArmorSlots[armorType];
			if(slot == -1)
				continue;
				
			// check if armor can be swapped
			// needs 1 free slot where it can put the old armor
			ItemStack oldArmor = mc.player.inventory.armorItemInSlot(armorType);
			if(oldArmor == null
				|| !(oldArmor.getItem() == InventoryUtils.NULL_ITEM)
				|| mc.player.inventory.getFirstEmptyStack() != -1)
			{
				// hotbar fix
				if(slot < 9)
					slot += 36;
				
				// swap armor
				mc.playerController.windowClick(0, 8 - armorType, 0,
					ClickType.QUICK_MOVE, mc.player);
				mc.playerController.windowClick(0, slot, 0,
					ClickType.QUICK_MOVE, Minecraft.getMinecraft().player);
			}
		}
		
		// reset timer
		updateLastMS();
	}
}
