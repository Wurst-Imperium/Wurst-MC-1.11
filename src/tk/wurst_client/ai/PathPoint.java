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
	private float priority;
	
	public PathPoint(BlockPos pos, float priority)
	{
		this.pos = pos;
		this.priority = priority;
	}
	
	public BlockPos getPos()
	{
		return pos;
	}
	
	public float getPriority()
	{
		return priority;
	}
}
