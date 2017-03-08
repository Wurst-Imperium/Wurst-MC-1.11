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
	description = "Allows you to run roughly 2.5x faster than you would by\n"
		+ "sprinting and jumping.\n"
		+ "Notice: This mod was patched in NoCheat+ version 3.13.2. It will\n"
		+ "only bypass older versions of NoCheat+. Type \"/ncp version\" to\n"
		+ "check the NoCheat+ version of a server.",
	name = "SpeedHack",
	tags = "speed hack",
	help = "Mods/SpeedHack")
@Mod.Bypasses(ghostMode = false, latestNCP = false)
public final class SpeedHackMod extends Mod implements UpdateListener
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
		// return if sneaking or not walking
		if(mc.player.isSneaking()
			|| mc.player.moveForward == 0 && mc.player.moveStrafing == 0)
			return;
		
		// activate sprint if walking forward
		if(mc.player.moveForward > 0 && !mc.player.isCollidedHorizontally)
			mc.player.setSprinting(true);
		
		// activate mini jump if on ground
		if(mc.player.onGround)
		{
			mc.player.motionY += 0.1;
			mc.player.motionX *= 1.8;
			mc.player.motionZ *= 1.8;
			double currentSpeed = Math.sqrt(Math.pow(mc.player.motionX, 2)
				+ Math.pow(mc.player.motionZ, 2));
			
			// limit speed to highest value that works on NoCheat+ version
			// 3.13.0-BETA-sMD5NET-b878
			// UPDATE: Patched in NoCheat+ version 3.13.2-SNAPSHOT-sMD5NET-b888
			double maxSpeed = 0.66F;
			if(currentSpeed > maxSpeed)
			{
				mc.player.motionX = mc.player.motionX / currentSpeed * maxSpeed;
				mc.player.motionZ = mc.player.motionZ / currentSpeed * maxSpeed;
			}
		}
	}
}
