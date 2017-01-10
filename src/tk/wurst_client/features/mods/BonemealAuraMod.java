/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
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
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.CheckboxSetting;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.InventoryUtils;

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
		new SliderSetting("Range", 4.25, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final CheckboxSetting saplings =
		new CheckboxSetting("Saplings", true);
	private final CheckboxSetting crops = new CheckboxSetting("Crops", true);
	private final CheckboxSetting stems = new CheckboxSetting("Stems", true);
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
		// check held item
		ItemStack stack = mc.player.inventory.getCurrentItem();
		if(InventoryUtils.isEmptySlot(stack)
			|| !(stack.getItem() instanceof ItemDye)
			|| stack.getMetadata() != 15)
			return;
		
		BlockPos playerPos = new BlockPos(mc.player);
		for(int y = -range.getValueI() + 1; y < range.getValueI() + 2; y++)
			for(int x = -range.getValueI(); x < range.getValueI() + 1; x++)
				for(int z = -range.getValueI(); z < range.getValueI() + 1; z++)
				{
					BlockPos pos = playerPos.add(x, y, z);
					if(BlockUtils.getPlayerBlockDistance(pos) > range
						.getValueF() || !isCorrectBlock(pos))
						continue;
					
					BlockUtils.faceBlockPacket(pos);
					mc.player.connection
						.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos,
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
	
	private boolean isCorrectBlock(BlockPos pos)
	{
		Block block = BlockUtils.getBlock(pos);
		
		if(!(block instanceof IGrowable) || block instanceof BlockGrass
			|| !((IGrowable)block).canGrow(mc.world, pos,
				BlockUtils.getState(pos), false))
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
