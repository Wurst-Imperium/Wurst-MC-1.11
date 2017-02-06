/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.PlayerUtils;

@Mod.Info(description = "Places random blocks around you.",
	name = "BuildRandom",
	tags = "build random",
	help = "Mods/BuildRandom")
@Mod.Bypasses
public class BuildRandomMod extends Mod implements UpdateListener
{
	private final Random random = new Random();
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.autoBuildMod, wurst.mods.fastPlaceMod,
			wurst.mods.autoSwitchMod};
	}
	
	// TODO: Visual indicator of current position similar to the one in Nuker
	
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
		if(wurst.mods.freecamMod.isActive()
			|| wurst.mods.remoteViewMod.isActive())
			return;
		
		// check timer
		if(mc.rightClickDelayTimer > 0 && !wurst.mods.fastPlaceMod.isActive())
			return;
		
		// set mode & range
		boolean legitMode = wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() > BypassLevel.ANTICHEAT.ordinal();
		int range = legitMode ? 5 : 6;
		int bound = range * 2 + 1;
		
		BlockPos pos;
		int attempts = 0;
		do
		{
			// generate random position
			pos = new BlockPos(mc.player).add(random.nextInt(bound) - range,
				random.nextInt(bound) - range, random.nextInt(bound) - range);
			attempts++;
		}while(attempts < 128 && !tryToPlaceBlock(legitMode, pos));
	}
	
	private boolean tryToPlaceBlock(boolean legitMode, BlockPos pos)
	{
		if(BlockUtils.getMaterial(pos) != Material.AIR)
			return false;
		
		if(legitMode)
		{
			if(!BlockUtils.placeBlockLegit(pos))
				return false;
			
			mc.rightClickDelayTimer = 4;
		}else
		{
			if(!BlockUtils.placeBlockSimple(pos))
				return false;
			
			PlayerUtils.swingArmClient();
			mc.rightClickDelayTimer = 4;
		}
		
		return true;
	}
}
