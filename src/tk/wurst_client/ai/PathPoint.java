/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import net.minecraft.util.math.BlockPos;

public class PathPoint
{
	private BlockPos pos;
	private PathPoint previous;
	private float priority;
	private float totalCost;
	
	public PathPoint(BlockPos pos, PathPoint previous, float totalCost,
		float priority)
	{
		this.pos = pos;
		this.previous = previous;
		this.totalCost = totalCost;
		this.priority = priority;
	}
	
	public BlockPos getPos()
	{
		return pos;
	}
	
	public PathPoint getPrevious()
	{
		return previous;
	}
	
	public float getPriority()
	{
		return priority;
	}
	
	public float getTotalCost()
	{
		return totalCost;
	}
}
