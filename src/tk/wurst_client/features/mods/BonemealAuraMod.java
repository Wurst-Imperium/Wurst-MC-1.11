/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.BlockUtils;

@Mod.Info(
	description = "Automatically uses bone meal on specific types of plants.\n"
		+ "Use the checkboxes to specify the types of plants.",
	name = "BonemealAura",
	tags = "bonemeal aura, bone meal aura, AutoBone, auto bone",
	help = "Mods/BonemealAura")
@Mod.Bypasses(ghostMode = false)
public class BonemealAuraMod extends Mod implements UpdateListener
{
	public final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	private final CheckboxSetting saplings =
		new CheckboxSetting("Saplings", true);
	private final CheckboxSetting crops =
		new CheckboxSetting("Carrots, Potatoes & Wheat", true);
	private final CheckboxSetting stems =
		new CheckboxSetting("Melons & Pumpkins", true);
	private final CheckboxSetting cocoa = new CheckboxSetting("Cocoa", true);
	private final CheckboxSetting other = new CheckboxSetting("Other", false);
	
	@Override
	public void initSettings()
	{
		settings.add(range);
		settings.add(saplings);
		settings.add(crops);
		settings.add(stems);
		settings.add(cocoa);
		settings.add(other);
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ItemStack item =
			mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem);
		if(item == null || !(item.getItem() instanceof ItemDye)
			|| item.getMetadata() != 15)
			return;
		
		BlockPos pos = mc.player.getPosition();
		for(int y = -range.getValueI() - 1; y < range.getValueI() + 1; y++)
			for(int x = -range.getValueI() - 1; x < range.getValueI() + 1; x++)
				for(int z = -range.getValueI() - 1; z < range.getValueI()
					+ 1; z++)
				{
					BlockPos currentPos = pos.add(x, y, z);
					if(BlockUtils.getPlayerBlockDistance(currentPos) > range
						.getValueF() || !isCorrectBlock(currentPos))
						continue;
					
					BlockUtils.faceBlockPacket(currentPos);
					mc.player.connection.sendPacket(
						new CPacketPlayerTryUseItemOnBlock(currentPos,
							EnumFacing.UP, EnumHand.MAIN_HAND, 0.5F, 1F, 0.5F));
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
	
	private boolean isCorrectBlock(BlockPos pos)
	{
		IBlockState state = mc.world.getBlockState(pos);
		Block block = state.getBlock();
		
		if(!(block instanceof IGrowable) || block instanceof BlockGrass
			|| !((IGrowable)block).canGrow(mc.world, pos, state, false))
			return false;
		
		if(block instanceof BlockSapling)
			return saplings.isChecked();
		else if(block instanceof BlockCrops)
			return crops.isChecked();
		else if(block instanceof BlockStem)
			return stems.isChecked();
		else if(block instanceof BlockCocoa)
			return cocoa.isChecked();
		else
			return other.isChecked();
	}
}
