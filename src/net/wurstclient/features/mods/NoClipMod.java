/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.wurstclient.events.listeners.UpdateListener;

@Mod.Info(
	description = "Allows you to freely move through blocks.\n"
		+ "A block (e.g. sand) must fall on your head to activate it.\n"
		+ "Warning: You will take damage while moving through blocks!",
	name = "NoClip",
	tags = "no clip",
	help = "Mods/NoClip")
@Mod.Bypasses
public final class NoClipMod extends Mod implements UpdateListener
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
		mc.player.noClip = false;
	}
	
	@Override
	public void onUpdate()
	{
		mc.player.noClip = true;
		mc.player.fallDistance = 0;
		mc.player.onGround = false;
		
		mc.player.capabilities.isFlying = false;
		mc.player.motionX = 0;
		mc.player.motionY = 0;
		mc.player.motionZ = 0;
		
		float speed = 0.2F;
		mc.player.jumpMovementFactor = speed;
		if(mc.gameSettings.keyBindJump.pressed)
			mc.player.motionY += speed;
		if(mc.gameSettings.keyBindSneak.pressed)
			mc.player.motionY -= speed;
	}
}
