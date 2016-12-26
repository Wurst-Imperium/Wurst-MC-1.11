/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.LeftClickEvent;
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.ModeSetting;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(
	description = "Destroys blocks around you.\n"
		+ "Use .nuker mode <mode> to change the mode.",
	name = "Nuker",
	help = "Mods/Nuker")
@Mod.Bypasses
public class NukerMod extends Mod
	implements LeftClickListener, RenderListener, UpdateListener
{
	private static Block currentBlock;
	private float currentDamage;
	private EnumFacing side = EnumFacing.UP;
	private byte blockHitDelay = 0;
	public static int id = 0;
	private BlockPos pos;
	private boolean shouldRenderESP;
	private int oldSlot = -1;
	
	public final SliderSetting range =
		new SliderSetting("Range", 6, 1, 6, 0.05, ValueDisplay.DECIMAL);
	public final ModeSetting mode = new ModeSetting("Mode",
		new String[]{"Normal", "ID", "Flat", "Smash"}, 0);
	
	@Override
	public String getRenderName()
	{
		switch(mode.getSelected())
		{
			case 0:
				return "Nuker";
			case 1:
				return "IDNuker [" + id + "]";
			default:
				return mode.getSelectedMode() + "Nuker";
		}
	}
	
	@Override
	public void initSettings()
	{
		settings.add(range);
		settings.add(mode);
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.nukerLegitMod, wurst.mods.speedNukerMod,
			wurst.mods.tunnellerMod, wurst.mods.fastBreakMod,
			wurst.mods.autoMineMod, wurst.mods.overlayMod};
	}
	
	@Override
	public void onEnable()
	{
		if(wurst.mods.nukerLegitMod.isEnabled())
			wurst.mods.nukerLegitMod.setEnabled(false);
		if(wurst.mods.speedNukerMod.isEnabled())
			wurst.mods.speedNukerMod.setEnabled(false);
		if(wurst.mods.tunnellerMod.isEnabled())
			wurst.mods.tunnellerMod.setEnabled(false);
		wurst.events.add(LeftClickListener.class, this);
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(LeftClickListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		if(oldSlot != -1)
		{
			mc.player.inventory.currentItem = oldSlot;
			oldSlot = -1;
		}
		currentDamage = 0;
		shouldRenderESP = false;
		id = 0;
		wurst.files.saveOptions();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRender()
	{
		if(blockHitDelay == 0 && shouldRenderESP)
			if(!mc.player.capabilities.isCreativeMode
				&& currentBlock.getPlayerRelativeBlockHardness(
					mc.world.getBlockState(pos), mc.player, mc.world, pos) < 1)
				RenderUtils.nukerBox(pos, currentDamage);
			else
				RenderUtils.nukerBox(pos, 1);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onUpdate()
	{
		shouldRenderESP = false;
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
		if(pos == null || !pos.equals(newPos))
			currentDamage = 0;
		pos = newPos;
		currentBlock = mc.world.getBlockState(pos).getBlock();
		if(blockHitDelay > 0)
		{
			blockHitDelay--;
			return;
		}
		BlockUtils.faceBlockPacket(pos);
		if(currentDamage == 0)
		{
			mc.player.connection.sendPacket(new CPacketPlayerDigging(
				Action.START_DESTROY_BLOCK, pos, side));
			if(wurst.mods.autoToolMod.isActive() && oldSlot == -1)
				oldSlot = mc.player.inventory.currentItem;
			if(mc.player.capabilities.isCreativeMode
				|| currentBlock.getPlayerRelativeBlockHardness(
					mc.world.getBlockState(pos), mc.player, mc.world, pos) >= 1)
			{
				currentDamage = 0;
				if(mc.player.capabilities.isCreativeMode
					&& wurst.special.yesCheatSpf.getBypassLevel()
						.ordinal() <= BypassLevel.MINEPLEX_ANTICHEAT.ordinal())
					nukeAll();
				else
				{
					shouldRenderESP = true;
					mc.player.swingArm(EnumHand.MAIN_HAND);
					mc.playerController.onPlayerDestroyBlock(pos);
				}
				return;
			}
		}
		if(wurst.mods.autoToolMod.isActive())
			AutoToolMod.setSlot(pos);
		mc.player.connection
			.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
		shouldRenderESP = true;
		BlockUtils.faceBlockPacket(pos);
		currentDamage += currentBlock.getPlayerRelativeBlockHardness(
			mc.world.getBlockState(pos), mc.player, mc.world, pos)
			* (wurst.mods.fastBreakMod.isActive()
				&& wurst.mods.fastBreakMod.getMode() == 0
					? wurst.mods.fastBreakMod.speed.getValueF() : 1);
		mc.world.sendBlockBreakProgress(mc.player.getEntityId(), pos,
			(int)(currentDamage * 10.0F) - 1);
		if(currentDamage >= 1)
		{
			mc.player.connection.sendPacket(
				new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, side));
			mc.playerController.onPlayerDestroyBlock(pos);
			blockHitDelay = (byte)4;
			currentDamage = 0;
		}else if(wurst.mods.fastBreakMod.isActive()
			&& wurst.mods.fastBreakMod.getMode() == 1)
			mc.player.connection.sendPacket(
				new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, side));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		if(mc.objectMouseOver == null
			|| mc.objectMouseOver.getBlockPos() == null)
			return;
		if(mode.getSelected() == 1
			&& mc.world.getBlockState(mc.objectMouseOver.getBlockPos())
				.getBlock().getMaterial(null) != Material.AIR)
		{
			id = Block.getIdFromBlock(mc.world
				.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock());
			wurst.files.saveOptions();
		}
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			default:
			case OFF:
			case MINEPLEX_ANTICHEAT:
				range.unlock();
				break;
			case ANTICHEAT:
			case OLDER_NCP:
			case LATEST_NCP:
			case GHOST_MODE:
				range.lockToMax(4.25);
				break;
		}
	}
	
	@SuppressWarnings("deprecation")
	private BlockPos find()
	{
		LinkedList<BlockPos> queue = new LinkedList<>();
		HashSet<BlockPos> alreadyProcessed = new HashSet<>();
		queue.add(new BlockPos(mc.player));
		while(!queue.isEmpty())
		{
			BlockPos currentPos = queue.poll();
			if(alreadyProcessed.contains(currentPos))
				continue;
			alreadyProcessed.add(currentPos);
			if(BlockUtils.getPlayerBlockDistance(currentPos) > range
				.getValueF())
				continue;
			int currentID = Block
				.getIdFromBlock(mc.world.getBlockState(currentPos).getBlock());
			if(currentID != 0)
				switch(mode.getSelected())
				{
					case 1:
						if(currentID == id)
							return currentPos;
						break;
					case 2:
						if(currentPos.getY() >= mc.player.posY)
							return currentPos;
						break;
					case 3:
						if(mc.world.getBlockState(currentPos).getBlock()
							.getPlayerRelativeBlockHardness(
								mc.world.getBlockState(pos), mc.player,
								mc.world, currentPos) >= 1)
							return currentPos;
						break;
					default:
						return currentPos;
				}
			if(wurst.special.yesCheatSpf.getBypassLevel()
				.ordinal() <= BypassLevel.MINEPLEX_ANTICHEAT.ordinal()
				|| !mc.world.getBlockState(currentPos).getBlock()
					.getMaterial(null).blocksMovement())
			{
				queue.add(currentPos.add(0, 0, -1));// north
				queue.add(currentPos.add(0, 0, 1));// south
				queue.add(currentPos.add(-1, 0, 0));// west
				queue.add(currentPos.add(1, 0, 0));// east
				queue.add(currentPos.add(0, -1, 0));// down
				queue.add(currentPos.add(0, 1, 0));// up
			}
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private void nukeAll()
	{
		for(int y = (int)range.getValueF(); y >= (mode.getSelected() == 2 ? 0
			: -range.getValueF()); y--)
			for(int x = (int)range.getValueF(); x >= -range.getValueF()
				- 1; x--)
				for(int z =
					(int)range.getValueF(); z >= -range.getValueF(); z--)
				{
					int posX = (int)(Math.floor(mc.player.posX) + x);
					int posY = (int)(Math.floor(mc.player.posY) + y);
					int posZ = (int)(Math.floor(mc.player.posZ) + z);
					BlockPos blockPos = new BlockPos(posX, posY, posZ);
					Block block = mc.world.getBlockState(blockPos).getBlock();
					float xDiff = (float)(mc.player.posX - posX);
					float yDiff = (float)(mc.player.posY - posY);
					float zDiff = (float)(mc.player.posZ - posZ);
					float currentDistance =
						BlockUtils.getBlockDistance(xDiff, yDiff, zDiff);
					if(Block.getIdFromBlock(block) != 0 && posY >= 0
						&& currentDistance <= range.getValueF())
					{
						if(mode.getSelected() == 1
							&& Block.getIdFromBlock(block) != id)
							continue;
						if(mode.getSelected() == 3
							&& block.getPlayerRelativeBlockHardness(
								mc.world.getBlockState(blockPos), mc.player,
								mc.world, blockPos) < 1)
							continue;
						side = mc.objectMouseOver.sideHit;
						shouldRenderESP = true;
						BlockUtils.faceBlockPacket(pos);
						mc.player.connection.sendPacket(
							new CPacketPlayerDigging(Action.START_DESTROY_BLOCK,
								blockPos, side));
						block.onBlockDestroyedByPlayer(mc.world, blockPos,
							mc.world.getBlockState(blockPos));
					}
				}
	}
}
