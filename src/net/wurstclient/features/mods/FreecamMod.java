/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.utils.EntityFakePlayer;

@Mod.Info(
	description = "Allows you to fly out of your body.\n"
		+ "Looks similar to spectator mode.",
	name = "Freecam",
	tags = "free cam, spectator",
	help = "Mods/Freecam")
@Mod.Bypasses
@Mod.DontSaveState
public final class FreecamMod extends Mod implements UpdateListener
{
	private EntityFakePlayer fakePlayer;
	
	@Override
	public void onEnable()
	{
		fakePlayer = new EntityFakePlayer();
		
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		
		fakePlayer.resetPlayerPosition();
		fakePlayer.despawn();
		
		mc.renderGlobal.loadRenderers();
	}
	
	@Override
	public void onUpdate()
	{
		mc.player.motionX = 0;
		mc.player.motionY = 0;
		mc.player.motionZ = 0;
		
		mc.player.jumpMovementFactor =
			wurst.mods.flightMod.speed.getValueF() / 10F;
		
		if(mc.gameSettings.keyBindJump.pressed)
			mc.player.motionY += wurst.mods.flightMod.speed.getValue();
		
		if(mc.gameSettings.keyBindSneak.pressed)
			mc.player.motionY -= wurst.mods.flightMod.speed.getValue();
	}
}
