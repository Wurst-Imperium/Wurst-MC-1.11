/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockVine;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.RotationUtils;

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
		
		// face next position
		facePosition(nextPos);
		
		// horizontal movement
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
		{
			mc.gameSettings.keyBindForward.pressed = true;
			
			if(mc.player.isInWater() && mc.player.posY < nextPos.getY())
				mc.gameSettings.keyBindJump.pressed = true;
			
			// vertical movement
		}else if(pos.getY() != nextPos.getY())
			// go up
			if(pos.getY() < nextPos.getY())
			{
				// climb up
				// TODO: Spider
				Block block = BlockUtils.getBlock(pos);
				if(block instanceof BlockLadder || block instanceof BlockVine)
				{
					RotationUtils.faceVectorForWalking(
						BlockUtils.getBoundingBox(pos).getCenter());
					
					mc.gameSettings.keyBindForward.pressed = true;
					
				}else
				{
					// directional jump
					if(index < path.size() - 1
						&& !nextPos.up().equals(path.get(index + 1)))
					{
						facePosition(path.get(index + 1));
						mc.gameSettings.keyBindForward.pressed = true;
					}
					
					// jump up
					mc.gameSettings.keyBindJump.pressed = true;
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
	
	@Override
	public void lockControls()
	{
		super.lockControls();
		mc.player.capabilities.isFlying = false;
	}
}
