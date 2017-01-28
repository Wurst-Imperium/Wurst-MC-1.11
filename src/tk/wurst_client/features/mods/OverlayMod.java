/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(description = "Renders the Nuker animation when you mine a block.",
	name = "Overlay",
	help = "Mods/Overlay")
@Mod.Bypasses
public class OverlayMod extends Mod implements RenderListener
{
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.nukerMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(RenderListener.class, this);
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		if(mc.objectMouseOver == null
			|| mc.objectMouseOver.typeOfHit != Type.BLOCK)
			return;
		BlockPos pos = mc.objectMouseOver.getBlockPos();
		Block mouseOverBlock =
			mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();
		if(Block.getIdFromBlock(mouseOverBlock) != 0)
			RenderUtils.nukerBox(pos, mc.playerController.curBlockDamageMP);
	}
}
