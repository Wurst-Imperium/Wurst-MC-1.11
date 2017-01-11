/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.util.ArrayDeque;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
	private float currentDamage;
	private EnumFacing side = EnumFacing.UP;
	private int blockHitDelay = 0;
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
		// disable other nukers
		if(wurst.mods.nukerLegitMod.isEnabled())
			wurst.mods.nukerLegitMod.setEnabled(false);
		if(wurst.mods.speedNukerMod.isEnabled())
			wurst.mods.speedNukerMod.setEnabled(false);
		if(wurst.mods.tunnellerMod.isEnabled())
			wurst.mods.tunnellerMod.setEnabled(false);
		
		// add listeners
		wurst.events.add(LeftClickListener.class, this);
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		// remove listeners
		wurst.events.remove(LeftClickListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		
		// reset slot
		if(oldSlot != -1)
		{
			mc.player.inventory.currentItem = oldSlot;
			oldSlot = -1;
		}
		
		// reset damage
		currentDamage = 0;
		
		// disable rendering
		shouldRenderESP = false;
		
		// reset ID
		id = 0;
	}
	
	@Override
	public void onRender()
	{
		if(!shouldRenderESP)
			return;
		
		// wait for timer
		if(blockHitDelay != 0)
			return;
		
		// check if block can be destroyed instantly
		if(mc.player.capabilities.isCreativeMode || BlockUtils.getState(pos)
			.getPlayerRelativeBlockHardness(mc.player, mc.world, pos) >= 1)
			RenderUtils.nukerBox(pos, 1);
		else
			RenderUtils.nukerBox(pos, currentDamage);
	}
	
	@Override
	public void onUpdate()
	{
		// disable rendering
		shouldRenderESP = false;
		
		// find closest valid block
		BlockPos newPos = find();
		
		// check if any block was found
		if(newPos == null)
		{
			// reset slot
			if(oldSlot != -1)
			{
				mc.player.inventory.currentItem = oldSlot;
				oldSlot = -1;
			}
			
			return;
		}
		
		// reset damage
		if(!newPos.equals(pos))
			currentDamage = 0;
		
		// set current pos & block
		pos = newPos;
		
		// wait for timer
		if(blockHitDelay > 0)
		{
			blockHitDelay--;
			return;
		}
		
		// face block
		BlockUtils.faceBlockPacket(pos);
		
		if(currentDamage == 0)
		{
			// start breaking the block
			mc.player.connection.sendPacket(new CPacketPlayerDigging(
				Action.START_DESTROY_BLOCK, pos, side));
			
			// save old slot
			if(wurst.mods.autoToolMod.isActive() && oldSlot == -1)
				oldSlot = mc.player.inventory.currentItem;
			
			// check if block can be destroyed instantly
			if(mc.player.capabilities.isCreativeMode || BlockUtils.getState(pos)
				.getPlayerRelativeBlockHardness(mc.player, mc.world, pos) >= 1)
			{
				// reset damage
				currentDamage = 0;
				
				// nuke all
				if(mc.player.capabilities.isCreativeMode
					&& wurst.special.yesCheatSpf.getBypassLevel()
						.ordinal() <= BypassLevel.MINEPLEX.ordinal())
					nukeAll();
				else
				{
					// enable rendering
					shouldRenderESP = true;
					
					// swing arm
					mc.player.swingArm(EnumHand.MAIN_HAND);
					
					// destroy block
					mc.playerController.onPlayerDestroyBlock(pos);
				}
				
				return;
			}
		}
		
		// AutoTool
		wurst.mods.autoToolMod.setSlot(pos);
		
		// swing arm
		mc.player.connection
			.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
		
		// enable rendering
		shouldRenderESP = true;
		
		// update damage
		currentDamage += BlockUtils.getState(pos)
			.getPlayerRelativeBlockHardness(mc.player, mc.world, pos)
			* (wurst.mods.fastBreakMod.isActive()
				&& wurst.mods.fastBreakMod.getMode() == 0
					? wurst.mods.fastBreakMod.speed.getValueF() : 1);
		
		// send damage to server
		mc.world.sendBlockBreakProgress(mc.player.getEntityId(), pos,
			(int)(currentDamage * 10) - 1);
		
		// check if block is ready to be destroyed
		if(currentDamage >= 1)
		{
			// destroy block
			mc.player.connection.sendPacket(
				new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, side));
			mc.playerController.onPlayerDestroyBlock(pos);
			
			// reset delay
			blockHitDelay = 4;
			
			// reset damage
			currentDamage = 0;
			
			// FastBreak instant mode
		}else if(wurst.mods.fastBreakMod.isActive()
			&& wurst.mods.fastBreakMod.getMode() == 1)
			
			// try to destroy block
			mc.player.connection.sendPacket(
				new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, side));
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		// check hitResult
		if(mc.objectMouseOver == null
			|| mc.objectMouseOver.getBlockPos() == null)
			return;
		
		// check mode
		if(mode.getSelected() != 1)
			return;
		
		// check material
		if(BlockUtils
			.getMaterial(mc.objectMouseOver.getBlockPos()) == Material.AIR)
			return;
		
		// set id
		id = Block.getIdFromBlock(
			BlockUtils.getBlock(mc.objectMouseOver.getBlockPos()));
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			default:
			case OFF:
			case MINEPLEX:
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
	
	private BlockPos find()
	{
		// abort if using IDNuker without an ID being set
		if(mode.getSelected() == 1 && id == 0)
			return null;
		
		// initialize queue
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		HashSet<BlockPos> visited = new HashSet<>();
		
		// prepare range check
		Vec3d eyesPos = new Vec3d(mc.player.posX,
			mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
		double rangeSq = Math.pow(range.getValue(), 2);
		
		// add start pos
		queue.add(new BlockPos(mc.player).up());
		
		// find block using breadth first search
		while(!queue.isEmpty())
		{
			BlockPos current = queue.pop();
			
			// check range
			if(eyesPos.squareDistanceTo(
				new Vec3d(current).addVector(0.5, 0.5, 0.5)) > rangeSq)
				continue;
			
			boolean canBeClicked = BlockUtils.canBeClicked(current);
			
			// check if block is valid
			if(canBeClicked)
				switch(mode.getSelected())
				{
					case 1:
						if(id == Block
							.getIdFromBlock(BlockUtils.getBlock(current)))
							return current;
						break;
					case 2:
						if(current.getY() >= mc.player.posY)
							return current;
						break;
					case 3:
						if(BlockUtils.getState(current)
							.getPlayerRelativeBlockHardness(mc.player, mc.world,
								current) >= 1)
							return current;
						break;
					default:
						return current;
				}
			
			if(!canBeClicked || wurst.special.yesCheatSpf.getBypassLevel()
				.ordinal() < BypassLevel.ANTICHEAT.ordinal())
			{
				// add neighbors
				for(EnumFacing facing : EnumFacing.values())
				{
					BlockPos next = current.offset(facing);
					
					if(visited.contains(next))
						continue;
					
					queue.add(next);
					visited.add(next);
				}
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
