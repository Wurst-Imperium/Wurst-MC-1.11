/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.block.material.Material;
import net.wurstclient.events.listeners.UpdateListener;

@Mod.Info(description = "Makes you fall like if you had a hang glider.",
	name = "Glide",
	help = "Mods/Glide")
@Mod.Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public final class GlideMod extends Mod implements UpdateListener
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
		if(mc.player.motionY < 0 && mc.player.isAirBorne
			&& !mc.player.isInWater() && !mc.player.isOnLadder()
			&& !mc.player.isInsideOfMaterial(Material.LAVA))
		{
			mc.player.motionY = -0.125f;
			mc.player.jumpMovementFactor *= 1.21337f;
		}
	}
}
