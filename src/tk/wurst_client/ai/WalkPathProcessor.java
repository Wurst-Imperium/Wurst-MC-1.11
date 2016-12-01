/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.utils.BlockUtils;

public class WalkPathProcessor extends PathProcessor
{
	public WalkPathProcessor(ArrayList<PathPos> path)
	{
		super(path);
	}
	
	@Override
	public void process()
	{
		// get positions
		BlockPos pos = new BlockPos(mc.player);
		BlockPos nextPos = path.get(index);
		
		// update index
		if(pos.equals(nextPos))
		{
			index++;
			
			// disable when done
			if(index >= path.size())
				done = true;
			
			return;
		}
		
		lockControls();
		
		// check if player moved off the path
		if(index > 0)
		{
			BlockPos prevPos = path.get(index - 1);
			if((pos.getX() != prevPos.getX() && pos.getX() != nextPos.getX())
				|| (pos.getY() != prevPos.getY()
					&& pos.getY() != nextPos.getY())
				|| (pos.getZ() != prevPos.getZ()
					&& pos.getZ() != nextPos.getZ()))
				System.err.println("Player moved off the path.");
		}
		
		// move
		BlockUtils.faceBlockClientHorizontally(nextPos);
		
		// horizontal movement
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
		{
			mc.gameSettings.keyBindForward.pressed = true;
			
			// vertical movement
		}else if(pos.getY() != nextPos.getY())
		{
			// go up
			if(pos.getY() < nextPos.getY())
			{
				// climb up
				// TODO: vines and spider
				if(mc.world.getBlockState(pos)
					.getBlock() instanceof BlockLadder)
				{
					BlockUtils.faceBlockClientHorizontally(
						pos.offset(mc.world.getBlockState(pos)
							.getValue(BlockHorizontal.FACING).getOpposite()));
					mc.gameSettings.keyBindForward.pressed = true;
					
					// jump up
				}else
				{
					mc.gameSettings.keyBindJump.pressed = true;
					
					// directional jump
					if(index < path.size() - 1)
					{
						BlockUtils
							.faceBlockClientHorizontally(path.get(index + 1));
						mc.gameSettings.keyBindForward.pressed = true;
					}
				}
				
				// go down
			}else
			{
				// skip mid-air nodes and go straight to the bottom
				while(index < path.size() - 1
					&& path.get(index).down().equals(path.get(index + 1)))
					index++;
				
				// walk off the edge
				if(mc.player.onGround)
					mc.gameSettings.keyBindForward.pressed = true;
			}
		}
	}
	
	@Override
	public void lockControls()
	{
		super.lockControls();
		mc.player.capabilities.isFlying = false;
	}
}
