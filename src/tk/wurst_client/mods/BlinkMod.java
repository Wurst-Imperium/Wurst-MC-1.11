/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import tk.wurst_client.events.PacketOutputEvent;
import tk.wurst_client.events.listeners.PacketOutputListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;

@Info(
	description = "Suspends all motion updates while enabled.\n"
		+ "Can be used for teleportation, instant picking up of items and more.",
	name = "Blink",
	help = "Mods/Blink")
@Bypasses
public class BlinkMod extends Mod implements PacketOutputListener
{
	private static ArrayList<Packet> packets = new ArrayList<Packet>();
	private EntityOtherPlayerMP fakePlayer = null;
	private double oldX;
	private double oldY;
	private double oldZ;
	private static long blinkTime;
	private static long lastTime;
	
	@Override
	public String getRenderName()
	{
		return "Blink [" + blinkTime + "ms]";
	}
	
	@Override
	public void onEnable()
	{
		lastTime = System.currentTimeMillis();
		
		oldX = mc.player.posX;
		oldY = mc.player.posY;
		oldZ = mc.player.posZ;
		fakePlayer =
			new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
		fakePlayer.clonePlayer(mc.player, true);
		fakePlayer.copyLocationAndAnglesFrom(mc.player);
		fakePlayer.rotationYawHead = mc.player.rotationYawHead;
		mc.world.addEntityToWorld(-69, fakePlayer);
		
		wurst.events.add(PacketOutputListener.class, this);
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		Packet packet = event.getPacket();
		if(packet instanceof CPacketPlayer)
		{
			if(mc.player.posX != mc.player.prevPosX
				|| mc.player.posZ != Minecraft.getMinecraft().player.prevPosZ
				|| mc.player.posY != Minecraft.getMinecraft().player.prevPosY)
			{
				blinkTime += System.currentTimeMillis() - lastTime;
				packets.add(packet);
			}
			lastTime = System.currentTimeMillis();
			event.cancel();
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(PacketOutputListener.class, this);
		
		for(Packet packet : packets)
			mc.player.connection.sendPacket(packet);
		packets.clear();
		mc.world.removeEntityFromWorld(-69);
		fakePlayer = null;
		blinkTime = 0;
	}
	
	public void cancel()
	{
		packets.clear();
		mc.player.setPositionAndRotation(oldX, oldY, oldZ,
			mc.player.rotationYaw, mc.player.rotationPitch);
		setEnabled(false);
	}
}
