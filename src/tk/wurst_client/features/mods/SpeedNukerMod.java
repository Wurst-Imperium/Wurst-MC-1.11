/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.ModeSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.ChatUtils;

@Mod.Info(description = "Faster Nuker that cannot bypass NoCheat+.",
	name = "SpeedNuker",
	tags = "FastNuker, speed nuker, fast nuker",
	help = "Mods/SpeedNuker")
@Mod.Bypasses(ghostMode = false,
	latestNCP = false,
	olderNCP = false,
	antiCheat = false)
public class SpeedNukerMod extends Mod
	implements LeftClickListener, UpdateListener
{
	private static Block currentBlock;
	private BlockPos pos;
	private int oldSlot = -1;
	
	public CheckboxSetting useNuker =
		new CheckboxSetting("Use Nuker settings", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					NukerMod nuker = wurst.mods.nukerMod;
					range.lockToValue(nuker.range.getValue());
					mode.lock(nuker.mode.getSelected());
				}else
				{
					range.unlock();
					mode.unlock();
				}
			};
		};
	public final SliderSetting range =
		new SliderSetting("Range", 6, 1, 6, 0.05, ValueDisplay.DECIMAL);
	public final ModeSetting mode = new ModeSetting("Mode",
		new String[]{"Normal", "ID", "Flat", "Smash"}, 0);
	
	@Override
	public void initSettings()
	{
		settings.add(useNuker);
		settings.add(range);
		settings.add(mode);
	}
	
	@Override
	public String getRenderName()
	{
		switch(mode.getSelected())
		{
			case 0:
				return "SpeedNuker";
			case 1:
				return "IDSpeedNuker [" + NukerMod.id + "]";
			default:
				return mode.getSelectedMode() + "SpeedNuker";
		}
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.nukerMod,
			wurst.mods.nukerLegitMod, wurst.mods.tunnellerMod,
			wurst.mods.fastBreakMod, wurst.mods.autoMineMod};
	}
	
	@Override
	public void onEnable()
	{
		if(wurst.mods.nukerMod.isEnabled())
			wurst.mods.nukerMod.setEnabled(false);
		if(wurst.mods.nukerLegitMod.isEnabled())
			wurst.mods.nukerLegitMod.setEnabled(false);
		if(wurst.mods.tunnellerMod.isEnabled())
			wurst.mods.tunnellerMod.setEnabled(false);
		wurst.events.add(LeftClickListener.class, this);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(LeftClickListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		if(oldSlot != -1)
		{
			mc.player.inventory.currentItem = oldSlot;
			oldSlot = -1;
		}
		NukerMod.id = 0;
		wurst.files.saveOptions();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onUpdate()
	{
		if(mc.player.capabilities.isCreativeMode)
		{
			ChatUtils.error(getName() + " doesn't work in creative mode.");
			setEnabled(false);
			ChatUtils
				.message("Switching to " + wurst.mods.nukerMod.getName() + ".");
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
	
	@SuppressWarnings("deprecation")
	@Override
	public void onLeftClick()
	{
		if(mc.objectMouseOver == null
			|| mc.objectMouseOver.getBlockPos() == null)
			return;
		if(mode.getSelected() == 1
			&& mc.world.getBlockState(mc.objectMouseOver.getBlockPos())
				.getBlock().getMaterial(null) != Material.AIR)
		{
			NukerMod.id = Block.getIdFromBlock(mc.world
				.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock());
			wurst.files.saveOptions();
		}
	}
	
	@SuppressWarnings("deprecation")
	private BlockPos find()
	{
		BlockPos closest = null;
		float closestDistance = range.getValueF() + 1;
		int nukerMode = mode.getSelected();
		for(int y = (int)range.getValueF(); y >= (nukerMode == 2 ? 0
			: -range.getValueF()); y--)
			for(int x = (int)range.getValueF(); x >= -range.getValueF()
				- 1; x--)
				for(int z =
					(int)range.getValueF(); z >= -range.getValueF(); z--)
				{
					if(mc.player == null)
						continue;
					if(x == 0 && y == -1 && z == 0)
						continue;
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
						if(nukerMode == 1
							&& Block.getIdFromBlock(block) != NukerMod.id)
							continue;
						if(nukerMode == 3
							&& block.getPlayerRelativeBlockHardness(
								mc.world.getBlockState(blockPos), mc.player,
								mc.world, blockPos) < 1)
							continue;
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
		int nukerMode = mode.getSelected();
		for(int y = (int)range.getValueF(); y >= (nukerMode == 2 ? 0
			: -range.getValueF()); y--)
			for(int x = (int)range.getValueF(); x >= -range.getValueF()
				- 1; x--)
				for(int z =
					(int)range.getValueF(); z >= -range.getValueF(); z--)
				{
					int posX = (int)(Math.floor(mc.player.posX) + x);
					int posY = (int)(Math.floor(mc.player.posY) + y);
					int posZ = (int)(Math.floor(mc.player.posZ) + z);
					if(x == 0 && y == -1 && z == 0)
						continue;
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
						if(nukerMode == 1
							&& Block.getIdFromBlock(block) != NukerMod.id)
							continue;
						if(nukerMode == 3
							&& block.getPlayerRelativeBlockHardness(
								mc.world.getBlockState(blockPos), mc.player,
								mc.world, blockPos) < 1)
							continue;
						if(!mc.player.onGround)
							continue;
						EnumFacing side = mc.objectMouseOver.sideHit;
						mc.player.connection.sendPacket(
							new CPacketPlayerDigging(Action.START_DESTROY_BLOCK,
								blockPos, side));
						mc.player.connection.sendPacket(
							new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK,
								blockPos, side));
					}
				}
	}
}
