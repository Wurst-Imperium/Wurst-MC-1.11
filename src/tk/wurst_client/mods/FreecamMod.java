/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import tk.wurst_client.events.listeners.UpdateListener;

@Mod.Info(
	description = "Allows you to fly out of your body.\n"
		+ "Looks similar to spectator mode.",
	name = "Freecam",
	tags = "free cam, spectator",
	help = "Mods/Freecam")
@Mod.Bypasses
public class FreecamMod extends Mod implements UpdateListener
{
	private EntityOtherPlayerMP fakePlayer = null;
	private double oldX;
	private double oldY;
	private double oldZ;
	
	@Override
	public void onEnable()
	{
		oldX = mc.player.posX;
		oldY = mc.player.posY;
		oldZ = mc.player.posZ;
		fakePlayer =
			new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
		fakePlayer.clonePlayer(mc.player, true);
		fakePlayer.copyLocationAndAnglesFrom(mc.player);
		fakePlayer.rotationYawHead = mc.player.rotationYawHead;
		mc.world.addEntityToWorld(-69, fakePlayer);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		mc.player.motionX = 0;
		mc.player.motionY = 0;
		mc.player.motionZ = 0;
		mc.player.jumpMovementFactor = wurst.mods.flightMod.speed / 10;
		if(mc.gameSettings.keyBindJump.pressed)
			mc.player.motionY += wurst.mods.flightMod.speed;
		if(mc.gameSettings.keyBindSneak.pressed)
			mc.player.motionY -= wurst.mods.flightMod.speed;
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		mc.player.setPositionAndRotation(oldX, oldY, oldZ,
			mc.player.rotationYaw, mc.player.rotationPitch);
		mc.world.removeEntityFromWorld(-69);
		fakePlayer = null;
		mc.renderGlobal.loadRenderers();
	}
}
