/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;

@Mod.Info(
	description = "Automatically throws splash healing potions if your health is below the set value.",
	name = "AutoSplashPot",
	tags = "AutoPotion,auto potion,auto splash potion",
	help = "Mods/AutoSplashPot")
@Mod.Bypasses
public class AutoSplashPotMod extends Mod implements UpdateListener
{
	public final SliderSetting health =
		new SliderSetting("Health", 18, 2, 20, 1, ValueDisplay.INTEGER);
	
	@Override
	public void initSettings()
	{
		settings.add(health);
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
	}
	
	@Override
	public void onUpdate()
	{
		// update timer
		updateMS();
		
		// check if no container is open
		if(mc.currentScreen instanceof GuiContainer
			&& !(mc.currentScreen instanceof GuiInventory))
			return;
		
		// check if health is low
		if(mc.player.getHealth() >= health.getValueF())
			return;
		
		// find health potions
		int potionInInventory = findPotion(9, 36);
		int potionInHotbar = findPotion(36, 45);
		
		// check if any potion was found
		if(potionInInventory == -1 && potionInHotbar == -1)
			return;
		
		if(hasTimePassedM(500))
			if(potionInHotbar != -1)
			{
				// throw potion in hotbar
				int oldSlot = mc.player.inventory.currentItem;
				NetHandlerPlayClient connection = mc.player.connection;
				connection.sendPacket(new CPacketPlayer.Rotation(
					mc.player.rotationYaw, 90.0F, mc.player.onGround));
				connection
					.sendPacket(new CPacketHeldItemChange(potionInHotbar - 36));
				mc.playerController.updateController();
				connection.sendPacket(
					new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
				connection.sendPacket(new CPacketHeldItemChange(oldSlot));
				connection.sendPacket(
					new CPacketPlayer.Rotation(mc.player.rotationYaw,
						mc.player.rotationPitch, mc.player.onGround));
				
				// reset timer
				updateLastMS();
			}else
				// move potion in inventory to hotbar
				mc.playerController.windowClick(0, potionInInventory, 0,
					ClickType.QUICK_MOVE, mc.player);
		
	}
	
	private int findPotion(int startSlot, int endSlot)
	{
		for(int i = startSlot; i < endSlot; i++)
		{
			ItemStack stack =
				mc.player.inventoryContainer.getSlot(i).getStack();
			if(stack != null && stack.getItem() == Items.SPLASH_POTION)
				for(PotionEffect effect : PotionUtils
					.getEffectsFromStack(stack))
					if(effect.getPotion() == MobEffects.INSTANT_HEALTH)
						return i;
		}
		return -1;
	}
}
