/*
 * Copyright � 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

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
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.ModeSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.RenderUtils;

@Info(
	description = "Slower Nuker that bypasses any cheat prevention\n"
		+ "PlugIn. Not required on most NoCheat+ servers!",
	name = "NukerLegit",
	tags = "LegitNuker, nuker legit, legit nuker",
	help = "Mods/NukerLegit")
@Bypasses
public class NukerLegitMod extends Mod implements LeftClickListener,
	RenderListener, UpdateListener
{
	private static Block currentBlock;
	private float currentDamage;
	private EnumFacing side = EnumFacing.UP;
	private byte blockHitDelay = 0;
	private BlockPos pos;
	private boolean shouldRenderESP;
	private int oldSlot = -1;
	
	public CheckboxSetting useNuker = new CheckboxSetting("Use Nuker settings",
		true)
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
	public final SliderSetting range = new SliderSetting("Range", 4.25, 1,
		4.25, 0.05, ValueDisplay.DECIMAL);
	public final ModeSetting mode = new ModeSetting("Mode", new String[]{
		"Normal", "ID", "Flat", "Smash"}, 0);
	
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
				return "NukerLegit";
			case 1:
				return "IDNukerLegit [" + NukerMod.id + "]";
			default:
				return mode.getSelectedMode() + "NukerLegit";
		}
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.nukerMod,
			wurst.mods.speedNukerMod, wurst.mods.tunnellerMod,
			wurst.mods.fastBreakMod, wurst.mods.autoMineMod};
	}
	
	@Override
	public void onEnable()
	{
		if(wurst.mods.nukerMod.isEnabled())
			wurst.mods.nukerMod.setEnabled(false);
		if(wurst.mods.speedNukerMod.isEnabled())
			wurst.mods.speedNukerMod.setEnabled(false);
		if(wurst.mods.tunnellerMod.isEnabled())
			wurst.mods.tunnellerMod.setEnabled(false);
		if(wurst.mods.speedTunnellerMod.isEnabled())
			wurst.mods.speedTunnellerMod.setEnabled(false);
		wurst.events.add(LeftClickListener.class, this);
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(RenderListener.class, this);
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
		BlockUtils.faceBlockClient(pos);
		if(currentDamage == 0)
		{
			mc.player.connection.sendPacket(new CPacketPlayerDigging(
				Action.START_DESTROY_BLOCK, pos, side));
			if(wurst.mods.autoToolMod.isActive() && oldSlot == -1)
				oldSlot = mc.player.inventory.currentItem;
			if(mc.player.capabilities.isCreativeMode
				|| currentBlock.getPlayerRelativeBlockHardness(
					mc.world.getBlockState(pos), mc.player, mc.world,
					pos) >= 1)
			{
				currentDamage = 0;
				shouldRenderESP = true;
				mc.player.swingArm(EnumHand.MAIN_HAND);
				mc.playerController.onPlayerDestroyBlock(pos);
				blockHitDelay = (byte)4;
				return;
			}
		}
		if(wurst.mods.autoToolMod.isActive())
			AutoToolMod.setSlot(pos);
		mc.player.connection.sendPacket(new CPacketAnimation(
			EnumHand.MAIN_HAND));
		shouldRenderESP = true;
		currentDamage +=
			currentBlock.getPlayerRelativeBlockHardness(
				mc.world.getBlockState(pos), mc.player, mc.world, pos);
		mc.world.sendBlockBreakProgress(mc.player.getEntityId(), pos,
			(int)(currentDamage * 10.0F) - 1);
		if(currentDamage >= 1)
		{
			mc.player.connection.sendPacket(new CPacketPlayerDigging(
				Action.STOP_DESTROY_BLOCK, pos, side));
			mc.playerController.onPlayerDestroyBlock(pos);
			blockHitDelay = (byte)4;
			currentDamage = 0;
		}
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
		NukerMod.id = 0;
		wurst.files.saveOptions();
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
			NukerMod.id =
				Block.getIdFromBlock(mc.world.getBlockState(
					mc.objectMouseOver.getBlockPos()).getBlock());
			wurst.files.saveOptions();
		}
	}
	
	@SuppressWarnings("deprecation")
	private BlockPos find()
	{
		LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
		HashSet<BlockPos> alreadyProcessed = new HashSet<BlockPos>();
		queue.add(new BlockPos(mc.player));
		while(!queue.isEmpty())
		{
			BlockPos currentPos = queue.poll();
			if(alreadyProcessed.contains(currentPos))
				continue;
			alreadyProcessed.add(currentPos);
			if(BlockUtils.getPlayerBlockDistance(currentPos) > Math.min(
				range.getValueF(), 4.25F))
				continue;
			int currentID =
				Block.getIdFromBlock(mc.world.getBlockState(currentPos)
					.getBlock());
			if(currentID != 0)
				switch(mode.getSelected())
				{
					case 1:
						if(currentID == NukerMod.id)
							return currentPos;
						break;
					case 2:
						if(currentPos.getY() >= mc.player.posY)
							return currentPos;
						break;
					case 3:
						if(mc.world
							.getBlockState(currentPos)
							.getBlock()
							.getPlayerRelativeBlockHardness(
								mc.world.getBlockState(pos), mc.player,
								mc.world, currentPos) >= 1)
							return currentPos;
						break;
					default:
						return currentPos;
				}
			if(!mc.world.getBlockState(currentPos).getBlock()
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
}
