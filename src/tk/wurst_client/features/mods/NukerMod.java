/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import tk.wurst_client.utils.BlockUtils.BlockValidator;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(
	description = "Destroys blocks around you.\n"
		+ "Use .nuker mode <mode> to change the mode.",
	name = "Nuker",
	help = "Mods/Nuker")
@Mod.Bypasses
public class NukerMod extends Mod
	implements LeftClickListener, UpdateListener, RenderListener
{
	public int id = 0;
	private BlockPos currentBlock;
	private BlockValidator validator;
	
	public final SliderSetting range =
		new SliderSetting("Range", 6, 1, 6, 0.05, ValueDisplay.DECIMAL);
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
					validator = (pos) -> id == BlockUtils.getId(pos);
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
		settings.add(range);
		settings.add(mode);
	}
	
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
		
		// resets
		mc.playerController.resetBlockRemoving();
		currentBlock = null;
		id = 0;
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
	public void onUpdate()
	{
		// abort if using IDNuker without an ID being set
		if(mode.getSelected() == 1 && id == 0)
			return;
		
		boolean legit = wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() > BypassLevel.MINEPLEX.ordinal();
		
		// find closest valid block
		currentBlock = null;
		for(BlockPos pos : BlockUtils.getValidBlocksByDistance(range.getValue(),
			!legit, validator))
		{
			currentBlock = pos;
			break;
		}
		
		// check if any block was found
		if(currentBlock == null)
		{
			mc.playerController.resetBlockRemoving();
			return;
		}
		
		// nuke all
		if(mc.player.capabilities.isCreativeMode && !legit)
		{
			mc.playerController.resetBlockRemoving();
			
			// break all blocks
			BlockUtils.getValidBlocks(range.getValue(), validator)
				.forEach((pos) -> BlockUtils.breakBlockPacketSpam(pos));
			
			return;
		}
		
		boolean successful;
		
		// break block
		if(legit)
			successful = BlockUtils.breakBlockLegit(currentBlock);
		else
			successful = BlockUtils.breakBlockSimple(currentBlock);
		
		// reset if failed
		if(!successful)
		{
			mc.playerController.resetBlockRemoving();
			currentBlock = null;
		}
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
}
