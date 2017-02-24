/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.block.material.Material;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.WurstClient;
import tk.wurst_client.events.PacketOutputEvent;
import tk.wurst_client.events.listeners.PacketOutputListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.BlockUtils;

@Mod.Info(
	description = "Allows you to walk on water.\n"
		+ "The real Jesus used this hack ~2000 years ago.\n"
		+ "Bypasses NoCheat+ if YesCheat+ is enabled.",
	name = "Jesus",
	help = "Mods/Jesus")
@Mod.Bypasses(ghostMode = false)
public final class JesusMod extends Mod
	implements UpdateListener, PacketOutputListener
{
	private int tickTimer = 10;
	private int packetTimer = 0;
	
	@Override
	public void onEnable()
	{
		WurstClient.INSTANCE.events.add(UpdateListener.class, this);
		WurstClient.INSTANCE.events.add(PacketOutputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		WurstClient.INSTANCE.events.remove(UpdateListener.class, this);
		WurstClient.INSTANCE.events.remove(PacketOutputListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// check if sneaking
		if(mc.gameSettings.keyBindSneak.pressed)
			return;
		
		// move up in water
		if(mc.player.isInWater())
		{
			mc.player.motionY = 0.11;
			tickTimer = 0;
			return;
		}
		
		// simulate jumping out of water
		if(tickTimer == 0)
			mc.player.motionY = 0.30;
		else if(tickTimer == 1)
			mc.player.motionY = 0;
		
		// update timer
		tickTimer++;
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		// check packet type
		if(!(event.getPacket() instanceof CPacketPlayer))
			return;
		
		CPacketPlayer packet = (CPacketPlayer)event.getPacket();
		
		// check if packet contains a position
		if(!(packet instanceof CPacketPlayer.Position
			|| packet instanceof CPacketPlayer.PositionRotation))
			return;
		
		// check inWater
		if(mc.player.isInWater())
			return;
		
		// check fall distance
		if(mc.player.fallDistance > 3F)
			return;
		
		if(!isOverLiquid())
			return;
		
		// if not actually moving, cancel packet
		if(mc.player.movementInput == null)
		{
			event.cancel();
			return;
		}
		
		// wait for timer
		packetTimer++;
		if(packetTimer < 4)
			return;
		
		// cancel old packet
		event.cancel();
		
		// get position
		double x = packet.getX(0);
		double y = packet.getY(0);
		double z = packet.getZ(0);
		
		// offset y
		if(mc.player.ticksExisted % 2 == 0)
			y -= 0.05;
		else
			y += 0.05;
		
		// create new packet
		Packet newPacket;
		if(packet instanceof CPacketPlayer.Position)
			newPacket = new CPacketPlayer.Position(x, y, z, true);
		else
			newPacket = new CPacketPlayer.PositionRotation(x, y, z,
				packet.getYaw(0), packet.getPitch(0), true);
		
		// send new packet
		mc.player.connection.sendPacketBypass(newPacket);
	}
	
	private boolean isOverLiquid()
	{
		boolean foundLiquid = false;
		boolean foundSolid = false;
		
		// check collision boxes below player
		for(AxisAlignedBB bb : mc.world.getCollisionBoxes(mc.player,
			mc.player.boundingBox.offset(0, -0.5, 0)))
		{
			BlockPos pos = new BlockPos(bb.getCenter());
			Material material = BlockUtils.getMaterial(pos);
			
			if(material == Material.WATER || material == Material.LAVA)
				foundLiquid = true;
			else if(material != Material.AIR)
				foundSolid = true;
		}
		
		return foundLiquid && !foundSolid;
	}
	
	public boolean shouldBeSolid()
	{
		return isActive() && mc.player != null && mc.player.fallDistance <= 3
			&& !mc.gameSettings.keyBindSneak.pressed && !mc.player.isInWater();
	}
}
