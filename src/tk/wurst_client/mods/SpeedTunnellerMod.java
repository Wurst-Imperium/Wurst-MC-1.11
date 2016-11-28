/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.special.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(
	description = "Faster Tunneller that does not bypass NoCheat+",
	name = "SpeedTunneller",
	help = "Mods/SpeedTunneller")
@Bypasses(ghostMode = false,
	latestNCP = false,
	olderNCP = false,
	antiCheat = false)
public class SpeedTunnellerMod extends Mod implements RenderListener, UpdateListener
{
	private static Block currentBlock;
	private float currentDamage;
	private EnumFacing side = EnumFacing.UP;
	private byte blockHitDelay = 0;
	private BlockPos pos;
	private boolean shouldRenderESP = false;
	private int oldSlot = -1;
	
	@Override
	public void onEnable()
	{
		if(wurst.mods.nukerMod.isEnabled())
			wurst.mods.nukerMod.setEnabled(false);
		if(wurst.mods.nukerLegitMod.isEnabled())
			wurst.mods.nukerLegitMod.setEnabled(false);
		if(wurst.mods.speedNukerMod.isEnabled())
			wurst.mods.speedNukerMod.setEnabled(false);
		if(wurst.mods.tunnellerMod.isEnabled())
			wurst.mods.tunnellerMod.setEnabled(false);
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.nukerMod,
			wurst.mods.nukerLegitMod, wurst.mods.speedNukerMod,
			wurst.mods.fastBreakMod, wurst.mods.autoMineMod,
			wurst.mods.tunnellerMod};
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRender()
	{
		if(blockHitDelay == 0 && shouldRenderESP)
			if(!mc.player.capabilities.isCreativeMode
				&& currentBlock.getPlayerRelativeBlockHardness(
					mc.world.getBlockState(pos), mc.player, mc.world,
					pos) < 1)
				RenderUtils.nukerBox(pos, currentDamage);
			else
				RenderUtils.nukerBox(pos, 1);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onUpdate()
	{
		if(mc.player.capabilities.isCreativeMode)
		{
			wurst.chat.error(getName() + " doesn't work in creative mode.");
			setEnabled(false);
			wurst.chat.message("Switching to " + wurst.mods.nukerMod.getName()
				+ ".");
			wurst.mods.nukerMod.setEnabled(true);
			return;
		}
		BlockPos newPos = find();
		if(newPos == null)
		{
			if(oldSlot != -1)
			{
				mc.player.inventory.currentItem = oldSlot;
				oldSlot = -1;
			}
			return;
		}
		pos = newPos;
		currentBlock = mc.world.getBlockState(pos).getBlock();
		if(wurst.mods.autoToolMod.isActive() && oldSlot == -1)
			oldSlot = mc.player.inventory.currentItem;
		if(!mc.player.capabilities.isCreativeMode
			&& wurst.mods.autoToolMod.isActive()
			&& currentBlock.getPlayerRelativeBlockHardness(
				mc.world.getBlockState(pos), mc.player, mc.world, pos) < 1)
			AutoToolMod.setSlot(pos);
		nukeAll();
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		if(oldSlot != -1)
		{
			mc.player.inventory.currentItem = oldSlot;
			oldSlot = -1;
		}
		currentDamage = 0;
		shouldRenderESP = false;
	}
	
	@SuppressWarnings("deprecation")
	private BlockPos find()
	{
		BlockPos closest = null;
		float closestDistance = 16;
		for(int y = 2; y >= 0; y--)
			for(int x = 1; x >= -1; x--)
				for(int z = 1; z >= -1; z--)
				{
					if(mc.player == null)
						continue;
					int posX = (int)(Math.floor(mc.player.posX) + x);
					int posY = (int)(Math.floor(mc.player.posY) + y);
					int posZ = (int)(Math.floor(mc.player.posZ) + z);
					BlockPos blockPos = new BlockPos(posX, posY, posZ);
					Block block =
						mc.world.getBlockState(blockPos).getBlock();
					float xDiff = (float)(mc.player.posX - posX);
					float yDiff = (float)(mc.player.posY - posY);
					float zDiff = (float)(mc.player.posZ - posZ);
					float currentDistance = xDiff + yDiff + zDiff;
					if(Block.getIdFromBlock(block) != 0 && posY >= 0)
					{
						if(wurst.mods.nukerMod.mode.getSelected() == 3
							&& block.getPlayerRelativeBlockHardness(
								mc.world.getBlockState(blockPos),
								mc.player, mc.world, blockPos) < 1)
							continue;
						side = mc.objectMouseOver.sideHit;
						if(closest == null)
						{
							closest = blockPos;
							closestDistance = currentDistance;
						}else if(currentDistance < closestDistance)
						{
							closest = blockPos;
							closestDistance = currentDistance;
						}
					}
				}
		return closest;
	}
	
	@SuppressWarnings("deprecation")
	private void nukeAll()
	{
		for(int y = 2; y >= 0; y--)
			for(int x = 1; x >= -1; x--)
				for(int z = 1; z >= -1; z--)
				{
					int posX = (int)(Math.floor(mc.player.posX) + x);
					int posY = (int)(Math.floor(mc.player.posY) + y);
					int posZ = (int)(Math.floor(mc.player.posZ) + z);
					BlockPos blockPos = new BlockPos(posX, posY, posZ);
					Block block =
						mc.world.getBlockState(blockPos).getBlock();
					if(Block.getIdFromBlock(block) != 0 && posY >= 0)
					{
						if(wurst.mods.nukerMod.mode.getSelected() == 3
							&& block.getPlayerRelativeBlockHardness(
								mc.world.getBlockState(blockPos),
								mc.player, mc.world, blockPos) < 1)
							continue;
						side = mc.objectMouseOver.sideHit;
						//shouldRenderESP = true;
						BlockUtils.faceBlockPacket(pos);
						mc.player.connection
							.sendPacket(new CPacketPlayerDigging(
								Action.START_DESTROY_BLOCK, blockPos, side));
						mc.player.connection
						.sendPacket(new CPacketPlayerDigging(
							Action.STOP_DESTROY_BLOCK, blockPos, side));
					}
				}
	}
}
