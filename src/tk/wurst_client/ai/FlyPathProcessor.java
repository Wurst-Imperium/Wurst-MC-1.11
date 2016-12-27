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
import net.minecraft.util.math.MathHelper;
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
		
		// move
		BlockUtils.faceBlockClientHorizontally(nextPos);
		
		// limit vertical speed
		if(Math.abs(pos.getY() - nextPos.getY()) <= 1)
			mc.player.motionY =
				MathHelper.clamp(mc.player.motionY, -0.25, 0.25);
		
		// horizontal movement
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
		{
			mc.gameSettings.keyBindForward.pressed = true;
			
			if(mc.player.isCollidedHorizontally)
				if(mc.player.posY > nextPos.getY() + 0.2)
					mc.gameSettings.keyBindSneak.pressed = true;
				else if(mc.player.posY < nextPos.getY())
					mc.gameSettings.keyBindJump.pressed = true;
				
			// vertical movement
		}else if(pos.getY() != nextPos.getY())
		{
			if(pos.getY() < nextPos.getY())
				mc.gameSettings.keyBindJump.pressed = true;
			else
				mc.gameSettings.keyBindSneak.pressed = true;
			
			if(mc.player.isCollidedVertically)
				mc.gameSettings.keyBindForward.pressed = true;
		}
	}
	
	@Override
	public void lockControls()
	{
		super.lockControls();
		mc.player.capabilities.isFlying = creativeFlying;
	}
}
