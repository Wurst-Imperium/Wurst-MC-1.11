/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.Feature;
import net.wurstclient.features.special_features.YesCheatSpf.BypassLevel;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.utils.InventoryUtils;
import net.wurstclient.utils.PlayerUtils;

@Mod.Info(
	description = "Automatically throws instant health splash potions if your health is lower than or equal to\n"
		+ "the set value.",
	name = "AutoSplashPot",
	tags = "AutoPotion,auto potion,auto splash potion",
	help = "Mods/AutoSplashPot")
@Mod.Bypasses
public final class AutoSplashPotMod extends Mod implements UpdateListener
{
	public final SliderSetting health =
		new SliderSetting("Health", 6, 0.5, 9.5, 0.5, ValueDisplay.DECIMAL);
	public CheckboxSetting ignoreScreen =
		new CheckboxSetting("Ignore screen", true);
	
	private int timer;
	
	@Override
	public void initSettings()
	{
		settings.add(health);
		settings.add(ignoreScreen);
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.potionSaverMod, wurst.mods.autoSoupMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		timer = 0;
	}
	
	@Override
	public void onUpdate()
	{
		// search potion in hotbar
		int potionInHotbar = findPotion(0, 9);
		
		// check if any potion was found
		if(potionInHotbar != -1)
		{
			// check timer
			if(timer > 0)
			{
				timer--;
				return;
			}
			
			// check health
			if(mc.player.getHealth() > health.getValueF() * 2F)
				return;
			
			// check screen
			if(!ignoreScreen.isChecked() && mc.currentScreen != null)
				return;
			
			// save old slot
			int oldSlot = mc.player.inventory.currentItem;
			
			// throw potion in hotbar
			mc.player.inventory.currentItem = potionInHotbar;
			mc.player.connection.sendPacket(new CPacketPlayer.Rotation(
				mc.player.rotationYaw, 90.0F, mc.player.onGround));
			PlayerUtils.processRightClick();
			
			// reset slot and rotation
			mc.player.inventory.currentItem = oldSlot;
			mc.player.connection
				.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw,
					mc.player.rotationPitch, mc.player.onGround));
			
			// reset timer
			timer = 10;
			
			return;
		}
		
		// search potion in inventory
		int potionInInventory = findPotion(9, 36);
		
		// move potion in inventory to hotbar
		if(potionInInventory != -1)
			mc.playerController.windowClick(0, potionInInventory, 0,
				ClickType.QUICK_MOVE, mc.player);
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			case GHOST_MODE:
			ignoreScreen.lock(() -> false);
			break;
			
			default:
			ignoreScreen.unlock();
			break;
		}
	}
	
	private int findPotion(int startSlot, int endSlot)
	{
		for(int i = startSlot; i < endSlot; i++)
		{
			ItemStack stack = mc.player.inventory.getStackInSlot(i);
			
			// filter out non-splash potion items
			if(!InventoryUtils.isSplashPotion(stack))
				continue;
			
			// search for instant health effects
			if(InventoryUtils.hasEffect(stack, MobEffects.INSTANT_HEALTH))
				return i;
		}
		
		return -1;
	}
}
