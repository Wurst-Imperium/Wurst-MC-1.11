/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import tk.wurst_client.WurstClient;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;

@Mod.Info(
	description = "Makes you jump automatically when reaching the edge of a block.\n"
		+ "Useful for parkours, Jump'n'Runs, etc.",
	name = "Parkour")
@Bypasses
public class ParkourMod extends Mod implements UpdateListener
{
	@Override
	public void onEnable()
	{
		WurstClient.INSTANCE.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(mc.player.onGround && !mc.player.isSneaking()
			&& !mc.gameSettings.keyBindSneak.pressed
			&& !mc.gameSettings.keyBindJump.pressed
			&& mc.world.getCollisionBoxes(mc.player,
				mc.player.getEntityBoundingBox().offset(0, -0.5, 0)
					.expand(-0.001, 0, -0.001))
				.isEmpty())
			mc.player.jump();
	}
	
	@Override
	public void onDisable()
	{
		WurstClient.INSTANCE.events.remove(UpdateListener.class, this);
	}
}
