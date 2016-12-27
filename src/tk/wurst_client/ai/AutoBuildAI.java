/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import net.minecraft.util.math.BlockPos;

public class AutoBuildAI
{
	private PathFinder pathFinder;
	private PathProcessor processor;
	
	private boolean done;
	private boolean failed;
	
	public AutoBuildAI(BlockPos goal)
	{
		pathFinder = new PathFinder(goal);
		pathFinder.setThinkTime(10);
	}
	
	public void update()
	{
		// find path
		if(!pathFinder.isDone() && !pathFinder.isFailed())
		{
			if(processor != null)
				processor.lockControls();
			
			pathFinder.think();
			
			if(!pathFinder.isDone() && !pathFinder.isFailed())
				return;
			
			pathFinder.formatPath();
			
			// set processor
			processor = pathFinder.getProcessor();
		}
		
		// check path
		if(processor != null
			&& !pathFinder.isPathStillValid(processor.getIndex()))
		{
			pathFinder = new PathFinder(pathFinder);
			return;
		}
		
		// process path
		processor.process();
		
		if(processor.isFailed())
			failed = true;
		
		if(processor.isDone())
			done = true;
	}
	
	public void stop()
	{
		if(processor != null)
			processor.stop();
	}
	
	public final boolean isDone()
	{
		return done;
	}
	
	public final boolean isFailed()
	{
		return failed;
	}
	
	public BlockPos getGoal()
	{
		return pathFinder.getGoal();
	}
}
