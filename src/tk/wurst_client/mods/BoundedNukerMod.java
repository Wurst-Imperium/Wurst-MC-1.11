/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.RightClickListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.ModeSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.special.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.BlockUtils;

@Info(
	description = "Boxed Nuker. When bounded, will not\n"
		+ "nuke until both corners are set.\n"
		+ "Use .bound to manage bounds.\n" + "So many checkboxes. XD\n\n",
	name = "BoundedNuker",
	tags = "BoundedNuker, bounded nuker, nuker, boundable",
	help = "Mods/BoundedNuker")
@Bypasses(antiCheat = true,
	olderNCP = false,
	latestNCP = false,
	ghostMode = false)
public class BoundedNukerMod extends Mod
	implements LeftClickListener, RightClickListener, UpdateListener
{
	public BlockPos bound1 = null;
	public BlockPos bound2 = null;
	public boolean alwaysBounded = false;
	public boolean clickToBound = true;
	private static Block currentBlock;
	private BlockPos pos;
	private int oldSlot = -1;
	private boolean speedFlag = true;
	private boolean compatibleCreative = false;
	private boolean effectivelyBounded;
	
	public CheckboxSetting useNuker = new CheckboxSetting(
		"Use Nuker settings.\n" + "Will not stay in any bounds.", false)
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
	public CheckboxSetting bounded =
		new CheckboxSetting("Make bounding combinable with other modes. "
			+ "Disables/overlaps/reiterates bounded mode itself.", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					alwaysBounded = true;
				}else
				{
					alwaysBounded = false;
				}
			};
		};
	public CheckboxSetting click =
		new CheckboxSetting("Bounds on left and right clicking.", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					clickToBound = true;
				}else
				{
					clickToBound = false;
				}
			};
		};
	public CheckboxSetting compatible =
		new CheckboxSetting("Enables creative mode compatibility.", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					compatibleCreative = true;
				}else
				{
					compatibleCreative = false;
				}
			};
		};
	public final SliderSetting range =
		new SliderSetting("Range", 6, 1, 6, 0.05, ValueDisplay.DECIMAL);
	public final ModeSetting mode = new ModeSetting("Mode",
		new String[]{"Normal", "ID", "Flat", "Smash", "Bounded"}, 0);
	
	@Override
	public void initSettings()
	{
		settings.add(useNuker);
		settings.add(bounded);
		settings.add(click);
		settings.add(compatible);
		settings.add(range);
		settings.add(mode);
	}
	
	@Override
	public String getRenderName()
	{
		switch(mode.getSelected())
		{
			case 0:
				return "BoundedNuker";
			case 1:
				return "IDBoundedNuker [" + NukerMod.id + "]";
			case 4:
				return "ReBoundedNuker";
			default:
				return mode.getSelectedMode() + "BoundedNuker";
		}
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.nukerMod,
			wurst.mods.nukerLegitMod, wurst.mods.tunnellerMod,
			wurst.mods.fastBreakMod, wurst.mods.autoMineMod,
			wurst.mods.speedTunnellerMod};
	}
	
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
		if(wurst.mods.speedTunnellerMod.isEnabled())
			wurst.mods.speedTunnellerMod.setEnabled(false);
		wurst.events.add(LeftClickListener.class, this);
		wurst.events.add(RightClickListener.class, this);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onUpdate()
	{
		speedFlag =
			wurst.special.yesCheatSpf.getBypassLevel() == BypassLevel.OFF
				&& !mc.player.capabilities.isCreativeMode;
		effectivelyBounded = alwaysBounded || mode.getSelected() == 4;
		if(mc.player.capabilities.isCreativeMode && !compatibleCreative)
		{
			wurst.chat.error("This doesn't work in creative mode.");
			wurst.chat.message("Disabling.");
			this.setEnabled(false);
		}
		if(effectivelyBounded && (bound1 == null || bound2 == null))
			return;
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
		wurst.events.remove(LeftClickListener.class, this);
		wurst.events.remove(RightClickListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		if(oldSlot != -1)
		{
			mc.player.inventory.currentItem = oldSlot;
			oldSlot = -1;
		}
		NukerMod.id = 0;
		resetBounds();
		wurst.files.saveOptions();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onLeftClick()
	{
		RayTraceResult hit = mc.objectMouseOver;
		if(hit == null || hit.getBlockPos() == null)
			return;
		if(mode.getSelected() == 1 && mc.world.getBlockState(hit.getBlockPos())
			.getBlock().getMaterial(null) != Material.AIR)
		{
			NukerMod.id = Block.getIdFromBlock(
				mc.world.getBlockState(hit.getBlockPos()).getBlock());
			wurst.files.saveOptions();
			if(effectivelyBounded && clickToBound)
			{
				setBound(hit.getBlockPos(), 1);
			}
		}else if(effectivelyBounded && mc.world.getBlockState(hit.getBlockPos())
			.getBlock().getMaterial(null) != Material.AIR && clickToBound)
		{
			setBound(hit.getBlockPos(), 1);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRightClick()
	{
		RayTraceResult hit = mc.objectMouseOver;
		if(hit == null || hit.getBlockPos() == null)
			return;
		if(effectivelyBounded && mc.world.getBlockState(hit.getBlockPos())
			.getBlock().getMaterial(null) != Material.AIR && clickToBound)
		{
			setBound(hit.getBlockPos(), 2);
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
					if(effectivelyBounded
						&& !BlockUtils.inBounds(bound1, bound2, blockPos))
						continue;
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
					if(effectivelyBounded
						&& !BlockUtils.inBounds(bound1, bound2, blockPos))
						continue;
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
						breakBlock(blockPos, side);
					}
				}
	}
	
	public void resetBounds()
	{
		setBound(null, 1);
		setBound(null, 2);
	}
	
	public void setBound(@Nullable BlockPos pos, int i)
	{
		if(i == 1)
		{
			bound1 = pos;
			if(pos != null)
				wurst.chat.message("Set corner 1 to " + pos.getX() + " "
					+ pos.getY() + " " + pos.getZ());
		}else if(i == 2)
		{
			bound2 = pos;
			if(pos != null)
				wurst.chat.message("Set corner 2 to " + pos.getX() + " "
					+ pos.getY() + " " + pos.getZ());
		}else
		{
			wurst.chat.failure("Couldn't set a bound");
		}
	}
	
	public void breakBlock(BlockPos pos, EnumFacing side)
	{
		
		Block block = mc.world.getBlockState(pos).getBlock();
		mc.player.connection.sendPacket(
			new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, side));
		if(speedFlag)
		{
			mc.player.connection.sendPacket(
				new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, side));
		}else
		{
			BlockUtils.faceBlockPacket(pos);
			block.onBlockDestroyedByPlayer(mc.world, pos,
				mc.world.getBlockState(pos));
			/*
			 * mc.player.connection.sendPacket(
			 * new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, side));
			 */
		}
	}
	
}
