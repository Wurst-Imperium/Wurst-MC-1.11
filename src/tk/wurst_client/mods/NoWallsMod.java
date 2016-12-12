/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import tk.wurst_client.events.PacketOutputEvent;
import tk.wurst_client.events.listeners.PacketOutputListener;

@Mod.Info(
	description = "Allows you walk through walls.\n"
		+ "Not all servers support this.",
	name = "NoWalls",
	help = "Mods/NoWalls")
@Mod.Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public class NoWallsMod extends Mod implements PacketOutputListener
{
	
	@Override
	public void onEnable()
	{
		wurst.events.add(PacketOutputListener.class, this);
		
		if(!wurst.mods.noClipMod.isEnabled())
			wurst.mods.noClipMod.setEnabled(true);
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		Packet packet = event.getPacket();
		if(packet instanceof CPacketPlayer)
			event.cancel();
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(PacketOutputListener.class, this);
		
		mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(
			mc.player.posX, mc.player.getEntityBoundingBox().minY,
			mc.player.posZ, mc.player.cameraYaw, mc.player.cameraPitch,
			mc.player.onGround));
		
		wurst.mods.noClipMod.setEnabled(false);
	}
}
