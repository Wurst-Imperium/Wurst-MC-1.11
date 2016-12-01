/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;

import net.minecraft.util.math.BlockPos;
import tk.wurst_client.utils.BlockUtils;

public class FlyPathProcessor extends PathProcessor
{
	private final boolean creativeFlying;
	private boolean stopped;
	
	public FlyPathProcessor(ArrayList<PathPos> path, boolean creativeFlying)
	{
		super(path);
		this.creativeFlying = creativeFlying;
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
			
			if(index < path.size())
			{
				// stop when changing directions
				if(creativeFlying && index >= 2)
				{
					BlockPos prevPos = path.get(index - 1);
					if(!path.get(index).subtract(prevPos)
						.equals(prevPos.subtract(path.get(index - 2))))
					{
						if(!stopped)
						{
							mc.player.motionX /=
								Math.max(Math.abs(mc.player.motionX) * 50, 1);
							mc.player.motionY /=
								Math.max(Math.abs(mc.player.motionY) * 50, 1);
							mc.player.motionZ /=
								Math.max(Math.abs(mc.player.motionZ) * 50, 1);
							stopped = true;
						}
					}
				}
				
				// disable when done
			}else
			{
				if(creativeFlying)
				{
					mc.player.motionX /=
						Math.max(Math.abs(mc.player.motionX) * 50, 1);
					mc.player.motionY /=
						Math.max(Math.abs(mc.player.motionY) * 50, 1);
					mc.player.motionZ /=
						Math.max(Math.abs(mc.player.motionZ) * 50, 1);
				}
				
				done = true;
			}
			
			return;
		}
		
		stopped = false;
		
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
			if(pos.getY() < nextPos.getY())
				mc.gameSettings.keyBindJump.pressed = true;
			else
				mc.gameSettings.keyBindSneak.pressed = true;
		}
	}
	
	@Override
	public void lockControls()
	{
		super.lockControls();
		mc.player.capabilities.isFlying = creativeFlying;
	}
}
