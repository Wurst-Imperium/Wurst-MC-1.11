/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.RenderUtils;
import tk.wurst_client.utils.RotationUtils;

@Mod.Info(description = "Digs a 3x3 tunnel around you.",
	name = "Tunneller",
	help = "Mods/Tunneller")
@Mod.Bypasses
public class TunnellerMod extends Mod implements RenderListener, UpdateListener
{
	private BlockPos currentBlock;
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.nukerMod, wurst.mods.nukerLegitMod,
			wurst.mods.speedNukerMod, wurst.mods.fastBreakMod,
			wurst.mods.autoMineMod, wurst.mods.autoToolMod,
			wurst.special.yesCheatSpf};
	}
	
	@Override
	public void onEnable()
	{
		// disable other nukers
		if(wurst.mods.nukerMod.isEnabled())
			wurst.mods.nukerMod.setEnabled(false);
		if(wurst.mods.nukerLegitMod.isEnabled())
			wurst.mods.nukerLegitMod.setEnabled(false);
		if(wurst.mods.speedNukerMod.isEnabled())
			wurst.mods.speedNukerMod.setEnabled(false);
		
		// add listeners
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		// remove listeners
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		
		// resets
		mc.playerController.resetBlockRemoving();
		currentBlock = null;
	}
	
	@Override
	public void onUpdate()
	{
		boolean legit = wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() > BypassLevel.MINEPLEX.ordinal();
		
		currentBlock = null;
		
		// get valid blocks
		Iterable<BlockPos> validBlocks =
			BlockUtils.getValidBlocks(1, (p) -> true);
		
		// nuke all
		if(mc.player.capabilities.isCreativeMode && !legit)
		{
			mc.playerController.resetBlockRemoving();
			
			// prepare distance check
			Vec3d eyesPos = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
			double closestDistanceSq = Double.POSITIVE_INFINITY;
			
			// break all blocks
			for(BlockPos pos : validBlocks)
			{
				BlockUtils.breakBlockPacketSpam(pos);
				
				// find closest block
				double currentDistanceSq =
					eyesPos.squareDistanceTo(new Vec3d(pos));
				if(currentDistanceSq < closestDistanceSq)
				{
					closestDistanceSq = currentDistanceSq;
					currentBlock = pos;
				}
			}
			
			return;
		}
		
		// find valid block
		for(BlockPos pos : validBlocks)
		{
			boolean successful;
			
			// break block
			if(legit)
				successful = BlockUtils.breakBlockLegit(pos);
			else
				successful = BlockUtils.breakBlockSimple(pos);
			
			// set currentBlock if successful
			if(successful)
			{
				currentBlock = pos;
				break;
			}
		}
		
		// reset if no block was found
		if(currentBlock == null)
			mc.playerController.resetBlockRemoving();
	}
	
	@Override
	public void onRender(float partialTicks)
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
