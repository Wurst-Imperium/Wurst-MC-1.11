/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import tk.wurst_client.events.PacketInputEvent;
import tk.wurst_client.events.listeners.PacketInputListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.InventoryUtils;

@Mod.Info(description = "Automatically catches fish.",
	name = "AutoFish",
	tags = "FishBot, auto fish, fish bot, fishing",
	help = "Mods/AutoFish")
@Mod.Bypasses
public class AutoFishMod extends Mod
	implements UpdateListener, PacketInputListener
{
	private int timer;
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(PacketInputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(PacketInputListener.class, this);
		timer = 0;
	}
	
	@Override
	public void onUpdate()
	{
		// check timer
		if(timer > 0)
		{
			timer--;
			return;
		}
		
		// check bobber
		if(mc.player.fishEntity != null)
			return;
		
		rightClick();
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		// check packet type
		if(!(event.getPacket() instanceof SPacketSoundEffect))
			return;
		
		// check sound type
		if(((SPacketSoundEffect)event.getPacket())
			.getSound() != SoundEvents.ENTITY_BOBBER_SPLASH)
			return;
		
		rightClick();
	}
	
	private void rightClick()
	{
		// check held item
		ItemStack stack = mc.player.inventory.getCurrentItem();
		if(InventoryUtils.isEmptySlot(stack)
			|| !(stack.getItem() instanceof ItemFishingRod))
			return;
		
		// right click
		mc.rightClickMouse();
		
		// reset timer
		timer = 15;
	}
}
