/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import tk.wurst_client.events.listeners.UpdateListener;

@Mod.Info(description = "Allows you to fly with boats",
	name = "BoatFly",
	tags = "BoatFlight, boat fly, boat flight",
	help = "Mods/BoatFly")
@Mod.Bypasses(ghostMode = false, latestNCP = false)
public class BoatFlyMod extends Mod implements UpdateListener
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
		// check if riding
		if(!mc.player.isRiding())
			return;
		
		// fly
		mc.player.getRidingEntity().motionY =
			mc.gameSettings.keyBindJump.pressed ? 0.3 : 0;
	}
}
