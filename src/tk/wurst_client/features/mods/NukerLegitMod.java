/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.LeftClickEvent;
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.settings.CheckboxSetting;
import tk.wurst_client.settings.ModeSetting;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.BlockUtils.BlockValidator;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(
	description = "Slower Nuker that bypasses all AntiCheat plugins.\n"
		+ "Not required on normal NoCheat+ servers!",
	name = "NukerLegit",
	tags = "LegitNuker, nuker legit, legit nuker",
	help = "Mods/NukerLegit")
@Mod.Bypasses
public class NukerLegitMod extends Mod
	implements LeftClickListener, RenderListener, UpdateListener
{
	private BlockPos currentBlock;
	private BlockValidator validator;
	
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
		new SliderSetting("Range", 4.25, 1, 4.25, 0.05, ValueDisplay.DECIMAL);
	public final ModeSetting mode = new ModeSetting("Mode",
		new String[]{"Normal", "ID", "Flat", "Smash"}, 0)
	{
		@Override
		public void update()
		{
			switch(getSelected())
			{
				default:
				case 0:
					// normal mode
					validator = (pos) -> true;
					break;
				
				case 1:
					// id mode
					validator = (
						pos) -> wurst.mods.nukerMod.id == BlockUtils.getId(pos);
					break;
				
				case 2:
					// flat mode
					validator = (pos) -> pos.getY() >= mc.player.posY;
					break;
				
				case 3:
					// smash mode
					validator = (pos) -> BlockUtils.getHardness(pos) >= 1;
					break;
			}
		}
	};
	
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
				return "IDNukerLegit [" + wurst.mods.nukerMod.id + "]";
			default:
				return mode.getSelectedMode() + "NukerLegit";
		}
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.nukerMod, wurst.mods.speedNukerMod,
			wurst.mods.tunnellerMod, wurst.mods.fastBreakMod,
			wurst.mods.autoMineMod};
	}
	
	@Override
	public void onEnable()
	{
		// disable other nukers
		if(wurst.mods.nukerMod.isEnabled())
			wurst.mods.nukerMod.setEnabled(false);
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
		
		// resets
		mc.gameSettings.keyBindAttack.pressed = false;
		currentBlock = null;
		wurst.mods.nukerMod.id = 0;
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
		wurst.mods.nukerMod.id =
			BlockUtils.getId(mc.objectMouseOver.getBlockPos());
	}
	
	@Override
	public void onUpdate()
	{
		// abort if using IDNuker without an ID being set
		if(mode.getSelected() == 1 && wurst.mods.nukerMod.id == 0)
			return;
		
		currentBlock = null;
		
		// get valid blocks
		Iterable<BlockPos> validBlocks = BlockUtils
			.getValidBlocksByDistance(range.getValue(), false, validator);
		
		// find closest valid block
		for(BlockPos pos : validBlocks)
		{
			// break block
			if(!BlockUtils.breakBlockExtraLegit(pos))
				continue;
			
			// set currentBlock if successful
			currentBlock = pos;
			break;
		}
		
		// reset if no block was found
		if(currentBlock == null)
			mc.gameSettings.keyBindAttack.pressed = false;
	}
	
	@Override
	public void onRender()
	{
		if(currentBlock == null)
			return;
		
		// check if block can be destroyed instantly
		if(mc.player.capabilities.isCreativeMode
			|| BlockUtils.getHardness(currentBlock) >= 1)
			RenderUtils.nukerBox(currentBlock, 1);
		else
			RenderUtils.nukerBox(currentBlock,
				mc.playerController.curBlockDamageMP);
	}
}
