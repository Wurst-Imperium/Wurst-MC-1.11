/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.CheckboxSetting;

@Mod.Info(description = "Eases the use of the Elytra.",
	name = "ExtraElytra",
	tags = "EasyElytra, extra elytra, easy elytra",
	help = "Mods/ExtraElytra")
@Mod.Bypasses
public class ExtraElytraMod extends Mod implements UpdateListener
{
	private CheckboxSetting instantFly =
		new CheckboxSetting("Instant fly", true);
	private CheckboxSetting easyFly = new CheckboxSetting("Easy fly", false);
	private CheckboxSetting stopInWater =
		new CheckboxSetting("Stop flying in water", true);
	
	@Override
	public void initSettings()
	{
		settings.add(instantFly);
		settings.add(easyFly);
		settings.add(stopInWater);
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
		updateMS();
		
		ItemStack chest =
			mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if(chest == null || chest.getItem() != Items.ELYTRA)
			return;
		
		if(mc.player.isElytraFlying())
		{
			if(stopInWater.isChecked() && mc.player.isInWater())
			{
				mc.player.connection.sendPacket(new CPacketEntityAction(
					mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
				return;
			}
			
			if(easyFly.isChecked())
			{
				if(mc.gameSettings.keyBindJump.pressed)
					mc.player.motionY += 0.08;
				else if(mc.gameSettings.keyBindSneak.pressed)
					mc.player.motionY -= 0.04;
				
				if(mc.gameSettings.keyBindForward.pressed
					&& mc.player.getPosition().getY() < 256)
				{
					float yaw = (float)Math.toRadians(mc.player.rotationYaw);
					mc.player.motionX -= MathHelper.sin(yaw) * 0.05F;
					mc.player.motionZ += MathHelper.cos(yaw) * 0.05F;
				}else if(mc.gameSettings.keyBindBack.pressed
					&& mc.player.getPosition().getY() < 256)
				{
					float yaw = (float)Math.toRadians(mc.player.rotationYaw);
					mc.player.motionX += MathHelper.sin(yaw) * 0.05F;
					mc.player.motionZ -= MathHelper.cos(yaw) * 0.05F;
				}
			}
		}else if(instantFly.isChecked() && ItemElytra.isBroken(chest)
			&& mc.gameSettings.keyBindJump.pressed)
		{
			if(hasTimePassedM(1000))
			{
				updateLastMS();
				mc.player.setJumping(false);
				mc.player.setSprinting(true);
				mc.player.jump();
			}
			mc.player.connection.sendPacket(new CPacketEntityAction(mc.player,
				CPacketEntityAction.Action.START_FALL_FLYING));
		}
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			default:
			case OFF:
			case MINEPLEX_ANTICHEAT:
			case ANTICHEAT:
			case OLDER_NCP:
			case LATEST_NCP:
				easyFly.unlock();
				break;
			case GHOST_MODE:
				easyFly.lock(false);
				break;
		}
	}
}
