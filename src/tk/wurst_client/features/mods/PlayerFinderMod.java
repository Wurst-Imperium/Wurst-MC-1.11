/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.awt.Color;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.PacketInputEvent;
import tk.wurst_client.events.listeners.PacketInputListener;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(description = "Finds far players during thunderstorms.",
	name = "PlayerFinder",
	tags = "player finder",
	help = "Mods/PlayerFinder")
@Mod.Bypasses
public class PlayerFinderMod extends Mod
	implements PacketInputListener, RenderListener
{
	private BlockPos pos;
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.playerEspMod, wurst.mods.tracersMod};
	}
	
	@Override
	public void onEnable()
	{
		pos = null;
		
		wurst.events.add(PacketInputListener.class, this);
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(PacketInputListener.class, this);
		wurst.events.remove(RenderListener.class, this);
	}
	
	@Override
	public void onRender()
	{
		if(pos == null)
			return;
		
		// generate rainbow color
		double x = System.currentTimeMillis() % 2000 / 1000D;
		double red = 0.5 + 0.5 * Math.sin(x * Math.PI);
		double green = 0.5 + 0.5 * Math.sin((x + 4D / 3D) * Math.PI);
		double blue = 0.5 + 0.5 * Math.sin((x + 8D / 3D) * Math.PI);
		Color color = new Color((float)red, (float)green, (float)blue);
		
		// draw line & box
		RenderUtils.tracerLine(pos.getX(), pos.getY(), pos.getZ(), color);
		RenderUtils.blockEsp(pos);
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		if(mc.player == null)
			return;
		
		Packet packet = event.getPacket();
		
		// get packet position
		BlockPos newPos = null;
		if(packet instanceof SPacketEffect)
		{
			SPacketEffect effect = (SPacketEffect)packet;
			newPos = effect.getSoundPos();
			
		}else if(packet instanceof SPacketSoundEffect)
		{
			SPacketSoundEffect sound = (SPacketSoundEffect)packet;
			newPos = new BlockPos(sound.getX(), sound.getY(), sound.getZ());
			
		}else if(packet instanceof SPacketSpawnGlobalEntity)
		{
			SPacketSpawnGlobalEntity lightning =
				(SPacketSpawnGlobalEntity)packet;
			newPos = new BlockPos(lightning.getX() / 32D,
				lightning.getY() / 32D, lightning.getZ() / 32D);
		}
		
		if(newPos == null)
			return;
		
		// check distance to player
		BlockPos playerPos = new BlockPos(mc.player);
		if(Math.abs(playerPos.getX() - newPos.getX()) > 250
			|| Math.abs(playerPos.getZ() - newPos.getZ()) > 250)
			pos = newPos;
	}
}
