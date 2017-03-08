/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.wurstclient.events.listeners.UpdateListener;

@Mod.Info(
	description = "While this is active, other people will think you are\n"
		+ "rolling your head around!\n" + "Looks a bit like nodding.",
	name = "HeadRoll",
	tags = "head roll",
	help = "Mods/HeadRoll")
@Mod.Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public final class HeadRollMod extends Mod implements UpdateListener
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
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(
			Minecraft.getMinecraft().player.rotationYaw,
			(float)Math.sin(mc.player.ticksExisted % 20 / 10d * Math.PI) * 90,
			mc.player.onGround));
	}
}
