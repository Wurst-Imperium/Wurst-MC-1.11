/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.navigator.NavigatorItem;

@Mod.Info(
	description = "Automatically uses the best weapon in your hotbar to attack\n"
		+ "entities. Tip: This works with Killaura.",
	name = "AutoSword",
	tags = "auto sword",
	help = "Mods/AutoSword")
@Mod.Bypasses(ghostMode = false,
	latestNCP = false,
	olderNCP = false,
	antiCheat = false)
public class AutoSwordMod extends Mod
	implements LeftClickListener, UpdateListener
{
	private int oldSlot;
	private int timer;
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.autoToolMod};
	}
	
	@Override
	public void onEnable()
	{
		oldSlot = -1;
		wurst.events.add(LeftClickListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(LeftClickListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(timer > 0)
		{
			timer--;
			return;
		}
		mc.player.inventory.currentItem = oldSlot;
		wurst.events.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onLeftClick()
	{
		if(mc.objectMouseOver != null
			&& mc.objectMouseOver.entityHit instanceof EntityLivingBase)
			setSlot();
	}
	
	public static void setSlot()
	{
		if(wurst.mods.autoEatMod.isEating())
			return;
		float bestSpeed = 1F;
		int bestSlot = -1;
		for(int i = 0; i < 9; i++)
		{
			ItemStack item = mc.player.inventory.getStackInSlot(i);
			if(item == null)
				continue;
			float speed = 0;
			if(item.getItem() instanceof ItemSword)
				speed = ((ItemSword)item.getItem()).getDamageVsEntity();
			else if(item.getItem() instanceof ItemTool)
				speed = ((ItemTool)item.getItem()).getToolMaterial()
					.getDamageVsEntity();
			if(speed > bestSpeed)
			{
				bestSpeed = speed;
				bestSlot = i;
			}
		}
		if(bestSlot != -1 && bestSlot != mc.player.inventory.currentItem)
		{
			wurst.mods.autoSwordMod.oldSlot = mc.player.inventory.currentItem;
			mc.player.inventory.currentItem = bestSlot;
			wurst.mods.autoSwordMod.timer = 4;
			wurst.events.add(UpdateListener.class, wurst.mods.autoSwordMod);
		}
	}
}
